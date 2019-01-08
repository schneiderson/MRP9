package knotwork;

import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.math.Vector2D;
import util.AngleUtil;

import java.util.ArrayList;

import static java.lang.Math.PI;
import static java.lang.Math.abs;

public class Crossing {

    public Coordinate pos;
    public KnotNodePair leftNodePair;
    public KnotNodePair rightNodePair;
    public ArrayList<KnotNode> metaPoints = new ArrayList<KnotNode>();
    public Vector2D normVector;
    public Edge edge;
    public int breakpoint = 0;

    public Crossing(Edge edge){
        this.edge = edge;
        pos = edge.midpoint;
        this.breakpoint = edge.breakpoint;

        normVector = getNormVector(edge);

        KnotNode firstLeftNode = KnotNode.createFromNormVector(pos, normVector, false, this);
        KnotNode firstRightNode = KnotNode.createFromNormVector(pos, normVector, true, this);

        leftNodePair = new KnotNodePair(firstLeftNode);
        rightNodePair = new KnotNodePair(firstRightNode);
    }

    public KnotNodePair getPairByNode(KnotNode node)
    {
        if(leftNodePair.contains(node)){
            return leftNodePair;
        } else if(rightNodePair.contains(node)){
            return rightNodePair;
        }
        return null;
    }

    public KnotNodePair getPerpendicularPairByNode(KnotNode node){
        if(leftNodePair.contains(node)){
            return rightNodePair;
        } else if(rightNodePair.contains(node)){
            return leftNodePair;
        }
        return null;
    }

    public KnotNodePair getPerpendicularPairByNodePair(KnotNodePair nodePair){
        if(nodePair.equals(leftNodePair)){
            return rightNodePair;
        } else if(nodePair.equals(rightNodePair)){
            return leftNodePair;
        }
        return null;
    }

    private void setBreakPointPair()
    {
        if (this.breakpoint == 1)
        {
            Coordinate posMeta = new Coordinate(this.pos.x + this.normVector.toCoordinate().x,
                    this.pos.y + this.normVector.toCoordinate().y);
            Coordinate negMeta = new Coordinate(this.pos.x - this.normVector.toCoordinate().x,
                    this.pos.y - this.normVector.toCoordinate().y);
            //two vector directions are possible
            Vector2D v1 =  this.normVector.rotateByQuarterCircle(1);
            Vector2D v2 =  this.normVector.rotateByQuarterCircle(3);
        }
        else if (this.breakpoint == 2)
        {

        }
    }

    //Creates meta point if the crossing is marked as a break point
    public KnotNode getMetaPoint(KnotNode prevNode)
    {
        if (!this.hasBreakPoint()) {return null;}

        Vector2D originVec = new Vector2D(prevNode.pos, this.pos);
        Coordinate posMeta, negMeta;
        Vector2D v1, v2;
        boolean rightNode;
        if (this.breakpoint == 1) // type 1: wall
        {
            rightNode = prevNode.isRightNode();
            posMeta = new Coordinate(this.pos.x + this.normVector.toCoordinate().x,
                    this.pos.y + this.normVector.toCoordinate().y);
            negMeta = new Coordinate(this.pos.x - this.normVector.toCoordinate().x,
                    this.pos.y - this.normVector.toCoordinate().y);
            //two vector directions are possible
            v1 =  this.normVector.rotateByQuarterCircle(1);
            v2 =  this.normVector.rotateByQuarterCircle(3);
        }
        else // type 2: ghost
        {
            rightNode = !prevNode.isRightNode();
            //Get the midpoints between the crossing and the edge ends
            posMeta = new Coordinate(Math.round((this.edge.c1.x + this.pos.x) / 2d),
                    Math.round((this.edge.c1.y + this.pos.y) / 2d));
            negMeta = new Coordinate(Math.round((this.edge.c2.x + this.pos.x) / 2d),
                    Math.round((this.edge.c2.y + this.pos.y) / 2d));
            //two vector directions are possible
            v1 =  this.normVector;
            v2 =  this.normVector.rotateByQuarterCircle(2);
        }
        Double angle1 = originVec.angleTo(v1);
        Double angle2 = originVec.angleTo(v2);
        Vector2D vec; Coordinate meta;
        if(abs(angle1) < abs(angle2)) { vec = v1; }
        else { vec = v2; }

        if(this.metaPoints.size() == 0) //if this is the first meta point
        {
            double posMetaDist = posMeta.distance(prevNode.pos);
            double negMetaDist = negMeta.distance(prevNode.pos);
            if(posMetaDist < negMetaDist) {meta = posMeta;}
            else {meta = negMeta;}

            KnotNode newMeta = new KnotNode(meta, vec, rightNode, this);
            this.metaPoints.add(newMeta);
            return newMeta;
        }
        else //if a meta point has already been used
        {
            if(this.metaPoints.contains(posMeta))
            {
                this.metaPoints.contains(negMeta);
                return new KnotNode(negMeta, vec, rightNode, this);
            }
            else if (this.metaPoints.contains(negMeta))
            {
                this.metaPoints.contains(posMeta);
                return new KnotNode(posMeta, vec, rightNode, this);
            }
        }
        return null;
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

    public boolean hasBreakPoint()
    {
        return (this.breakpoint == 1 || this.breakpoint == 2);
    }
}
