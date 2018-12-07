package knotwork;

import jogamp.opengl.glu.nurbs.BezierArc;
import knotwork.curve.CubicBezier;
import knotwork.curve.Curve;
import svg.SVGUtil;
import mesh.VoronoiMesh;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class Knotwork {

    public static void main(String[] args) {

        VoronoiMesh mesh = new VoronoiMesh();
        //alternative:
        // choose different input image path AND/OR different number of stipples (= mesh density)
        // default: imgPath = "res/robot-2.jpg", numDots = 1000
        // VoronoiMesh mesh = new VoronoiMesh("res/shaded_cube.png", 500);

        // SVGUtil svgutil = mesh.createTriangularMesh();
        // or:
        // SVGUtil svgutil = mesh.createVoronoiMesh();
        // or: (from svg mesh representation)
        SVGUtil svgutil = mesh.fromSVGMesh("res/test2.svg");

        // create knotwork graph
        KnotworkGraph graph = new KnotworkGraph(svgutil.nodes, svgutil.edges);

        // create svg from knotwork graph
        SVGUtil svgUtilKnotwork = new SVGUtil(svgutil.edges, svgutil.nodes, graph.curveLists);
        // save svg
        svgUtilKnotwork.createSVG(System.getProperty("user.dir") + "/res/curve_2.svg", true);


        // Print control and curve sets/lists
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
            System.out.println("\n");
        }

    }
}
