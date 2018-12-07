package knotwork.curve;

import knotwork.KnotNode;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.math.Vector2D;
import util.MathUtil;


public class CubicBezier extends Curve {

    private Coordinate anchor1;
    private Coordinate anchor2;
    private Coordinate control1;
    private Coordinate control2;
    private LineSegment lineSegmentAnchorPoints;

    public CubicBezier(KnotNode kn1, KnotNode kn2) {
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

        this.control1 = hasIntersection(this.knotNode1, this.anchor1, this.control1, 0.3333333333);
        this.control2 = hasIntersection(this.knotNode2, this.anchor2, this.control2, 0.6666666666);
    }

    /**
     * Checks whether tangent line segment and the line segment perpendicular to the anchorLineSegment
     * at segmentLengthFraction along the anchorLineSegment intersect or not.
     * And returns appropriate control point.
     *
     * @param segmentLengthFraction
     * @return
     */
    private Coordinate hasIntersection(KnotNode knotNode, Coordinate anchor, Coordinate control, double segmentLengthFraction) {
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
        if (anchorToControl.intersection(pointOALThroughControlPrime) != null)
            return controlPrime;
        return control;
    }

    // Creates an bezier curve as segment of original curve, from t0 into the curve to t1,
    // where 0 <= t0 <= t1 <= 1
    public CubicBezier segmentCurve(double t0, double t1){
        CubicBezier cubicBezier = new CubicBezier(super.knotNode1, super.knotNode2);

        double u0 = 1 - t0;
        double u1 = 1 - t1;

        double x1  = this.anchor1.x;
        double y1  = this.anchor1.y;
        double bx1 = this.control1.x;
        double by1 = this.control1.y;
        double bx2 = this.control2.x;
        double by2 = this.control2.y;
        double x2  = this.anchor2.x;
        double y2  = this.anchor2.y;

        double qxa =  x1*u0*u0 + bx1*2*t0*u0 + bx2*t0*t0;
        double qxb =  x1*u1*u1 + bx1*2*t1*u1 + bx2*t1*t1;
        double qxc = bx1*u0*u0 + bx2*2*t0*u0 +  x2*t0*t0;
        double qxd = bx1*u1*u1 + bx2*2*t1*u1 +  x2*t1*t1;
        double qya =  y1*u0*u0 + by1*2*t0*u0 + by2*t0*t0;
        double qyb =  y1*u1*u1 + by1*2*t1*u1 + by2*t1*t1;
        double qyc = by1*u0*u0 + by2*2*t0*u0 +  y2*t0*t0;
        double qyd = by1*u1*u1 + by2*2*t1*u1 +  y2*t1*t1;
        double xa = qxa*u0 + qxc*t0;
        double xb = qxa*u1 + qxc*t1;
        double xc = qxb*u0 + qxd*t0;
        double xd = qxb*u1 + qxd*t1;
        double ya = qya*u0 + qyc*t0;
        double yb = qya*u1 + qyc*t1;
        double yc = qyb*u0 + qyd*t0;
        double yd = qyb*u1 + qyd*t1;

        cubicBezier.anchor1.setX(xa);
        cubicBezier.anchor1.setY(ya);

        cubicBezier.control1.setX(xb);
        cubicBezier.control1.setY(yb);

        cubicBezier.control2.setX(xc);
        cubicBezier.control2.setY(yc);

        cubicBezier.anchor2.setX(xd);
        cubicBezier.anchor2.setY(yd);

        return cubicBezier;
    }


    public Coordinate getAnchor1() {
        return anchor1;
    }

    public Coordinate getAnchor2() {
        return anchor2;
    }

    public Coordinate getControl1() {
        return control1;
    }

    public Coordinate getControl2() {
        return control2;
    }

}
