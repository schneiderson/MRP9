package mesh;

import java.io.IOException;

import svg.SVGUtil;

public class Mesh {

	public static void main(String[] args) {
		// play with Isophotes("...", [No. ISOPHOTES], [BLUR FACTOR]);
//		Isophotes isos = new Isophotes("res/flower_contrast.png", 25, 50);
		//Isophotes isos = new Isophotes("res/flower_contrast.png",1,50);
	
		//MeshGenerator meshG = new MeshGenerator(1, isos.init(), 1);
		// MeshGenerator(String imgPath, int cellWidth, int noBins, int blurFactor)
		MeshGenerator meshG = new MeshGenerator("res/flower_contrast.png",50,2,50);
		meshG.init();
	}
}
