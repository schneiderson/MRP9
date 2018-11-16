package knotwork;

import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;

import static java.lang.Math.PI;

public class CrossingTest {

    public static void main(String args[]){

        // create edge
        Edge edge = new Edge(new Coordinate(0,0), new Coordinate(1,1));
        Crossing crossing = new Crossing(edge);


        System.out.println(Angle.toDegrees(2*PI +crossing.normVector.angle()));
        System.out.println(crossing.leftNodePair.node1.getAngleDegree(false));
        System.out.println(crossing.leftNodePair.node2.getAngleDegree(false));
        System.out.println(crossing.rightNodePair.node1.getAngleDegree(false));
        System.out.println(crossing.rightNodePair.node2.getAngleDegree(false));




    }

}
