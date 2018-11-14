package stippling.main;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import org.locationtech.jts.algorithm.CGAlgorithms;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.triangulate.VoronoiDiagramBuilder;

//-----------------------------------------------------------------------------

/**
 * Performs stipple calculations.
 * @author cambolbro
 */
public class Stippler 
{
	//-----------------------------------------

	/** Number of stipple dots (some will be eliminated as whitespace). */
	public static int numDots = 5000; 
		
	/** Sample rate. */
	public static int overSample = 1;
		
	/** Scale each cell to fit in a cellBuffer-sized square window for computing the centroid. */
	public static int cellBuffer = 128;  //100;  
	
	/** Number of relaxation passes. */
	public static int numPasses = 2;

	/** Dot multiple for output. */
	public static double dotSize = 1.0;

	/** Difference in dot size. */
	public static double dotRange = 2.0;

	/** */
	public static int blurFactor = 1;
	
	/** Exclude white dots. */
	//public static boolean excludeWhite = true;
	public static int whiteThreshold = 95;

	/** Current pass number. */
	public static int pass = 0;
	
	/** User pause. */
	public static boolean pause = false;
	
	//-----------------------------------------
	
	/** */
	protected String imagePath = "";
	
	/** */
	protected String imageName = "";
	
	/** */
	protected BufferedImage source = null;

	/** */
	protected BufferedImage scaled = null;

	/** */
	protected BufferedImage blurred = null;
	
	/** */
	protected BufferedImage segmented = null;
	
	/** */
	protected Stipple app = null;
	
	/** Pixel intensities in range 0..1. */
	protected float[][] intensity;
	
	/** The stipples. */
	protected List<Dot> dots = new ArrayList<Dot>();
	
	/** RNG. */
	protected Random rng = new Random(System.currentTimeMillis());
	
	/** Vividsolutions voronoi diagram builder. */
	protected VoronoiDiagramBuilder builder = null;
	
	/** Geometry factory for constructing VD. */
	protected GeometryFactory geomFactory = new GeometryFactory();

	/** Collection of polygons from VD. */
	protected GeometryCollection polys = null;
	
	/** Clipping region. */
	protected Envelope frame = null;

//   /** Distance map. */
//  	protected float[][] map = null;
  	
  	/** Adjacent steps. */
 	final int[][] steps = { {-1, -1}, {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0} }; 
  	
 	/** Relative distance weights (Euclidean). */
 	final float[] weights = { 1.0f, 0.707f, 1.0f, 0.707f, 1.0f, 0.707f, 1.0f, 0.707f }; 
 	
	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 * @param app
	 */
	public Stippler(final Stipple app)
	{
		this.app = app;
		loadImage("src/res/robot-2.jpg");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Source image.
	 */
	public BufferedImage source()
	{
		return source;
	}
	
	/**
	 * @return Scaled copy of source image.
	 */
	public BufferedImage scaled()
	{
		return scaled;
	}
	
	/**
	 * @return Blurred copy of scaled image.
	 */
	public BufferedImage blurred()
	{
		return blurred;
	}
	
	/**
	 * @return Segmented copy of blurred image.
	 */
	public BufferedImage segmented()
	{
		return segmented;
	}
	
	/**
	 * @return List of dots.
	 */
	public List<Dot> dots()
	{
		return dots;
	}
		
	/**
	 * @return Polygons.
	 */
	public GeometryCollection polys()
	{
		return polys;
	}
	
//	/** 
//	 * @return Distance map.
//	 */
//	public float[][] map()
//	{
//		return map;
//	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Reload the source image. Do this after changing overSample rate.
	 */
	public void reloadImage()
	{
		loadImage(imagePath);
	}
	
	/**
	 * Load the source image.
	 * @param filePath
	 * @return Whether image was loaded successfully.
	 */
	public boolean loadImage(final String filePath)
	{
		boolean loaded = false;
		
		System.out.println("Loading image \"" + filePath + "\"...");
		
		//final URL url = getClass().getResource(filePath);
		//System.out.println("url is " + url);
		
		final File file = new File(filePath);
		try 
		{
			source = ImageIO.read(file);
			//source = ImageIO.read(url);
			loaded = true;
		} 
		catch (IOException e) 
		{ 
			app.mainView().setError("Couldn't load image file " + filePath + ".");
			e.printStackTrace(); 
		}
		
		if (loaded)
		{
			createScaledImage();
			createBlurredImage();
			determineIntensity();
			//distanceMap();
		
			polys = null;
			createInitialDots();

			imagePath = new String(filePath);
			imageName = new String(imagePath);
			while (imageName.contains("/"))
				imageName = imageName.substring(imageName.indexOf('/') + 1);
		}
		pass = 0;
		
		if (app.mainView() != null)
			app.mainView().repaint();

		if (app.dotView() != null)
			app.dotView().repaint();
		
		if (app.globeView() != null)
			app.globeView().repaint();

		return loaded;
	}

	//-------------------------------------------------------------------------

	/**
	 * Load the source image.
	 * @param filePath
	 */
	public void loadDots(final String filePath)
	{
		System.out.println("Loading dots from file \"" + filePath + "\"...");
		
	   	try
	    {
 	   	  	final File file = new File(filePath);
	    	if (!file.exists())
	    	{
	    		System.out.println("Couldn't find file " + filePath + ".");
	    		return;
	    	}
	    		
	    	dots.clear();
		 	
	    	final FileReader fileReader = new FileReader(file.getPath());
	    	final BufferedReader reader = new BufferedReader(fileReader);
	    	String line;
	    	while ((line = reader.readLine()) != null) 
	    	{ 
	    		String[] subs = line.split(" ");
	    		//System.out.println(subs.length + " substrings.");
	    		
	    		// Extract dot information, four items per line
	    		final Dot dot = new Dot();
	    		if (subs.length == 4)
	    		{
	    			dot.ix = Double.parseDouble(subs[0]);
	    			dot.iy = Double.parseDouble(subs[1]);
	    			dot.ir = Double.parseDouble(subs[2]);
	    		
	    			String sub = subs[3].substring(2);  // skip "0x"
	    			dot.rgba = Integer.parseInt(sub, 16);
	    		}
	    	}
	    	reader.close();   	
	    }
	    catch(IOException e)
	    {
	    	e.printStackTrace();
	    }
		
//	   	mappedToSphere = false;
	   	
//		if (app.dotView() != null)
//			app.dotView().repaint();
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Scale the source image.
	 */
	void createScaledImage()
	{
		if (source == null)
			return;
		
		final int sx = source.getWidth();
		final int sy = source.getHeight();
		
		// Scale as appropriate
		final int dx = sx * overSample;
		final int dy = sy * overSample;
		
		scaled = new BufferedImage(dx, dy, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = (Graphics2D)scaled.getGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		g2d.drawImage(source, 0, 0, dx, dy, 0, 0, sx, sy, null);
	}
	 
	/**
	 * Blur the source image.
	 */
	void createBlurredImage()
	{
		if (scaled == null)
			return;
		
		final int wd = scaled.getWidth();
		final int ht = scaled.getHeight();
		
		blurred = new BufferedImage(wd, ht, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = (Graphics2D)blurred.getGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		g2d.drawImage(scaled, 0, 0, null);
		
  		final int blurAmount = overSample * blurFactor;  //sample + 1;
  		blurred = Filters.gaussianBlurFilter(blurAmount, true).filter(blurred, null);
  		blurred = Filters.gaussianBlurFilter(blurAmount, false).filter(blurred, null);
	}
	
	/** 
	 * Determine intensity from blurred image.
	 */
	void determineIntensity()
	{
		final int wd = blurred.getWidth();
		final int ht = blurred.getHeight();

		intensity = new float[wd][ht];
		
		for (int x = 0; x < wd; x++)
			for (int y = 0; y < ht; y++)
			{
				final int rgba = blurred.getRGB(x, y);
				intensity[x][y] = (float)intensity(rgba);
			}
	}

	//-------------------------------------------------------------------------

	/**
	 * @param rgba
	 * @return Intensity in range 0..1.
	 */
	public static double intensity(final int rgba)
	{
		final int r = (rgba >> 16) & 0xff;
		final int g = (rgba >>  8) & 0xff;
		final int b = (rgba      ) & 0xff;
		//final int a = (rgba >> 24) & 0xff;
		final double value = ((r / 255.0) + (g / 255.0) + (b / 255.0)) / 3.0;
		return value;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Create initial dot placement.
	 */
	public void createInitialDots()
	{
		int x, y, perc;
		
		System.out.println("Creating " + numDots + " initial dots...");
		
		final int wd = blurred.getWidth();
		final int ht = blurred.getHeight();

		dots.clear();
		
		while (dots.size() < numDots)
		{
			do
			{
				x = rng.nextInt(wd);
				y = rng.nextInt(ht);
				perc = (int)(intensity[x][y] * 100);
				//perc = (int)(blend(x, y) * 100);
			} while (rng.nextInt(100) <= perc);
			
			final Dot dot = new Dot(x, y);
			
			if (dots.contains(dot))
			{
				// Don't add duplicate points
//				System.out.println("+");
				continue;
			}
			dots.add(dot);
		}
	
		setDotRadii();
		mapToSphere();
		
		if (app.dotView() != null)
		{
			app.dotView().paintImmediately(0, 0, app.dotView().getWidth(), app.dotView().getHeight()); 
			//app.dotView().repaint(); 
		}

		if (app.globeView() != null)
		{
			app.globeView().paintImmediately(0, 0, app.globeView().getWidth(), app.globeView().getHeight()); 
			//app.globeView().repaint(); 
		}

	}
	
	//-------------------------------------------------------------------------

//	/**
//	 * @param x
//	 * @param y
//	 * @return Blend of intensity and distance, in range 0..1 (not guaranteed).
//	 */
//	float blend(final int x, final int y)
//	{
//		return (intensity[x][y] + 0.5f * (map[x][y] == 0 ? 1 : map[x][y]));  // / 5.0f;
//	}
	
	//-------------------------------------------------------------------------
	
	/** 
	 * Perform the conforming Delaunay triangulation.
	 */
	public void createVoronoi()
	{
		builder = new VoronoiDiagramBuilder();

	  	Coordinate[] coords = new Coordinate[dots.size()];
	  	for (int i = 0; i < dots.size(); i++)
	  	{
	  		final Dot dot = dots.get(i);
	  		coords[i] = new Coordinate(dot.ix, dot.iy);
	  	}
	  	Geometry geom = geomFactory.createMultiPoint(coords);
		builder.setSites(geom);
		
		frame = new Envelope(0, 0, blurred.getWidth(), blurred.getHeight());
		builder.setClipEnvelope(frame);
		
		// Perform the triangulation
		polys = (GeometryCollection)builder.getDiagram(geomFactory);
	}

	//-------------------------------------------------------------------------

	/**
	 * Iterate one relaxation pass using weighted Lloyd's algorithm.
	 */
	public void relax()
	{		
		if (pass == 0 || pause)
			return;
		
		System.out.println("Stippler.relax(): Pass " + (numPasses - pass + 1) + "/" + numPasses + ".");
		
		createVoronoi();

		final int wd = blurred.getWidth();
		final int ht = blurred.getHeight();
		
//		final double targetArea = wd * ht * 0.25;
//		final double areaPerDot = targetArea / (numDots * 0.9);
			
		for (int n = 0; n < polys.getNumGeometries(); n++)
		{
			if (pass == 0 || pause)
				return;  // check whether user interrupted
			
			final Polygon poly = (Polygon)polys.getGeometryN(n);
					
			float xMax = 0;
			float xMin = blurred.getWidth();
			float yMax = 0;
			float yMin = blurred.getHeight();
			float xt, yt;
	
			final Coordinate[] coords = poly.getCoordinates();
			
			for (int i = 0; i < coords.length; i++) 
			{ 
				xt = (float)coords[i].getOrdinate(CoordinateSequence.X);
				yt = (float)coords[i].getOrdinate(CoordinateSequence.Y);
	
				if (xt < xMin)	xMin = xt;
				if (xt > xMax)	xMax = xt;
				if (yt < yMin)	yMin = yt;
				if (yt > yMax)	yMax = yt;
			}
			if (xMin < 0)	xMin = 0;			
			if (xMax < 0)	xMax = 0;
			if (xMin >= wd)	xMin = wd-1;			
			if (xMax >= wd)	xMax = wd-1;
			
			if (yMin < 0)	yMin = 0;			
			if (yMax < 0)	yMax = 0;
			if (yMin >= ht)	yMin = ht-1;			
			if (yMax >= ht)	yMax = ht-1;
			
			float xDiff = xMax - xMin;
			float yDiff = yMax - yMin;
			float maxSize = Math.max(xDiff, yDiff);
			float minSize = Math.min(xDiff, yDiff);
	
			float scaleFactor = 1.0f;
	
			// Maximum voronoi cell extent should be between
			// cellBuffer/2 and cellBuffer in size.
	
			while (maxSize > cellBuffer)
			{
				scaleFactor *= 0.5f;
				maxSize *= 0.5f;
			}
	
			while (maxSize < (cellBuffer/2))
			{
				scaleFactor *= 2;
				maxSize *= 2;
			}
	
			if ((minSize * scaleFactor) > (cellBuffer/2))
			{   // Special correction for objects of near-unity (square-like) aspect ratio, 
				// which have larger area *and* where it is less essential to find the exact centroid:
				scaleFactor *= 0.5f;
			}
	
			float StepSize = (1/scaleFactor);
	
			float xSum = 0;
			float ySum = 0;
			float dSum = 0;       
			float PicDensity = 1.0f;
			
			for (float x=xMin; x<xMax; x += StepSize) 
				for (float y=yMin; y<yMax; y += StepSize) 
				{
					final Coordinate coord = new Coordinate(x, y);
					if (CGAlgorithms.isPointInRing(coord, coords))
					{
						// Thanks to polygon clipping, NO vertices will be beyond the sides of imgblur
						float weight = intensity[(int)(x)][(int)(y)];
						//weight *= weight;
						PicDensity = 255.001f - 255.0f * weight;  //blend((int)x, (int)y);
						xSum += PicDensity * x;
						ySum += PicDensity * y; 
						dSum += PicDensity;
					}
				}
	
			if (dSum > 0)
			{
				xSum /= dSum;
				ySum /= dSum;
			}
			float xTemp  = (xSum);
			float yTemp  = (ySum);
	
			if (xTemp < 0 || xTemp >= wd || yTemp < 0 || yTemp >= ht) 
			{
				// If new centroid is computed to be outside the visible region, use the geometric centroid instead.
				// This will help to prevent runaway points due to numerical artifacts. 
				//System.out.println("x");
				
				final Point cent = poly.getCentroid();
				
				xTemp = (float)cent.getX();
				yTemp = (float)cent.getY();
	
				// Enforce sides, if absolutely necessary:  (Failure to do so *will* cause a crash, eventually.)
	
				if (xTemp < 0)		xTemp = 1; 
				if (xTemp >= wd)	xTemp = wd - 1; 
				if (yTemp < 0)		yTemp = 0; 
				if (yTemp >= ht)	yTemp = ht - 1;
			} 
			
			// Determine dot position and colour
			dots.get(n).ix = xTemp;
			dots.get(n).iy = yTemp;
			dots.get(n).area = poly.getArea();
			
			final int rgba = blurred.getRGB((int)(xTemp), (int)(yTemp));
			dots.get(n).rgba = rgba;
			
			// **
			// ** Do radius calculation during export, so can adjust final output.
			// **
//			// Determine dot radius
// 			double r = Math.sqrt(areaPerDot / Math.PI) / 2.0;
// 			r *= dotSize;
// 			r += (r * dotRange) * (1 - intensity(rgba));
// 			dots.get(n).ir = r;
		}
	
		setDotRadii();
		mapToSphere();

		if (app.mainView() != null)
		{
			app.mainView().paintImmediately(0, 0, app.mainView().getWidth(), app.mainView().getHeight()); 
			//app.mainView().repaint(); 
		}
	
		if (app.dotView() != null)
		{
			app.dotView().paintImmediately(0, 0, app.dotView().getWidth(), app.dotView().getHeight()); 
			//app.dotView().repaint(); 
		}

		if (app.globeView() != null)
		{
			app.globeView().paintImmediately(0, 0, app.globeView().getWidth(), app.globeView().getHeight()); 
			//app.globeView().repaint(); 
		}

//		if (app.mapView() != null)
//			app.mapView().repaint();

		pass--;
		
		if (pass > 0 && !pause)
		{
			// Trigger the next iteration
			//app.worker().iterate();
			app.iterate();
		}
		else
		{
			// Save the result
			exportSVG(true);
			exportSVG(false);
			exportRaw();
			System.out.println("Done.");
		}
	}
	
//	/**
//	 * @param numThreads
//	 */
//	public void relaxMultithreaded(final int numThreads)
//	{
//		if (pass == 0 || pause)
//			return;
//		
//		System.out.println("Stippler.relaxMt(): Pass " + (numPasses - pass + 1) + "/" + numPasses + ".");
//		
//		createVoronoi();
//
//		final int wd = blurred.getWidth();
//		final int ht = blurred.getHeight();
//		
//		final double targetArea = wd * ht * 0.25;
//		final double areaPerDot = targetArea / (numDots * 0.9);
//			
//		int dotsPerThread = numDots / numThreads + 1;
//		
//		for (int n = 0; n < numDots; n+= dotsPerThread)
//		{
//			final int from = n;
//			final int to = Math.min(n+dotsPerThread, numDots-1);
//		
//			Thread thread = new Thread() {
//				public void run() 
//				{
//					relax(from, to, areaPerDot);
//				}
//			};
//			thread.start();
//		}
//		
//		if (app.mainView() != null)
//		{
//			app.mainView().paintImmediately(0, 0, app.mainView().getWidth(), app.mainView().getHeight()); 
//			app.mainView().repaint(); 
//		}
//	
////		if (app.mapView() != null)
////			app.mapView().repaint();
//
//		pass--;
//		
//		if (pass > 0 && !pause)
//		{
//			// Trigger the next iteration
//			//app.worker().iterate();
//			app.iterate();
//		}
//		else
//		{
//			// Save the result
//			exportSVG(true);
//			exportSVG( false);
//			exportRaw(true);
//			exportRaw(false);
//			System.out.println("Done.");
//		}
//
//	}
//	
//	/**
//	 * Iterate one relaxation pass using weighted Lloyd's algorithm.
//	 * @param from 
//	 * @param to 
//	 * @param areaPerDot 
//	 */
//	public void relax(final int from, final int to, final double areaPerDot)
//	{		
////		if (pass == 0 || pause)
////			return;
////		
////		System.out.println("Stippler.relax(): Pass " + (numPasses - pass + 1) + "/" + numPasses + ".");
////		
////		createVoronoi();
//
//		final int wd = blurred.getWidth();
//		final int ht = blurred.getHeight();
//		
//		for (int n = from; n < to; n++)
//		{
//			if (pass == 0 || pause)
//				return;  // check whether user interrupted
//			
//			final Polygon poly = (Polygon)polys.getGeometryN(n);
//					
//			float xMax = 0;
//			float xMin = blurred.getWidth();
//			float yMax = 0;
//			float yMin = blurred.getHeight();
//			float xt, yt;
//	
//			final Coordinate[] coords = poly.getCoordinates();
//			
//			for (int i = 0; i < coords.length; i++) 
//			{ 
//				xt = (float)coords[i].getOrdinate(CoordinateSequence.X);
//				yt = (float)coords[i].getOrdinate(CoordinateSequence.Y);
//	
//				if (xt < xMin)	xMin = xt;
//				if (xt > xMax)	xMax = xt;
//				if (yt < yMin)	yMin = yt;
//				if (yt > yMax)	yMax = yt;
//			}
//			if (xMin < 0)	xMin = 0;			
//			if (xMax < 0)	xMax = 0;
//			if (xMin >= wd)	xMin = wd-1;			
//			if (xMax >= wd)	xMax = wd-1;
//			
//			if (yMin < 0)	yMin = 0;			
//			if (yMax < 0)	yMax = 0;
//			if (yMin >= ht)	yMin = ht-1;			
//			if (yMax >= ht)	yMax = ht-1;
//			
//			float xDiff = xMax - xMin;
//			float yDiff = yMax - yMin;
//			float maxSize = Math.max(xDiff, yDiff);
//			float minSize = Math.min(xDiff, yDiff);
//	
//			float scaleFactor = 1.0f;
//	
//			// Maximum voronoi cell extent should be between
//			// cellBuffer/2 and cellBuffer in size.
//	
//			while (maxSize > cellBuffer)
//			{
//				scaleFactor *= 0.5f;
//				maxSize *= 0.5f;
//			}
//	
//			while (maxSize < (cellBuffer/2))
//			{
//				scaleFactor *= 2;
//				maxSize *= 2;
//			}
//	
//			if ((minSize * scaleFactor) > (cellBuffer/2))
//			{   // Special correction for objects of near-unity (square-like) aspect ratio, 
//				// which have larger area *and* where it is less essential to find the exact centroid:
//				scaleFactor *= 0.5f;
//			}
//	
//			float StepSize = (1/scaleFactor);
//	
//			float xSum = 0;
//			float ySum = 0;
//			float dSum = 0;       
//			float PicDensity = 1.0f;
//			
//			for (float x=xMin; x<xMax; x += StepSize) 
//				for (float y=yMin; y<yMax; y += StepSize) 
//				{
//					final Coordinate coord = new Coordinate(x, y);
//					if (CGAlgorithms.isPointInRing(coord, coords))
//					{
//						// Thanks to polygon clipping, NO vertices will be beyond the sides of imgblur. 
//						PicDensity = 255.001f - 255.0f * intensity[(int)(x)][(int)(y)];  //blend((int)x, (int)y);
//						xSum += PicDensity * x;
//						ySum += PicDensity * y; 
//						dSum += PicDensity;
//					}
//				}
//	
//			if (dSum > 0)
//			{
//				xSum /= dSum;
//				ySum /= dSum;
//			}
//			float xTemp  = (xSum);
//			float yTemp  = (ySum);
//	
//			if (xTemp < 0 || xTemp >= wd || yTemp < 0 || yTemp >= ht) 
//			{
//				// If new centroid is computed to be outside the visible region, use the geometric centroid instead.
//				// This will help to prevent runaway points due to numerical artifacts. 
//				//System.out.println("x");
//				
//				final Point cent = poly.getCentroid();
//				
//				xTemp = (float)cent.getX();
//				yTemp = (float)cent.getY();
//	
//				// Enforce sides, if absolutely necessary:  (Failure to do so *will* cause a crash, eventually.)
//	
//				if (xTemp < 0)		xTemp = 1; 
//				if (xTemp >= wd)	xTemp = wd - 1; 
//				if (yTemp < 0)		yTemp = 0; 
//				if (yTemp >= ht)	yTemp = ht - 1;
//			} 
//			
//			// Determine dot position and colour
//			dots.get(n).ix = xTemp;
//			dots.get(n).iy = yTemp;
//			dots.get(n).area = poly.getArea();
//			
//			final int rgba = blurred.getRGB((int)(xTemp), (int)(yTemp));
//			dots.get(n).rgba = rgba;
//			
//			// Determine dot radius
// 			double r = Math.sqrt(areaPerDot / Math.PI) / 2.0;
// 			r *= dotSize;
// 			r += (r * dotRange) * (1 - intensity(rgba));
// 			dots.get(n).ir = r;
//		}
//	}

	//-------------------------------------------------------------------------

	/**
	 * Set dot radii.
	 */
	public void setDotRadii()
	{		
		final int wd = blurred.getWidth();
		final int ht = blurred.getHeight();
		
		final double targetArea = wd * ht * 0.25;
		final double areaPerDot = targetArea / (numDots * 0.9);

		for (Dot dot : dots)
		{
			double r = Math.sqrt(areaPerDot / Math.PI) / 2.0;
			r *= dotSize;
			r += (r * dotRange) * (1 - intensity(dot.rgba));
			dot.ir = r;
		}
	}
	
	//--------------------------------------------

	/**
	 * @return Centroid stored as XYZ, with Z being maximum radius.
	 */
	public XYZ dotCentroid()
	{
		final XYZ centroid = new XYZ();
		
	 	// Determine extent of dots
	  	int x0 =  1000000;
	  	int y0 =  1000000;
	  	int x1 = -1000000;
	  	int y1 = -1000000;
	   	
		for (Dot dot : dots)  //app.stippler().dots())
		{
//			if (intensity(dot.rgba) * 100 >= whiteThreshold)
//				continue;
				
	   		if ((int)dot.ix < x0)
				x0 = (int)dot.ix;
			
	   		if ((int)dot.iy < y0)
				y0 = (int)dot.iy;
			
	   		if ((int)dot.ix > x1)
				x1 = (int)dot.ix;
			
	   		if ((int)dot.iy > y1)
				y1 = (int)dot.iy;
		}
		
//		final int sx = x1 - x0;
//		final int sy = y1 - y0;
		
		centroid.x = (x0 + x1) / 2.0;
		centroid.y = (y0 + y1) / 2.0;
		
		// Determine max distance from centre
		//centroid.z = Math.max(sx, sy) / 2.0;
		centroid.z = 0;
		for (Dot dot : dots)  //app.stippler().dots())
		{
//			if (intensity(dot.rgba) * 100 >= whiteThreshold)
//			continue;
			
			final double dx = centroid.x - dot.ix;
			final double dy = centroid.y - dot.iy;
			
			final double dist = Math.sqrt(dx*dx + dy*dy);
			if (dist > centroid.z)
				centroid.z = dist;
		}
		System.out.println("centroid: x=" + centroid.x + ", y=" + centroid.y + ", r=" + centroid.z + ".");
		
		return centroid;
	}
	
	/**
	 * Map dots to spherical coordinates.
	 */
	public void mapToSphere()
	{
		final XYZ centroid = dotCentroid();

		for (Dot dot : dots)  //app.stippler().dots())
		{
//			if (intensity(dot.rgba) * 100 >= whiteThreshold)
//				continue;
		
//			final double r = 1;  //1 - 0.5 * intensity(dot.rgba);  //(dot.red() + dot.green() + dot.blue()) / (3.0 * 255);
			final double r = 10.0 * (1 - 0.9 * intensity(dot.rgba));  //(dot.red() + dot.green() + dot.blue()) / (3.0 * 255);
			
//			if (r < 0 || r > 1)
//				System.out.println("r=" + r);
			
			dot.spherical.radius = r;
			
			final double x = (centroid.x - dot.ix) / centroid.z;
			final double y = (centroid.y - dot.iy) / centroid.z;
			
			final double z = Math.sqrt(r*r - x*x - y*y);
			
			dot.spherical.inclination = Math.acos(z / r);
			dot.spherical.azimuth = Math.atan2(y, x);  			
//			if (x < 0)
//			{
//				if (y < 0)
//					dot.spherical.azimuth -= Math.PI;
//				else
//					dot.spherical.azimuth += Math.PI;
//			}
//			else if (x == 0)
//			{
//				if (y < 0)
//					dot.spherical.azimuth = -Math.PI / 2.0;
//				else
//					dot.spherical.azimuth =  Math.PI / 2.0;				
//			}

//			final double sx = dot.spherical.radius 
//				 	 * 
//				 	 Math.sin(dot.spherical.inclination) 
//				 	 * 
//				 	 Math.cos(dot.spherical.azimuth);
//      		final double sy = dot.spherical.radius 
//				 	 * 
//				 	 Math.sin(dot.spherical.inclination) 
//				 	 * 
//				 	 Math.sin(dot.spherical.azimuth);
//      		final double sz = dot.spherical.radius 
//				 	 * 
//				 	 Math.cos(dot.spherical.inclination);
//
//			final double error = Math.abs(x - sx) + Math.abs(y - sy) + Math.abs(z - sz);
//			if (error > 0.0000001)
//				System.out.println("error=" + error);			
		}
	}
	
	//--------------------------------------------
	
//	/**
//	 * Generate distance map.
//	 */
//	public void distanceMap()
//	{
//		final float orth = 1.0f;
//		final float diag = (float)Math.sqrt(2);
//
//		final int sx = blurred.getWidth();
//		final int sy = blurred.getHeight();
//	  	
//		System.out.print("Calculating distance map...");
//
//		map = new float[sx][sy];
//		for (int x = 0; x < sx; x++)
//			for (int y = 0; y < sy; y++)
//			{
//				if (x == 0 || y == 0 || x == sx-1 || y == sy-1 || intensity[x][y] >= 0.98)
//					map[x][y] = 0;
//				else
//					map[x][y] = 1000000.0f;
//			}
//	  		
//		// Forward pass
//		for (int y = 1; y < sy-1; y++)
//			for (int x = 1; x < sx-1; x++)
//			{
//				if (map[x][y] == 0)
//	 	   			continue;
//	 	   			
//				//  a b c
//				//  d + .
//				//  . . .
//				final float a = map[x-1][y-1] + diag;
//				final float b = map[x  ][y-1] + orth;
//				final float c = map[x+1][y-1] + diag;
//				final float d = map[x-1][y  ] + orth;
//	 	   			
//				map[x][y] = Math.min(Math.min(Math.min(a, b), c), d);
//			}
//	  	
//		// Backward pass
//		for (int y = sy-2; y > 0; y--)
//			for (int x = sx-2; x > 0; x--)
//			{
//				//  . . .
//				//  . + e
//				//  f g h
//				final float e = map[x+1][y  ] + orth;
//				final float f = map[x-1][y+1] + diag;
//				final float g = map[x  ][y+1] + orth;
//				final float h = map[x+1][y+1] + diag;
//	
//				map[x][y] = Math.min(map[x][y], Math.min(Math.min(Math.min(e, f), g), h));
//			}
//		  
//		// Square root to make round profile, normalise to 0..1.
//		final double maxHt = Math.sqrt(Math.min(sx, sy) * 0.5);
//		for (int x = 0; x < sx; x++)
//			for (int y = 0; y < sy; y++)
//				map[x][y] = (float)(Math.sqrt(map[x][y]) / maxHt);
//		  	
//		System.out.println(" done.");
//	}
  
	//-------------------------------------------------------------------------
	
	/**
	 * @param bw
	 */
	@SuppressWarnings("boxing")
	void exportSVG(final boolean bw)
	{
		String str;
		
		final List<String> output = new ArrayList<String>();

		String path = new String(imageName);
		
		// Remove image extension
		while (path.length() > 1 && path.charAt(path.length()-1) != '.')
			path = path.substring(0, path.length()-1);
		path = path.substring(0, path.length()-1);
		
		// Add settings and .svg extension
		path += "-" + (numDots < 1000 ? numDots : (numDots / 1000 + "k"));
		path += "-x" + overSample;
		path += (bw ? "-bw" : "-rgb");
		path += ".svg";
		
		// Move from src/res to out
		path = "out/" + path;
		
		System.out.println("Saving SVG " + (bw ? "B&W " : "RGBA") + " to " + path + "...");

 		try
    	{
 	   	  	final File file = new File("src/res/svg_header.txt");
    		if (!file.exists())
    		{
    			System.out.println("Couldn't find header file.");
    			app.mainView().setError("Couldn't find header file.");
    			return;
    		}
    		
    		final FileReader fileReader = new FileReader(file.getPath());
    	    final BufferedReader reader = new BufferedReader(fileReader);
    	    String line;
    	    while ((line = reader.readLine()) != null) 
    	    {
    	    	str = line;
       	    	if (str.contains("width="))
    	    		str = String.format("width=\"%d\"", blurred.getWidth() / overSample);
       	    	if (str.contains("height="))
    	    		str = String.format("height=\"%d\"", blurred.getHeight() / overSample);
    	    	output.add(new String(str));
    	    }
    	    reader.close();   	
    	}
    	catch(IOException e)
    	{
			app.mainView().setError("Couldn't open file " + path + " to export SVG.");
    		e.printStackTrace();
    	}

 		for (Dot dot : dots)
 		{
 			final double x = dot.ix / overSample;
 			final double y = dot.iy / overSample;
 			
 			final int rgba = dot.rgba;
 			
// 			if (intensity(rgba) * 100 >= whiteThreshold)
// 				continue;  // ignore pure white

 			// Determine dot radius
 			double r = dot.ir / overSample;
 			if (bw)
 				r *= 0.5;
 			 			
 			final int rr = (rgba >> 16) & 0xff;
 			final int gg = (rgba >>  8) & 0xff;
 			final int bb = (rgba      ) & 0xff;
 				 			
 			str = String.format("<circle cx=\"%.3f\" cy=\"%.3f\" r=\"%.3f\"", x, y, r);
  			if (bw)
 				str += " style=\"fill:black;stroke:none;\"/>";
 			else
 				str += String.format(" style=\"fill:rgb(%d,%d,%d);stroke:none;\"/>", rr, gg, bb);
 			output.add(str);
  		}
 		output.add(new String("</g></g></svg>"));

		try
		{
			File file = new File(path);
			if (!file.exists())
				file.createNewFile();
	
			FileWriter fileWriter = new FileWriter(path);
		    BufferedWriter writer = new BufferedWriter(fileWriter);
	
		    for (String s : output)
		    	writer.write(s + "\n");
		    
		    writer.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	} 
	
	/**
	 * Export raw data.
	 */
	@SuppressWarnings("boxing")
	void exportRaw()
	{
		String str;
		
		final List<String> output = new ArrayList<String>();

		String path = new String(imageName);
		
		// Remove image extension
		while (path.length() > 1 && path.charAt(path.length()-1) != '.')
			path = path.substring(0, path.length()-1);
		path = path.substring(0, path.length()-1);
		
		// Add settings and .svg extension
		path += "-" + (numDots < 1000 ? numDots : (numDots / 1000 + "k"));
		path += "-x" + overSample;
		path += ".txt";		
		
		// Move from src/res to out
		path = "out/" + path;

		System.out.println("Saving raw data to " + path + "...");
 		
		str = String.format("%d %d", source.getWidth(), source.getHeight());
		output.add(str);
		
 		for (Dot dot : dots)
 		{
 			final double x = dot.ix / overSample;
 			final double y = dot.iy / overSample;
 			
 			final int rgba = dot.rgba;
 			
// 			if (intensity(rgba) * 100 >= whiteThreshold)
// 				continue;  // ignore pure white

 			double r = dot.ir / overSample;
 				 			
 			str = String.format("%.3f %.3f %.3f 0x%x", x, y, r, rgba);
  			output.add(str);
  		}
 
		try
		{
			File file = new File(path);
			if (!file.exists())
				file.createNewFile();
	
			FileWriter fileWriter = new FileWriter(path);
		    BufferedWriter writer = new BufferedWriter(fileWriter);
	
		    for (String s : output)
		    	writer.write(s + "\n");
		    
		    writer.close();
		}
		catch(IOException e)
		{
			app.mainView().setError("Couldn't open file " + path + " to export raw data.");
			e.printStackTrace();
		}
	} 
	
	//-------------------------------------------------------------------------

	/**
	 * Start stippling.
	 */
	public void start()
	{
		System.out.println("At Stippler.start()...");
		
		pass = numPasses;
		relax();
		//relaxMultithreaded(4);
	}
	
	/**
	 * Stop stippling.
	 */
	public void pause()
	{
		pause = !pause;
		if (!pause && pass > 0)
			relax();
	}
	
	/**
	 * Stop stippling.
	 */
	public void stop()
	{
		pass = 0;
		//pause = false;
	}
	
	//-------------------------------------------------------------------------

}
