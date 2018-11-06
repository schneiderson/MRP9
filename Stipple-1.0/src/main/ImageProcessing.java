package main;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * Image processing routines.
 * @author cambolbro
 */
public class ImageProcessing 
{
	//-------------------------------------------------------------------------
	
	/**
	 * Flood fills image, replacing all pixels with the replacement colour, unless target colour or already visited.
	 * @param img
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param rgbaTarget
	 * @param rgbaReplacement
	 */
	public static void floodFill
	(
		BufferedImage img, final int x, final int y, final int width, final int height, 
		final int[] rgbaTarget, final int[] rgbaReplacement
	) 
	{
		if (x < 0 || y < 0 || x >= width || y >= height)
			return;
		
		WritableRaster raster  = img.getRaster(); 
   		int[] rgba = { 0, 0, 0, 255 }; 
   		
   		raster.getPixel(x, y, rgba);
   		
   		if (rgba[0] == rgbaReplacement[0] && rgba[1] == rgbaReplacement[1] && rgba[2] == rgbaReplacement[2] && rgba[3] == rgbaReplacement[3])
    	  	return;  // already visited
    		
   		if (rgba[0] == rgbaTarget[0] && rgba[1] == rgbaTarget[1] && rgba[2] == rgbaTarget[2] && rgba[3] == rgbaTarget[3])
   			return;  // is target colour

   		//System.out.printf("rgba at (%d,%d) is %d, %d, %d, %d.\n", x, y, rgba[0], rgba[1], rgba[2], rgba[3]);

    	raster.setPixel(x, y, rgbaReplacement);

    	final int[][] off = { {-1,0}, {0,1}, {1,0}, {0,-1} }; 
		for (int a = 0; a < 4; a++)
		{
			floodFill(img, x+off[a][0], y+off[a][1],   width, height, rgbaTarget, rgbaReplacement);
		}
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Contracts an image by one pixel, i.e. shaves off out pixel layer.
	 * @param img
	 * @param width
	 * @param height
	 */
	public static void contractImage(BufferedImage img, final int width, final int height)
	{
		int x, y;
		final int[][] off = { {-1,0}, {0,1}, {1,0}, {0,-1} }; 
		
		WritableRaster raster  = img.getRaster(); 
   		int[] rgba = { 0, 0, 0, 255 }; 
   		final int[] rgbaOff = { 0, 0, 0, 0 }; 
   		
		//	Pass 1: Find border
		boolean[][] border = new boolean[width][height];
		   		
   		for (y = 0; y < height; y++)
   			for (x = 0; x < width; x++)
   			{
   				raster.getPixel(x, y, rgba);
   		  		if (rgba[0] != 0 || rgba[1] != 0 || rgba[2] != 0 || rgba[3] != 0)
   		  		{
   		  			for (int a = 0; a < 4; a++)
   		  			{
	   		  			final int xx = x + off[a][0];
	   		  			final int yy = y + off[a][1];
	   		  			if (xx >= 0 && yy >= 0 && xx < width && yy < height)
	   		  			{
	   		  				raster.getPixel(xx, yy, rgba);
	   		  				if (rgba[0] == 0 && rgba[1] == 0 && rgba[2] == 0 && rgba[3] == 0)
	   		  					border[x][y] = true;
	   		  			}
   		  			}
   		  		}
   			}
   		
   		//	Pass 2: Remove border pixels
   		for (y = 0; y < height; y++)
   			for (x = 0; x < width; x++)
   				if (border[x][y])
   					raster.setPixel(x, y, rgbaOff);
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Converts all non-transparent pixels to the specified mask colour.
	 * @param img
	 * @param width
	 * @param height
	 * @param rgbaMask Mask colour.
	 */
	public static void makeMask(BufferedImage img, final int width, final int height, final int[] rgbaMask)
	{
   		WritableRaster raster  = img.getRaster(); 
   		int[] rgba     = { 0, 0, 0, 255 }; 
   		
   		for (int y = 0; y < height; y++)
   			for (int x = 0; x < width; x++)
   			{
   				raster.getPixel(x, y, rgba);
   		  		if (rgba[0] != 0 || rgba[1] != 0 || rgba[2] != 0 || rgba[3] != 0)
   		  			raster.setPixel(x, y, rgbaMask);
   			}
	}
	
}
