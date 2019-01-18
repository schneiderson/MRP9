package knotwork;

import org.locationtech.jts.geom.Coordinate;

public class Edge {
    public Coordinate c1;
    public Coordinate c2;
    public Coordinate midpoint;
    public int breakpoint = 0;

    public Edge(Coordinate c1, Coordinate c2) {
        this.c1 = c1;
        this.c2 = c2;
        this.midpoint = getMidpoint();
    }

    public Edge(Coordinate c1, Coordinate c2, int breakpoint)
    {
        this.c1 = c1;
        this.c2 = c2;
        this.midpoint = getMidpoint();
        if (breakpoint == 1 || breakpoint == 2)
        {this.breakpoint = breakpoint;}
    }

    private Coordinate getMidpoint() {
        return new Coordinate(Math.round((c1.x + c2.x) / 2d), Math.round((c1.y + c2.y) / 2d));
    }

    public boolean equals(Edge other) {
        if ((c1.equals(other.c1) && c2.equals(other.c2)) || (c1.equals(other.c2) && c2.equals(other.c1))) {
            return true;
        }
        return false;
    }

    public boolean equals(Edge other, Double tolerance){
        if((c1.distance(other.c1) <= tolerance && c2.distance(other.c2) <= tolerance)
                || (c1.distance(other.c2) <= tolerance && c2.distance(other.c1) <= tolerance)){
            System.out.println("filtering out duplicate based on tolerance: " + tolerance);
            System.out.println("Current, C1: " + c1 + " C2: "+ c2 );
            System.out.println("Other, C1: " + other.c1 + " C2: "+ other.c2 );
            System.out.println("----");
            return true;
        }
        return false;
    }

    public boolean isIncidentToVertex(Coordinate node){
        return (c1.equals(node) || c2.equals(node));
    }

    public String toString() {
        return "C1: " + c1.toString() + ", C2: " + c2.toString() + ", M: " + midpoint.toString();
    }

    public Double getLength() {
        return c1.distance(c2);
    }

    public boolean hasBreakpoint()
    {
        return (this.breakpoint == 1 || this.breakpoint == 2);
    }
}
