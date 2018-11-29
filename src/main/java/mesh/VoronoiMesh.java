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
        svgwrite.createSVG(System.getProperty("user.dir") + "/triangular_mesh.svg");
        
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
        svgwrite.createSVG(System.getProperty("user.dir") + "/voronoi_mesh.svg");
        
        return svgwrite;
	}
	
	public void stipple(){
		stippler.loadImage(this.imgPath);
		stippler.numDots = this.numDots;
    	stippler.start();
	}
}
