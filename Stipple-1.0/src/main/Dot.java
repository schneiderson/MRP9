package main;

/**
 * Stipple dot.
 * @author cambolbro
 */
public class Dot 
{
	/** Image position X. */
	public double ix = 0;
	
	/** Image position Y. */
	public double iy = 0;
	
	/** Image radius. */
	public double ir = 2; 
		
	/** */
	public double area = 0; 
	
	/** */
	public int rgba = 0;

//	/** World coordinate. */
//	public XYZ world = new XYZ();
//	
//	/** World radius. */
//	public double wr = 0; 

	/** Spherical mapping. */
	public Spherical spherical = new Spherical();
	
	//-------------------------------------------------------------------------
	
	/**
	 * Default constructor.
	 */
	public Dot()
	{
	}
	
	/**
	 * Constructor.
	 * @param x
	 * @param y
	 */
	public Dot(final double x, final double y)
	{
		this.ix = x;
		this.iy = y;
	}

	/**
	 * Constructor.
	 * @param x
	 * @param y
	 * @param r
	 * @param rgba
	 */
	public Dot(final double x, final double y, final double r, final int rgba)
	{
		this.ix = x;
		this.iy = y;
		this.ir = r;
		this.rgba = rgba;
	}
	

	//-------------------------------------------------------------------------

	/**
	 * @return Red component.
	 */
	public int red()
	{
		return (rgba >> 16) & 0xff;
	}

	/**
	 * @return Green component.
	 */
	public int green()
	{
		return (rgba >> 8) & 0xff;
	}

	/**
	 * @return Blue component.
	 */
	public int blue()
	{
		return (rgba) & 0xff;
	}

	/**
	 * @return Alpha component.
	 */
	public int alpha()
	{
		return (rgba >> 24) & 0xff;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
    public boolean equals(Object object)
    {
        boolean sameSame = false;
        if (object != null && object instanceof Dot)
        {
            sameSame = (this.ix == ((Dot)object).ix && this.iy == ((Dot)object).iy);
        }
        return sameSame;
    }

	@Override
	public int hashCode() 
	{
		return super.hashCode();
	}

	//-------------------------------------------------------------------------

}
