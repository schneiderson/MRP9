package knotwork;

import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.math.Vector2D;
import util.AngleUtil;

public class KnotNode extends BaseNode {

    private boolean right;
    private Crossing crossing;

    public static KnotNode createFromNormVector(Coordinate pos, Vector2D normVec, boolean right, Crossing crossing){
        Vector2D vec;

        // rotate from norm vector by 45 degree
        Double rotation = Angle.toRadians(45);

        // if it's a right node, rotate by -45, otherwise 45 degrees
        if(right){
            vec = normVec.rotate(rotation);
        } else{
            vec = normVec.rotate(-rotation);
        }

        return new KnotNode(pos, vec, right, crossing);
    }

    public KnotNode(Coordinate pos, Coordinate to, boolean right, Crossing crossing){
        super(pos, new Vector2D(pos, to).normalize());
        this.crossing = crossing;
        this.right = right;
    }

    public KnotNode(Coordinate pos, Vector2D vector, boolean right, Crossing crossing){
        super(pos, vector.normalize());
        this.crossing = crossing;
        this.right = right;
    }

    public KnotNode(KnotNode knotNode){
        /*
        * Creates copy of KnotNode
        * [Except for the Crossing object]
        * Also inverts the vector (multiplying by -1)
        * */
        super(knotNode.pos, knotNode.vector);
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

    public Crossing getCrossing() { return crossing; }


    public boolean equals(KnotNode other) {
        return (this.vector.equals(other.vector) && this.pos.equals(other.pos) && right == other.right);
    }
}
