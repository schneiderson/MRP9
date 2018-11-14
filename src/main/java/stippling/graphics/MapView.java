package stippling.graphics;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import stippling.main.Stipple;

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

//---------------------------------------------------------------------
    
    /**
     * Constructor.
     * @param app
     */
    public MapView(final Stipple app)
    {
    	this.app = app;
    }
    

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

}
