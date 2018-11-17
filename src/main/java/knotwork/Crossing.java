package knotwork;

import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.math.Vector2D;
import util.AngleUtil;

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
            normVec = Vector2D.create(edge.c1, edge.c2).rotate(Angle.toRadians(90));
        } else {
            normVec = Vector2D.create(edge.c1, edge.c2).rotate(Angle.toRadians(-90));
        }

        return normVec;
    }

    /**
     * Returns the angle of the vector in radian
     *
     * @param normalized If set to 'true' angle is normalized to be in the range ( -Pi, Pi ].
     * @return angle in radian
     */
    public Double getNormVectorAngleRad(boolean normalized){
        if(normalized){
            return normVector.angle();
        } else{
            return AngleUtil.getAngleRadiansRescaled(normVector.angle());
        }
    }

    /**
     * Returns the angle of the vector in degree
     *
     * @param normalized If set to 'true' angle is normalized to be in the range ( -180, 180 ].
     * @return angle in degree
     */
    public Double getNormVectorAngleDeg(boolean normalized){
        return Angle.toDegrees(getNormVectorAngleRad(normalized));
    }


}
