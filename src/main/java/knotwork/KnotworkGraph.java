package knotwork;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.math.Vector2D;

import java.util.ArrayList;

public class KnotworkGraph {

    private Boolean debugOutput = true;

    public ArrayList<Crossing> crossings = new ArrayList<>();
    public ArrayList<Edge> edges = new ArrayList<>();
    public ArrayList<Coordinate> nodes = new ArrayList<>();


    public KnotworkGraph(ArrayList<Coordinate> nodes, ArrayList<Edge> edges) {
        this.nodes = nodes;
        this.edges = edges;

        // create crossings for each edge
        edges.forEach(x -> crossings.add(new Crossing(x)));

        // TODO: remove debug output
        if (debugOutput) {
            crossings.forEach(x -> System.out.println(
                    "Edge: " + x.edge + " " +
                            "Crossing at: " + x.pos + " norm vector angle: " + x.getNormVectorAngleDeg(false)));
        }

    }

    public KnotNode getInitialKnotNode() throws Exception {
        if (crossings.size() > 0) {
            return crossings.get(0).rightNodePair.node1;
        } else {
            throw new Exception("No crossings initialized");
        }
    }

    public ArrayList<KnotNode> getAllNodes() {
        ArrayList<KnotNode> allNodes = new ArrayList<>();
        for (Crossing crossing : crossings) {
            allNodes.add(crossing.leftNodePair.node1);
            allNodes.add(crossing.leftNodePair.node2);
            allNodes.add(crossing.rightNodePair.node1);
            allNodes.add(crossing.rightNodePair.node2);
        }
        return allNodes;
    }

    public ArrayList<KnotNodePair> getAllNodePairs() {
        ArrayList<KnotNodePair> allNodePairs = new ArrayList<>();
        for (Crossing crossing : crossings) {
            allNodePairs.add(crossing.leftNodePair);
            allNodePairs.add(crossing.rightNodePair);
        }
        return allNodePairs;
    }

    public ArrayList<Edge> getAdjacentEdges(Crossing cross, KnotNode node) {
        // find junction first
        // to do that, create two helper vectors from midpoint to endpoint of edge
        Vector2D hv1 = new Vector2D(cross.edge.midpoint, cross.edge.c1);

        // calculate the difference in angle from helper vectors to norm vector
        Double diffAngle1 = cross.normVector.angleTo(hv1);
        Double diffAngleNode = cross.normVector.angleTo(node.getVector());

        Coordinate junction;
        // if difference in angle of node vector and helper vector have the same sign they point in the "same" direction
        if(diffAngle1 * diffAngleNode >= 0){
            // same sign
            junction = cross.edge.c1;
        } else {
            // different sign
            junction = cross.edge.c2;
        }

        // TODO: remove debug output
        if(debugOutput){
            System.out.println("Coordinate1 : " + cross.edge.c1);
            System.out.println("Coordinate2 : " + cross.edge.c2);
            System.out.println("Angle1 " + diffAngle1);
            System.out.println("AngleDiff " + diffAngleNode);
            System.out.println("Junction " + junction);

        }

        ArrayList<Edge> incidentEdges = new ArrayList<>();
        // now get all edges incident to this junction (except for this one)
        for (Edge edge : edges) {
            if(edge.equals(cross.edge)){
                continue;
            }
            if(edge.isIncidentToVertex(junction)){
                incidentEdges.add(edge);
            }
        }
        return incidentEdges;
    }

    public Crossing getCorrespondingCrossing(KnotNode node) {
        Crossing matchingCrossing = null;
        for (Crossing crossing : crossings) {
            if (crossing.rightNodePair.node1 == node
                    || crossing.rightNodePair.node2 == node
                    || crossing.leftNodePair.node1 == node
                    || crossing.leftNodePair.node2 == node) {
                matchingCrossing = crossing;
                break;
            }
        }
        return matchingCrossing;
    }
}
