package mesh;

import java.io.IOException;

import knotwork.KnotworkGraph;
import svg.SVGUtil;

public class Mesh {

	public static void main(String[] args) {
		// play with Isophotes("...", [No. ISOPHOTES], [BLUR FACTOR]);
//		Isophotes isos = new Isophotes("res/flower_contrast.png", 25, 50);
		//Isophotes isos = new Isophotes("res/flower_contrast.png",1,50);
	
		//MeshGenerator meshG = new MeshGenerator(1, isos.init(), 1);
		// MeshGenerator(String imgPath, int cellWidth, int noBins, int blurFactor)
//		MeshGenerator meshG = new MeshGenerator("res/iris.jpg",50,6,10);
		MeshGenerator meshG = new MeshGenerator("res/flower_contrast.png",60,2,50);
		SVGUtil svgutil = meshG.init();

       ///////////////////// create knotwork on mesh ///////////////////////
       // create knotwork graph
       System.out.println("\n> Creating Graph");
       KnotworkGraph graph = new KnotworkGraph(svgutil.nodes, svgutil.edges);
       // create svg from knotwork graph
       SVGUtil svgUtilKnotwork = new SVGUtil(svgutil.edges, svgutil.nodes, graph.curveLists, graph.overpassCurveList);
       // save svg
       svgUtilKnotwork.createSVG(System.getProperty("user.dir") + "/res/knotwork.svg", false);
       System.out.println("\n> SVG Created");
       // Print control and curve sets/lists
       System.out.println("Number of controlSets = " + graph.controlSets.size() + "\n");
		
	}
}
