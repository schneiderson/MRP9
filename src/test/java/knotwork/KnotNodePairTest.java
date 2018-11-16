package knotwork;

import org.locationtech.jts.geom.Coordinate;


public class KnotNodePairTest {

    public static void main(String[] args){
        testAngles();

    }


    private static void testAngles(){
        KnotNode kn = new KnotNode(new Coordinate(0,0), new Coordinate(1,0), true);
        KnotNodePair knp = new KnotNodePair(kn);

        System.out.println("Original node vector angle: " + knp.node1.getAngleDegree(false));
        System.out.println("Opposite node vector angle: " + knp.node2.getAngleDegree(false));

    }
}

