package knotwork;

import org.locationtech.jts.geom.Coordinate;


public class KnotNodeTest {

    public static void main(String[] args){
        // test the angle calculation
        testAngles(false);
        testAngles(true);

    }


    private static void testAngles(Boolean norm){
        System.out.println("\n-------------");
        System.out.println("Test Angles");
        System.out.println("-------------");

        // 0 degree vector
        KnotNode node = new KnotNode(new Coordinate(0, 0), new Coordinate(1,0), true);
        System.out.println("Angle Radians: " + node.getAngleRadians(norm));
        System.out.println("Angle Degree: " + node.getAngleDegree(norm));

        // 45 degree vector
        node = new KnotNode(new Coordinate(0, 0), new Coordinate(1,1), true);
        System.out.println("Angle Radians: " + node.getAngleRadians(norm));
        System.out.println("Angle Degree: " + node.getAngleDegree(norm));

        // 90 degree vector
        node = new KnotNode(new Coordinate(0, 0), new Coordinate(0,1), true);
        System.out.println("Angle Radians: " + node.getAngleRadians(norm));
        System.out.println("Angle Degree: " + node.getAngleDegree(norm));

        // 135 degree vector
        node = new KnotNode(new Coordinate(0, 0), new Coordinate(-1,1), true);
        System.out.println("Angle Radians: " + node.getAngleRadians(norm));
        System.out.println("Angle Degree: " + node.getAngleDegree(norm));

        // 180 degree vector
        node = new KnotNode(new Coordinate(0, 0), new Coordinate(-1,0), true);
        System.out.println("Angle Radians: " + node.getAngleRadians(norm));
        System.out.println("Angle Degree: " + node.getAngleDegree(norm));

        // 225 degree vector
        node = new KnotNode(new Coordinate(0, 0), new Coordinate(-1,-1), true);
        System.out.println("Angle Radians: " + node.getAngleRadians(norm));
        System.out.println("Angle Degree: " + node.getAngleDegree(norm));

        // 270 degree vector
        node = new KnotNode(new Coordinate(0, 0), new Coordinate(0,-1), true);
        System.out.println("Angle Radians: " + node.getAngleRadians(norm));
        System.out.println("Angle Degree: " + node.getAngleDegree(norm));

        // 315 degree vector
        node = new KnotNode(new Coordinate(0, 0), new Coordinate(1,-1), true);
        System.out.println("Angle Radians: " + node.getAngleRadians(norm));
        System.out.println("Angle Degree: " + node.getAngleDegree(norm));

        // 360/0 degree vector
        node = new KnotNode(new Coordinate(0, 0), new Coordinate(0,0), true);
        System.out.println("Angle Radians: " + node.getAngleRadians(norm));
        System.out.println("Angle Degree: " + node.getAngleDegree(norm));

    }
}

