package knotwork;

import knotwork.curve.CubicBezier;
import knotwork.curve.Curve;
import knotwork.curve.OverpassCurve;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.math.Vector2D;
import util.AngleUtil;
import util.MathUtil;

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
        System.out.println(">> Control Sets Created \t size: " + this.controlSets.size());

        // sets over/under for every knotnode(pair)
        determineOverUnderPattern();
        System.out.println(">> Over-under Pattern Determined");

        // create curve list:
        this.curveLists = this.createCurveLists();
        System.out.println(">> Curve Lists Created");

        // create list with curves that are overpasses (go on top at crossing)
        this.overpassCurveList = this.createOverpassCurveList();
        System.out.println(">> Overpass Curve List Created");
    }

    private ArrayList<OverpassCurve> createOverpassCurveList() {
        if (curveLists == null) return null;

        ArrayList<OverpassCurve> overpassCurveList = new ArrayList<>();
        for (int j = 0; j < curveLists.size(); j++) {
            ArrayList<Curve> curveList = curveLists.get(j);
            for (int i = 0; i < curveList.size(); i++) {
                Curve curve = curveList.get(i);

                if (curve.knotNode1.getOverpass()) {
                    boolean firstSegmentIsPartCurve = false;

                    CubicBezier cb = (CubicBezier) curve;
                    CubicBezier cubicBezierSegment1;

                    if (cb.isPartCurve()) firstSegmentIsPartCurve = true;

                    cubicBezierSegment1 = cb.segmentCurve(0, 0.2);

                    // Get preceding curve:
                    Curve precedingCurve;
                    if (i == 0) {
                        precedingCurve = curveList.get(curveList.size() - 1);
                    } else {
                        precedingCurve = curveList.get(i - 1);
                    }
                    if (firstSegmentIsPartCurve) i++;

                    // Calculate second bezier segment:
                    CubicBezier cb2 = (CubicBezier) precedingCurve;
                    CubicBezier cubicBezierSegment2;
                    if (cubicBezierSegment1.getAnchor1().equals2D(cb2.getAnchor2(), 0.001)) {
                        cubicBezierSegment2 = cb2.segmentCurve(0.8, 1); // if anchor2 cb2 == anchor1 of cb1
                    } else{
                        cubicBezierSegment2 = cb2.segmentCurve(0.0, 0.2);
                    }

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
            Boolean x = controlSet.get(0).getPrependicularKnotNodePair().getOverpass();
            if (x == null || x)
                overpassValue = false;

            for (KnotNode knotNode : controlSet) {
                knotNode.setOverpass(overpassValue);
                overpassValue = !overpassValue;
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

                int vectorOrientation = checkVectorOrientation(knotNodeList.get(i), knotNode2);

                if (vectorOrientation == 1) { // vector orientation is parallel
                    Curve[] c = createPointyCurveFromParallel(knotNodeList.get(i), knotNode2);
                    curveList.add(c[0]);
                    curveList.add(c[1]);
                } else if (vectorOrientation == 2) { // vector orientation is diverging
                    Curve[] c = createPointyCurveFromDiverging(knotNodeList.get(i), knotNode2);
                    curveList.add(c[0]);
                    curveList.add(c[1]);
                } else {
                    curveList.add(new CubicBezier(
                            knotNodeList.get(i),
                            knotNode2
                    ));
                }
            }
            curveLists.add(curveList);
        }
        return curveLists;
    }

    /**
     * check if knot node vectors are parallel or diverging:
     *
     * @param knotNode1 first knot node
     * @param knotNode2 second knot node
     * @return int value representing type of orientation
     */
    private int checkVectorOrientation(KnotNode knotNode1, KnotNode knotNode2) {
        // if the angle is 0 (or close to 0) then the vectors are parallel
        double angle = knotNode1.getVector().angle(knotNode2.getVector());
        double epsilon = 0.01;

        // inverse vector test:
        Vector2D v1Reverse = knotNode1.getVector().rotateByQuarterCircle(2);
        Vector2D v2Reverse = knotNode2.getVector().rotateByQuarterCircle(2);
        LineSegment lineSegment1 = new LineSegment(knotNode1.getPos(), v1Reverse.multiply(1000d).translate(knotNode1.getPos()));
        LineSegment lineSegment2 = new LineSegment(knotNode2.getPos(), v2Reverse.multiply(1000d).translate(knotNode2.getPos()));
        Coordinate intersectionReverseVectors = lineSegment1.intersection(lineSegment2);

        if (angle > -epsilon && angle < epsilon) {
            // parallel knot node vectors
            return 1;
        } else if (!(angle > (Math.PI - epsilon) && angle < (Math.PI + epsilon)) && intersectionReverseVectors != null) {
            // diverging knot node vectors
            return 2;
        }
        return 0;
    }

    private Curve[] createPointyCurveFromDiverging(KnotNode knotNode1, KnotNode knotNode2) {
        // Calculate the curve tip
        // Rotate vectors 180 degrees, make line segments, and find their intersection point:
        Vector2D v1Reverse = knotNode1.getVector().rotateByQuarterCircle(2);
        Vector2D v2Reverse = knotNode2.getVector().rotateByQuarterCircle(2);
        LineSegment ls1 = new LineSegment(knotNode1.getPos(), v1Reverse.multiply(1000d).translate(knotNode1.getPos()));
        LineSegment ls2 = new LineSegment(knotNode2.getPos(), v2Reverse.multiply(1000d).translate(knotNode2.getPos()));
        Coordinate intersectionReverseVectors = ls1.intersection(ls2);

        // Calculate point on line segment between both knot nodes, from where it will get extended to the curve tip
        // line segment between knot nodes:
        LineSegment lsKnotNodes = new LineSegment(knotNode1.getPos(), knotNode2.getPos());
        // add vectors two get the vector splitting them:
        Vector2D splitVector = knotNode1.getVector().add(knotNode2.getVector()).normalize();
        LineSegment splitLS = new LineSegment(intersectionReverseVectors, splitVector.multiply(1000d).translate(intersectionReverseVectors));
        Coordinate intersectionPointOnKnotNodeLS = splitLS.intersection(lsKnotNodes);

        // given a factor and avg edge length determine pointy curve tip:
        double averageOfEdgeLengths = (knotNode1.getCrossing().edge.getLength() + knotNode2.getCrossing().edge.getLength()) / 2.0;
        double curveTipLengthFactor = 0.75;
        Coordinate curveTip = splitVector.multiply(curveTipLengthFactor * averageOfEdgeLengths).translate(intersectionPointOnKnotNodeLS);

        return getPartCurveFromDiverging(knotNode1, knotNode2, curveTip);
    }


    private Curve[] getPartCurveFromDiverging(KnotNode knotNode1, KnotNode knotNode2, Coordinate curveTip) {
        Coordinate control11;
        Coordinate control12;
        Coordinate control21;
        Coordinate control22;

        Coordinate knotNode1Coord = knotNode1.getPos();
        Coordinate knotNode2Coord = knotNode2.getPos();

        LineSegment knotNode1ToCurveTip = new LineSegment(knotNode1Coord, curveTip);
        LineSegment knotNode2ToCurveTip = new LineSegment(knotNode2Coord, curveTip);

        Vector2D knotNode1ToCurveTipVector = new Vector2D(knotNode1Coord, curveTip);
        Vector2D knotNode2ToCurveTipVector = new Vector2D(knotNode2Coord, curveTip);

        double fraction = 1.0 / 3.0;
        Coordinate oneThirdKnotNode1 = knotNode1ToCurveTip.pointAlong(fraction);
        Coordinate twoThirdsKnotNode1 = knotNode1ToCurveTip.pointAlong(1 - fraction);
        Coordinate oneThirdKnotNode2 = knotNode2ToCurveTip.pointAlong(fraction);
        Coordinate twoThirdsKnotNode2 = knotNode2ToCurveTip.pointAlong(1 - fraction);

        // Which way to rotate?
        Vector2D plus90 = knotNode1ToCurveTipVector.rotate(0.5 * Math.PI);
        Vector2D minus90 = knotNode1ToCurveTipVector.rotate(-0.5 * Math.PI);
        Coordinate intersectionPlus = knotNode2ToCurveTip.intersection(
                new LineSegment(oneThirdKnotNode1, plus90.multiply(20.0).translate(oneThirdKnotNode1))
        );

        double offsetFactor = 0.25;
        double offsetLengthControlPointsKnotNode1 = offsetFactor * knotNode1ToCurveTip.getLength();
        double offsetLengthControlPointsKnotNode2 = offsetFactor * knotNode2ToCurveTip.getLength();

        if (intersectionPlus == null){ // plus 90 degree rotate did not intersect knot node line segment
            control11 = plus90.normalize().multiply(offsetLengthControlPointsKnotNode1).translate(oneThirdKnotNode1);
            control12 = plus90.normalize().multiply(offsetLengthControlPointsKnotNode1).translate(twoThirdsKnotNode1);
            control21 = knotNode2ToCurveTipVector.rotate(-0.5 * Math.PI).normalize()
                    .multiply(offsetLengthControlPointsKnotNode2).translate(oneThirdKnotNode2);
            control22 = knotNode2ToCurveTipVector.rotate(-0.5 * Math.PI).normalize()
                    .multiply(offsetLengthControlPointsKnotNode2).translate(twoThirdsKnotNode2);
            return new Curve[]{
                    new CubicBezier(knotNode1, knotNode2, knotNode1Coord, control11, curveTip, control12, true),
                    new CubicBezier(knotNode1, knotNode2, knotNode2Coord, control21, curveTip, control22, true)
            };
        }

        control11 = minus90.normalize().multiply(offsetLengthControlPointsKnotNode1).translate(oneThirdKnotNode1);
        control12 = minus90.normalize().multiply(offsetLengthControlPointsKnotNode1).translate(twoThirdsKnotNode1);
        control21 = knotNode2ToCurveTipVector.rotate(0.5 * Math.PI).normalize()
                .multiply(offsetLengthControlPointsKnotNode2).translate(oneThirdKnotNode2);
        control22 = knotNode2ToCurveTipVector.rotate(0.5 * Math.PI).normalize()
                .multiply(offsetLengthControlPointsKnotNode2).translate(twoThirdsKnotNode2);
        return new Curve[]{
                new CubicBezier(knotNode1, knotNode2, knotNode1Coord, control11, curveTip, control12, true),
                new CubicBezier(knotNode1, knotNode2, knotNode2Coord, control21, curveTip, control22, true)
        };
    }


    private Curve[] createPointyCurveFromParallel(KnotNode knotNode1, KnotNode knotNode2) {
        // Parallel vectors
        LineSegment ls = new LineSegment(knotNode1.getPos(), knotNode2.getPos());
        Coordinate curveTip = knotNode1
                .getVector()
                .multiply(.75 * knotNode1.getCrossing().edge.getLength())
                .translate(ls.midPoint());

        CubicBezier curve1 = halfCurveFromParallelVectors(knotNode1.getPos(), knotNode1.getVector(), curveTip, knotNode1, knotNode2);
        CubicBezier curve2 = halfCurveFromParallelVectors(knotNode2.getPos(), knotNode2.getVector(), curveTip, knotNode1, knotNode2);

        return new Curve[]{curve1, curve2};
    }

    private CubicBezier halfCurveFromParallelVectors(Coordinate knotNodeCoord, Vector2D knotNodeVector, Coordinate curveTip, KnotNode kn1, KnotNode kn2) {
        Coordinate control1;
        Coordinate control2;
        LineSegment knotNodeToCurveTip = new LineSegment(knotNodeCoord, curveTip);
        Vector2D knotNodeToCurveTipVector = new Vector2D(knotNodeCoord, curveTip);
        knotNodeToCurveTipVector.multiply(2.0);
        LineSegment knotNodeVectorExtension = new LineSegment(knotNodeCoord, knotNodeVector.multiply(knotNodeToCurveTip.getLength()).translate(knotNodeCoord));

        // find point one third along the knot node to curve tip line
        double fraction = 1.0 / 3.0;
        Coordinate oneThirdPoint = knotNodeToCurveTip.pointAlong(fraction);
        Coordinate twoThirdsPoint = knotNodeToCurveTip.pointAlong(fraction * 2.0);

        // try turning 90 degrees each way and check which way intersects with knot node vector extension line
        LineSegment plus90degrees = new LineSegment(oneThirdPoint, knotNodeToCurveTipVector.rotate(0.5 * Math.PI).translate(oneThirdPoint));
        LineSegment minus90degrees = new LineSegment(oneThirdPoint, knotNodeToCurveTipVector.rotate(-0.5 * Math.PI).translate(oneThirdPoint));
        Coordinate plus90Intersection = plus90degrees.intersection(knotNodeVectorExtension);
        Coordinate minus90Intersection = minus90degrees.intersection(knotNodeVectorExtension);

        if (plus90Intersection == null && minus90Intersection == null) {
            throw new IllegalArgumentException("Unable to find intersection needed for pointy curve with parallel vectors!");
        }

        if (plus90Intersection != null) {
            control1 = plus90Intersection;

            LineSegment twoThirds90Degrees = new LineSegment(twoThirdsPoint,
                    knotNodeToCurveTipVector.rotate(0.5 * Math.PI).translate(twoThirdsPoint));
            LineSegment lsPar = new LineSegment(control1, knotNodeToCurveTipVector.translate(control1));

            control2 = twoThirds90Degrees.intersection(lsPar);

        } else {
            control1 = minus90Intersection;

            LineSegment twoThirds90Degrees = new LineSegment(twoThirdsPoint,
                    knotNodeToCurveTipVector.rotate(-0.5 * Math.PI).translate(twoThirdsPoint));
            LineSegment lsPar = new LineSegment(control1, knotNodeToCurveTipVector.translate(control1));

            control2 = twoThirds90Degrees.intersection(lsPar);
        }

        return new CubicBezier(kn1, kn2, knotNodeCoord, control1, curveTip, control2, true);
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
            allNodePairs.add(crossing.leftNodePair);
            allNodePairs.add(crossing.rightNodePair);
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
        // to find correct junction, create a helper vector from midpoint to endpoint of edge
        Vector2D hv1 = new Vector2D(cross.edge.midpoint, cross.edge.c1);

        // calculate the difference in angle from helper vector to norm vector
        Double diffAngle1 = cross.normVector.angleTo(hv1);
        Double diffAngleNode = cross.normVector.angleTo(node.getVector());

        Coordinate junction;
        // if difference in angle of node vector and helper vector have the same sign they point in the "same" direction
        if (diffAngle1 * diffAngleNode >= 0) {
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
            if (crossing.rightNodePair.contains(node)
                    || crossing.leftNodePair.contains(node)) {
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
        getKnotNodePairFromNode(node).visit();

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
            if (node.isLeftNode()) {
                nodePair = crossing.rightNodePair;
            }
            if (node.isRightNode()) {
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

            // get node pointing away from junction
            Vector2D juncVec = new Vector2D(junction, nodePair.node1.getPos());
            Double angle1 = juncVec.angleTo(nodePair.node1.getVector());
            Double angle2 = juncVec.angleTo(nodePair.node2.getVector());
            if (abs(angle1) < abs(angle2)) {
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
