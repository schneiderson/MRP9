package mesh.thinning;

import javax.swing.*;
import java.awt.*;

public class PolygonPanel extends JPanel {
    Polygon poly;

    public PolygonPanel(Polygon poly){
        this.poly = poly;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.BLUE);
        // Draw the polygon
        g.drawPolygon(poly);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(800, 600);
    }

}