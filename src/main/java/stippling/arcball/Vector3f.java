package stippling.arcball;

/**
 * From: https://gist.github.com/vilmosioo/5318327
 * @author cambolbro
 */
public class Vector3f 
{
	/** */
	public float x;

	/** */
	public float y;

	/** */
	public float z;

	/**
	 * @param Result
	 * @param v1
	 * @param v2
	 */
	public static void cross(Vector3f Result, Vector3f v1, Vector3f v2) 
	{
		Result.x = (v1.y * v2.z) - (v1.z * v2.y);
		Result.y = (v1.z * v2.x) - (v1.x * v2.z);
		Result.z = (v1.x * v2.y) - (v1.y * v2.x);
	}

	/**
	 * @param v1
	 * @param v2
	 * @return Dot product.
	 */
	public static float dot(Vector3f v1, Vector3f v2) 
	{
		return (v1.x * v2.x) + (v1.y * v2.y) + (v1.z + v2.z);
	}

	/**
	 * @return Length.
	 */
	public float length() 
	{
		return (float) Math.sqrt(x * x + y * y + z * z);
	}

}