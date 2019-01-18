package mesh;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.locationtech.jts.geom.Coordinate;

public class Lines {

    ArrayList<Coordinate> contour = new ArrayList<Coordinate>();
    ArrayList<Coordinate> featureCoords = new ArrayList<Coordinate>();

    int width, height;

    Lines(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Extract lines from pixel map.
     */
    public ArrayList<ArrayList<Coordinate>> extractLines(float[][] map) {

        ArrayList<Coordinate> visited = new ArrayList<Coordinate>();
        ArrayList<Coordinate> featureCoords = new ArrayList<Coordinate>();

        for (int y = 5; y < height - 5; y++) {
            for (int x = 5; x < width - 5; x++) {
                if (map[x][y] == 255) {
                    featureCoords.add(new Coordinate(x, y));
                }
            }
        }

        filterSingleCornerPixels(featureCoords);

        ArrayList<ArrayList<Coordinate>> featureLines = new ArrayList<ArrayList<Coordinate>>();

        boolean loop = true;
        while (loop) {
            if (featureCoords.size() <= visited.size()) {
                loop = false;
                break;
            }

            ArrayList<Coordinate> ring = new ArrayList<Coordinate>();
            Coordinate cur = getUnvisited(featureCoords, visited);
            Coordinate first = cur;
            ring.add(first);

            while (cur != null) {
                visited.add(cur);

                cur = next(first, cur, ring.size(), featureCoords, visited);
                if(cur != null){
                    ring.add(cur);
                }
            }
            if( ring.size() > 4){
                System.out.println("WARNING: Detected line is too short. Ignoring...");
                featureLines.add(ring);
            }
        }

        return featureLines;
    }

    public Coordinate getUnvisited(ArrayList<Coordinate> featureCoords, ArrayList<Coordinate> visited){
        for (Coordinate featureCoord : featureCoords) {
            if(!visited.contains(featureCoord)){
                return featureCoord;
            }
        }
        return null;
    }

    /**
     * Filter out single corner pixels
     */
    private void filterSingleCornerPixels(ArrayList<Coordinate> featureCoords){
        ArrayList<Coordinate> singlePixels = new ArrayList<Coordinate>();

        for (Coordinate featureCoord : featureCoords) {
            ArrayList<Coordinate> neighbours = getNeighbours(featureCoords, featureCoord);
            if(neighbours.size() < 2){
                singlePixels.add(featureCoord);
            } else if(neighbours.size() == 2){
                if(neighbours.get(0).distance(neighbours.get(1)) < 2){
                    singlePixels.add(featureCoord);
                }
            }
        }

        featureCoords.removeAll(singlePixels);
    }


    public ArrayList<Coordinate> getNeighbours(ArrayList<Coordinate> featureCoords, Coordinate cur){
        Coordinate test = null;
        ArrayList<Coordinate> neighbours = new ArrayList<Coordinate>();

        // determine all possible next coordinates within neighbourhood
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if(i == 0 && j == 0){
                    continue;
                }
                test = new Coordinate(cur.x + i, cur.y + j);
                if (featureCoords.contains(test)) {
                    neighbours.add(test);
                }
            }
        }

        return neighbours;
    }


    /**
     * Find next in line.
     */
    public Coordinate next(Coordinate first, Coordinate cur, int count,
                           ArrayList<Coordinate> featureCoords, ArrayList<Coordinate> visited) {
        Coordinate test = null;
        ArrayList<Coordinate> neighbours = getNeighbours(featureCoords, cur);

        neighbours.removeAll(visited);

        // select next coordinate which has a valid neighbourhood on its own
        for (Coordinate n : neighbours) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    test = new Coordinate(n.x + i, n.y + j);
                    // equals first within first three
                    if (test.equals(first)) {
                        System.out.println("Ring completed");
                        return n;
                    }
                    if (featureCoords.contains(test)) {
                        return n;
                    }
                }
            }
        }

        return null;
    }


    /**
     * Extracts contour line (longest line) in list.
     */
    public int extractContour(ArrayList<ArrayList<Coordinate>> lines) {
        int index = 0;

        ArrayList<Coordinate> contour = lines.get(0);
        for (int i = 1; i < lines.size() - 1; i++) {
            if (lines.get(i).size() > lines.get(index).size()) {
                index = i;
            }
        }

        return index;
    }


    /**
     * Create pixel map from list of lines.
     */
    public float[][] linesToPixels(ArrayList<ArrayList<Coordinate>> lines, int sx, int sy) {

        float[][] map = new float[sx][sy];

        for (int x = 0; x < sx; x++) {
            Arrays.fill(map[x], 0);
        }

        for (ArrayList<Coordinate> line : lines) {
            for (Coordinate coord : line) {
                map[(int) coord.x][(int) coord.y] = 255;
            }
        }

        return map;
    }

    public float[][] lineToPixels(ArrayList<Coordinate> line, int sx, int sy){
        float[][] map = new float[sx][sy];

        for (int x = 0; x < sx; x++) {
            Arrays.fill(map[x], 0);
        }
        for (Coordinate coord : line) {
            map[(int) coord.x][(int) coord.y] = 255;
        }

        return map;
    }

    /**
     * Performs sekeletonization/thinning operation on pixel map.
     * ... to be completed
     */
    public float[][] skeletonize(float[][] map) {

        int sx = map.length;
        int sy = map[0].length;

        float[][] thinMap = new float[sx][sy];


        return thinMap;
    }


    /**
     * Generates pixel map with 1 for pixels within contour (main feature line) and 0 for outside contour.
     */
    public float[][] contourMap(ArrayList<ArrayList<Coordinate>> featureLines, int sx, int sy) {

        BufferedImage img = new BufferedImage(sx, sy, BufferedImage.TYPE_INT_RGB);
        final int black = (0 << 16) | (0 << 8) | 0;
        final int white = (255 << 16) | (255 << 8) | 255;
        final int red = (255 << 16) | (0 << 8) | 0;

        ArrayList<Coordinate> contour = (ArrayList<Coordinate>) featureLines.get(0).clone();
//        for (int i = 1; i < featureLines.size() - 1; i++) {
//            if (featureLines.get(i).size() > contour.size()) {
//                contour = (ArrayList<Coordinate>) featureLines.get(i).clone();
//            }
//        }

        float[][] figureMap = new float[sx][sy];

        for (int x = 0; x < sx; x++) {
            Arrays.fill(figureMap[x], 0);
        }
        for (int x = 0; x < sx; x++) {
            for (int y = 0; y < sy; y++) {
                img.setRGB(x, y, white);
            }
        }

        int startY = (int) contour.get(0).y;
        int count = 0;

        boolean loop = true;
        while (loop) {

            if (contour.size() < 1) {
                loop = false;
                break;
            }

            Coordinate c = contour.get(0);

            if (c.y >= startY) {
                while (contour.contains(c)) {
                    figureMap[(int) c.x][(int) c.y] = 1;
                    img.setRGB((int) c.x, (int) c.y, red);
                    contour.remove(c);
                    c = new Coordinate(c.x + 1, c.y);
                }

                boolean end = true;
                for (int x = (int) c.x + 2; x < sx; x++) {
                    Coordinate test = new Coordinate(x, c.y);
                    if (contour.contains(test)) {
                        end = false;
                        break;
                    }
                }

                if (end) {
                    continue;
                }

                for (int x = (int) c.x + 1; x < sx; x++) {
                    c = new Coordinate(x, c.y);
                    figureMap[(int) c.x][(int) c.y] = 1;
                    img.setRGB((int) c.x, (int) c.y, black);
                    while (contour.contains(c)) {
                        figureMap[(int) c.x][(int) c.y] = 1;
                        img.setRGB((int) c.x, (int) c.y, red);
                        contour.remove(c);
                        c = new Coordinate(c.x + 1, c.y);
                        end = true;
                    }
                    if (end) {
                        break;
                    }
                }
            } else {
                while (contour.contains(c)) {
                    figureMap[(int) c.x][(int) c.y] = 1;
                    img.setRGB((int) c.x, (int) c.y, red);
                    contour.remove(c);
                    c = new Coordinate(c.x - 1, c.y);
                }

                boolean end = true;
                for (int x = (int) c.x - 2; x >= 0; x--) {
                    Coordinate test = new Coordinate(x, c.y);
                    if (contour.contains(test)) {
                        end = false;
                        break;
                    }
                }

                if (end) {
                    continue;
                }

                for (int x = (int) c.x - 1; x >= 0; x--) {
                    c = new Coordinate(x, c.y);
                    figureMap[(int) c.x][(int) c.y] = 1;
                    img.setRGB((int) c.x, (int) c.y, black);
                    while (contour.contains(c)) {
                        figureMap[(int) c.x][(int) c.y] = 1;
                        img.setRGB((int) c.x, (int) c.y, red);
                        contour.remove(c);
                        c = new Coordinate(c.x - 1, c.y);
                        end = true;
                    }
                    if (end)
                        break;
                }
            }
            count++;
        }

        JFrame frame = new JFrame();
        JLabel lblimage = new JLabel(new ImageIcon(img));
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(lblimage);

        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        frame.add(mainPanel);
        frame.setVisible(true);

        return figureMap;
    }

}
