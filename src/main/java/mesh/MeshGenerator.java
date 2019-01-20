package mesh;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Array;
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

import org.locationtech.jts.math.Vector2D;
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

        ArrayList<ArrayList<Coordinate>> points = createMesh(lines1, cornerPoints, cellWidth, contour_index);

        // draw variable points
        img.drawCrosses(points.get(1), 3, 255, 0, 255, 0);
        // draw fixed points
        //img.drawCrosses(points.get(0), 3, 255, 255, 0, 0);
        img.displayImage();

        System.out.println("Done.");
    }


	public ArrayList<ArrayList<Coordinate>> createMesh(ArrayList<ArrayList<Coordinate>> lines, ArrayList<Coordinate> cornerPoints, int cellWidth, int contour_index){
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
        int pointDist = (int) Math.round(contour_length / num_outer_vertecies);

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

                int index_p1 = indexOfCoordinate(outerContour, point1);
                int index_p2 = indexOfCoordinate(outerContour, point2);

                int distance = index_p1 - index_p2;
                if(distance == 0){
                    distance = fixedVertices.get(contour_index).size();
                }
                if(distance < 0){
                    distance = fixedVertices.get(contour_index).size() - index_p1 + index_p2;
                }

                // we can fit at most distance/cellWidth variable points between the two fixed points
                int points_in_between = (int) Math.floor(distance / pointDist);

                for (int n = 0; n < points_in_between; n++) {
                    int index_new = index_p1 + (n + 1) * pointDist;
                    if(index_new > fixedVertices.get(contour_index).size()){
                        index_new = index_new - fixedVertices.get(contour_index).size();
                    }
                    variableVertices.get(contour_index).add(outerContour.get(index_new));
                }
            }
        } else {
            // if there are no fixed points, simply distribute the variable points evenly
            for (int i = 0; i < number_variable_points; i++) {
                variableVertices.get(contour_index).add(outerContour.get(i * pointDist));
            }
        }

        // now propagate points to inner contours
        for (int current_contour_index = 0; current_contour_index < lines.size() - 1; current_contour_index++) {

            ArrayList<Coordinate> combinedPoints = new ArrayList<Coordinate>();
            //combinedPoints.addAll(fixedVertices.get(current_contour_index));
            combinedPoints.addAll(variableVertices.get(current_contour_index));

            // find point on next contour closest to perpendicular line from current point
            ArrayList<Coordinate> corresponding = getPointsOnNextContour(combinedPoints, current_contour_index, lines);
//            int nextContourIndex = getNextContour(current_contour_index, lines, true);
            int nextContourIndex = current_contour_index + 1;

            while(true){
                if(moveUntilNoChange(corresponding, fixedVertices.get(nextContourIndex), lines.get(nextContourIndex))){
                    break;
                }
            }

            variableVertices.get(nextContourIndex).addAll(corresponding);

        }

        // add everything together and return
        ArrayList<ArrayList<Coordinate>> vertecies = new ArrayList<>();
        vertecies.add(new ArrayList<Coordinate>());
        vertecies.add(new ArrayList<Coordinate>());

        for (int i = 0; i < lines.size(); i++) {
            vertecies.get(0).addAll(fixedVertices.get(i));
            vertecies.get(1).addAll(variableVertices.get(i));
        }

        return vertecies;

    }

    public int getIndexPointHalfwayBetween(Coordinate point1, Coordinate point2, ArrayList<Coordinate> contour){
        int p1 = indexOfCoordinate(contour, point1);
        int p2 = indexOfCoordinate(contour, point2);

        if(p1 < p2){
            return p1 + (int) Math.round((p2 - p1) / 2);
        } else {
            int offset = (int) Math.round(getDistanceOnContour(point1, point2, contour) / 2);
            if( p1 + offset > contour.size()){
                return offset - (contour.size() - p1);
            } else {
                return p1 + offset;
            }
        }
    }

    public int getDistanceOnContour(Coordinate point1, Coordinate point2, ArrayList<Coordinate> contour){
        int p1 = indexOfCoordinate(contour, point1);
        int p2 = indexOfCoordinate(contour, point2);

        int dist;
        if(p1 > p2){
            dist = contour.size() - p1 + p2;
        } else {
            dist = p2 - p1;
        }

        return dist;
    }

    public boolean moveUntilNoChange(ArrayList<Coordinate> variablePoints, ArrayList<Coordinate> fixedPoints, ArrayList<Coordinate> contour){
	    boolean noChange = true;

        for (int i = 0; i < variablePoints.size(); i++) {
            // merge with close fixed points
            for (Coordinate fixedPoint : fixedPoints) {
                if(variablePoints.get(i).distance(fixedPoint) <= cellWidth / 4){
                    variablePoints.set(i, fixedPoint);
                }
            }
        }

        // enforce minimum distance to other points
        for (int i = 0; i < variablePoints.size(); i++) {
            boolean skip = false;
            // merge with close fixed points
            for (Coordinate fixedPoint : fixedPoints) {
                if(fixedPoint.equals(variablePoints.get(i))){
                    skip = true;
                    System.out.println("skipping");
                    break;
                }
            }
            if(skip) continue;

            int prev = i;
            int next = i;

            while(variablePoints.get(i).equals(variablePoints.get(prev))){
                prev--;
                if(prev < 0){
                    prev = variablePoints.size() + prev;
                }
            }

            while(variablePoints.get(i).equals(variablePoints.get(next))){
                next++;
                if(next > variablePoints.size() - 1){
                    next = 0;
                }
            }


            int dist_prev_next = getDistanceOnContour(variablePoints.get(prev), variablePoints.get(next), contour);
            int dist_prev = getDistanceOnContour(variablePoints.get(prev), variablePoints.get(i), contour);
            int dist_next = getDistanceOnContour(variablePoints.get(i), variablePoints.get(next), contour);

            if(dist_prev < cellWidth || dist_next < cellWidth){
                if(dist_prev_next < cellWidth * 2){ // merge with closest
                    int merge_with = prev;

                    if(dist_prev > dist_next) {
                        merge_with = next;
                    }

                    if(fixedPoints.contains(variablePoints.get(merge_with))){
                        // move point to fixed point
                        movePointsWithCoordinate(variablePoints, (Coordinate) variablePoints.get(i).clone(), contour.get(merge_with));
                    } else {
                        // move both half way
                        int newIndex = getIndexPointHalfwayBetween(variablePoints.get(merge_with), variablePoints.get(i), contour);
                        movePointsWithCoordinate(variablePoints, (Coordinate) variablePoints.get(i).clone(), contour.get(newIndex));
                        movePointsWithCoordinate(variablePoints, (Coordinate) variablePoints.get(merge_with).clone(), contour.get(newIndex));
                    }

                    //System.out.println("change 1 detected");
                    noChange = false;
                } else { // enough space to move
                    // move half way
                    int oldIndex = indexOfCoordinate(contour, variablePoints.get(i));
                    int newIndex = getIndexPointHalfwayBetween(variablePoints.get(prev), variablePoints.get(next), contour);
                    movePointsWithCoordinate(variablePoints, (Coordinate) variablePoints.get(i).clone(), contour.get(newIndex));

                    //System.out.println("change 2 detected");
                    noChange = false;
                }
            }
        }

        System.out.println("return");
        return noChange;
    }


    public ArrayList<Coordinate> getPointsOnNextContour(ArrayList<Coordinate> outerContourPoints, int outerContourIndex,
                                                  ArrayList<ArrayList<Coordinate>> lines){

        ArrayList<Coordinate> currentContour = lines.get(outerContourIndex);

        //int nextContourIndex = getNextContour(outerContourIndex, lines, true);
        int nextContourIndex = outerContourIndex + 1;
        ArrayList<Coordinate> nextContour = lines.get(nextContourIndex);

        ArrayList<Coordinate> corresponding = new ArrayList<Coordinate>();

        for (int i = 0; i < outerContourPoints.size(); i++) {
            int prev = i - 1;
            int next = i + 1;

            if(i == 0){
                prev = outerContourPoints.size() - 1;
            } else if(i == outerContourPoints.size() - 1){
                next = 0;
            }

            // draw "line" between previous and next point
            Vector2D helpVec = new Vector2D(outerContourPoints.get(prev), outerContourPoints.get(next));

            // normalize vector, then scale by cellWidth
            helpVec = helpVec.normalize().multiply(cellWidth);

            // rotate vector by 90 degrees
            helpVec = helpVec.rotateByQuarterCircle(1);

            // determine point
            Coordinate refPoint1 = helpVec.translate(outerContourPoints.get(i));
            Coordinate refPoint2 = helpVec.multiply(-1).translate(outerContourPoints.get(i));

            // find point on next contour that is closest
            Coordinate closest1 = null;
            Coordinate closest2 = null;
            double dist1 = Double.MAX_VALUE;
            double dist2 = Double.MAX_VALUE;

            for (Coordinate coordinate : nextContour) {
                if(coordinate.distance(refPoint1) < dist1){
                    dist1 = coordinate.distance(refPoint1);
                    closest1 = coordinate;
                }
                if(coordinate.distance(refPoint2) < dist2){
                    dist2 = coordinate.distance(refPoint2);
                    closest2 = coordinate;
                }
            }

            if(dist1 < dist2){
                corresponding.add(closest1);
            } else {
                corresponding.add(closest2);
            }

        }

        return corresponding;
    }

    public void movePointsWithCoordinate(ArrayList<Coordinate> points, Coordinate from, Coordinate to){
        for (int i = 0; i < points.size(); i++) {
            if(points.get(i).equals(from)){
                points.set(i, to);
            }

        }
    }

    public int getNextContour(int current_index, ArrayList<ArrayList<Coordinate>> lines, boolean inner){
        ArrayList<Coordinate> currentContour = lines.get(current_index);

        // get arbitrary point on outer contour
        Coordinate refPoint = currentContour.get(0);

        int nextIndex = -1;
        boolean nextContourFound = false;

        for (int i = 0; i < lines.size(); i++) {
            if(i == current_index) continue;

            for (Coordinate coordinate : lines.get(i)) {
                if(coordinate.distance(refPoint) < cellWidth + 2){
                    nextIndex = i;
                    nextContourFound = true;
                    break;
                }
            }

            if ( nextContourFound ) break;
        }

        return nextIndex;
    }

    public boolean isPointContainedInContour(ArrayList<Coordinate> contour, Coordinate point){
        int[] xCoords = new int[contour.size()];
        int[] yCoords = new int[contour.size()];

        for (int i = 0; i < contour.size(); i++) {
            xCoords[i] = (int) contour.get(i).getX();
            yCoords[i] = (int) contour.get(i).getY();
        }

        Polygon pol = new Polygon(xCoords, yCoords, contour.size());

        return pol.contains(point.x, point.y);
    }

    public int indexOfCoordinate(ArrayList<Coordinate> coordinates, Coordinate coordinate){
	    int index = -1;
        for (int i = 0; i < coordinates.size(); i++) {
            if(coordinates.get(i).equals(coordinate)){
                index = i;
                break;
            }
        }
        return index;
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
