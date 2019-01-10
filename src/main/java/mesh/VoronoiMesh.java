package mesh;

import java.util.ArrayList;
import java.util.List;

import knotwork.Edge;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;

import stippling.main.Stippler;
import svg.SVGUtil;

public class VoronoiMesh {
	
	Stippler stippler = new Stippler();
	String imgPath = "res/robot-2.jpg";
	int numDots = 1000;
	
	/**
	 * Constructors.
	 */
	public VoronoiMesh(){
		stipple();
	}
	public VoronoiMesh(String imgPath){
		this.imgPath = imgPath;
		stipple();
	}
	public VoronoiMesh(int numDots){
		this.numDots = numDots;
		stipple();
	}
	public VoronoiMesh(String imgPath, int numDots){
		this.imgPath = imgPath;
		this.numDots = numDots;
		stipple();
	}

	public SVGUtil fromSVGMesh(String path){
		// start with reading the edges from the svg
		SVGUtil svgutil = new SVGUtil(null, null);
		svgutil.readFromSvg(path);
		return  svgutil;
	}
	
	public SVGUtil createTriangularMesh()
	{		
		//String path = stippler.createOutputPath("-mesh.svg");
		//System.out.println("Saving SVG mesh to " + path + "...");
		
		ArrayList<Edge> edges = new ArrayList<Edge>();
		
		// outputs triangulation as mesh
		List<Coordinate[]> triangles = stippler.getTriang().getTriangleCoordinates(false);
		
		for (Coordinate[] triangle : triangles)
		{
			for (int i = 0; i < 3; i++)
			{
				edges.add(new Edge(triangle[i], triangle[i+1]));
			}
		}
		
        SVGUtil svgwrite = new SVGUtil(edges, null);
        svgwrite.createSVG(System.getProperty("user.dir") + "/res/triangular_mesh.svg");
        
        return svgwrite;
	}
	
	public SVGUtil createVoronoiMesh(){

		ArrayList<Edge> edges = new ArrayList<Edge>();
		
		GeometryCollection polys = stippler.getPolys();
		int numPolys = polys.getNumGeometries();
		
		for (int i = 0; i < numPolys; i++)
		{
			Geometry poly = polys.getGeometryN(i);
			Coordinate[] coords = poly.getCoordinates();
			
			for (int j = 0; j < coords.length-1; j++)
			{
				edges.add(new Edge(coords[j], coords[j+1]));
			}
		}
		
        SVGUtil svgwrite = new SVGUtil(edges, null);
        removeLongEdges(svgwrite, 1.2);
        svgwrite.createSVG(System.getProperty("user.dir") + "/voronoi_mesh.svg");
        
        return svgwrite;
	}
	
	public SVGUtil createQuadrangularMesh(){
		
		ArrayList<Edge> edges = new ArrayList<Edge>();
		GeometryCollection polys = stippler.getPolys();
		int numPolys = polys.getNumGeometries();

		for (int i = 0; i < numPolys; i++)
		{
			Geometry poly = polys.getGeometryN(i);
			Coordinate[] coords = poly.getCoordinates();
			Coordinate centroid = poly.getCentroid().getCoordinate();
			
			for (int j = 0; j < coords.length-1; j++)
			{
				edges.add(new Edge(coords[j], coords[j+1]));
				Coordinate edgeMidpoint = new Coordinate((coords[j].x + coords[j+1].x) / 2d, (coords[j].y + coords[j+1].y) / 2d);
				edges.add(new Edge(edgeMidpoint, centroid));
			}
		}
		
        SVGUtil svgwrite = new SVGUtil(edges, null);
        removeLongEdges(svgwrite, 1.2);
        svgwrite.createSVG(System.getProperty("user.dir") + "/quadrangular_mesh.svg");
        
        return svgwrite;
	}
	
	public void removeLongEdges(SVGUtil mesh, double factor){
		
		ArrayList<Edge> edges = mesh.edges;
		ArrayList<Double> edgeLengths = new ArrayList<Double>();
		double thisLength;
		double avgLength = 0;
		
		for(Edge e : edges){
			thisLength = e.getLength();
			edgeLengths.add(thisLength);
			avgLength += thisLength;
		}
		
		avgLength = avgLength/edges.size();
		
		for(int i = 0; i < edges.size(); i++){
			if(edgeLengths.get(i) >= factor*avgLength){
				edgeLengths.remove(i);
				edges.remove(i);
				i -= 1;
			}
		}
	}

	public void stipple(){
		stippler.numDots = this.numDots;
		stippler.loadImage(this.imgPath);
    	stippler.start();
	}
}
