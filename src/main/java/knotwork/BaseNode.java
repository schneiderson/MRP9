package knotwork;

import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.math.Vector2D;
import util.AngleUtil;

public class BaseNode
{
    protected Coordinate pos;
    protected Vector2D vector;

    public BaseNode(Coordinate pos, Vector2D vector)
    {
        this.pos = pos;
        this.vector = vector;
    }

    public Vector2D getVector(){
        return vector;
    }

    public Coordinate getPos() {
        return pos;
    }

    /**
     * Returns the angle of the vector in radian
     *
     * @param normalized If set to 'true' angle is normalized to be in the range ( -Pi, Pi ].
     * @return angle in radian
     */
    public Double getAngleRadians(boolean normalized){
        if(normalized){
            return vector.angle();
        } else{
            return AngleUtil.getAngleRadiansRescaled(vector.angle());
        }
    }

    /**
     * Returns the angle of the vector in degree
     *
     * @param normalized If set to 'true' angle is normalized to be in the range ( -180, 180 ].
     * @return angle in degree
     */
    public Double getAngleDegree(boolean normalized){
        return Angle.toDegrees(getAngleRadians(normalized));
    }

    public boolean equals(BaseNode other) {
        return (vector.equals(other.vector) && pos.equals(other.pos));
    }

    @Override
    public String toString()
    {
        return "Position: "+this.pos+" , Vector: "+this.vector;
    }
}
