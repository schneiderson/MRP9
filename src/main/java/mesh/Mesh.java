package mesh;

import svg.SVGUtil;

public class Mesh {

	public static void main(String[] args) {
		// play with Isophotes("...", [No. ISOPHOTES], [BLUR FACTOR]);
		Isophotes isos = new Isophotes("res/robot-2.jpg", 10, 30);
		isos.init();
	}

}
