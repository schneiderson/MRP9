package stippling.graphics;

import java.awt.Point;

/**
 * Sample mouse position.
 * @author cambolbro
 */
public class Sample 
{
	/** Position. */
	protected Point pt;
	
	/** Time. */
	protected long tick = 0;
	
	/** X. */
	protected double a = 0;
	
	/** Y. */
	protected double b = 0;
	
	/** Curvature. */
	protected double c = 0;

	//-------------------------------------------------------------------------
	
	/**
	 * Constructor.
	 * @param pt
	 * @param tick
	 * @param a
	 * @param b
	 * @param c
	 */
	public Sample(final Point pt, final long tick, final double a, final double b, final double c)
	{
		this.pt = new Point(pt.x, pt.y);
		this.tick = tick;
		this.a = a;
		this.b = b;
		this.c = c;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Position.
	 */
	public Point pt()
	{
		return pt;
	}
	
	/**
	 * @return Time.
	 */
	public long tick()
	{
		return tick;
	}
	
	/**
	 * @return X.
	 */
	public double a()
	{
		return a;
	}
	
	/**
	 * @return Y.
	 */
	public double b()
	{
		return b;
	}
	
	/**
	 * @return C.
	 */
	public double c()
	{
		return c;
	}
	
	//-------------------------------------------------------------------------

}
