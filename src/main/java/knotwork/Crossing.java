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
    public KnotNodePair negMetaPair;
    public KnotNodePair posMetaPair;
    public Vector2D normVector;
    public Edge edge;
    public int breakpoint = 0;
    public double metaPointDistance = 20;


    public Crossing(Edge edge) {
        this.edge = edge;
        pos = edge.midpoint;
        breakpoint = edge.breakpoint;
        metaPointDistance = edge.getLength() / 5;

        normVector = getNormVector(edge);

        if(breakpoint == 0){ // regular crossing
            KnotNode firstLeftNode = KnotNode.createFromNormVector(pos, normVector, false, this);
            KnotNode firstRightNode = KnotNode.createFromNormVector(pos, normVector, true, this);

            leftNodePair = new KnotNodePair(firstLeftNode);
            rightNodePair = new KnotNodePair(firstRightNode);

        } else {
            if(breakpoint == 1){ // breakpoint type 1 (wall)
                KnotNode posRightNode = KnotNode.createMeta1FromNormVector(pos, normVector, true, this, false, metaPointDistance);
                KnotNode negRightNode = KnotNode.createMeta1FromNormVector(pos, normVector, true, this, true, metaPointDistance);

                posMetaPair = new KnotNodePair(posRightNode, false);
                negMetaPair = new KnotNodePair(negRightNode, false);

            } else { // breakpoint type 2 (ghost)
                KnotNode posRightNode = KnotNode.createMeta2FromNormVector(pos, normVector, true, this, false, metaPointDistance);
                KnotNode negRightNode = KnotNode.createMeta2FromNormVector(pos, normVector, true, this, true, metaPointDistance);

                posMetaPair = new KnotNodePair(posRightNode, false);
                negMetaPair = new KnotNodePair(negRightNode, false);

            }
        }
    }

    public KnotNodePair getPairByNode(KnotNode node) {
        if(breakpoint > 0){
            if(posMetaPair.contains(node)){
                return posMetaPair;
            } else if(negMetaPair.contains(node)){
                return negMetaPair;
            }
        } else {
            if (leftNodePair.contains(node)) {
                return leftNodePair;
            } else if (rightNodePair.contains(node)) {
                return rightNodePair;
            }
        }
        return null;
    }

    public KnotNodePair getPerpendicularPairByNode(KnotNode node) {
        if(breakpoint > 0) {
            return null;
        }

        if (leftNodePair.contains(node)) {
            return rightNodePair;
        } else if (rightNodePair.contains(node)) {
            return leftNodePair;
        }
        return null;
    }

    public KnotNodePair getPerpendicularPairByNodePair(KnotNodePair nodePair) {
        if (nodePair.equals(leftNodePair)) {
            return rightNodePair;
        } else if (nodePair.equals(rightNodePair)) {
            return leftNodePair;
        }
        return null;
    }


    /**
     * Returns metaPointPair
     */
    public KnotNodePair getMetaPointPair(KnotNode prevNode, Coordinate junction) {
        if (!this.hasBreakPoint()) {
            return null;
        }

        if(breakpoint == 1){ // breakpoint type 1 (wall)
            Vector2D vec = Vector2D.create(pos, junction);

            if( vec.angleTo(posMetaPair.node1.getVector()) == 0 ) {
                if(posMetaPair.node1.isLeftNode() && prevNode.isRightNode()
                        || posMetaPair.node1.isRightNode() && prevNode.isLeftNode()){
                    return posMetaPair;
                }
            } else if( vec.angleTo(posMetaPair.node2.getVector()) == 0 ){
                if(posMetaPair.node2.isLeftNode() && prevNode.isRightNode()
                        || posMetaPair.node2.isRightNode() && prevNode.isLeftNode()){
                    return posMetaPair;
                }
            }

            return negMetaPair;

        } else { // breakpoint type 2 (ghost)
            double distancePos = prevNode.getPos().distance(normVector.translate(pos));
            double distanceNeg = prevNode.getPos().distance(normVector.multiply(-1.0).translate(pos));

            if(distancePos > distanceNeg){
                if(prevNode.isRightNode()){
                    return posMetaPair;
                } else {
                    return negMetaPair;
                }
            } else {
                if(prevNode.isRightNode()){
                    return negMetaPair;
                } else {
                    return posMetaPair;
                }
            }
        }

    }

    public static Vector2D getNormVector(Edge edge) {
        // get absolute angle (in rad)
        Double absAngleRad = abs(Angle.angle(edge.c1, edge.c2));

        // get Norm vector
        Vector2D normVec;
        if (absAngleRad >= PI / 2) {
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
    public Double getNormVectorAngleRad(boolean normalized) {
        if (normalized) {
            return normVector.angle();
        } else {
            return AngleUtil.getAngleRadiansRescaled(normVector.angle());
        }
    }

    /**
     * Returns the angle of the vector in degree
     *
     * @param normalized If set to 'true' angle is normalized to be in the range ( -180, 180 ].
     * @return angle in degree
     */
    public Double getNormVectorAngleDeg(boolean normalized) {
        return Angle.toDegrees(getNormVectorAngleRad(normalized));
    }

    public boolean hasBreakPoint() {
        return (this.breakpoint == 1 || this.breakpoint == 2);
    }
}
