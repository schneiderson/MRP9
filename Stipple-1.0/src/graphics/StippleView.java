package graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollectionIterator;

import main.Stipple;

//---------------------------------------------------------------

/**
 * Main view.
 * @author cambolbro
 */
public class StippleView extends JPanel implements MouseListener, MouseMotionListener 
{
	/** Default class UID. */
	private static final long serialVersionUID = 1L;
	
	/** */
	protected BufferedImage target = null;
	
//	/** */
//	protected List<Sample> samples = new ArrayList<Sample>();
		
	/** */
	protected Point downAt = null;
	
	/** */
	protected Stipple app = null;
	
	/** Error message. */
	protected String error = "";
	
    //---------------------------------------------------------------------
    
    /**
     * Constructor.
     * @param app
     */
    public StippleView(final Stipple app)
    {
    	this.app = app;
    	
    	addMouseListener(this);
     	addMouseMotionListener(this);
    	setFocusable(true);  // for key input
     }
    
    //---------------------------------------------------------------------

    /**
     * @return Target image.
     */
    public BufferedImage target()
    {
    	return target;
    }
    
    /**
     * @param str
     */
    public void setError(final String str)
    {
    	error = new String(str);
    	repaint();
    }
    
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
//    }
    
    //---------------------------------------------------------------------

    @SuppressWarnings("static-access")
	public void paint(Graphics g) 
    {
    	Graphics2D g2d = (Graphics2D)g;
     	g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    	g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    	g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

       	g2d.setPaint(Color.white);
    	g2d.fillRect(0, 0, getWidth(), getHeight());

    	if (app.stippler() != null)
    	{
    		final BufferedImage img = app.stippler().blurred();  //app.stippler().source();
    		if (img != null)  	
    			g2d.drawImage(img, 0, 0, null);
    		
    		drawVD(g2d);
    	
    		// Show status
    		String str = "Done.";
    		if (app.stippler().pass > 0)
    		{
    			str = "Pass " + (app.stippler().numPasses - app.stippler().pass + 1) + 
    		   					"/" + app.stippler().numPasses + ".";
    			if (app.stippler().pass > 1)
    				str += "..";
    		}
       		g2d.setPaint(new Color(255, 255, 255));
    		g2d.drawString(str, 10, getHeight()-9);
       		g2d.setPaint(new Color(0, 0, 0));
    		g2d.drawString(str, 9, getHeight()-10);
    	}
    	
    	if (error != "")
    	{
       		g2d.setPaint(new Color(255, 0, 0));
    		g2d.drawString(error, 9, getHeight()-30);    		
    	}
    }	
    
    /**
     * Draw all the Delaunay triangles.
     * @param g2d
     */
    public void drawVD(final Graphics2D g2d) 
    {
    	if (app.stippler().polys() == null)
    		return;
    	
    	final Color meshColour = new Color(100, 200, 255, 100);
    	final Color midColour  = new Color(255, 0, 0, 100);
    	
//    	final int wd = app.stippler().blurred().getWidth();
//    	final int ht = app.stippler().blurred().getHeight();
    	
		@SuppressWarnings({ "rawtypes" })
		Iterator geomi = new GeometryCollectionIterator(app.stippler().polys());
	    while (geomi.hasNext()) 
	    {
	    	Geometry item = (Geometry)geomi.next();
	    	Coordinate[] poly = item.getCoordinates();
	    	
	    	final int len = poly.length;
	    	
            final int[] xs = new int[len];
            final int[] ys = new int[len];
            int n;
            for (n = 0; n < len; n++) 
            {
                xs[n] = (int)(poly[n].x + 0.5);
                ys[n] = (int)(poly[n].y + 0.5);
            
//                if (xs[n] < 0 || xs[n] >= wd || ys[n] < 0 || ys[n] >= ht)
//                	break;
            }
            //if (n >= len)
            {
            	g2d.setPaint(meshColour);
            	g2d.drawPolygon(xs, ys, len);
//            	for (n = 0; n < poly.length; n++)
//            	{
//            		final int x0 = xs[n];
//            		final int y0 = ys[n];
//            		final int x1 = xs[(n + 1) % len];
//            		final int y1 = ys[(n + 1) % len];
//                    if 
//                    (
////                    	x0 >= 0 && y0 >= 0 && x0 < wd && y0 < ht 
////                    	&&
////                    	x1 >= 0 && y1 >= 0 && x1 < wd && y1 < ht
////                    	&&
//                    	(x0 != x1 || y0 != y1)
//                    )
//                    	g2d.drawLine(x0, y0, x1, y1);
//            	}
            }
            
            // Draw centroid
	       	final com.vividsolutions.jts.geom.Point pt = item.getCentroid();
	       	final int cx = (int)(pt.getX() + 0.5);
	       	final int cy = (int)(pt.getY() + 0.5);

	       	g2d.setPaint(midColour);
	       	g2d.fillOval(cx-1, cy-1, 3, 3);
	    }
    }

    //-------------------------------------------------------------------------
    
	@Override
	public void mouseClicked(MouseEvent e) 
	{
		System.out.println("Click at (" + e.getPoint().x + "," + e.getPoint().y + ").");
//		}
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
	public void mousePressed(MouseEvent e) 
	{
//		//System.out.println("Press at (" + e.getPoint().x + "," + e.getPoint().y + ").");
//		downAt = new Point(e.getPoint().x, e.getPoint().y);
//		addSample(e.getPoint());
	}

	@Override
	public void mouseReleased(MouseEvent e) 
	{
		downAt = null;
	}

	@Override
	public void mouseDragged(MouseEvent e) 
	{
//		//System.out.println("Drag to (" + e.getPoint().x + "," + e.getPoint().y + ").");
//		addSample(e.getPoint());
	}

	@Override
	public void mouseMoved(MouseEvent e) 
	{
		// TODO Auto-generated method stub	
	}

    //--------------------------------------------

}
