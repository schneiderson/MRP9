package graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

//import arcball.ArcBall;
//import arcball.Quat4f;
//import arcball.Vector3f;
//import arcball2.ArcBall;
//import arcball2.Quaternion;
//import arcball2.Vector3f;
import arcball3.Arcball;
import arcball3.Arcball.Quat;
import main.Dot;
import main.Stipple;
//import main.Stippler;
//import main.XYZ;
//import processing.core.PVector;

//---------------------------------------------------------------

/**
 * Game view for board display and GUI.
 * @author cambolbro
 */
public class GlobeView extends JPanel implements MouseListener, MouseMotionListener, KeyListener
{
	/** Default class UID. */
	private static final long serialVersionUID = 3L;

	/** */
	protected Stipple app = null;
 	
 	/** */
 	protected BufferedImage buffer = null;
 
	/** */
	final int overSample = 4;

	/** */
	protected double mx = 0;
	
	/** */
	protected double my = 0;
	
	/** */
	protected double mz = 0;
	
	/** */
	protected Point lastPosn = null;
	
	/** */
	protected boolean shiftDown = false;
	
	/** */
	//protected ArcBall arcball = new ArcBall(300, 300);
	protected Arcball arcball = new Arcball(720, 720, 300);
	
	/** Quaternion showing current orientation. */
	//protected Quat4f quat = new Quat4f();
	//protected Quaternion quat = new Quaternion(0, 0, 0, 1);
	protected Quat quat = new Quat();  //(0, 0, 0, 1);

    //---------------------------------------------------------------------
    
    /**
     * Constructor.
     * @param app
     */
    public GlobeView(final Stipple app)
    {
       	addMouseListener(this);
     	addMouseMotionListener(this);
     	addKeyListener(this);
    	setFocusable(true);  // for key input

    	this.app = app;  
    }
    
    //---------------------------------------------------------------------

    @SuppressWarnings("boxing")
	public void paint(Graphics g)
    {
    	Graphics2D g2d = (Graphics2D)g;
     	g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    	g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    	g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    	final int wd = getWidth();
    	final int ht = getHeight();
    	
       	g2d.setPaint(Color.white);
    	g2d.fillRect(0, 0, wd, ht);

    	if (buffer == null)
    		buffer = new BufferedImage(wd*overSample, ht*overSample, BufferedImage.TYPE_INT_ARGB);
     	
    	if (app.stippler() != null && app.stippler().dots() != null)
    		showDots(g2d);
    	
    	// Show status
    	String strX = "X: " + mx + "\u00b0";
    	String strY = "Y: " + my + "\u00b0";
    	String strZ = "Z: " + mz + "\u00b0";
   		g2d.setPaint(new Color(0, 0, 0));
		g2d.drawString(strX, 8, getHeight()-42);
		g2d.drawString(strY, 8, getHeight()-26);
		g2d.drawString(strZ, 8, getHeight()-10);

		final double x01 = Math.sin(Math.toRadians(mx));
		final double y01 = Math.sin(Math.toRadians(my));
		final double z01 = Math.sin(Math.toRadians(mz));
		String strX01 = String.format("%.4f", x01);
		String strY01 = String.format("%.4f", y01);
		String strZ01 = String.format("%.4f", z01);
		final int extentX = (int)g2d.getFontMetrics().getStringBounds(strX01, g2d).getWidth();
		final int extentY = (int)g2d.getFontMetrics().getStringBounds(strY01, g2d).getWidth();
		final int extentZ = (int)g2d.getFontMetrics().getStringBounds(strZ01, g2d).getWidth();
    	g2d.setPaint(new Color(0, 0, 0));
		g2d.drawString(strX01, 110-extentX, getHeight()-41);
		g2d.drawString(strY01, 110-extentY, getHeight()-25);
		g2d.drawString(strZ01, 110-extentZ, getHeight()-9);

		final double qx = arcball.qNow.x;
		final double qy = arcball.qNow.y;
		final double qz = arcball.qNow.z;
		final double qw = arcball.qNow.w;
//		final double qx = arcball.total.x;
//		final double qy = arcball.total.y;
//		final double qz = arcball.total.z;
//		final double qw = arcball.total.w;
    	strX = "Q.x: " + String.format("%.8f", qx);
    	strY = "Q.y: " + String.format("%.8f", qy);
    	strZ = "Q.z: " + String.format("%.8f", qz);
    	String strW = "Q.w: " + String.format("%.8f", qw);
   		g2d.setPaint(new Color(0, 0, 0));
   		g2d.drawString(strX, 132, getHeight()-58);
   		g2d.drawString(strY, 132, getHeight()-42);
		g2d.drawString(strZ, 132, getHeight()-26);
		g2d.drawString(strW, 132, getHeight()-10);
		
//		// Test dot
//		final Dot dot = new Dot(400, 400, 5, 0xff3377ff);
//    
//		final XYZ xyz = new XYZ(dot.ix, dot.iy, dot.ir);  //sample(dot);
//		
//		g2d.setPaint(new Color(0, 127, 255));
//		g2d.fillOval((int)dot.ix-2, (int)dot.iy-2, 5, 5);
//		
//		g2d.setPaint(new Color(255, 0, 0));
//		g2d.drawOval((int)xyz.x-2, (int)xyz.y-2, 5, 5);

		// Text axes
//		final Point ptX = new Point(0, 100);
//		final Vector3f vecX = new Vector3f();
//		arcball.mapToSphere(ptX, vecX);
//		
//		g2d.setPaint(new Color(0, 127, 255));
//		g2d.fillOval(360+(int)vecX.x-2, 360+(int)vecX.y-2, 5, 5);
		
		//PVector rotate(PVector v, PVector r, float a) {
		
//		PVector v = new PVector(0, 0, 100);
//		PVector r = new PVector(mx*100, my*100, mz*100);
//		float a = 100;	
//		
//		Quaternion2 Q1 = new Quaternion2(0, v.x, v.y, v.z);
//		  Quaternion2 Q2 = new Quaternion2((float)Math.cos(a / 2), r.x * (float)Math.sin(a / 2), r.y * (float)Math.sin(a / 2), r.z * (float)Math.sin(a / 2));
//		  Quaternion2 Q3 = Q2.mult(Q1).mult(Q2.conjugate());
//		  PVector vv = new PVector(Q3.X, Q3.Y, Q3.Z);
//
//		g2d.setPaint(new Color(0, 127, 255));
//		g2d.fillOval(360+(int)vv.x-2, 360+(int)vv.y-2, 5, 5);

		
		double[] from = new double[4];
		double[] to = new double[4];
		
		QuatToScreen(arcball.qNow, from, to);
//		QuaternionToScreen(arcball.total, from, to);

		final int r = 200;
		
		g2d.setPaint(new Color(127, 127, 127, 127));
		g2d.fillOval(360-r, 360-r, 2*r+1, 2*r+1);

		g2d.setPaint(new Color(0, 0, 255));
		g2d.fillOval(360+(int)(from[0]*r)-2, 360-(int)(from[1]*r)-2, 5, 5);
		
		g2d.setPaint(new Color(255, 0, 0));
		g2d.fillOval(360+(int)(to[0]*r)-2, 360-(int)(to[1]*r)-2, 5, 5);


    }
 
    //-------------------------------------------------------------------------

    /**
     * Convert a unit quaternion to two points on a unit sphere. •/ 
     * @param q
    * @param from
    * @param to
     */
    void QuatToScreen(Quat q, double[] from, double[] to)
    {
       double s = Math.sqrt(q.x*q.x + q.y*q.y);
       if (s == 0.0)
       {
           from[0] = 0;
           from[1] = 1;
           from[2] = 0;
       }
       else
       {
           from[0] = -q.y / s;
           from[1] =  q.x / s;
           from[2] =  0;
       }
 
       to[0] = q.w * from[0] - q.z * from[1];
       to[1] = q.w * from[1] + q.z * from[0];
       to[2] = q.x * from[1] - q.y * from[0];
       
       if (q.w < 0)
       {
    	   from[0] = -from[0];
    	   from[1] = -from[1];
    	   from[2] = 0;
       }
    }

//    /**
//     * Convert a unit quaternion to two points on a unit sphere. •/ 
//     * @param q
//     * @param from
//     * @param to
//     */
//    void QuaternionToScreen(Quaternion q, double[] from, double[] to)
//    {
//       double s = Math.sqrt(q.x*q.x + q.y*q.y);
//       if (s == 0.0)
//       {
//           from[0] = 0;
//           from[1] = 1;
//           from[2] = 0;
//       }
//       else
//       {
//           from[0] = -q.y / s;
//           from[1] =  q.x / s;
//           from[2] =  0;
//       }
// 
//       to[0] = q.w * from[0] - q.z * from[1];
//       to[1] = q.w * from[1] + q.z * from[0];
//       to[2] = q.x * from[1] - q.y * from[0];
//       
//       if (q.w < 0)
//       {
//    	   from[0] = -from[0];
//    	   from[1] = -from[1];
//    	   from[2] = 0;
//       }
//    }

    //-------------------------------------------------------------------------

    /**
     * Show the distance map.
     * @param g2d
     */
    void showDots(Graphics2D g2d)
    {
      	final int wdSrc = buffer.getWidth();
       	final int htSrc = buffer.getHeight();

       	Graphics2D g2dBuffer = (Graphics2D)buffer.getGraphics();
    	g2dBuffer.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    	g2dBuffer.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    	g2dBuffer.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

       	// Determine extent of dots
    	//determineCentroid();
//    	final XYZ centroid = app.stippler().dotCentroid();
//    
//    	final double scale = Math.max(wdSrc, htSrc) / centroid.z * 0.5 * .85;

    	final int cx = wdSrc / 2;
    	final int cy = htSrc / 2;

    	final double scale = Math.min(wdSrc, htSrc) / 2.0 * 0.9;
    	
    	//System.out.println("cx=" + cx + ", cy=" + cy + ", scale=" + scale + ".");
    	
    	// Show dots
  		g2dBuffer.setPaint(Color.white);
  		g2dBuffer.fillRect(0, 0, wdSrc, htSrc);

//  		int numDots = app.stippler().dots().size();
//  		int dotsDrawn = 0;
  		
       	for (Dot dot : app.stippler().dots())
    	{
       		final int rgba = dot.rgba;
//       		if (Stippler.intensity(rgba) * 100 >= Stippler.whiteThreshold)
//    			continue;  // don't show white dots

       		int rr = (rgba >> 16) & 0xff;
       		int gg = (rgba >>  8) & 0xff;
       		int bb = (rgba      ) & 0xff;
//       	final int aa = (rgba >> 24) & 0xff;

//      	final int x = x0 + (int)((dot.ix - centroid.x) * scale + 0.5);
//        	final int y = y0 + (int)((dot.iy - centroid.y) * scale + 0.5);
//       	
////    		if (x < 1 || x >= wdSrc-1 || y < 1 || y >= htSrc-1)
////       			continue;  
//
//      	final int r = (int)(dot.ir*scale + 0.5);  // + 1;

    		final double x01 = Math.sin(Math.toRadians(mx));
    		final double y01 = Math.sin(Math.toRadians(my));
    		final double z01 = Math.sin(Math.toRadians(mz));

    		final double r     = dot.spherical.radius;  // + z01 * Math.PI * 2;
       		final double theta = dot.spherical.inclination + x01 * Math.PI * 2;
       		final double phi   = dot.spherical.azimuth + y01 * Math.PI * 2;
       		
       		final double sx = r *  Math.sin(theta) * Math.cos(phi);
       		final double sy = r *  Math.sin(theta) * Math.sin(phi);
       		final double sz = r *  Math.cos(theta);
       		//System.out.println("sz=" + sz + ".");
        	
     		final int ix = cx - (int)(sx * scale + 0.5); 
     		final int iy = cy - (int)(sy * scale + 0.5); 
       		
     		//final int ir = (int)(sz * dot.ir * 8 + 0.5); 
     		final int ir = (int)(dot.ir * overSample * 0.75 + 0.5);  // + 0.5); 
     		//if (ir < 1)
     		//	System.out.println("ir=" + ir + ".");
        	
      		g2dBuffer.setPaint(new Color(rr, gg, bb));
      		g2dBuffer.fillOval(ix-ir, iy-ir, 2*ir+1, 2*ir+1);
      		
//      		dotsDrawn++;
    	}
//       	System.out.println(dotsDrawn + " dots drawn out of " + numDots + ".");
       	
       	final int wdDst = getWidth();
       	final int htDst = getHeight();

       	g2d.drawImage(buffer, 0, 0, wdDst, htDst, 0, 0, wdSrc, htSrc, null);
    }

    //-------------------------------------------------------------------------

	@Override
	public void mouseClicked(MouseEvent e) 
	{
		// TODO Auto-generated method stub	
	}

	@Override
	public void mousePressed(MouseEvent e) 
	{
		//arcball.click(e.getLocationOnScreen());
		//arcball.dragStart(e.getLocationOnScreen());
		arcball.mousePressed(e.getLocationOnScreen());
		lastPosn = new Point(e.getLocationOnScreen().x, e.getLocationOnScreen().y);
	}

	@Override
	public void mouseReleased(MouseEvent e) 
	{
		arcball.mouseReleased(e.getLocationOnScreen());
		lastPosn = null;
	}

	@Override
	public void mouseEntered(MouseEvent e) 
	{
		// TODO Auto-generated method stub	
	}

	@Override
	public void mouseExited(MouseEvent e) 
	{
		// TODO Auto-generated method stub		
	}

	@Override
	public void mouseDragged(MouseEvent e) 
	{
		//arcball.drag(e.getLocationOnScreen(), quat);
		//quat = arcball.drag(e.getLocationOnScreen());
		arcball.mouseDragged(e.getLocationOnScreen());
		
		final int x = e.getLocationOnScreen().x;
		final int y = e.getLocationOnScreen().y;

		//System.out.println("Mouse dragged to (" + x + "," + y + ").");
		
		final int dx = x - lastPosn.x;
		final int dy = y - lastPosn.y;
		
		final double resn = 100;
		
		if (shiftDown)
		{
			// Zooming: change Z value
			final int dist = (int)(Math.sqrt(dx * dx + dy * dy) + 0.5);
			final int sign = (dx - dy > 0) ? 1 : -1;
			
			mz += sign * dist / resn;
		
			while (mz < 0)
				mz += 360;
			while (mz >= 360)
				mz -= 360;
		}
		else
		{
			mx += dx / resn;
			my += dy / resn;
			
			while (mx < 0)
				mx += 360;
			while (mx >= 360)
				mx -= 360;
		
			while (my < 0)
				my += 360;
			while (my >= 360)
				my -= 360;
		}
		
		lastPosn.setLocation(x, y);
		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) 
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void keyTyped(KeyEvent e) 
	{
		// TODO Auto-generated method stub	
	}

	@Override
	public void keyPressed(KeyEvent e) 
	{
		//System.out.println("Key pressed, id=" + e.getID() + ", char=" + e.getKeyChar() + ", code=" + e.getKeyCode());

		if (e.getKeyCode() == 16)
			shiftDown = true;
	}

	@Override
	public void keyReleased(KeyEvent e) 
	{
		if (e.getKeyCode() == 16)
			shiftDown = false;
	}
        
	//-------------------------------------------------------------------------

}
