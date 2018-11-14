package svg;

import org.locationtech.jts.geom.Coordinate;
import svg.Edge;
import svg.SVGUtil;

import java.util.ArrayList;

public class svgtest {

    public static void main(String[] args){
        ArrayList<Edge> edges = new ArrayList<Edge>();

        // create triangle
        edges.add(new Edge(new Coordinate(10, 10), new Coordinate(10, 50)));
        edges.add(new Edge(new Coordinate(50, 10), new Coordinate(10, 50)));
        edges.add(new Edge(new Coordinate(50, 10), new Coordinate(10, 10)));

        // add a duplicate edge (should be filtered out in the SVG
        edges.add(new Edge(new Coordinate(10, 10), new Coordinate(50, 10)));


        SVGUtil svgwrite = new SVGUtil(edges, null);

        svgwrite.createSVG("test.svg");


        SVGUtil svgread = new SVGUtil(null, null);

        svgread.readFromSvg("test.svg");

        for (Edge edge: svgread.edges) {
            System.out.println(edge);
        }

        for (Coordinate node: svgread.nodes) {
            System.out.println(node);
        }

    }

}
