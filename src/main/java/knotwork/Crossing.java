package knotwork;

import com.sun.corba.se.spi.transport.CorbaAcceptor;
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
    public int breakpoint = 0;


    public Crossing(Edge edge){
        this.edge = edge;
        pos = edge.midpoint;

        normVector = getNormVector(edge);

        KnotNode firstLeftNode = KnotNode.createFromNormVector(pos, normVector, false, this);
        KnotNode firstRightNode = KnotNode.createFromNormVector(pos, normVector, true, this);

        leftNodePair = new KnotNodePair(firstLeftNode);
        rightNodePair = new KnotNodePair(firstRightNode);
    }

    public void setBreakpoint(int type)
    {
        if (type == 0 || type == 1 || type == 2)
        {this.breakpoint = type;}
    }

    //Creates meta point if the crossing is marked as a break point
    public BaseNode getMetaPoint(BaseNode prevNode)
    {
        if (this.breakpoint == 1)
        {
            Coordinate posMeta = new Coordinate(this.pos.x + this.normVector.toCoordinate().x,
                    this.pos.y + this.normVector.toCoordinate().y);
            Coordinate negMeta = new Coordinate(this.pos.x - this.normVector.toCoordinate().x,
                    this.pos.y - this.normVector.toCoordinate().y);
            double posMetaDist = posMeta.distance(prevNode.pos);
            double negMetaDist = negMeta.distance(prevNode.pos);
            if(posMetaDist < negMetaDist)
            {
                //might need code to determine which in direction to rotate

               return new BaseNode(posMeta, this.normVector.rotateByQuarterCircle(1));
            }
            else
            {
                return new BaseNode(negMeta, this.normVector.rotateByQuarterCircle(1));
            }
        }
        else if (this.breakpoint == 2)
        {
            Coordinate posMeta = this.normVector.rotate(90).toCoordinate();
            Coordinate negMeta = this.normVector.rotate(180).toCoordinate();
            double posMetaDist = posMeta.distance(prevNode.pos);
            double negMetaDist = negMeta.distance(prevNode.pos);
            if(posMetaDist < negMetaDist)
            {
                //might need code to determine which in direction to rotate
                return new BaseNode(posMeta, this.normVector);
            }
            else
            {
                return new BaseNode(negMeta, this.normVector);
            }
        }
        else {return null;}
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

        return normVec.normalize();
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
