package stippling.arcball;

/**
 * From: https://gist.github.com/vilmosioo/5318327
 * @author cambolbro
 */
public class Quat4f 
{
	/** */
	public float x;

	/** */
	public float y;

	/** */
	public float z;

	/** */
	public float w;

	/**
	 * Constructor.
	 */
	public Quat4f() 
	{
		this(0, 0, 0, 0);
	}

	/**
	 * @param angle
	 * @param x
	 * @param y
	 * @param z
	 */
	public Quat4f(double angle, float x, float y, float z) 
	{
		double s = Math.sqrt(x * x + y * y + z * z);
		double c = 1.0 / s;

		final double xx = x * c;
		final double yy = y * c;
		final double zz = z * c;

		float omega = (float)(-0.5f * angle);
		s = (float)Math.sin(omega);

		this.x = (float)(s * xx);
		this.y = (float)(s * yy);
		this.z = (float)(s * zz);
		this.w = (float)Math.cos(omega);
	}
	
}