package knotwork;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.math.Vector2D;

public class BreakPointTest
{
    public static void main(String args[])
    {
        Edge edge = new Edge(new Coordinate(0,0), new Coordinate(0,4));
        Crossing crossing = new Crossing(edge);

        System.out.println(crossing.normVector);

        //crossing.setBreakpoint(2);
        //BaseNode meta = crossing.getMetaPoint(new BaseNode(new Coordinate(2,0), new Vector2D(0,1)));

        //System.out.println(meta);
    }
}
