package knotwork;

import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.math.Vector2D;

public class KnotNodePair {

    private boolean visited = false;
    public KnotNode node1;
    public KnotNode node2;
    private Boolean overpass = null;

    public KnotNodePair(KnotNode node1, KnotNode node2){
        this.node1 = node1;
        this.node2 = node2;
    }

    public KnotNodePair(KnotNode node1){
        this.node1 = node1;

        // rotate vector by 180 degrees
        Double rotation = Angle.toRadians(180);
        Vector2D newVec = node1.getVector().rotate(rotation);

        this.node2 = new KnotNode(node1.getPos(), newVec, node1.isRightNode(), node1.getCrossing());
    }

    public Boolean getOverpass() {
        return overpass;
    }

    public void setOverpass(Boolean overpass) {
        this.overpass = overpass;
    }

    public Crossing getCrossing(){
        return node1.getCrossing();
    }

    public boolean contains(KnotNode node){
        return (node1.equals(node) || node2.equals(node));
    }

    public boolean equals(KnotNodePair otherPair){
        return (node1.equals(otherPair.node1) && node2.equals(otherPair.node2)
                || node1.equals(otherPair.node2) && node2.equals(otherPair.node1));
    }

    public void visit(){
        visited = true;
    }

    public boolean isVisited(){
        return visited;
    }
}
