package knotwork;

import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.math.Vector2D;
import util.AngleUtil;

public class KnotNode {

    private Coordinate pos;
    private Vector2D vector;
    private boolean right;
    private Crossing crossing;

    public static KnotNode createFromNormVector(Coordinate pos, Vector2D normVec, boolean right, Crossing crossing){
        Vector2D vec;

        // rotate from norm vector by 45 degree
        Double rotation = Angle.toRadians(45);

        // if it's a right node, rotate by -45, otherwise 45 degrees
        if(right){
            vec = normVec.rotate(-rotation);
        } else{
            vec = normVec.rotate(rotation);
        }

        return new KnotNode(pos, vec, right, crossing);
    }

    public KnotNode(Coordinate pos, Coordinate to, boolean right, Crossing crossing){
        this.pos = pos;
        this.crossing = crossing;
        this.vector = new Vector2D(pos, to).normalize();
        this.right = right;
    }

    public KnotNode(Coordinate pos, Vector2D vector, boolean right, Crossing crossing){
        this.pos = pos;
        this.crossing = crossing;
        this.vector = vector.normalize();
        this.right = right;
    }

    public KnotNode(KnotNode knotNode){
        /*
        * Creates copy of KnotNode
        * [Except for the Crossing object]
        * Also inverts the vector (multiplying by -1)
        * */
        this.pos = (Coordinate) knotNode.pos.clone();
        this.right = knotNode.right;
        this.crossing = knotNode.crossing;
        this.vector = knotNode.vector.multiply(-1);
    }

    public boolean isRightNode(){
        return right;
    }

    public boolean isLeftNode(){
        return !right;
    }

    public Vector2D getVector(){
        return vector;
    }

    public Coordinate getPos() {
        return pos;
    }

    public Crossing getCrossing() { return crossing; }

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


    public boolean equals(KnotNode other) {
        return (vector.equals(other.vector) && pos.equals(other.pos) && right == other.right);
    }
}
