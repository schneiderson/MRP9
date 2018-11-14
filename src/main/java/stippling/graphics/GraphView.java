package stippling.graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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


    public void paint(Graphics g) 
    {
    	if (bgs == null)
    		init();
    	
    	Graphics2D g2d = (Graphics2D)g;
    	g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    	g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    	g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

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

    }
    
	//-------------------------------------------------------------------------
	
}
