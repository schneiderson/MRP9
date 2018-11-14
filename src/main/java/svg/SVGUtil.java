package svg;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLResourceDescriptor;
import org.locationtech.jts.geom.Coordinate;
import org.w3c.dom.*;

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

    public SVGUtil(ArrayList<Edge> edges, ArrayList<Coordinate> nodes){
        if(edges != null){
            for(Edge e : edges){
                if(!containsEdge(e)){
                    this.edges.add(e);
                }
            }
        }
        if(nodes != null){
            this.nodes = nodes;
        } else {
            getNodesFromEdges(this.edges);
        }
    }


    private void getNodesFromEdges(ArrayList<Edge> edges){
        for(Edge e : edges){
            if(!containsNode(e.c1)){
                this.nodes.add(e.c1);
            }
            if(!containsNode(e.c2)){
                this.nodes.add(e.c2);
            }
        }
    }


    private Boolean containsEdge(Edge edge){
        for(Edge e : edges){
            if(e.equals(edge)){
                return true;
            }
        }
        return false;
    }


    private Boolean containsNode(Coordinate node){
        for(Coordinate n : nodes){
            if(n.equals(node)){
                return true;
            }
        }
        return false;
    }


    public void createSVG(String path){

        try{

            // find largest x and y coordinates
            double x_max = 0, y_max = 0;
            for (Coordinate node: nodes) {
                x_max = max(x_max, node.x);
                y_max = max(y_max, node.y);
            }

            DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
            String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
            Document doc = impl.createDocument(svgNS, "svg", null);

            // get the root element (the svg element)
            Element svgRoot = doc.getDocumentElement();

            // set the width and height attribute on the root svg element
            svgRoot.setAttributeNS(null, "width", Double.toString(Math.ceil(x_max + 10)));
            svgRoot.setAttributeNS(null, "height", Double.toString(Math.ceil(y_max + 10)));

            // create the line for each edge
            for (Edge e: edges) {

                Element line = doc.createElementNS(svgNS, SVGConstants.SVG_LINE_TAG);
                line.setAttributeNS(null, SVGConstants.SVG_X1_ATTRIBUTE, Double.toString(e.c1.x));
                line.setAttributeNS(null, SVGConstants.SVG_Y1_ATTRIBUTE, Double.toString(e.c1.y));
                line.setAttributeNS(null, SVGConstants.SVG_X2_ATTRIBUTE, Double.toString(e.c2.x));
                line.setAttributeNS(null, SVGConstants.SVG_Y2_ATTRIBUTE, Double.toString(e.c2.y));

                line.setAttributeNS(null, SVGConstants.SVG_STYLE_TAG, "stroke:rgb(0,0,0);stroke-width:1");

                // attach the line to the svg root element
                svgRoot.appendChild(line);
            }

            // write svg file
            OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(path)));
            javax.xml.transform.Result result = new StreamResult(out);
            // Use a transformer for pretty printing
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.setOutputProperty(OutputKeys.INDENT, "yes");
            xformer.transform(new DOMSource(doc), result);
            out.flush();
            out.close();

        } catch ( Exception e){
            System.out.println("Couldn't save SVG file");
            System.out.println(e);
        }
    }


    public void readFromSvg(String path){
        try {
            String parser = XMLResourceDescriptor.getXMLParserClassName();
            SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
            Document doc = f.createDocument(path);

            Element svgRoot = doc.getDocumentElement();
            NodeList children = svgRoot.getChildNodes();

            // read lines from SVG
            for(int i = 0; i < children.getLength(); i++){
                if(children.item(i).getNodeName() == "line"){
                    NamedNodeMap attribs = children.item(i).getAttributes();

                    HashMap<String, Double> coor = new HashMap<>();

                    for(int j = 0; j < attribs.getLength(); j++){
                        String name = attribs.item(j).getNodeName();
                        if(name == "x1" || name == "y1" || name == "x2" || name == "y2"){
                            coor.put(name, Double.parseDouble(attribs.item(j).getNodeValue()));
                        }
                    }

                    Coordinate c1 = new Coordinate(coor.get("x1"), coor.get("y1"));
                    Coordinate c2 = new Coordinate(coor.get("x2"), coor.get("y2"));
                    Edge newEdge = new Edge(c1, c2);

                    if(!containsEdge(newEdge)){
                        edges.add(newEdge);
                    }

                }
            }

            getNodesFromEdges(edges);

        } catch (IOException ex) {
            System.out.println("Couldn't load SVG file");
            System.out.println(ex);
        }
    }
}
