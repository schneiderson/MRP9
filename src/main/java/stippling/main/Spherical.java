package stippling.main;

/**
 * Spherical coordinate.
 * @author cambolbro
 */
public class Spherical 
{
	/** */
	public double radius = 0;
	
	/** */
	public double inclination = 0;
	
	/** */
	public double azimuth = 0; 

	//-------------------------------------------------------------------------
	
	/**
	 * Default constructor.
	 */
	public Spherical()
	{
	}
	
	/**
	 * Constructor.
	 * @param r
	 * @param i
	 * @param a
	 */
	public Spherical(final double r, final double i, final double a)
	{
		this.radius = r;
		this.inclination = i;
		this.azimuth = a;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
    public boolean equals(Object object)
    {
        boolean sameSame = false;
        final double resn = 0.0000001;
        if (object != null && object instanceof Spherical)
        {
            sameSame = 	Math.abs(this.radius - ((Spherical)object).radius) < resn 
            			&&  
            			Math.abs(this.inclination - ((Spherical)object).inclination) < resn 
            			&&  
            			Math.abs(this.azimuth - ((Spherical)object).azimuth) < resn;  // &&  && this.y == ((XYZ)object).y && this.z == ((XYZ)object).z);
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
