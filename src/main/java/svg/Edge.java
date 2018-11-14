package svg;

import org.locationtech.jts.geom.Coordinate;

public class Edge {
    public Coordinate c1;
    public Coordinate c2;

    public Edge(Coordinate c1, Coordinate c2){
        this.c1 = c1;
        this.c2 = c2;
    }

    public Boolean equals(Edge other){
        if( (c1.equals(other.c1) && c2.equals(other.c2)) || (c1.equals(other.c2) && c2.equals(other.c1)) ){
            return true;
        }
        return false;
    }

    public String toString(){
        return "C1: " + c1.toString() + ", C2: " + c2.toString();
    }
}
