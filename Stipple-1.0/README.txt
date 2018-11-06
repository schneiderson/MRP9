The key functions are in Stippler.java:
1. createInitialDots();  // generates a random distribution of dots across the image area
2. createVoronoi();      // generates a VD from the dots
3. relax();              // applies one iteration of nudging the dots to fit the image shape. 
			 // This uses Lloyd’s algorithm (from the “Weighted Voronoi Stippling” paper).

This code could already provide you with a basic mesh fitted to the target shape to start with, 
or at least demonstrate how to do so.

For each relaxation step, if you repaint the view to show the current VD, you should see the initially 
random distribution of dots shuffling around to better fit the image shape.

Some relevant terminology:
— A triangulation is a graph generated from a set of vertices, such that every face or cell is a triangle.
— A Delaunay Triangulation (DT) is triangulation that maximises the regularity of the triangles.
— A Voronoi Diagram (VD) is the dual of a DT and vice versa: https://en.wikipedia.org/wiki/Voronoi_diagram
— A VD is an example of a mesh: https://en.wikipedia.org/wiki/Mesh_generation

Note that we are dealing with a specific type of DT called the Conforming Delaunay Triangulation (CDT) 
which is a DT with boundary edges that remain fixed.

I would describe the key task as: automatically generating “artistic" meshes from arbitrary images 
that are conducive to applying celtic knotwork designs.