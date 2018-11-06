package graphics;

//import main.Dot;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
//import java.util.List;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.awt.image.DataBufferInt;

import javax.swing.JPanel;

import main.Stipple;

//---------------------------------------------------------------

/**
 * Game view for board display and GUI.
 * @author cambolbro
 */
public class MapView extends JPanel 
{
	/** Default class UID. */
	private static final long serialVersionUID = 1L;

	/** */
	protected Stipple app = null;
	
	/** */
	protected BufferedImage source = null;
	
   /** Distance map. */
  	protected float[][] map = null;

  	/** Distance map. */
	protected float[][] buffer = null;

  	/** Distance map. */
	protected int[][] peaks = null;
  	
  	/** Adjacent steps. */
 	final int[][] steps = { {-1, -1}, {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0} }; 
  	
 	/** Relative distance weights (Euclidean). */
 	final float[] weights = { 1.0f, 0.707f, 1.0f, 0.707f, 1.0f, 0.707f, 1.0f, 0.707f }; 
  	
// 	/** List of dots. */
// 	final List<Dot> dots = new ArrayList<Dot>();
 	
    //---------------------------------------------------------------------
    
    /**
     * Constructor.
     * @param app
     */
    public MapView(final Stipple app)
    {
    	this.app = app;
    }
    
    //---------------------------------------------------------------------

////    /**
////     * @return List of stipple dots.
////     */
////    public List<Dot> dots()
////    {
////    	return dots;
////    }
//    
//    /**
//     * @param src
//     */
//    public void setSourceImage(final BufferedImage src)
//    {
//    	source = src;
//    	init();
//    }
    
    //---------------------------------------------------------------------

//    /**
//     * @param pt
//     */
//    void addSample(final Point pt)
//    {
//       	final double a = (pt.x - downAt.x) / (double)getWidth();
//       	final double b = (pt.y - downAt.y) / (double)getHeight();
//    	final double c = 0;  //curvature(?);
//    	
//    	final long tick = System.currentTimeMillis();
//    	
//    	final Sample sample = new Sample(new Point(pt.x, pt.y), tick, a, b, c);
//    	samples.add(0, sample);
//
//     	//graphView.invalidate();
//    }
    
//    /**
//     * Update the graph view.
//     */
//    void updateGraphView()
//    {
//    	graphView.setData(samples);
//    	graphView.invalidate();
//    }
    
    //---------------------------------------------------------------------

//    /**
//     * Initialise graphics.
//     */
//    void init()
//    {
//		if (map == null)
//		{
//			map    = new float[source.getWidth()][source.getHeight()];
//			buffer = new float[source.getWidth()][source.getHeight()];
//			peaks  = new   int[source.getWidth()][source.getHeight()];
//		}
//
//		distanceMap();
////		getDots();
//		invalidate();
//    }

    //---------------------------------------------------------------------

    public void paint(Graphics g) 
    {
    	Graphics2D g2d = (Graphics2D)g;
     	g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    	g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    	g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    	//showDistanceMap(g2d);
    	if (app.stippler() != null)
    	{
    		final BufferedImage image = app.stippler().segmented();
    		if (image != null)
    			g2d.drawImage(image,  0, 0,  null);
    	}
    }
    
    //--------------------------------------------

//    /**
//     * Show the distance map.
//     * @param g2d
//     */
//    void showDistanceMap(Graphics2D g2d)
//    {
//    	if (source == null)
//    		return;
//    	
//    	final int sx = source.getWidth();
//    	final int sy = source.getHeight();
//
//		final BufferedImage canvas = new BufferedImage(sx, sy, BufferedImage.TYPE_INT_ARGB);
//    	Graphics2D g2dCanvas = (Graphics2D)canvas.getGraphics();
//    	g2dCanvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//    	g2dCanvas.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//    	g2dCanvas.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//
//    	final int highlight = 255 |  (0 << 8) | (0 << 16) | (0xff << 24);
//			
//     	for (int x = 0; x < sx; x++)
//      		for (int y = 0; y < sy; y++)
//      		{
////      			if (map[x][y] == 0)
////     				canvas.setRGB(x, y, Color.red.getRGB());
////      			else
////      				canvas.setRGB(x, y, Color.white.getRGB());
//
//      			final int intensity = 255 - (int)(map[x][y] * 255);
//      			
////      			if ((x + y) % 100 == 0)
////      				System.out.println(map[x][y] + " gives " + intensity);
//      			
//      			final int rgb = (intensity) |  (intensity << 8) | (intensity << 16) | (0xff << 24);
//      			canvas.setRGB(x, y, rgb);
//      			
//      			if (peaks[x][y] != 0)
//      				canvas.setRGB(x, y, highlight);
//      		}
//     	g2d.drawImage(canvas, 0, 0, null);
//    }
//    
//    //--------------------------------------------
//
//    /**
//     * Generate distance map.
//     */
//    public void distanceMap()
//    {
////    	final float orth = 1.0f;
////    	final float diag = (float)Math.sqrt(2);
//       	int x, y; 	
//
//    	final int sx = source.getWidth();
//    	final int sy = source.getHeight();
//    	
//    	final long startAt = System.nanoTime();
//    	
//    	// Clear maps
//       	for (x = 0; x < sx; x++)
//       	{
//      		Arrays.fill(map[x], 0xffff);
//      		Arrays.fill(peaks[x], 0);
////      		Arrays.fill(ridge[x], 0);
//       	}
//       	
//       	// Get pixel intensities
//       	final int[] pixels = ((DataBufferInt)source.getRaster().getDataBuffer()).getData();
//       	for (int p = 0; p < pixels.length; p++)
//       	{
//       		final int rgba = pixels[p];
//       		final int r = (rgba >> 16) & 0xff;
//       		final int g = (rgba >>  8) & 0xff;
//       		final int b = (rgba      ) & 0xff;
////       		final int a = (rgba >> 24) & 0xff;
//       		
//       		final float intensity = 1.0f - (r + g + b) / (255.0f * 3);
//       		
//        		//if (r < threshold || g < threshold || b < threshold)
//       			map[p % sx][p / sx] = intensity;  //0;
//       		
////       		if ((pixels[p] & white) != white)
////      			map[p % sx][p / sx] = 0;
//         }
//    	
////    	// Forward pass
////   	   	for (y = 1; y < sy-1; y++)
////   	   		for (x = 1; x < sx-1; x++)
////       	   	{
////   	   			if (map[x][y] == 0)
////   	   				continue;
////   	   			
////   	   			//  a b c
////   	   			//  d + .
////   	   			//  . . .
////   	   			final float a = map[x-1][y-1] + diag;
////   	   			final float b = map[x  ][y-1] + orth;
////   	   			final float c = map[x+1][y-1] + diag;
////   	   			final float d = map[x-1][y  ] + orth;
////   	   			
////       	   		map[x][y] = Math.min(Math.min(Math.min(a, b), c), d);
////       	   	}
////    	
////    	// Backward pass
////   	   	for (y = sy-2; y > 0; y--)
////   	   		for (x = sx-2; x > 0; x--)
////       	   	{
////   	   			//  . . .
////   	   			//  . + e
////   	   			//  f g h
////   	   			final float e = map[x+1][y  ] + orth;
////   	   			final float f = map[x-1][y+1] + diag;
////   	   			final float g = map[x  ][y+1] + orth;
////   	   			final float h = map[x+1][y+1] + diag;
//// 
////        	   	map[x][y] = Math.min(map[x][y], Math.min(Math.min(Math.min(e, f), g), h));
////       	   	}
//
//       	final int blurPasses = 0;
//       	for (int pass = 0; pass < blurPasses; pass++)
//       	{
//       		for (x = 0; x < sx; x++)
//       			for (y = 0; y < sy; y++)
//       			{
//       				float acc = map[x][y];
//       				float weight = 1;
//       				
//       				for (int a = 0; a < steps.length; a++)
//       				{
//       					final int xx = x + steps[a][0];
//       					final int yy = y + steps[a][1];
//       					
//       					if (xx >= 0 && xx < sx && yy >= 0 && yy < sy)
//       					{
//       						acc    += weights[a] * map[xx][yy];
//       						weight += weights[a];
//       					}
//       				}
//       				buffer[x][y] = acc / weight;
//       			}
//       		for (x = 1; x < sx-1; x++)
//       			for (y = 1; y < sy-1; y++)
//       				map[x][y] = buffer[x][y];
//       	}
//       	
//       	// Find peaks
//   		for (x = 0; x < sx; x++)
//   			for (y = 0; y < sy; y++)
//   			{
//   				final float ref = map[x][y];
//   				if (ref < 0.1)
//   					continue;
//   				
//   				int a;
//   				for (a = 0; a < steps.length; a++)
//   				{
//   					final int xx = x + steps[a][0];
//   					final int yy = y + steps[a][1];
//   					
//   					if (xx >= 0 && xx < sx && yy >= 0 && yy < sy)
//   					{
//   						if (map[xx][yy] > ref)
//   							break;  // higher adjacent value
//   					}
//   				}
//   				if (a >= steps.length)
//   					peaks[x][y] = 1;
//   			}
//       	
//    	final long endAt = System.nanoTime();
//    	final double ms = (endAt - startAt) / 1000000.0;
//    	System.out.println("Mpping took " + ms + " ms.");
//
////   	   	final Graphics2D g2d = (Graphics2D)this.getGraphics();
////
//////   	   	if (x != 0 && y != 0)
//////   	   	{
//////   	   		// Ridge found
//////  			g2d.setColor(Color.green);
//////         	g2d.fillOval(x-1, y-1, 3, 3);
//////   	   	}   	   	   	
////////   	   	g2d.drawImage(img, 0, 0, null);
////    	
////		BufferedImage imgM = new BufferedImage(sx, sy, BufferedImage.TYPE_INT_ARGB);
////		Graphics2D g2dM = (Graphics2D)imgM.getGraphics();
////		g2dM.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
////	    g2dM.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
////		g2dM.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
////
////		final int ridgeColour = (255 << 24) | (0 << 16) | (200 << 8) | (0);   	   			
////  			
////   	   	for (y = 0; y < sy; y++)
////   	   		for (x = 0; x < sx; x++)
////       	   	{
////   	   			final int shade = Math.min(255, (int)(map[x][y] / (double)orth));
////   	   			final int r = 255;
////   	   			final int g = 255 - shade;
////   	   			final int b = 255 - shade;   	   			
////   	   			final int a = 127;
////   	   			final int col = (a << 24) | (r << 16) | (g << 8) | b;   	   			
////   	   			imgM.setRGB(x, y, col);
////   	   			
//////   	   			if (ridge[x][y] == 1)
//////   	   				imgM.setRGB(x, y, ridgeColour);
////      	   	}
////    	g2d.drawImage(imgM, 0, 0, null);
//    }
//    
//    //-------------------------------------------------------------------------
//
////    /**
////     * Get stipple dots.
////     */
////    void getDots()
////    {
////    	dots.clear();
////    	    	
////    	final int sx = source.getWidth();
////    	final int sy = source.getHeight();
////
////     	for (int x = 0; x < sx; x++)
////      		for (int y = 0; y < sy; y++)
////      			if (peaks[x][y] != 0)
////      			{
////      				// Create a dot
////      				final int rgba = source.getRGB(x, y);
////      				final Dot dot = new Dot(x, y, rgba);
////      				dots.add(dot);
////      			}
////     }
//    
//    //-------------------------------------------------------------------------
//
////    /**
////     * @param x
////     * @param y
////     * @return Whether the specified pixel is a ridge
////     */
////    boolean isRidge(final int x, final int y)
////    {
////    	if (map[x][y] == 0)
////    		return false;
////    	
////        final int sx = getWidth();
////    	final int sy = getHeight();
////
////    	if (x < 1 || y < 1 || x >= sx-1 || y >= sy-1)
////    		return false;  // don't go to border
////    	
////    	//  0 1 2
////    	//  7 + 3
////    	//  6 5 4
////       	final float[] nbor = new float[8];    	
////    	for (int n = 0; n < 8; n++)
////        	nbor[n] = map[x+steps[n][0]][y+steps[n][1]];
////
////    	if 
////    	(	
////    		nbor[0] == map[x][y] && nbor[1] == nbor[0] && nbor[7] == nbor[0]
////    		||
////    		nbor[1] == map[x][y] && nbor[2] == nbor[1] && nbor[3] == nbor[1]
////    		||
////    		nbor[4] == map[x][y] && nbor[3] == nbor[4] && nbor[5] == nbor[4]
////    		||
////    		nbor[6] == map[x][y] && nbor[5] == nbor[6] && nbor[7] == nbor[6]
////    	)
////    		return true;  // 4x4 plateau
////    	
////    	// Count number of monotonically increasing runs in circuit of nbors
////    	final int[] slope = new int[8];
////    	int numFlat = 0;
////    	for (int n = 0; n < 8; n++)
////     		if (nbor[n] < nbor[(n + 1) % 8])
////     		{
////     			slope[n] = 1;
////     		}
////     		else if (nbor[n] > nbor[(n + 1) % 8])
////     		{
////     			slope[n] = -1;
////     		}
////     		else
////     		{
////     			slope[n] = 0;
////     			numFlat++;
////     		}
////       	if (numFlat >= 8)
////    		return true;  // plateau: central cell must be an apex(?)
////    	
////    	// Relabel 0 according to next slope
////    	for (int n = 0; n < 8; n++)
////    		if (slope[n] == 0)
////    		{
////    			for (int m = 1; m < 8; m++)
////    			{
////    				final int nn = (n + m) % 8;
////    				if (slope[nn] != 0)
////    				{
////    					slope[n] = slope[nn];
////    					break;
////    				}
////    			}
////    		}
////    		
////    	// Count total changes in slope	
////    	int changes = 0;
////    	for (int n = 0; n < 8; n++)
////    		if (slope[n] != slope[(n + 1) % 8])
////    			changes++;
////    	
////    	if (changes >= 4)
////    		return true;  // is on ridge
////    	
////    	return false;
////    }
////    
////    //-------------------------------------------------------------------------
////
////    /**
////     * Follows the ridge from the specified ridge point.
////     * @param x
////     * @param y
////     */
////    void followRidge(final int x, final int y)
////    {
////    	ridge[x][y] = 1;
////    	
////        final int sx = getWidth();
////    	final int sy = getHeight();
////
////    	for (int n = 0; n < 8; n++)
////    	{
////    		final int xx = x + steps[n][0];
////    		final int yy = y + steps[n][1];
////    		
////        	if (xx < 1 || yy < 1 || xx >= sx-1 || yy >= sy-1)
////        		continue;  // don't go to border
////
////    		if (ridge[xx][yy] == 0 && isRidge(xx, yy))
////    			followRidge(xx, yy);
////    	}
////    }
    
    //-------------------------------------------------------------------------
}
