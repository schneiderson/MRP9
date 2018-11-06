package graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
//import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import java.util.ArrayList;
//import java.util.List;
import java.awt.image.BufferedImage;

import javax.swing.Timer;
import javax.swing.JPanel;

//-----------------------------------------------------------------------------

/**
 * Graph of mouse movement.
 * @author cambolbro
 */
public class GraphView extends JPanel 
{
	/** */
	private static final long serialVersionUID = 2L;

//	/** */
//	protected Point posn = new Point();
//	
//	/** */
//	protected List<Sample> samples = new ArrayList<Sample>();

	/** */
	protected StippleView mainView = null;
	
	/** View timer. */
	protected Timer timer;

	/** */
	protected int lastX = -1;

	/** */
	protected int lastY = -1;

	/** */
	protected int lastA = 0;

	/** */
	protected int lastB = 0;

	/** */
	protected int lastC = 0;
	
	/** */
	protected final int step = 5;
	
	/** */
	protected BufferedImage[] bgs = null;
	
	/** */
	protected Graphics2D[] g2ds = null;
	
	/** */
	protected int ticks = 0;
	
	//-------------------------------------------------------------------------

	/**
	 * @param mainView
	 */
	public GraphView(final StippleView mainView)
	{
		this.mainView = mainView;
	}
	
	//-------------------------------------------------------------------------

//	/**
//	 * @param samps
//	 */
//	public void setData(final List<Sample> samps)
//	{
//		samples = samps;
//		
//		System.out.println("-- New sample at (" + samples.get(0).pt().x + "," + samples.get(0).pt().y + ").");
//		
//	}
	
	//-------------------------------------------------------------------------

    public void paint(Graphics g) 
    {
    	if (bgs == null)
    		init();
    	
    	Graphics2D g2d = (Graphics2D)g;
    	g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    	g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    	g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

//    	g2d.setPaint(Color.white);
//    	g2d.fillRect(0, 0, getWidth(), getHeight());
//
//    	g2d.setPaint(new Color(200, 200, 255));
//    	g2d.drawLine(0, getHeight()/4, getWidth(), getHeight()/4);
//
//    	g2d.setPaint(new Color(0, 0, 0));
//    	g2d.drawLine(0, getHeight()/2, getWidth(), getHeight()/2);
//    	
//    	g2d.setPaint(new Color(200, 200, 255));
//    	g2d.drawLine(0, getHeight()*3/4, getWidth(), getHeight()*3/4);
    	
    	drawStep(g2d);
    }

    //-------------------------------------------------------------------------
    
    /** 
     * @param g2d
     */
    void drawStep(final Graphics2D g2d)
    {
//       	final int wd = getWidth();
    	final int ht = getHeight();
    	final int ht1 = ht / 4;
    	final int ht2 = ht / 2;
    	final int ht3 = ht * 3 / 4;

    	final int on  = ticks % 2;
    	final int next = 1 - on;

    	// Push old image back
    	g2ds[next].drawImage(bgs[on], step, 0, null);

    	// Draw sliver background
    	g2ds[next].setPaint(Color.white);
    	g2ds[next].fillRect(0, 0, step, ht);

    	g2ds[next].setPaint(new Color(200, 200, 255));
    	g2ds[next].drawLine(0, ht1, step, ht1);

    	g2ds[next].setPaint(new Color(0, 0, 0));
    	g2ds[next].drawLine(0, ht2, step, ht2);
    	
    	g2ds[next].setPaint(new Color(200, 200, 255));
    	g2ds[next].drawLine(0, ht3, step, ht3);
    	
    	if (mainView.downAt == null)  // && mainView.samples.size() > 0)
    	{
    		lastX = -1;
    		lastY = -1;
    	}
    	else
    	{
//    		// Draw new lines
//    		final Sample sample = mainView.samples.get(0);
//    		final int nextX = (int)((mainView.getWidth() - sample.pt.x) / (double)mainView.getWidth() * ht2 + 0.5);
//    		final int nextY = ht2 - (int)((mainView.getHeight() - sample.pt.y) / (double)mainView.getHeight() * ht2 + 0.5);
//    	
//    		if (lastX != -1 && lastY != -1)
//    		{
//    			g2ds[next].setPaint(new Color(127, 127, 127));  //255, 100, 100));
//    			g2ds[next].drawLine(0, nextX, step, lastX);
//    		
//    			g2ds[next].setPaint(new Color(200, 200, 200));  //0, 127, 255));
//    			g2ds[next].drawLine(0, nextY, step, lastY);
//    		}
//    		
//        	lastX = nextX;
//        	lastY = nextY;
    	}
     	
    	g2d.drawImage(bgs[next], 0, 0, null);
    }
    
    //-------------------------------------------------------------------------

    /**
     * Search timer.
     * @author cambolbro
     */
    class TimerListener implements ActionListener
    {
		@Override
		public void actionPerformed(ActionEvent arg0) 
		{
			ticks++;
			//System.out.println("Tick...");
			repaint();
		}    	
    }
    
    //-------------------------------------------------------------------------

    /**
     * Start the timer.
     */
    public void init()
    {
    	final int wd = getWidth();
    	final int ht = getHeight();
    	final int ht1 = ht / 4;
    	final int ht2 = ht / 2;
    	final int ht3 = ht * 3 / 4;
    	
    	// Prepare background
    	bgs  = new BufferedImage[2];
    	g2ds = new Graphics2D[2];
    	
    	bgs[0]  = new BufferedImage(wd, ht, BufferedImage.TYPE_INT_ARGB);
    	g2ds[0] = (Graphics2D)bgs[0].getGraphics();
    	g2ds[0].setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    	g2ds[0].setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    	g2ds[0].setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    	g2ds[0].setPaint(Color.white);
    	g2ds[0].fillRect(0, 0, wd, ht);

    	g2ds[0].setPaint(new Color(200, 200, 255));
    	g2ds[0].drawLine(0, ht1, wd, ht1);

    	g2ds[0].setPaint(new Color(0, 0, 0));
    	g2ds[0].drawLine(0, ht2, wd, ht2);
    	
    	g2ds[0].setPaint(new Color(200, 200, 255));
    	g2ds[0].drawLine(0, ht3, wd, ht3);

    	bgs[1]  = new BufferedImage(wd, ht, BufferedImage.TYPE_INT_ARGB);
    	g2ds[1] = (Graphics2D)bgs[1].getGraphics();
    	g2ds[1].setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    	g2ds[1].setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    	g2ds[1].setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    	g2ds[1].drawImage(bgs[0], 0, 0, null);
    	
//    	// Start timer
//    	timer = new Timer(50, new TimerListener());
//    	//timer.setInitialDelay(1000);
//    	timer.start(); 
    }
    
	//-------------------------------------------------------------------------
	
}
