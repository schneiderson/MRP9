package knotwork;

import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.math.Vector2D;
import util.AngleUtil;

public class KnotNode {

    private boolean right;
    private Crossing crossing;
    protected Coordinate pos;
    protected Vector2D vector;

    public KnotNode(Coordinate pos, Coordinate to, boolean right, Crossing crossing) {
        this.pos = pos;
        this.vector = vector;
        this.crossing = crossing;
        this.right = right;
    }

    public KnotNode(Coordinate pos, Vector2D vector, boolean right, Crossing crossing) {
        this.pos = pos;
        this.vector = vector;
        this.crossing = crossing;
        this.right = right;
    }

    public KnotNode(KnotNode knotNode) {
        /*
         * Creates copy of KnotNode
         * [Except for the Crossing object]
         * Also inverts the vector (multiplying by -1)
         * */
        this.pos = pos;
        this.vector = vector;
        this.pos = (Coordinate) knotNode.pos.clone();
        this.right = knotNode.right;
        this.crossing = knotNode.crossing;
        this.vector = knotNode.vector.multiply(-1);
    }


    public static KnotNode createFromNormVector(Coordinate pos, Vector2D normVec, boolean right, Crossing crossing) {
        Vector2D vec;

        // rotate from norm vector by 45 degree
        Double rotation = Angle.toRadians(45);

        // if it's a right node, rotate by -45, otherwise 45 degrees
        if (right) {
            vec = normVec.rotate(rotation);
        } else {
            vec = normVec.rotate(-rotation);
        }

        return new KnotNode(pos, vec, right, crossing);
    }

    // type 1: wall
    public static KnotNode createMeta1FromNormVector(Coordinate pos, Vector2D normVec, boolean right, Crossing crossing,
                                                     boolean neg, double metaPointDistance) {
        Vector2D vec;
        Coordinate position;

        if (neg) {
            position = normVec.multiply(-metaPointDistance).translate(pos);

        } else {
            position = normVec.multiply(metaPointDistance).translate(pos);

        }

        // two vector directions are possible
        if (neg && right || !neg && !right) {
            vec = normVec.rotateByQuarterCircle(3);
        } else {
            vec = normVec.rotateByQuarterCircle(1);
        }

        return new KnotNode(position, vec, right, crossing);

    }

    // type 2: ghost
    public static KnotNode createMeta2FromNormVector(Coordinate pos, Vector2D normVec, boolean right, Crossing crossing,
                                                     boolean neg, double metaPointDistance) {
        Vector2D vec;
        Coordinate position;

        // Get the midpoints between the crossing and the edge ends
        if (!neg) {
            position = normVec.rotateByQuarterCircle(1).multiply(metaPointDistance).translate(pos);
        } else {
            position = normVec.rotateByQuarterCircle(3).multiply(metaPointDistance).translate(pos);
        }

        // two vector directions are possible
        if(neg && !right || !neg && right){
            vec = normVec;
        } else {
            vec = normVec.rotateByQuarterCircle(2);
        }

        return new KnotNode(position, vec, right, crossing);

    }

    public Vector2D getVector() {
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
    public Double getAngleRadians(boolean normalized) {
        if (normalized) {
            return vector.angle();
        } else {
            return AngleUtil.getAngleRadiansRescaled(vector.angle());
        }
    }

    /**
     * Returns the angle of the vector in degree
     *
     * @param normalized If set to 'true' angle is normalized to be in the range ( -180, 180 ].
     * @return angle in degree
     */
    public Double getAngleDegree(boolean normalized) {
        return Angle.toDegrees(getAngleRadians(normalized));
    }

    @Override
    public String toString() {
        return "Position: " + this.pos + " , Vector: " + this.vector;
    }

    public boolean isRightNode() {
        return right;
    }

    public boolean isLeftNode() {
        return !right;
    }

    public Crossing getCrossing() {
        return crossing;
    }

    public KnotNodePair getPrependicularKnotNodePair() {
        return crossing.getPerpendicularPairByNode(this);
    }

    public boolean equals(KnotNode other) {
        return (this.vector.equals(other.vector) && this.pos.equals(other.pos) && right == other.right);
    }

    public Boolean getOverpass() {
        if (crossing.getPairByNode(this) == null) //for breakpoints
        {
            return true;
        } else {
            return crossing.getPairByNode(this).getOverpass();
        }
    }

    public void setOverpass(boolean overpass) {
        crossing.getPairByNode(this).setOverpass(overpass);
    }

    public KnotNode getKnotNodeFromPair(){
        KnotNodePair knotNodePair = this.getCrossing().getPairByNode(this);
        if (this.equals(knotNodePair.node1)){
            return knotNodePair.node2;
        }
        return knotNodePair.node1;
    }
}
