package graphics;

import processing.core.PVector;

/***************************************************************************
 * Quaternion class written by BlackAxe / Kolor aka Laurent Schmalen in 1997
 * Translated to Java(with Processing) by RangerMauve in 2012
 * this class is freeware. you are fully allowed to use this class in non-
 * commercial products. Use in commercial environment is strictly prohibited
 */

public class Quaternion2 {
  public  float W, X, Y, Z;      // components of a quaternion

  // default constructor
  public Quaternion2() {
    W = 1.0f;
    X = 0.0f;
    Y = 0.0f;
    Z = 0.0f;
  }

  // initialized constructor

  public Quaternion2(float w, float x, float y, float z) {
    W = w;
    X = x;
    Y = y;
    Z = z;
  }

  // quaternion multiplication
  public Quaternion2 mult (Quaternion2 q) {
    float w = W*q.W - (X*q.X + Y*q.Y + Z*q.Z);

    float x = W*q.X + q.W*X + Y*q.Z - Z*q.Y;
    float y = W*q.Y + q.W*Y + Z*q.X - X*q.Z;
    float z = W*q.Z + q.W*Z + X*q.Y - Y*q.X;

    W = w;
    X = x;
    Y = y;
    Z = z;
    return this;
  }

  // conjugates the quaternion
  public Quaternion2 conjugate () {
    X = -X;
    Y = -Y;
    Z = -Z;
    return this;
  }

  // inverts the quaternion
  public Quaternion2 reciprical () {
    float norme = (float)Math.sqrt(W*W + X*X + Y*Y + Z*Z);
    if (norme == 0.0)
      norme = 1.0f;

    float recip = 1.0f / norme;

    W =  W * recip;
    X = -X * recip;
    Y = -Y * recip;
    Z = -Z * recip;

    return this;
  }

  // sets to unit quaternion
  public Quaternion2 normalize() {
    float norme = (float)Math.sqrt(W*W + X*X + Y*Y + Z*Z);
    if (norme == 0.0)
    {
      W = 1.0f; 
      X = Y = Z = 0.0f;
    }
    else
    {
      float recip = 1.0f/norme;

      W *= recip;
      X *= recip;
      Y *= recip;
      Z *= recip;
    }
    return this;
  }

  // Makes quaternion from axis
  public Quaternion2 fromAxis(float Angle, float x, float y, float z) { 
    float omega, s, c;
    int i;

    s = (float)Math.sqrt(x*x + y*y + z*z);

    if (Math.abs(s) > Float.MIN_VALUE)
    {
      c = 1.0f/s;

      x *= c;
      y *= c;
      z *= c;

      omega = -0.5f * Angle;
      s = (float)Math.sin(omega);

      X = s*x;
      Y = s*y;
      Z = s*z;
      W = (float)Math.cos(omega);
    }
    else
    {
      X = Y = 0.0f;
      Z = 0.0f;
      W = 1.0f;
    }
    normalize();
    return this;
  }

  public Quaternion2 fromAxis(float Angle, PVector axis) {
    return this.fromAxis(Angle, axis.x, axis.y, axis.z);
  }

  // Rotates towards other quaternion
  public void slerp(Quaternion2 a, Quaternion2 b, float t)
  {
    float omega, cosom, sinom, sclp, sclq;
    int i;


    cosom = a.X*b.X + a.Y*b.Y + a.Z*b.Z + a.W*b.W;


    if ((1.0f+cosom) > Float.MIN_VALUE)
    {
      if ((1.0f-cosom) > Float.MIN_VALUE)
      {
        omega = (float)Math.acos(cosom);
        sinom = (float)Math.sin(omega);
        sclp = (float)Math.sin((1.0f-t)*omega) / sinom;
        sclq = (float)Math.sin(t*omega) / sinom;
      }
      else
      {
        sclp = 1.0f - t;
        sclq = t;
      }

      X = sclp*a.X + sclq*b.X;
      Y = sclp*a.Y + sclq*b.Y;
      Z = sclp*a.Z + sclq*b.Z;
      W = sclp*a.W + sclq*b.W;
    }
    else
    {
      X =-a.Y;
      Y = a.X;
      Z =-a.W;
      W = a.Z;

      sclp = (float)Math.sin((1.0f-t) * Math.PI * 0.5);
      sclq = (float)Math.sin(t * Math.PI * 0.5);

      X = sclp*a.X + sclq*b.X;
      Y = sclp*a.Y + sclq*b.Y;
      Z = sclp*a.Z + sclq*b.Z;
    }
  }

  public Quaternion2 exp()
  {                               
    float Mul;
    float Length = (float)Math.sqrt(X*X + Y*Y + Z*Z);

    if (Length > 1.0e-4)
      Mul = (float)Math.sin(Length)/Length;
    else
      Mul = 1.0f;

    W = (float)Math.cos(Length);

    X *= Mul;
    Y *= Mul;
    Z *= Mul; 

    return this;
  }

  public Quaternion2 log()
  {
    float Length;

    Length = (float)Math.sqrt(X*X + Y*Y + Z*Z);
    Length = (float)Math.atan(Length/W);

    W = 0.0f;

    X *= Length;
    Y *= Length;
    Z *= Length;

    return this;
  }
  
};
