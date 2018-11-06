package main;

/**
 * XYZ coordinate.
 * @author cambolbro
 */
public class XYZ 
{
	/** */
	public double x = 0;
	
	/** */
	public double y = 0;
	
	/** */
	public double z = 0; 

	//-------------------------------------------------------------------------
	
	/**
	 * Default constructor.
	 */
	public XYZ()
	{
	}
	
	/**
	 * Constructor.
	 * @param x
	 * @param y
	 * @param z
	 */
	public XYZ(final double x, final double y, final double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
    public boolean equals(Object object)
    {
        boolean sameSame = false;
        final double resn = 0.0000001;
        if (object != null && object instanceof XYZ)
        {
            sameSame = 	Math.abs(this.x - ((XYZ)object).x) < resn 
            			&&  
            			Math.abs(this.y - ((XYZ)object).y) < resn 
            			&&  
            			Math.abs(this.z - ((XYZ)object).z) < resn;  // &&  && this.y == ((XYZ)object).y && this.z == ((XYZ)object).z);
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
