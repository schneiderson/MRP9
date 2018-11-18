package knotwork;

import svg.SVGUtil;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Knotwork {

    public static void main(String[] args){

        // start with reading the edges from the svg
        String path = "test.svg";
        SVGUtil svgutil = new SVGUtil(null, null);
        svgutil.readFromSvg(path);

        KnotworkGraph graph = new KnotworkGraph(svgutil.nodes, svgutil.edges);

        KnotNode node = null;
        ArrayList<ArrayList<KnotNode>> controlSets = graph.getControlSets();

        System.out.println("Number of controlSets " + controlSets.size());

        for (int i = 0; controlSets.size() > i; i++) {
            System.out.println("Control set " + (1+i));
            for (KnotNode kN : controlSets.get(i)){
                System.out.println("node: " + kN.getPos() + " vector: " + kN.getVector());
            }
        }




    }

}
