package mesh;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import knotwork.Edge;
import mesh.thinning.*;

import org.locationtech.jts.geom.Coordinate;

import stippling.main.Stippler;
import svg.SVGUtil;

public class MeshGenerator{
	
	int cellWidth = 50;
	int noBins;
	int blurFactor;
	MyImage img;
	float[][] map;
	float[][] figureMap;
	ArrayList<ArrayList<Coordinate>> featureLines;
	int width, height;
	double ringDist = 1;
	
	// Constructor:
	// temporary:
//	public MeshGenerator(int cellWidth, ArrayList<ArrayList<Coordinate>> isophotes, double ringThickness){
//		this.cellWidth = cellWidth;
//		this.ringDist = ringThickness;
//		this.featureLines = isophotes;
//		this.map = isophotesToContour(isophotes);
//    	this.sx = map.length;
//    	this.sy = map[0].length;
//	}
	
	public MeshGenerator(String imgPath, int cellWidth, int noBins, int blurFactor){
		img = new MyImage();
		img.readImage(imgPath);
		this.noBins = noBins;
		this.blurFactor = blurFactor;
		this.cellWidth = cellWidth;
    	this.width = img.getImageWidth();
    	this.height = img.getImageHeight();
	}
	
	
	/**
     * Initialise mesh generation.
     */
	public void init(){
		// operations on RGBA image
		img.blurImage(blurFactor);
		img.setOpaque();
		img.toGrayscale();
		img.quantize(noBins);
		img.calculateGradient(); // (also transforms into binary image)

        Skeletonization.binaryImage(img);
        img.displayImage();

        // operations on binary image
        Lines lineOps = new Lines(width, height);

        ArrayList<ArrayList<Coordinate>> lines = lineOps.extractLines(img.toMap());
        drawMesh(lines);

		int contour_index = lineOps.extractContour(lines);
		img.update(lineOps.lineToPixels(lines.get(contour_index), width, height));

		//figureMap = lineOps.contourMap(contour, width, height);
        img.invertBinary();
        img.displayImage();
		map = distanceMap(img.toMap());

		img.update(map);

		//img.invertBinary();
        Skeletonization.binaryImage(img);

        img.displayImage();
        img.writeImage("res/skeletonized.png");
        //img.invertBinary();

        img.removeBackground(lines.get(contour_index));
        img.writeImage("res/skeletonized_nb.png");

        ArrayList<ArrayList<Coordinate>> lines1 = lineOps.extractLines(img.toMap());
        img.update(lineOps.linesToPixels(lines1, width, height));
        img.writeImage("res/skeletonized1.png");
        drawMesh(lines1);

        ArrayList<Coordinate> cornerPoints = img.getCornerPoints(6, 5, 0.001, 40);
        //img.displayImage();

        createMesh(lines1, cornerPoints, cellWidth, contour_index);

		System.out.println("Done.");
	}


	public void createMesh(ArrayList<ArrayList<Coordinate>> lines, ArrayList<Coordinate> cornerPoints, int cellWidth, int contour_index){
        ArrayList<ArrayList<Coordinate>> variableVertices = new ArrayList<>();
        ArrayList<ArrayList<Coordinate>> fixedVertices = new ArrayList<>();

        for (ArrayList<Coordinate> line : lines) {
            variableVertices.add(new ArrayList<Coordinate>());
            fixedVertices.add(new ArrayList<Coordinate>());
        }

	    // first map corner points to contour
        for (Coordinate cornerPoint : cornerPoints) {
            Coordinate closestPoint = new Coordinate(-100 ,-100);
            int i_closest_line = -1;

            for (int i = 0; i < lines.size(); i++) {
                for (Coordinate coordinate : lines.get(i)) {
                    if(coordinate.distance(cornerPoint) < closestPoint.distance(cornerPoint)){
                        closestPoint = coordinate;
                        i_closest_line = i;
                    }
                }
            }

            ArrayList<Coordinate> fv = fixedVertices.get(i_closest_line);
            fv.add(closestPoint);

        }

        // then start distributing points on outermost contour
        ArrayList<Coordinate> outerContour = lines.get(contour_index);
        int contour_length = outerContour.size();

        int num_outer_vertecies = (int) Math.floor(contour_length / cellWidth);
        int number_fixed_points = fixedVertices.get(contour_index).size();

        int number_variable_points = num_outer_vertecies - number_fixed_points;

        if(fixedVertices.get(contour_index).size() > 0){

            for (int i = 0; i < fixedVertices.get(contour_index).size(); i++) {
                int j = i + 1;
                if(i == fixedVertices.get(contour_index).size()){
                    j = 0;
                }

                Coordinate point1 = fixedVertices.get(contour_index).get(i);
                Coordinate point2 = point1;
                if(fixedVertices.get(contour_index).size() > 1){
                    point2 = fixedVertices.get(contour_index).get(j);
                }

                int index_p1 = outerContour.indexOf(point1);
                int index_p2 = outerContour.indexOf(point2);

                int distance = index_p1 - index_p2;
                if(distance == 0){
                    distance = fixedVertices.get(contour_index).size();
                }
                if(distance < 0){
                    distance = fixedVertices.get(contour_index).size() - index_p1 + index_p2;
                }

                // we can fit at most distance/cellWidth variable points between the two fixed points
                int points_in_between = (int) Math.floor(distance / cellWidth);

                for (int n = 0; n < points_in_between; n++) {
                    int index_new = index_p1 + (n + 1) * cellWidth;
                    if(index_new > fixedVertices.get(contour_index).size()){
                        index_new = index_new - fixedVertices.get(contour_index).size();
                    }
                    variableVertices.get(contour_index).add(outerContour.get(index_new));
                }
            }
        } else {
            // if there are no fixed points, simply distribute the variable points evenly
            for (int i = 0; i < number_variable_points; i++) {
                variableVertices.get(contour_index).add(outerContour.get(i*cellWidth));
            }
        }

        System.out.println("bla");



    }
	
	
	/**
     * Transforms list of feature lines into set of 'cellWidth'-long edges.
     */
	public void drawMesh(ArrayList<ArrayList<Coordinate>> featureLines){
		
		// place equally spaced points on contour line
		ArrayList<Edge> edges = new ArrayList<Edge>();
		// IMPLEMENT WITH coord.DISTANCE, NOT Count
		
//		for (ArrayList<Coordinate> ring : featureLines){
//			float eqCellWidth = ring.size()/Math.round(ring.size()/cellWidth);
//			int prev = 0;
//			for (float i = eqCellWidth-1; i < ring.size(); i += eqCellWidth){
//				edges.add(new Edge(ring.get(prev), ring.get(Math.round(i))));
//				prev = Math.round(i);
//			}
//			edges.add(new Edge(ring.get(0), ring.get(prev)));
//		}
//		
		
		// connect all pixels
		for (ArrayList<Coordinate> ring : featureLines){
			for (int i = 1; i < ring.size(); i++){
				edges.add(new Edge(ring.get(i-1), ring.get(i)));
			}
			edges.add(new Edge(ring.get(0), ring.get(ring.size()-1)));
		}
		
		SVGUtil svgwrite = new SVGUtil(edges, null);
        svgwrite.createSVG(System.getProperty("user.dir") + "/res/isophoteLines.svg");
	}
	
	
    /**
     * Generate distance map on feature map.
     */
    public float[][] distanceMap(float[][] map)
    {
    	final float orth = 1.0f;
    	final float diag = (float)Math.sqrt(2);
       	int x, y;
    	
    	final long startAt = System.nanoTime();
       	
       	// Set top and bottom edges to 0
      	for (x = 0; x < width; x++)
       	{
      		map[x][0] = 0;
      		map[x][height-1] = 0;
       	}
      	
      	// Set left and right edges to 0
  		for (y = 0; y < height; y++)
       	{
      		map[0][y] = 0;
      		map[width-1][y] = 0;
       	}
      	
    	// Forward pass
   	   	for (y = 1; y < height-1; y++) {
            for (x = 1; x < width - 1; x++) {
                if (map[x][y] == 0)
                    continue;

                //  a b c
                //  d + .
                //  . . .
                final float a = map[x - 1][y - 1] + diag;
                final float b = map[x][y - 1] + orth;
                final float c = map[x + 1][y - 1] + diag;
                final float d = map[x - 1][y] + orth;

                map[x][y] = Math.min(Math.min(Math.min(a, b), c), d);
            }
        }
    	
    	// Backward pass
   	   	for (y = height-2; y > 0; y--) {
            for (x = width - 2; x > 0; x--) {
                //  . . .
                //  . + e
                //  f g h
                final float e = map[x + 1][y] + orth;
                final float f = map[x - 1][y + 1] + diag;
                final float g = map[x][y + 1] + orth;
                final float h = map[x + 1][y + 1] + diag;

                map[x][y] = Math.min(map[x][y], Math.min(Math.min(Math.min(e, f), g), h));
            }
        }

    	final long endAt = System.nanoTime();
    	final double ms = (endAt - startAt) / 1000000.0;
    	System.out.println("Distance map took " + ms + " ms.");

    	
    	BufferedImage imgM = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);		
		//----------------------------------------------------------------------------------------
		// display distance map in color space
		float min = 1000000000;
		float max = 0;
		
   	   	for (y = 0; y < height; y++) {
            for (x = 0; x < width; x++) {
                if (map[x][y] > max)
                    max = map[x][y];
                if (map[x][y] < min)
                    min = map[x][y];
            }
        }
		
   	   	float range = max+1-min;
   	   	
   	   	for (y = 0; y < height; y++) {
            for (x = 0; x < width; x++) {
                float col = (map[x][y] / range);
                int newColor = (Math.round(col * 255) << 16) | (Math.round(col * 255) << 8) | Math.round(col * 255);
                imgM.setRGB(x, y, newColor);
            }
        }
   	   	
   	   	displayImage(imgM);
   	   	//----------------------------------------------------------------------------------------
   	   	
   	   
   	   	// display distance map as rings
   	   	BufferedImage imgM2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		//final int ridgeColour = (255 << 24) | (0 << 16) | (200 << 8) | (0);   	   				   			
		final int ridgeColour = (0 << 16) | (0 << 8) | (0);
		
   	   	for (y = 0; y < height; y++) {
            for (x = 0; x < width; x++) {
                final int col = (255 << 16) | (255 << 8) | 255;
                imgM2.setRGB(x, y, col);

//   	   			if (map[x][y] % 16 >= 0 && map[x][y] % 16 <= ringDist && figureMap[x][y] == 1){
                if (map[x][y] % cellWidth >= 0 && map[x][y] % cellWidth <= ringDist) {
                    imgM2.setRGB(x, y, ridgeColour);
                    map[x][y] = 255;
                } else
                    map[x][y] = 0;
            }
        }

   	   	displayImage(imgM2);
   	   	//----------------------------------------------------------------------------------------
		
		return map;
    }

    public void findPoints(ArrayList<ArrayList<Coordinate>> contours){

    }



	public void displayImage(BufferedImage img){
		JFrame frame = new JFrame();
		JLabel lblimage = new JLabel(new ImageIcon(img));
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(lblimage);

		frame.setExtendedState(frame.getExtendedState()|JFrame.MAXIMIZED_BOTH);
		frame.add(mainPanel);
		frame.setVisible(true);
	}
}
