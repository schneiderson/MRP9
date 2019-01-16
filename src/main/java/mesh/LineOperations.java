package mesh;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.locationtech.jts.geom.Coordinate;

public class LineOperations {

	ArrayList<Coordinate> contour = new ArrayList<Coordinate>();
	ArrayList<Coordinate> featureCoords = new ArrayList<Coordinate>();
	
	/**
     * Extract lines from pixel map.
     */
	public ArrayList<ArrayList<Coordinate>> extractLines(float[][] map){
		
    	int sx = map.length;
    	int sy = map[0].length;
		
		featureCoords = new ArrayList<Coordinate>();
		
   	   	for (int y = 5; y < sy-5; y++){
   	   		for (int x = 5; x < sx-5; x++){
				if (map[x][y] == 1)
					featureCoords.add(new Coordinate(x,y));
			}
		}
   	   	
   	   	ArrayList<ArrayList<Coordinate>> featureLines = new ArrayList<ArrayList<Coordinate>>();
		
		boolean loop = true;
		while (loop){
			if(featureCoords.size() < 1){
				loop = false;
				break;
			}
			
			Coordinate c = featureCoords.get(0);
			
			int i = (int) c.x;
			int j = (int) c.y;
			
			Coordinate frst = null;
			Coordinate cur = new Coordinate(i,j);
			int cnt = 0;
			ArrayList<Coordinate> ring = new ArrayList<Coordinate>();
			while (cur != null){
				ring.add(cur);
				featureCoords.remove(cur);
				Coordinate temp = cur;
				cur = next(frst, temp, cnt);
				cnt++;
				if (frst == null)
					frst = new Coordinate(temp.x, temp.y);
			}
			featureLines.add(ring);
		}
		
		return featureLines;
	}
	
	
	/**
     * Find next in line.
     */
	public Coordinate next(Coordinate first, Coordinate cur, int count){
		Coordinate test = null;
		
		ArrayList<Coordinate> neighbours = new ArrayList<Coordinate>();		
		
		// determine all possible next coordinates within neighbourhood
		for (int i = -1; i <= 1; i++)
			for (int j = -1; j <= 1; j++)
				test = new Coordinate(cur.x+i, cur.y+j);
				if (featureCoords.contains(test)){
					neighbours.add(test);
					featureCoords.remove(test);
				}
		
		// select next coordinate which has a valid neighbourhood on its own
		for (Coordinate n : neighbours)
			for (int i = -1; i <= 1; i++)
				for (int j = -1; j <= 1; j++){
					test = new Coordinate(n.x+i, n.y+j);
					// equals first within first three
					if (test.equals(first) && count > 10)
						System.out.println("Ring completed");
					if (featureCoords.contains(test))
						return n;
				}

		System.out.println("WARNING: LINE ENDS IN NOWHERE!");		
		return null;
	}
	
	
	/**
     * Extracts contour line (longest line) in list. 
     */
	public ArrayList<ArrayList<Coordinate>> extractContour(ArrayList<ArrayList<Coordinate>> lines){
		
		ArrayList<ArrayList<Coordinate>> list = new ArrayList<ArrayList<Coordinate>>();
		ArrayList<Coordinate> contour = lines.get(0);
		for (int i = 1; i < lines.size()-1; i++)
			if (lines.get(i).size() > contour.size())
				contour = lines.get(i);
		list.add(contour);

		return list;
	}	
	
	
	/**
     * Create pixel map from list of lines.
     */
	public float[][] linesToPixels(ArrayList<ArrayList<Coordinate>> lines, int sx, int sy){
		
		float[][] map = new float[sx][sy];
		
		for (int x = 0; x < sx; x++)
			Arrays.fill(map[x], 0);
		
		for(ArrayList<Coordinate> line : lines)
			for(Coordinate coord : line)
				map[(int) coord.x][(int) coord.y] = 1;
		
		return map;
	}
	
	/**
     * Performs sekeletonization/thinning operation on pixel map.
     * ... to be completed
     */
	public float[][] skeletonize(float[][] map){
		int sx = map.length;
    	int sy = map[0].length;
    	
		float[][] thinMap = new float[sx][sy];
		
		
		
		return thinMap;
	}
	
	
	/**
     * Generates pixel map with 1 for pixels within contour (main feature line) and 0 for outside contour.
     */
	public float[][] contourMap(ArrayList<ArrayList<Coordinate>> featureLines, int sx, int sy){
		
		BufferedImage img = new BufferedImage(sx, sy, BufferedImage.TYPE_INT_RGB);
		final int black = (0 << 16) | (0 << 8) | 0;
		final int white = (255 << 16) | (255 << 8) | 255;
		final int red = (255 << 16) | (0 << 8) | 0;
		
		ArrayList<Coordinate> contour = featureLines.get(0);
		for (int i = 1; i < featureLines.size()-1; i++)
			if (featureLines.get(i).size() > contour.size())
				contour = featureLines.get(i);
		
		float[][] figureMap = new float[sx][sy];
		
		for (int x = 0; x < sx; x++)
			Arrays.fill(figureMap[x], 0);		
		for (int x = 0; x < sx; x++)
			for (int y = 0; y < sy; y++){
				img.setRGB(x, y, white);
			}
		
		int startY = (int) contour.get(0).y;
		int count = 0;
		
		boolean loop = true;
		while (loop){
			
			if(contour.size() < 1){
				loop = false;
				break;
			}
			
			Coordinate c = contour.get(0);
			
			if (c.y >= startY){
				while(contour.contains(c)){
					figureMap[(int) c.x][(int) c.y] = 1;
					img.setRGB((int) c.x, (int) c.y, red);
					contour.remove(c);
					c = new Coordinate(c.x+1, c.y);
				}
				
				boolean end = true;
				for (int x = (int) c.x+2; x < sx; x++){
					Coordinate test = new Coordinate(x, c.y);
					if (contour.contains(test)){
						end = false;
						break;
					}
				}
				
				if (end)
					continue;
				
				for (int x = (int) c.x+1; x < sx; x++){
					c = new Coordinate(x, c.y);
					figureMap[(int) c.x][(int) c.y] = 1;
					img.setRGB((int) c.x, (int) c.y, black);
					while(contour.contains(c)){
						figureMap[(int) c.x][(int) c.y] = 1;
						img.setRGB((int) c.x, (int) c.y, red);
						contour.remove(c);
						c = new Coordinate(c.x+1, c.y);
						end = true;
					}
					if (end)
						break;
				}
			}
			else{
				while(contour.contains(c)){
					figureMap[(int) c.x][(int) c.y] = 1;
					img.setRGB((int) c.x, (int) c.y, red);
					contour.remove(c);
					c = new Coordinate(c.x-1, c.y);
				}
				
				boolean end = true;
				for (int x = (int) c.x-2; x >= 0; x--){
					Coordinate test = new Coordinate(x, c.y);
					if (contour.contains(test)){
						end = false;
						break;
					}
				}
				
				if (end)
					continue;
				
				for (int x = (int) c.x-1; x >= 0; x--){
					c = new Coordinate(x, c.y);
					figureMap[(int) c.x][(int) c.y] = 1;
					img.setRGB((int) c.x, (int) c.y, black);
					while(contour.contains(c)){
						figureMap[(int) c.x][(int) c.y] = 1;
						img.setRGB((int) c.x, (int) c.y, red);
						contour.remove(c);
						c = new Coordinate(c.x-1, c.y);
						end = true;
					}
					if (end)
						break;
				}
			}
			count++;
		}
		
		JFrame frame = new JFrame();
		JLabel lblimage = new JLabel(new ImageIcon(img));		
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(lblimage);
		
		frame.setExtendedState(frame.getExtendedState()|JFrame.MAXIMIZED_BOTH);
		frame.add(mainPanel);
		frame.setVisible(true);

		return figureMap;
	}
	
}
