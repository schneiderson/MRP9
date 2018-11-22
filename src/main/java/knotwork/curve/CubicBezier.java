package knotwork.curve;

import knotwork.Edge;
import knotwork.KnotNode;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.math.Vector2D;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class CubicBezier extends Curve{

    private Coordinate anchor1;
    private Coordinate anchor2;
    private Coordinate control1;
    private Coordinate control2;

    public CubicBezier(KnotNode kn1, KnotNode kn2){
        super(kn1, kn2);

        this.anchor1 = kn1.getPos();
        this.anchor2 = kn2.getPos();

        this.determineControlPoints();
    }

    @Override
    public CubicBezier getCubicBezierPoints() {
        return this;
    }

    private void determineControlPoints() {
        this.control1 = this.knotNode1.getVector().multiply(1.0 * this.knotNode1.getCrossing().edge.getLength()).translate(knotNode1.getPos());
        this.control2 = this.knotNode2.getVector().multiply(1.0 * this.knotNode2.getCrossing().edge.getLength()).translate(knotNode2.getPos());
    }

    public Coordinate getAnchor1() { return anchor1; }

    public Coordinate getAnchor2() { return anchor2; }

    public Coordinate getControl1() { return control1; }

    public Coordinate getControl2() { return control2; }
}
