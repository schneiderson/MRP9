package knotwork;

import knotwork.curve.CubicBezier;
import knotwork.curve.Curve;
import knotwork.curve.OverpassCurve;
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
    public ArrayList<OverpassCurve> overpassCurveList;


    public KnotworkGraph(ArrayList<Coordinate> nodes, ArrayList<Edge> edges) {
        this.nodes = nodes;
        this.edges = edges;

        // create crossings for each edge
        edges.forEach(x -> crossings.add(new Crossing(x)));

        // create control sets:
        this.controlSets = this.getControlSets();

        // sets over/under for every knotnode(pair)
        determineOverUnderPattern();

        // create curve list:
        this.curveLists = this.createCurveLists();

        // create list with curves that are overpasses (go on top at crossing)
        this.overpassCurveList = this.createOverpassCurveList();
    }

    private ArrayList<OverpassCurve> createOverpassCurveList() {
        if (curveLists == null) return null;

        ArrayList<OverpassCurve> overpassCurveList = new ArrayList<>();
        for (int j = 0; j < curveLists.size(); j++) {
            ArrayList<Curve> curveList = curveLists.get(j);
            for (int i = 0; i < curveList.size(); i++) {
                Curve curve = curveList.get(i);

                if (curve.knotNode1.getOverpass()) {
                    CubicBezier cb = (CubicBezier) curve;
                    CubicBezier cubicBezierSegment1 = cb.segmentCurve(0, 0.2);

                    // get preceding curve:
                    Curve precedingCurve;
                    if (i == 0) {
                        precedingCurve = curveList.get(curveList.size() - 1);
                    } else {
                        precedingCurve = curveList.get(i - 1);
                    }

                    CubicBezier cb2 = (CubicBezier) precedingCurve;
                    CubicBezier cubicBezierSegment2 = cb2.segmentCurve(0.8, 1);

                    // add curves to list:
                    overpassCurveList.add(new OverpassCurve(cubicBezierSegment1, cubicBezierSegment2, j));
                }

            }
        }

        return overpassCurveList;
    }

    private void determineOverUnderPattern() {
        if (controlSets == null) return;

        for (ArrayList<KnotNode> controlSet : controlSets) {
            // alternating overpass value
            boolean overpassValue = true;

            // determine overpass value for  first knotnode in control set:
            KnotNodePair perpendicularPair = controlSet.get(0).getPrependicularKnotNodePair();
            Boolean x = null;
            if(perpendicularPair != null){
                x = perpendicularPair.getOverpass();
            }
            if (x == null || x)
                overpassValue = false;

            for (KnotNode knotNode : controlSet) {
                knotNode.setOverpass(overpassValue);
                if (!knotNode.getCrossing().hasBreakPoint()) {
                    overpassValue = !overpassValue;
                }
            }
        }
    }

    public boolean hasEqualSizeControlSetsAndCurveLists() {
        return (curveLists.size() == controlSets.size());
    }

    private ArrayList<ArrayList<Curve>> createCurveLists() {
        curveLists = new ArrayList<>();
        for (ArrayList<KnotNode> knotNodeList : controlSets) {
            ArrayList<Curve> curveList = new ArrayList<>();
            for (int i = 0; i < knotNodeList.size(); i++) {
                int j = i + 1;
                if (i == knotNodeList.size() - 1) {
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

    public KnotNode getInitialKnotNode() {
        ArrayList<KnotNodePair> unvisited = getUnvisitedNodePairs();
        if (unvisited.size() > 0) {
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
            if(crossing.hasBreakPoint()){
                allNodePairs.add(crossing.negMetaPair);
                allNodePairs.add(crossing.posMetaPair);
            } else {
                allNodePairs.add(crossing.leftNodePair);
                allNodePairs.add(crossing.rightNodePair);
            }
        }
        return allNodePairs;
    }


    public ArrayList<KnotNodePair> getUnvisitedNodePairs() {
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
            if (edge.equals(cross.edge)) {
                continue;
            }
            if (edge.isIncidentToVertex(junction)) {
                incidentEdges.add(edge);
            }
        }
        return incidentEdges;
    }

    public Coordinate getNextJunction(KnotNode node) {
        Crossing cross = getCrossingForNode(node);

        Coordinate junction;

        if(cross.breakpoint == 2){
            double dist1 = node.getPos().distance(cross.edge.c1);
            double dist2 = node.getPos().distance(cross.edge.c2);

            if(dist1 < dist2){
                junction = cross.edge.c1;
            } else {
                junction = cross.edge.c2;
            }

        } else {
            // to find correct junction, create a helper vector from midpoint to endpoint of edge
            Vector2D hv1 = new Vector2D(cross.edge.midpoint, cross.edge.c1);

            // calculate the difference in angle from helper vector to norm vector
            Double diffAngle1 = cross.normVector.angleTo(hv1);
            Double diffAngleNode = cross.normVector.angleTo(node.getVector());

            // if difference in angle of node vector and helper vector have the same sign they point in the "same" direction
            if (diffAngle1 * diffAngleNode >= 0) { // same sign
                junction = cross.edge.c1;
            } else { // different sign
                junction = cross.edge.c2;
            }
        }

        return junction;
    }

    public Crossing getCrossingForNode(KnotNode node) {
        Crossing matchingCrossing = null;
        for (Crossing crossing : crossings) {
            if ((crossing.rightNodePair != null && crossing.rightNodePair.contains(node))
                    || (crossing.leftNodePair != null && crossing.leftNodePair.contains(node))
                    || (crossing.posMetaPair != null && crossing.posMetaPair.contains(node))
                    || (crossing.negMetaPair != null && crossing.negMetaPair.contains(node))
            ) {
                matchingCrossing = crossing;
                break;
            }
        }
        return matchingCrossing;
    }

    public Crossing getCrossingForEdge(Edge edge) {
        Crossing matchingCrossing = null;
        for (Crossing crossing : crossings) {
            if (crossing.edge.equals(edge)) {
                matchingCrossing = crossing;
                break;
            }
        }
        return matchingCrossing;
    }

    public KnotNodePair getKnotNodePairFromNode(KnotNode node) {
        KnotNodePair nodePair = null;
        ArrayList<KnotNodePair> allNodePairs = getAllNodePairs();
        for (KnotNodePair nP : allNodePairs) {
            if (nP.contains(node)) {
                nodePair = nP;
            }
        }
        return nodePair;
    }

    public ArrayList<ArrayList<KnotNode>> getControlSets() {
        ArrayList<ArrayList<KnotNode>> controlSets = new ArrayList<>();
        while (getUnvisitedNodePairs().size() > 0) {
            ArrayList<KnotNode> controlSet = runMercat();
            if (controlSet != null && controlSet.size() > 0) {
                controlSets.add(controlSet);
            }
        }
        return controlSets;
    }


    private ArrayList<KnotNode> runMercat() {
        ArrayList<KnotNode> controlSet = new ArrayList<>();
        KnotNode initialNode = getInitialKnotNode();
        KnotNode node = initialNode;

        // add starting node
        controlSet.add(node);

        // mark this nodePair as visited
        getKnotNodePairFromNode(initialNode).visit();

        // repeat until the no further nodes found
        while (true) {
            KnotNode nextNode = getNextNode(initialNode, node);
            if (nextNode == null) {
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
    private KnotNode getNextNode(KnotNode initialNode, KnotNode node) {
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

            if (angleE1 == angleE2) {
                return 0;
            }
            return (angleE1 < angleE2) ? -1 : 1;
        });


        // if node is right -> clockwise (increasing angle)
        // if node is left -> counter-clock-wise (decreasing angle)
        if (node.isLeftNode()) {
            Collections.reverse(incidentEdges);
        }


        // get next unvisited nodePair
        KnotNodePair nodePair = null;

        for (Edge incidentEdge : incidentEdges) {
            Crossing crossing = getCrossingForEdge(incidentEdge);
            // nodePair must be opposite orientation of current node (if right, then left... etc.)
            if(crossing.hasBreakPoint()){
                nodePair = crossing.getMetaPointPair(node, junction);
            } else if (node.isLeftNode()) {
                nodePair = crossing.rightNodePair;
            } else if (node.isRightNode()) {
                nodePair = crossing.leftNodePair;
            }

            if (nodePair.contains(initialNode)) {
                nodePair = null;
                break;
            }
            if (nodePair.isVisited()) {
                nodePair = null;
            } else {
                break;
            }
        }

        if (nodePair != null) {
            if(nodePair.getCrossing().hasBreakPoint() && nodePair.getCrossing().breakpoint == 2){
                // get node pointing away from previous node
                Double angle1 = node.getVector().angleTo(nodePair.node1.getVector());
                Double angle2 = node.getVector().angleTo(nodePair.node2.getVector());
                if (abs(angle1) < abs(angle2)) {
                    newNode = nodePair.node1;
                } else {
                    newNode = nodePair.node2;
                }

            } else {
                // get node pointing away from junction
                Vector2D juncVec = new Vector2D(junction, nodePair.node1.getPos());
                Double angle1 = juncVec.angleTo(nodePair.node1.getVector());
                Double angle2 = juncVec.angleTo(nodePair.node2.getVector());
                if (abs(angle1) < abs(angle2)) {
                    newNode = nodePair.node1;
                } else {
                    newNode = nodePair.node2;
                }
            }
            // mark nodePair as visited
            nodePair.visit();
        }

        return newNode;
    }
}
