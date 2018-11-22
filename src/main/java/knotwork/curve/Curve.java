package knotwork.curve;

import knotwork.KnotNode;
import org.locationtech.jts.geom.Coordinate;

import java.util.List;

public abstract class Curve {

    protected KnotNode knotNode1;
    protected KnotNode knotNode2;

    public Curve(KnotNode kn1, KnotNode kn2){
        this.knotNode1 = kn1;
        this.knotNode2 = kn2;
    }

    public abstract CubicBezier getCubicBezierPoints();

}
