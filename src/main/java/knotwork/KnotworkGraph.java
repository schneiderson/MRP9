package knotwork;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.math.Vector2D;

import java.util.ArrayList;

public class KnotworkGraph {

    public ArrayList<Crossing> crossings = new ArrayList<>();
    public ArrayList<Edge> edges;
    public ArrayList<Coordinate> nodes;


    public KnotworkGraph(ArrayList<Coordinate> nodes, ArrayList<Edge> edges) {
        this.nodes = nodes;
        this.edges = edges;

        // create crossings for each edge
        edges.forEach(x -> crossings.add(new Crossing(x)));
    }

    public KnotNode getInitialKnotNode(){
        ArrayList<KnotNodePair> unvisited = getUnvisitedNodePairs();
        if(unvisited.size() > 0){
            return unvisited.get(0).node1;
        }
        return null;
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

    public ArrayList<KnotNodePair> getUnvisitedNodePairs(){
        ArrayList<KnotNodePair> nodePairs = getAllNodePairs();
        // remove nodePairs which have been visited
        nodePairs.removeIf(knotNodePair -> knotNodePair.isVisited());
        return nodePairs;
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

    public ArrayList<ArrayList<KnotNode>> getControlSets(){
        ArrayList<ArrayList<KnotNode>> controlSets = new ArrayList<>();
        while(getUnvisitedNodePairs().size() > 0){
            ArrayList<KnotNode> controlSet = runMercat();
            if(controlSet != null && controlSet.size() > 0){
                controlSets.add(controlSet);
            }
        }
        return controlSets;
    }

    public ArrayList<KnotNode> runMercat(){
        ArrayList<KnotNode> controlSet = new ArrayList<>();
        KnotNode node = getInitialKnotNode();
        // add starting node
        controlSet.add(node);

        while(controlSet.size() < 2 || controlSet.get(0).equals(controlSet.get(controlSet.size()-1))){

            Crossing cross = getCorrespondingCrossing(node);
            ArrayList<Edge> incidentEdges = getAdjacentEdges(cross, node);

            // TODO: order the edges by their angle
            // depending on weather we look at a right or left node

            // add next node to controlSet

            // repeat until the controlSet is closed

            
        }

        return controlSet;
    }
}
