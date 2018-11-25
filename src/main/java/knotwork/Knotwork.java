package knotwork;

import jogamp.opengl.glu.nurbs.BezierArc;
import knotwork.curve.CubicBezier;
import knotwork.curve.Curve;
import svg.SVGUtil;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Knotwork {

    public static void main(String[] args){
        // start with reading the edges from the svg
        String path = "test2.svg";
        SVGUtil svgutil = new SVGUtil(null, null);
        svgutil.readFromSvg(path);

        KnotworkGraph graph = new KnotworkGraph(svgutil.nodes, svgutil.edges);

        System.out.println("Number of controlSets = " + graph.controlSets.size() + "\n");

        for (int i = 0; graph.controlSets.size() > i; i++) {
            System.out.println("Control set " + (1 + i) + " has size " + graph.controlSets.get(i).size());
            for (KnotNode kN : graph.controlSets.get(i)){
                System.out.println("node: " + kN.getPos() + " vector: " + kN.getVector());
            }

            System.out.println("\nSize curveLists: " + graph.curveLists.size());
            for (ArrayList<Curve> curveList : graph.curveLists){
                System.out.println("Number of curves in curve list " + (i + 1) + ": " + graph.curveLists.get(0).size());
                for (Curve curve : curveList){
                    CubicBezier cbCurve = curve.getCubicBezierPoints();
                    System.out.println(cbCurve.getAnchor1() + " " + cbCurve.getControl1() +
                            " " + cbCurve.getControl2() + " " + cbCurve.getAnchor2());
                }
            }
        }

        SVGUtil svgUtil2 = new SVGUtil(svgutil.edges, svgutil.nodes, graph.curveLists);
        svgUtil2.createSVG(System.getProperty("user.dir") + "/curve.svg");
    }

}
