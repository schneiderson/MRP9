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

        //VoronoiMesh mesh = new VoronoiMesh();
        //alternative:
        // choose different input image path AND/OR different number of stipples (= mesh density)
        // default: imgPath = "res/robot-2.jpg", numDots = 1000
//         VoronoiMesh mesh = new VoronoiMesh("res/shaded_cube.png", 300);
//
////         SVGUtil svgutil = mesh.createTriangularMesh();
//        // or:
//        //SVGUtil svgutil = mesh.createVoronoiMesh();
//        // or: (from svg mesh representation)
//         SVGUtil svgutil = mesh.createQuadrangularMesh();


//         SVGUtil svgutil = mesh.createTriangularMesh();
        // or:
        //SVGUtil svgutil = mesh.createVoronoiMesh();
        // or: (from svg mesh representation)

        SVGUtil svgutil = new SVGUtil(null, null);
        svgutil.readFromSvg("res/test5.svg");

        // create knotwork graph
        System.out.println("\n> Creating Graph");
        KnotworkGraph graph = new KnotworkGraph(svgutil.nodes, svgutil.edges);

        // create svg from knotwork graph
        SVGUtil svgUtilKnotwork = new SVGUtil(svgutil.edges, svgutil.nodes, graph.curveLists, graph.overpassCurveList, graph.undulationList);

        // save svg
        svgUtilKnotwork.createSVG(System.getProperty("user.dir") + "/res/curve_2.svg", false, false);
        System.out.println("\n> SVG Created");

        // Print control and curve sets/lists
        System.out.println("Number of controlSets = " + graph.controlSets.size() + "\n");

    }
}
