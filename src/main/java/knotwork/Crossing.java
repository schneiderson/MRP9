package knotwork;

import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.math.Vector2D;

import static java.lang.Math.PI;
import static java.lang.Math.abs;

public class Crossing {

    public Coordinate pos;
    public KnotNodePair leftNodePair;
    public KnotNodePair rightNodePair;
    public Vector2D normVector;
    public Edge edge;


    public Crossing(Edge edge){
        this.edge = edge;
        pos = edge.midpoint;

        normVector = getNormVector(edge);

        KnotNode firstLeftNode = KnotNode.createFromNormVector(pos, normVector, false);
        KnotNode firstRightNode = KnotNode.createFromNormVector(pos, normVector, true);

        leftNodePair = new KnotNodePair(firstLeftNode);
        rightNodePair = new KnotNodePair(firstRightNode);
    }


    public static Vector2D getNormVector(Edge edge){
        // get absolute angle (in rad)
        Double absAngleRad = abs(Angle.angle(edge.c1, edge.c2));

        // get Norm vector
        Vector2D normVec;
        if(absAngleRad >= PI/2){
            System.out.println("Deg: " + Angle.toDegrees(Angle.angle(edge.c1, edge.c2)));
            normVec = Vector2D.create(edge.c1, edge.c2).rotate(Angle.toRadians(90));
        } else {
            normVec = Vector2D.create(edge.c1, edge.c2).rotate(Angle.toRadians(-90));
        }

        return normVec;
    }

    // TODO: maybe put this as helper function in separate util class
    /**
     * Returns the angle of the vector in radian
     *
     * @param normalized If set to 'true' angle is normalized to be in the range ( -Pi, Pi ].
     * @return angle in radian
     */
    public Double getNormVectorAngleRad(Boolean normalized){
        if(normalized){
            return normVector.angle();
        } else{
            Double angle = normVector.angle();
            if(angle < 0){
                angle = 2 * PI + angle;
            }
            return angle;
        }
    }

    /**
     * Returns the angle of the vector in degree
     *
     * @param normalized If set to 'true' angle is normalized to be in the range ( -180, 180 ].
     * @return angle in degree
     */
    public Double getNormVectorAngleDeg(Boolean normalized){
        Double tmp = getNormVectorAngleRad(normalized);
        return Angle.toDegrees(tmp);
    }


}
