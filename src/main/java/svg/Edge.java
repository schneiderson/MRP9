package svg;

import org.locationtech.jts.geom.Coordinate;

public class Edge {
    public Coordinate c1;
    public Coordinate c2;
    public Coordinate midpoint;

    public Edge(Coordinate c1, Coordinate c2){
        this.c1 = c1;
        this.c2 = c2;
        this.midpoint = getCrossing();
    }

    private Coordinate getCrossing()
    {
        return new Coordinate(Math.round((this.c1.x + this.c2.x) / 2d), Math.round((this.c1.y + this.c2.y) / 2d));
    }

    public Boolean equals(Edge other){
        if( (c1.equals(other.c1) && c2.equals(other.c2)) || (c1.equals(other.c2) && c2.equals(other.c1)) ){
            return true;
        }
        return false;
    }

    public String toString(){
        return "C1: " + c1.toString() + ", C2: " + c2.toString() + ", M: " + midpoint.toString();
    }
}
