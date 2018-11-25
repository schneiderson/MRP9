package knotwork.curve;

import knotwork.KnotNode;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.math.Vector2D;
import util.MathUtil;


public class CubicBezier extends Curve{

    private Coordinate anchor1;
    private Coordinate anchor2;
    private Coordinate control1;
    private Coordinate control2;
    private LineSegment lineSegmentAnchorPoints;

    public CubicBezier(KnotNode kn1, KnotNode kn2){
        super(kn1, kn2);

        this.anchor1 = kn1.getPos();
        this.anchor2 = kn2.getPos();
        this.lineSegmentAnchorPoints = new LineSegment(this.anchor1, this.anchor2);

        this.determineControlPoints();
    }

    @Override
    public CubicBezier getCubicBezierPoints() {
        return this;
    }

    private void determineControlPoints() {
        this.control1 = this.knotNode1.getVector().multiply(1.0 * this.knotNode1.getCrossing().edge.getLength()).translate(anchor1);
        this.control2 = this.knotNode2.getVector().multiply(1.0 * this.knotNode2.getCrossing().edge.getLength()).translate(anchor2);

        this.control1 = MathUtil.roundCoordinate(this.control1, 2d);
        this.control2 = MathUtil.roundCoordinate(this.control2, 2d);

        this.control1 = hasIntersection(this.knotNode1,this.anchor1, this.control1, 0.3333333333);
        this.control2 = hasIntersection(this.knotNode2,this.anchor2, this.control2, 0.6666666666);
    }

    /**
     * Checks whether tangent line segment and the line segment perpendicular to the anchorLineSegment
     * at segmentLengthFraction along the anchorLineSegment intersect or not.
     * And returns appropriate control point.
     * @param segmentLengthFraction
     * @return
     */
    private Coordinate hasIntersection(KnotNode knotNode, Coordinate anchor, Coordinate control, double segmentLengthFraction){
        // Calculate point on anchor line segment segmentLengthFraction away from 'anchor1'
        // on the line perpendicular from this point the possible 'new' control point will be located
        // (where is intersects the tangent line segment (vector))
        Coordinate pointOnAnchorLine =
                MathUtil.roundCoordinate(lineSegmentAnchorPoints.pointAlong((segmentLengthFraction)), 2d);

        // multiply angle with -1 because y-axis of output (svg) is inverse
        double angle = -1 * knotNode.getVector().angle(new Vector2D(anchor, pointOnAnchorLine));
        double hypotenuse = anchor.distance(pointOnAnchorLine) / Math.cos(angle);

        // Control prime is the point following the vector from the anchor that intersects
        // with the line perpendicular to anchor line segment on pointOnAnchorLine
        Coordinate controlPrime = knotNode.getVector().multiply(hypotenuse).translate(anchor);

        LineSegment anchorToControl = new LineSegment(anchor, control);

        // To avoid floating point errors when determining 'intersection', multiply the length of line segment from
        // pointOnAnchorLine to controlPrime by 2 (done with use of vector)
        Vector2D utilVector = new Vector2D(pointOnAnchorLine, controlPrime);
        utilVector = utilVector.multiply(2.0);
        LineSegment pointOALThroughControlPrime =
                new LineSegment(pointOnAnchorLine, utilVector.translate(pointOnAnchorLine));

        // Determine intersection between the two line segments
        if(anchorToControl.intersection(pointOALThroughControlPrime) != null)
            return controlPrime;
        return control;
    }

    public Coordinate getAnchor1() { return anchor1; }

    public Coordinate getAnchor2() { return anchor2; }

    public Coordinate getControl1() { return control1; }

    public Coordinate getControl2() { return control2; }

}
