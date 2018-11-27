package knotwork;

import knotwork.curve.CubicBezier;
import knotwork.curve.Curve;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.math.Vector2D;
import util.AngleUtil;

import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Math.abs;

public class KnotworkGraph {

    public ArrayList<Crossing> crossings = new ArrayList<>();
    public ArrayList<Edge> edges;
    public ArrayList<Coordinate> nodes;
    public ArrayList<ArrayList<KnotNode>> controlSets;

    public ArrayList<ArrayList<Curve>> curveLists;


    public KnotworkGraph(ArrayList<Coordinate> nodes, ArrayList<Edge> edges) {
        this.nodes = nodes;
        this.edges = edges;

        // create crossings for each edge
        edges.forEach(x -> crossings.add(new Crossing(x)));

        // create control sets:
        this.controlSets = this.getControlSets();

        // create curve list:
        this.curveLists = this.createCurveLists();
    }

    public boolean hasEqualSizeControlSetsAndCurveLists(){
        return (curveLists.size() == controlSets.size());
    }

    private ArrayList<ArrayList<Curve>> createCurveLists(){
        curveLists = new ArrayList<>();
        for (ArrayList<KnotNode> knotNodeList : controlSets) {
            ArrayList<Curve> curveList = new ArrayList<>();
            for (int i = 0; i < knotNodeList.size(); i++) {
                int j = i + 1;
                if (i == knotNodeList.size() - 1){
                    j = 0;
                }
                KnotNode knotNode2 = new KnotNode(knotNodeList.get(j));
                curveList.add(new CubicBezier(
                        knotNodeList.get(i),
                        knotNode2
                        ));
            }
            curveLists.add(curveList);
        }
       return curveLists;
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
        Coordinate junction = getNextJunction(node);

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

    public Coordinate getNextJunction(KnotNode node){
        Crossing cross = getCrossingForNode(node);
        // to find correct junction, create a helper vector from midpoint to endpoint of edge
        Vector2D hv1 = new Vector2D(cross.edge.midpoint, cross.edge.c1);

        // calculate the difference in angle from helper vector to norm vector
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
        return junction;
    }

    public Crossing getCrossingForNode(KnotNode node) {
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

    public Crossing getCrossingForEdge(Edge edge) {
        Crossing matchingCrossing = null;
        for (Crossing crossing : crossings){
            if (crossing.edge.equals(edge)){
                matchingCrossing = crossing;
                break;
            }
        }
        return matchingCrossing;
    }

    public KnotNodePair getKnotNodePairFromNode(KnotNode node){
        KnotNodePair nodePair = null;
        ArrayList<KnotNodePair> allNodePairs = getAllNodePairs();
        for(KnotNodePair nP : allNodePairs){
            if(nP.node1.equals(node) || nP.node2.equals(node)){
                nodePair = nP;
            }
        }
        return nodePair;
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


    private ArrayList<KnotNode> runMercat(){
        ArrayList<KnotNode> controlSet = new ArrayList<>();
        KnotNode node = getInitialKnotNode();

        // add starting node
        controlSet.add(node);

        // mark this nodePair as visited
        getKnotNodePairFromNode(node).visit();

        // repeat until the controlSet list is closed (1st element == last element)
        while(controlSet.size() < 2 || !controlSet.get(0).equals(controlSet.get(controlSet.size() - 1))){
            KnotNode nextNode = getNextNode(node);
            if(nextNode == null){
                break;
            }

            controlSet.add(nextNode);
            node = nextNode;
        }

        return controlSet;
    }

    /**
     * Finds the next node based on the current node
     * (basically the core of the mercat algorithm)
     *
     * @param node currentNode in the path
     * @return nextNode in the path
     */
    private KnotNode getNextNode(KnotNode node){
        KnotNode newNode = null;
        Crossing cross = getCrossingForNode(node);
        ArrayList<Edge> incidentEdges = getAdjacentEdges(cross, node);


        // sort incident edges by angle relative to current edge (increasing)
        Coordinate junction = getNextJunction(node);
        Vector2D baseVec = new Vector2D(junction, node.getPos());
        incidentEdges.sort((e1, e2) -> {
            Vector2D edgeVecE1 = new Vector2D(junction, e1.midpoint);
            Vector2D edgeVecE2 = new Vector2D(junction, e2.midpoint);
            Double angleE1 = AngleUtil.getAngleRadiansRescaled(baseVec.angleTo(edgeVecE1));
            Double angleE2 = AngleUtil.getAngleRadiansRescaled(baseVec.angleTo(edgeVecE2));

            if(angleE1 == angleE2){
                return 0;
            }
            return (angleE1 < angleE2) ? -1 : 1;
        });


        // if node is right -> clockwise (increasing angle)
        // if node is left -> counter-clock-wise (decreasing angle)
        if(node.isRightNode()){
            Collections.reverse(incidentEdges);
        }


        // get next unvisited nodePair
        KnotNodePair nodePair = null;

        for (Edge incidentEdge : incidentEdges) {
            Crossing crossing = getCrossingForEdge(incidentEdge);
            // nodePair must be opposite orientation of current node (if right, then left... etc.)
            if(node.isLeftNode() && !crossing.rightNodePair.isVisited()){
                nodePair = crossing.rightNodePair;
            }
            if(node.isRightNode() && !crossing.leftNodePair.isVisited()) {
                nodePair = crossing.leftNodePair;
            }
        }


        if(nodePair != null){

            // get node pointing away from junction
            Vector2D juncVec = new Vector2D(junction, nodePair.node1.getPos());
            Double angle1 = juncVec.angleTo(nodePair.node1.getVector());
            Double angle2 = juncVec.angleTo(nodePair.node2.getVector());
            if(abs(angle1) < abs(angle2)){
                newNode = nodePair.node1;
            } else {
                newNode = nodePair.node2;
            }

            // mark nodePair as visited
            nodePair.visit();
        }

        return newNode;
    }
}
