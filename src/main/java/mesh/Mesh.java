package mesh;

import java.io.IOException;

import svg.SVGUtil;

public class Mesh {

	public static void main(String[] args) {
		// play with Isophotes("...", [No. ISOPHOTES], [BLUR FACTOR]);
//		Isophotes isos = new Isophotes("res/flower_contrast.png", 25, 50);
		Isophotes isos = new Isophotes("res/circle.png", 1,1);
		isos.init();
	}

}
