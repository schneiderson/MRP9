package knotwork;

import org.locationtech.jts.geom.Coordinate;
import svg.SVGUtil;

import java.util.ArrayList;

public class Knotwork {

    public static void main(String[] args){

        // start with reading the edges from the svg
        String path = "test.svg";
        SVGUtil svgutil = new SVGUtil(null, null);
        svgutil.readFromSvg(path);

        ArrayList<Edge> edges = svgutil.edges;
        ArrayList<Coordinate> nodes = svgutil.nodes;

        ArrayList<Crossing> crossings = new ArrayList<>();

        edges.forEach(x -> { crossings.add(new Crossing(x)); });

        crossings.forEach(x -> { System.out.println(
                "Edge: " + x.edge +  " " +
                "Crossing at: " + x.pos + " norm vector angle: " + x.getNormVectorAngleDeg(false)); });





    }

}
