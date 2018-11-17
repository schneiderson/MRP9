package knotwork;

import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.math.Vector2D;

import static java.lang.Math.PI;

public class KnotNode {

    private Coordinate pos;
    private Vector2D vector;
    private Boolean right;

    public static KnotNode createFromNormVector(Coordinate pos, Vector2D normVec, Boolean right){
        Vector2D vec;

        // rotate from norm vector by 45 degree
        Double rotation = Angle.toRadians(45);

        if(right){
            vec = normVec.rotate(-rotation);
        } else{
            vec = normVec.rotate(rotation);
        }

        return new KnotNode(pos, vec, right);
    }

    public KnotNode(Coordinate pos, Coordinate to, Boolean right){
        this.pos = pos;
        this.vector = new Vector2D(pos, to);
        this.right = right;
    }

    public KnotNode(Coordinate pos, Vector2D vector, Boolean right){
        this.pos = pos;
        this.vector = vector;
        this.right = right;
    }

    public Boolean isRightNode(){
        return right;
    }

    public Boolean isLeftNode(){
        return !right;
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
    public Double getAngleRadians(Boolean normalized){
        if(normalized){
            return vector.angle();
        } else{
            Double angle = vector.angle();
            if(angle < 0){
                angle = 2 * PI + angle;
            }
            return angle;
        }
    }

    /**
     * Returns the angle of the vector in degree
     *
     * @param normalized If set to 'true' angle is normalized to be in the range ( -180, 180 ].
     * @return angle in degree
     */
    public Double getAngleDegree(Boolean normalized){
        Double tmp = getAngleRadians(normalized);
        return Angle.toDegrees(tmp);
    }
}
