package stippling.graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import stippling.main.Dot;
import stippling.main.Stipple;
import stippling.main.XYZ;

//---------------------------------------------------------------

/**
 * Game view for board display and GUI.
 * @author cambolbro
 */
public class DotView extends JPanel implements MouseListener, MouseMotionListener, KeyListener
{
	/** Default class UID. */
	private static final long serialVersionUID = 2L;

	/** */
	protected Stipple app = null;
 	
 	/** */
 	protected BufferedImage buffer = null;
 
	/** */
	final int overSample = 4;
	
//	/** */
//	final ArcBall arcball = new ArcBall(100, 100);
//	
//	/** Quaternion showing current orientation. */
//	final Quat4f quat = new Quat4f();
	
    //---------------------------------------------------------------------
    
    /**
     * Constructor.
     * @param app
     */
    public DotView(final Stipple app)
    {
       	addMouseListener(this);
     	addMouseMotionListener(this);
     	addKeyListener(this);
    	setFocusable(true);  // for key input

    	this.app = app;  
    }
    
    //---------------------------------------------------------------------

//    @SuppressWarnings("boxing")
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
    }
        
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
    	final XYZ centroid = app.stippler().dotCentroid();
    
    	final double scale = Math.max(wdSrc, htSrc) / centroid.z * 0.5 * .85;

    	final int x0 = wdSrc / 2;
    	final int y0 = htSrc / 2;
    	
    	//System.out.println("Scale is " + scale + ".");
    	
    	// Show dots
  		g2dBuffer.setPaint(Color.white);
  		g2dBuffer.fillRect(0, 0, wdSrc, htSrc);

       	for (Dot dot : app.stippler().dots())
    	{
       		final int rgba = dot.rgba;
//       		if (Stippler.intensity(rgba) * 100 >= Stippler.whiteThreshold)
//    			continue;  // don't show white dots

       		final int x = x0 + (int)((dot.ix - centroid.x) * scale + 0.5);
        	final int y = y0 + (int)((dot.iy - centroid.y) * scale + 0.5);
       		
//       		if (x < 1 || x >= wdSrc-1 || y < 1 || y >= htSrc-1)
//       			continue;  

       		int rr = (rgba >> 16) & 0xff;
       		int gg = (rgba >>  8) & 0xff;
       		int bb = (rgba      ) & 0xff;
//       	final int aa = (rgba >> 24) & 0xff;
       		
       		final int r = (int)(dot.ir*scale + 0.5);  // + 1;
       		
//      	g2d.setPaint(new Color(0, 0, 0));
      		g2dBuffer.setPaint(new Color(rr, gg, bb));
      		g2dBuffer.fillOval(x-r, y-r, 2*r+1, 2*r+1);
    	}
       	final int wdDst = getWidth();
       	final int htDst = getHeight();

       	g2d.drawImage(buffer, 0, 0, wdDst, htDst, 0, 0, wdSrc, htSrc, null);
    }

    //-------------------------------------------------------------------------

	@Override
	public void mouseClicked(MouseEvent e) 
	{
		// ...
	}

	@Override
	public void mousePressed(MouseEvent e) 
	{
		// ...
	}

	@Override
	public void mouseReleased(MouseEvent e) 
	{
		// ...
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
		// ...
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
		// ...
	}

	@Override
	public void keyReleased(KeyEvent e) 
	{
		// ...
	}
        
	//-------------------------------------------------------------------------

}
