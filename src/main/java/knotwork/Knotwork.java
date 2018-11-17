package knotwork;

import svg.SVGUtil;
import java.util.ArrayList;

public class Knotwork {

    public static void main(String[] args){

        // start with reading the edges from the svg
        String path = "test.svg";
        SVGUtil svgutil = new SVGUtil(null, null);
        svgutil.readFromSvg(path);

        KnotworkGraph graph = new KnotworkGraph(svgutil.nodes, svgutil.edges);

        KnotNode node = null;
        try{
            node = graph.getInitialKnotNode();
        } catch(Exception e){

        }

        Crossing cross = graph.getCorrespondingCrossing(node);
        ArrayList<Edge> incidentEdges = graph.getAdjacentEdges(cross, node);

        System.out.println(incidentEdges.size());

        incidentEdges.forEach(x -> System.out.println(x.midpoint));



    }

}
