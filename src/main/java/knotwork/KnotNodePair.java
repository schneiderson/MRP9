package knotwork;

import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.math.Vector2D;

public class KnotNodePair {

    private Boolean visited = false;
    public KnotNode node1;
    public KnotNode node2;

    public KnotNodePair(KnotNode node1, KnotNode node2){
        this.node1 = node1;
        this.node2 = node2;
    }

    public KnotNodePair(KnotNode node1){
        this.node1 = node1;

        // rotate vector by 180 degrees
        Double rotation = Angle.toRadians(180);
        Vector2D newVec = node1.getVector().rotate(rotation);

        this.node2 = new KnotNode(node1.getPos(), newVec, node1.isRightNode());
    }

    public void visit(){
        visited = true;
    }

    public Boolean isVisited(){
        return visited;
    }

}
