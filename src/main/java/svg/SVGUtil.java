package svg;

import knotwork.Edge;
import knotwork.curve.CubicBezier;
import knotwork.curve.Curve;
import knotwork.curve.OverpassCurve;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLResourceDescriptor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.w3c.dom.*;
import util.MathUtil;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;

import static java.lang.Math.max;


public class SVGUtil {

    public ArrayList<Edge> edges = new ArrayList<Edge>();
    public ArrayList<Coordinate> nodes = new ArrayList<Coordinate>();
    public ArrayList<ArrayList<Curve>> curveLists = new ArrayList<>();
    public ArrayList<OverpassCurve> overpassCurveList = new ArrayList<>();
    private Double distanceTolerance = 0.5;
    public ArrayList<LineSegment> undulationList = new ArrayList<>();


    public SVGUtil(ArrayList<Edge> edges, ArrayList<Coordinate> nodes) {
        if (edges != null) {
            for (Edge e : edges) {
                if (!containsEdge(e, 0.0)) {
                    this.edges.add(e);
                }
            }
        }
        if (nodes != null) {
            this.nodes = nodes;
        } else {
            getNodesFromEdges(this.edges);
        }
    }

    public SVGUtil(ArrayList<Edge> edges, ArrayList<Coordinate> nodes, ArrayList<ArrayList<Curve>> curveLists) {
        this(edges, nodes);
        if (curveLists != null) {
            this.curveLists = curveLists;
        }
    }

    public SVGUtil(ArrayList<Edge> edges, ArrayList<Coordinate> nodes,
                   ArrayList<ArrayList<Curve>> curveLists, ArrayList<OverpassCurve> overpassCurveList) {
        this(edges, nodes);
        if (curveLists != null) {
            this.curveLists = curveLists;
        }
        if (overpassCurveList != null) {
            this.overpassCurveList = overpassCurveList;
        }
    }

    public SVGUtil(ArrayList<Edge> edges, ArrayList<Coordinate> nodes,
                   ArrayList<ArrayList<Curve>> curveLists, ArrayList<OverpassCurve> overpassCurveList,
                   ArrayList<LineSegment> undulationList) {
        this(edges, nodes);
        if (curveLists != null) {
            this.curveLists = curveLists;
        }
        if (overpassCurveList != null) {
            this.overpassCurveList = overpassCurveList;
        }
        if (undulationList != null){
            this.undulationList = undulationList;
        }
    }

    private void getNodesFromEdges(ArrayList<Edge> edges) {
        for (Edge e : edges) {
            if (!containsNode(e.c1)) {
                this.nodes.add(e.c1);
            }
            if (!containsNode(e.c2)) {
                this.nodes.add(e.c2);
            }
        }
    }


    private boolean containsEdge(Edge edge, Double tolerance) {
        for (Edge e : edges) {
            if (e.equals(edge, tolerance)) {
                return true;
            }
        }
        return false;
    }


    private boolean containsNode(Coordinate node) {
        for (Coordinate n : nodes) {
            if (n.equals(node)) {
                return true;
            }
        }
        return false;
    }


    public void createSVG(String path) {
        createSVG(path, true, true);
    }

    public void createSVG(String path, boolean includeMesh, boolean includeAnchorControlLine) {
        try {
            // set up color iterator for the different curve lists:
            String[] colorArray = new String[]{"red", "green", "blue", "yellow", "purple", "cyan", "orange"};
            // the color list to signify normal edges vs those with breakpoints
            String[] edgeColorArray = new String[]{"rgb(0,0,0)", "rgb(0,0,255)", "rgb(255,255,0)"};
            Iterator<String> colors = MathUtil.cycle(colorArray);
            String outlineColor = "black";
            String strokeWidthWide = "10";
            String strokeWidthNarrow = "6";

            // find largest x and y coordinates
            double x_max = 0, y_max = 0;
            for (Coordinate node : nodes) {
                x_max = max(x_max, node.x);
                y_max = max(y_max, node.y);
            }

            DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
            String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
            Document doc = impl.createDocument(svgNS, "svg", null);

            // get the root element (the svg element)
            Element svgRoot = doc.getDocumentElement();

            if (includeMesh) {
                // create the line for each edge
                for (Edge e : edges) {
                    Element line = doc.createElementNS(svgNS, SVGConstants.SVG_LINE_TAG);
                    line.setAttributeNS(null, SVGConstants.SVG_STYLE_TAG, "stroke:" + edgeColorArray[e.breakpoint] + ";stroke-width:1");
                    line.setAttributeNS(null, SVGConstants.SVG_X1_ATTRIBUTE, Double.toString(e.c1.x));
                    line.setAttributeNS(null, SVGConstants.SVG_Y1_ATTRIBUTE, Double.toString(e.c1.y));
                    line.setAttributeNS(null, SVGConstants.SVG_X2_ATTRIBUTE, Double.toString(e.c2.x));
                    line.setAttributeNS(null, SVGConstants.SVG_Y2_ATTRIBUTE, Double.toString(e.c2.y));

                    // attach the line to the svg root element
                    svgRoot.appendChild(line);
                }
            }

            // create curves between knot nodes:
            for (ArrayList<Curve> curveList : this.curveLists) {
                String curveListColor = colors.next();
                for (int i = 0; i < curveList.size(); i++) {
                    Curve curve = curveList.get(i);
                    CubicBezier cbCurve = curve.getCubicBezierPoints();

                    // wide thread:
                    Element curvePathWide = doc.createElementNS(svgNS, SVGConstants.SVG_PATH_TAG);
                    // narrow thread:
                    Element curvePathNarrow = doc.createElementNS(svgNS, SVGConstants.SVG_PATH_TAG);

                    if (cbCurve.isPartCurve()){
                        // get second part of pointy curve:
                        CubicBezier secondCurve = (CubicBezier) curveList.get(i + 1);

                        // create svg element:
                        curvePathWide.setAttributeNS(null, SVGConstants.SVG_D_ATTRIBUTE,
                                "M"+ cbCurve.getAnchor1().x + "," + cbCurve.getAnchor1().y + " " +
                                        "C" + cbCurve.getControl1().x + "," + cbCurve.getControl1().y + " " +
                                        cbCurve.getControl2().x + "," + cbCurve.getControl2().y + " " +
                                        cbCurve.getAnchor2().x + "," + cbCurve.getAnchor2().y + " " +
                                        "C" +
                                        secondCurve.getControl2().x + "," + secondCurve.getControl2().y + " " +
                                        secondCurve.getControl1().x + "," + secondCurve.getControl1().y + " " +
                                        secondCurve.getAnchor1().x + "," + secondCurve.getAnchor1().y
                        );
                        curvePathWide.setAttributeNS(null, SVGConstants.SVG_STROKE_ATTRIBUTE, outlineColor);
                        curvePathWide.setAttributeNS(null, SVGConstants.SVG_STYLE_TAG,
                                "fill:none;stroke-width:" + strokeWidthWide);

                        // create svg narrow curve element
                        curvePathNarrow.setAttributeNS(null, SVGConstants.SVG_D_ATTRIBUTE,
                                "M"+ cbCurve.getAnchor1().x + "," + cbCurve.getAnchor1().y + " " +
                                        "C" + cbCurve.getControl1().x + "," + cbCurve.getControl1().y + " " +
                                        cbCurve.getControl2().x + "," + cbCurve.getControl2().y + " " +
                                        cbCurve.getAnchor2().x + "," + cbCurve.getAnchor2().y + " " +
                                        "C" +
                                        secondCurve.getControl2().x + "," + secondCurve.getControl2().y + " " +
                                        secondCurve.getControl1().x + "," + secondCurve.getControl1().y + " " +
                                        secondCurve.getAnchor1().x + "," + secondCurve.getAnchor1().y
                        );
                        curvePathNarrow.setAttributeNS(null, SVGConstants.SVG_STROKE_ATTRIBUTE, curveListColor);
                        curvePathNarrow.setAttributeNS(null, SVGConstants.SVG_STYLE_TAG,
                                "stroke-linecap:round;fill:none;stroke-width:" + strokeWidthNarrow);

                        // skip second part of curve in iteration
                        i++;
                    } else {
                        curvePathWide.setAttributeNS(null, SVGConstants.SVG_D_ATTRIBUTE,
                                "M" + cbCurve.getAnchor1().x + "," + cbCurve.getAnchor1().y + " " +
                                        "C" + cbCurve.getControl1().x + "," + cbCurve.getControl1().y + " " +
                                        cbCurve.getControl2().x + "," + cbCurve.getControl2().y + " " +
                                        cbCurve.getAnchor2().x + "," + cbCurve.getAnchor2().y
                        );
                        curvePathWide.setAttributeNS(null, SVGConstants.SVG_STROKE_ATTRIBUTE, outlineColor);
                        curvePathWide.setAttributeNS(null, SVGConstants.SVG_STYLE_TAG,
                                "fill:none;stroke-width:" + strokeWidthWide);

                        curvePathNarrow.setAttributeNS(null, SVGConstants.SVG_D_ATTRIBUTE,
                                "M"+ cbCurve.getAnchor1().x + "," + cbCurve.getAnchor1().y + " " +
                                        "C" + cbCurve.getControl1().x + "," + cbCurve.getControl1().y + " " +
                                        cbCurve.getControl2().x + "," + cbCurve.getControl2().y + " " +
                                        cbCurve.getAnchor2().x + "," + cbCurve.getAnchor2().y
                        );
                        curvePathNarrow.setAttributeNS(null, SVGConstants.SVG_STROKE_ATTRIBUTE, curveListColor);
                        curvePathNarrow.setAttributeNS(null, SVGConstants.SVG_STYLE_TAG,
                                "stroke-linecap:round;fill:none;stroke-width:" + strokeWidthNarrow);
                    }

                    // attach the curves to the svg root element
                    svgRoot.appendChild(curvePathWide);
                    svgRoot.appendChild(curvePathNarrow);
                }
            }


            // apply over-under pattern:
            for (OverpassCurve overpassCurve : overpassCurveList) {
                String color = colorArray[overpassCurve.getId() % colorArray.length];

                CubicBezier cbCurve1 = overpassCurve.getCurve1().getCubicBezierPoints();
                CubicBezier cbCurve2 = overpassCurve.getCurve2().getCubicBezierPoints();

                Coordinate[] coordinates = findSharedAnchor(cbCurve1, cbCurve2);

                Element curvePathWide = doc.createElementNS(svgNS, SVGConstants.SVG_PATH_TAG);
                Element curvePathNarrow = doc.createElementNS(svgNS, SVGConstants.SVG_PATH_TAG);

                curvePathWide.setAttributeNS(null, SVGConstants.SVG_D_ATTRIBUTE,
                        "M"+ coordinates[0].x + "," + coordinates[0].y + " " +
                                "C" + coordinates[1].x + "," + coordinates[1].y + " " +
                                coordinates[2].x + "," + coordinates[2].y + " " +
                                coordinates[3].x + "," + coordinates[3].y + " " +
                                "C" +
                                coordinates[4].x + "," + coordinates[4].y + " " +
                                coordinates[5].x + "," + coordinates[5].y + " " +
                                coordinates[6].x + "," + coordinates[6].y
                );
                curvePathNarrow.setAttributeNS(null, SVGConstants.SVG_D_ATTRIBUTE,
                        "M"+ coordinates[0].x + "," + coordinates[0].y + " " +
                                "C" + coordinates[1].x + "," + coordinates[1].y + " " +
                                coordinates[2].x + "," + coordinates[2].y + " " +
                                coordinates[3].x + "," + coordinates[3].y + " " +
                                "C" +
                                coordinates[4].x + "," + coordinates[4].y + " " +
                                coordinates[5].x + "," + coordinates[5].y + " " +
                                coordinates[6].x + "," + coordinates[6].y
                );

                curvePathWide.setAttributeNS(null, SVGConstants.SVG_STROKE_ATTRIBUTE, outlineColor);
                curvePathWide.setAttributeNS(null, SVGConstants.SVG_STYLE_TAG,
                        "stroke-linecap:none;fill:none;stroke-width:" + strokeWidthWide);
                curvePathNarrow.setAttributeNS(null, SVGConstants.SVG_STROKE_ATTRIBUTE, color);
                curvePathNarrow.setAttributeNS(null, SVGConstants.SVG_STYLE_TAG,
                        "stroke-linecap:round;fill:none;stroke-width:" + strokeWidthNarrow);

                // attach the curves to the svg root element
                svgRoot.appendChild(curvePathWide);
                svgRoot.appendChild(curvePathNarrow);
            }


            if (includeAnchorControlLine){
                for (ArrayList<Curve> curveList : curveLists) {
                    for (Curve curve : curveList) {
                        CubicBezier cbCurve = curve.getCubicBezierPoints();

                        boolean undulation = cbCurve.checkForUndulation();

                        if (true) {

                            Element line1 = doc.createElementNS(svgNS, SVGConstants.SVG_LINE_TAG);
                            line1.setAttributeNS(null, SVGConstants.SVG_STYLE_TAG, "stroke:lime;stroke-width:0.5");
                            line1.setAttributeNS(null, SVGConstants.SVG_X1_ATTRIBUTE, Double.toString(cbCurve.getAnchor1().x));
                            line1.setAttributeNS(null, SVGConstants.SVG_Y1_ATTRIBUTE, Double.toString(cbCurve.getAnchor1().y));
                            line1.setAttributeNS(null, SVGConstants.SVG_X2_ATTRIBUTE, Double.toString(cbCurve.getControl1().x));
                            line1.setAttributeNS(null, SVGConstants.SVG_Y2_ATTRIBUTE, Double.toString(cbCurve.getControl1().y));

                            Element line2 = doc.createElementNS(svgNS, SVGConstants.SVG_LINE_TAG);
                            line2.setAttributeNS(null, SVGConstants.SVG_STYLE_TAG, "stroke:lime;stroke-width:0.5");
                            line2.setAttributeNS(null, SVGConstants.SVG_X1_ATTRIBUTE, Double.toString(cbCurve.getAnchor2().x));
                            line2.setAttributeNS(null, SVGConstants.SVG_Y1_ATTRIBUTE, Double.toString(cbCurve.getAnchor2().y));
                            line2.setAttributeNS(null, SVGConstants.SVG_X2_ATTRIBUTE, Double.toString(cbCurve.getControl2().x));
                            line2.setAttributeNS(null, SVGConstants.SVG_Y2_ATTRIBUTE, Double.toString(cbCurve.getControl2().y));

                            svgRoot.appendChild(line1);
                            svgRoot.appendChild(line2);

                            // add anchor to opposite control line
                            Element line3 = doc.createElementNS(svgNS, SVGConstants.SVG_LINE_TAG);
                            line3.setAttributeNS(null, SVGConstants.SVG_STYLE_TAG, "stroke:blue;stroke-width:0.5");
                            line3.setAttributeNS(null, SVGConstants.SVG_X1_ATTRIBUTE, Double.toString(cbCurve.getAnchor1().x));
                            line3.setAttributeNS(null, SVGConstants.SVG_Y1_ATTRIBUTE, Double.toString(cbCurve.getAnchor1().y));
                            line3.setAttributeNS(null, SVGConstants.SVG_X2_ATTRIBUTE, Double.toString(cbCurve.getControl2().x));
                            line3.setAttributeNS(null, SVGConstants.SVG_Y2_ATTRIBUTE, Double.toString(cbCurve.getControl2().y));

                            Element line4 = doc.createElementNS(svgNS, SVGConstants.SVG_LINE_TAG);
                            line4.setAttributeNS(null, SVGConstants.SVG_STYLE_TAG, "stroke:blue;stroke-width:0.5");
                            line4.setAttributeNS(null, SVGConstants.SVG_X1_ATTRIBUTE, Double.toString(cbCurve.getAnchor2().x));
                            line4.setAttributeNS(null, SVGConstants.SVG_Y1_ATTRIBUTE, Double.toString(cbCurve.getAnchor2().y));
                            line4.setAttributeNS(null, SVGConstants.SVG_X2_ATTRIBUTE, Double.toString(cbCurve.getControl1().x));
                            line4.setAttributeNS(null, SVGConstants.SVG_Y2_ATTRIBUTE, Double.toString(cbCurve.getControl1().y));

//                            svgRoot.appendChild(line3);
//                            svgRoot.appendChild(line4);
                        }
                    }
                }

                for (int i = 0; i < undulationList.size(); i++) {
                    LineSegment lineSegment = undulationList.get(i);
                    String color = "pink";

                    Element line1 = doc.createElementNS(svgNS, SVGConstants.SVG_LINE_TAG);
                    line1.setAttributeNS(null, SVGConstants.SVG_STYLE_TAG, "stroke:"+color+";stroke-width:0.5");
                    line1.setAttributeNS(null, SVGConstants.SVG_X1_ATTRIBUTE, Double.toString(lineSegment.p0.x));
                    line1.setAttributeNS(null, SVGConstants.SVG_Y1_ATTRIBUTE, Double.toString(lineSegment.p0.y));
                    line1.setAttributeNS(null, SVGConstants.SVG_X2_ATTRIBUTE, Double.toString(lineSegment.p1.x));
                    line1.setAttributeNS(null, SVGConstants.SVG_Y2_ATTRIBUTE, Double.toString(lineSegment.p1.y));

                    svgRoot.appendChild(line1);
                }
            }


            // set the width and height attribute on the root svg element:
            svgRoot.setAttributeNS(null, "width", Double.toString(Math.ceil(x_max * 2)));
            svgRoot.setAttributeNS(null, "height", Double.toString(Math.ceil(y_max * 2)));

            // write svg file
            OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(path)));
            javax.xml.transform.Result result = new StreamResult(out);

            // Use a transformer for pretty printing
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.setOutputProperty(OutputKeys.INDENT, "yes");
            xformer.transform(new DOMSource(doc), result);
            out.flush();
            out.close();

        } catch (Exception e) {
            System.out.println("Couldn't save SVG file");
            System.out.println(e);
        }
    }

    private Coordinate[] findSharedAnchor(CubicBezier cbCurve1, CubicBezier cbCurve2) {
        Coordinate[] result = new Coordinate[7];
        if (cbCurve1.getAnchor1().equals(cbCurve2.getAnchor1())){
            result[0] = cbCurve1.getAnchor2();
            result[1] = cbCurve1.getControl2();
            result[2] = cbCurve1.getControl1();
            result[3] = cbCurve1.getAnchor1();

            result[4] = cbCurve2.getControl1();
            result[5] = cbCurve2.getControl2();
            result[6] = cbCurve2.getAnchor2();
        }
        else if (cbCurve1.getAnchor1().equals(cbCurve2.getAnchor2())){
            result[0] = cbCurve1.getAnchor2();
            result[1] = cbCurve1.getControl2();
            result[2] = cbCurve1.getControl1();
            result[3] = cbCurve1.getAnchor1();

            result[4] = cbCurve2.getControl2();
            result[5] = cbCurve2.getControl1();
            result[6] = cbCurve2.getAnchor1();
        }
        else if (cbCurve1.getAnchor2().equals(cbCurve2.getAnchor1())){
            result[0] = cbCurve1.getAnchor1();
            result[1] = cbCurve1.getControl1();
            result[2] = cbCurve1.getControl2();
            result[3] = cbCurve1.getAnchor2();

            result[4] = cbCurve2.getControl1();
            result[5] = cbCurve2.getControl2();
            result[6] = cbCurve2.getAnchor2();
        }
        else if (cbCurve1.getAnchor2().equals(cbCurve2.getAnchor2())){
            result[0] = cbCurve1.getAnchor1();
            result[1] = cbCurve1.getControl1();
            result[2] = cbCurve1.getControl2();
            result[3] = cbCurve1.getAnchor2();

            result[4] = cbCurve2.getControl2();
            result[5] = cbCurve2.getControl1();
            result[6] = cbCurve2.getAnchor1();
        }
        else {
            throw new NullPointerException("No matching anchor! There should be!");
        }
        return result;
    }

    public void mergeCloseCoordinates(ArrayList<Edge> edges){
        for (Edge e1 : edges) {
            for (Edge e2 : edges){
                if(e1.equals(e2)){
                    continue;
                }
                if(!e1.c1.equals(e2.c1) && e1.c1.distance(e2.c1) < distanceTolerance){
                    e2.c1 = e1.c1;
                }
                if(!e1.c1.equals(e2.c2) && e1.c1.distance(e2.c2) < distanceTolerance){
                    e2.c2 = e1.c1;
                }
                if(!e1.c2.equals(e2.c1) && e1.c2.distance(e2.c1) < distanceTolerance){
                    e2.c1 = e1.c2;
                }
                if(!e1.c2.equals(e2.c2) && e1.c2.distance(e2.c2) < distanceTolerance){
                    e2.c2 = e1.c2;
                }
            }
        }
    }


    public void readFromSvg(String path) {
        try {
            String parser = XMLResourceDescriptor.getXMLParserClassName();
            SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
            Document doc = f.createDocument(path);

            Element svgRoot = doc.getDocumentElement();
            NodeList children = svgRoot.getChildNodes();

            // read lines from SVG
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i).getNodeName().equals("line")) {
                    NamedNodeMap attribs = children.item(i).getAttributes();

                    HashMap<String, Double> coor = new HashMap<>();
                    int edgeType = 0;

                    for (int j = 0; j < attribs.getLength(); j++) {
                        String name = attribs.item(j).getNodeName();
                        if (name.equals("x1") || name.equals("y1") || name.equals("x2") || name.equals("y2")) {
                            coor.put(name, Double.parseDouble(attribs.item(j).getNodeValue()));

                        } else if (name.equals("style")) { //read what type of edge this is
                            String nodeValue = attribs.item(j).getNodeValue();
                            if (nodeValue.matches(".*stroke:(rgb\\(0,\\s?0,\\s?255\\)|#0000ff).*")) {
                                edgeType = 1;
                            } else if (nodeValue.matches(".*stroke:(rgb\\(255,\\s?255,\\s?0\\)|#ffff00).*")) {
                                edgeType = 2;
                            } else {
                                edgeType = 0;
                            }
                        }
                    }

                    Coordinate c1 = new Coordinate(coor.get("x1"), coor.get("y1"));
                    Coordinate c2 = new Coordinate(coor.get("x2"), coor.get("y2"));
                    Edge newEdge = new Edge(c1, c2, edgeType);

                    // filter out redundant edges
                    if (!containsEdge(newEdge, distanceTolerance)) {
                        edges.add(newEdge);
                    }

                }
            }

            // merge close coordinates
            mergeCloseCoordinates(edges);

            // extract nodes from edges
            getNodesFromEdges(edges);

        } catch (IOException ex) {
            System.out.println("Couldn't load SVG file");
            System.out.println(ex);
        }
    }
}
