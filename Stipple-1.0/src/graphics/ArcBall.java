package graphics;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseListener;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Implements an ArcBall. The ball sits on top of the Canvas, allowing the user to
 * grab it with the mouse and drag it to rotate the ball as well as the model shown
 * on the canvas. The ongoing drag, as well as the outline of the ball is shown, while
 * dragging. The resulting drag may also be shown by pressing Caps-Lock. Using the
 * Ctrl, Shift or Ctrl+Shift keys will constrain the rotation to Camera, Body or User
 * defined Axes. While moving the mouse and pressing the keys, the selected constraint
 * axis will highlight. After pressing the mouse button (and a key), a rotation will
 * start, constrained by the selected axis.
 * <p>
 * Original ArcBall C code from Ken Shoemake, Graphics Gems IV, 1993.
 *
 * @author Mark Donszelmann
 * @version $Id: ArcBall.java,v 1.5 2002/12/13 17:40:47 duns Exp $
 */

public class ArcBall implements MouseMotionListener, MouseListener, KeyListener  {

    private double size;
    private Quaternion q0;
    private Quaternion qCurrent;

    private Component component;
    private boolean enabled = false;

    private static final Color RIMCOLOR = new Color(255, 255, 255);
    private static final Color FARCOLOR = new Color(195, 127, 31);
    private static final Color NEARCOLOR = new Color(255, 255, 63);
    private static final Color DRAGCOLOR = new Color(127, 255, 255);
    private static final Color RESCOLOR = new Color(195, 31, 31);

    private boolean showResult  = false;
    private boolean nextShowResult = !showResult;
    private boolean dragging    = false;
    private Vector3 from        = new Vector3(0,0,1);
    private Vector3 to          = new Vector3(0,0,1);
    private Vector3 fromResult  = new Vector3(0,0,1);
    private Vector3 toResult    = new Vector3(0,0,1);

    private static int NO_AXES = -1;
    private static int CAMERA_AXES = 0;
    private static int BODY_AXES = 1;
    private static int OTHER_AXES = 2;
    private Vector3[][] sets = {
          { // CAMERA_AXES
            new Vector3(1.0, 0.0, 0.0),
            new Vector3(0.0, 1.0, 0.0),
            new Vector3(0.0, 0.0, 1.0) },
          { // BODY_AXES
            new Vector3(1.0, 0.0, 0.0),
            new Vector3(0.0, 1.0, 0.0),
            new Vector3(0.0, 0.0, 1.0) },
          { // OTHER_AXES
            null,
            null,
            null }
        };
    private int axisSet = NO_AXES;
    private int axisIndex = 0;

    /**
     * Creates an ArcBall of size 0.8 to be placed on the given component
     */
    public ArcBall(Component component) {
        this(component, 0.8);
    }

    /**
     * Creates an ArcBall of given size to be placed on the given component
     */
    public ArcBall(Component component, double size) {
        this.size = size;
        this.component = component;
        q0 = new Quaternion();
        qCurrent = new Quaternion(0, 0, 0, 1);

        scale = new Scale(size, size, size);

        setEnabled(true);
    }

    /**
     * enables or disables the arcball
     */
    public void setEnabled(boolean on) {
        if (on && !enabled) {
            enabled = true;
            component.addKeyListener(this);
            component.addMouseListener(this);
            component.addMouseMotionListener(this);

            // FIXME: Enable this when we support only 1.3 and up!
//            showResult = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
            component.repaint();
        } else if (!on && enabled) {
            enabled = false;
            component.removeKeyListener(this);
            component.removeMouseListener(this);
            component.removeMouseMotionListener(this);
            component.repaint();
        }
    }

    /**
     * @return true if arcball is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets one of the user axes.
     *
     * @param index in range 0..2
     * @param axis
     */
    public void setOtherAxis(int index, Vector3 axis) {
        if ((index < 0) || (index > 2)) {
            throw new IllegalArgumentException("ArcBall.setOtherAxis: index should be in range 0..2: "+index);
        }
        sets[OTHER_AXES][index] = axis;
    }

    /**
     * @return an X screen coordinate between -1 and 1 for the
     * square part of the ball.
     */
    private double getX(MouseEvent event) {
        Rectangle d = event.getComponent().getBounds();
        double factor = 1.0;
        if (d.width > d.height) {
            factor = (double)d.width / d.height;
        }
        return (2.0*event.getX() - d.width)*factor/d.width;
    }

    /**
     * @return an Y screen coordinate between -1 and 1 for the
     * square part of the ball.
     */
    private double getY(MouseEvent event) {
        Rectangle d = event.getComponent().getBounds();
        double factor = 1.0;
        if (d.height > d.width) {
            factor = (double)d.height / d.width;
        }
        return (d.height - 2.0*event.getY())*factor/d.height;
    }

    /**
     * Ignored
     */
    public void mouseClicked(MouseEvent event) {
        // ignored
    }

    /**
     * Grabs keyboard focus
     */
    public void mouseEntered(MouseEvent event) {
        event.getComponent().requestFocus();
    }

    /**
     * Ignored
     */
    public void mouseExited(MouseEvent event) {
        // ignored
    }

    /**
     * Left mouse button will store start state of the drag, as well as showing the
     * constraint axis if any.
     */
    public void mousePressed(MouseEvent event) {
        if ((event.getModifiers() & MouseEvent.BUTTON1_MASK ) != 0) {
            ArcBallMath.projectToSphere(size, getX(event), getY(event), from);
            ArcBallMath.projectToSphere(size, getX(event), getY(event), to);
    	    if (axisSet != NO_AXES) {
    	        ArcBallMath.constrainToAxis(from, sets[axisSet][axisIndex], from);
    	        ArcBallMath.constrainToAxis(to, sets[axisSet][axisIndex], from);
            }

            q0.set(qCurrent);
            dragging = true;

            event.getComponent().repaint();
         }
    }

    /**
     * Left mouse button will store and if requested show the resulting dragged arc.
     */
    public void mouseReleased(MouseEvent event) {
        if ((event.getModifiers() & MouseEvent.BUTTON1_MASK ) != 0) {
            fromResult = from;
            toResult = to;
            dragging = false;

            event.getComponent().repaint();
        }
    }

    private Quaternion qDrag;

    /**
     * Left mouse button will update the (matrix) state of the ArcBall, and show
     * the dragged (constraint) path.
     */
    public void mouseDragged(MouseEvent event) {
        if ((event.getModifiers() & MouseEvent.BUTTON1_MASK ) != 0) {
            ArcBallMath.projectToSphere(size, getX(event), getY(event), to);

    	    if ((axisSet != NO_AXES) && (axisIndex != -1)) {
    	        ArcBallMath.constrainToAxis(to, sets[axisSet][axisIndex], to);
    	    }
            qDrag = ArcBallMath.buildQuaternion(size, from, to, qDrag);
            if (qDrag != null) {
                q0.multiply(qDrag, qDrag);
                setState(qDrag);
                event.getComponent().repaint();
            }
        }
    }

    /**
     * Moving the mouse will highlight (if a key is pressed) one of the constraint axes.
     */
    public void mouseMoved(MouseEvent event) {
    	if (axisSet != NO_AXES) {
            ArcBallMath.projectToSphere(size, getX(event), getY(event), to);
    	    axisIndex = ArcBallMath.nearestConstraintAxis(to, sets[axisSet]);
            event.getComponent().repaint();
    	}
    }

    /**
     * Handles the pressing of the Ctrl, Shift and Caps-Lock keys
     */
    public void keyPressed(KeyEvent event) {
        boolean repaint = handleKeyEvent(event);

        // handle caps lock
        if (event.getKeyCode() == KeyEvent.VK_CAPS_LOCK) {
            repaint |= (showResult != nextShowResult);
            showResult = nextShowResult;
        }

        if (repaint) {
            event.getComponent().repaint();
        }
    }

    /**
     * Handles the release of the Ctrl, Shift and Caps-Lock keys
     */
    public void keyReleased(KeyEvent event) {
        boolean repaint = handleKeyEvent(event);

        // handle caps lock
        if (event.getKeyCode() == KeyEvent.VK_CAPS_LOCK) {
            nextShowResult = !nextShowResult;
        }

        if (repaint) {
            event.getComponent().repaint();
        }
    }

    /**
     * Ignored
     */
    public void keyTyped(KeyEvent event) {
        // ignored
    }

    /**
     * @return true if a repaint is needed for this particular key event.
     */
    private boolean handleKeyEvent(KeyEvent event) {
        boolean repaint = false;
        if (!dragging) {
            if (event.isControlDown() && event.isShiftDown()) {
                repaint = (axisSet != OTHER_AXES);
                axisSet = OTHER_AXES;
            } else if (event.isControlDown()) {
                repaint = (axisSet != BODY_AXES);
                axisSet = BODY_AXES;
            } else if (event.isShiftDown()) {
                repaint = (axisSet != CAMERA_AXES);
                axisSet = CAMERA_AXES;
            } else {
                repaint = (axisSet != NO_AXES);
                axisSet = NO_AXES;
            }
        }
        return repaint;
    }

    private final static int RENORMCOUNT = 97;
    private int count=0;
    private Matrix4 ssMatrix;

    /**
     * Sets the current (matrix) state, renormalizing for errors. The BodyAxes are also updated.
     */
    private void setState(Quaternion q) {
        qCurrent.set(q);
        if (count++ > RENORMCOUNT) {
            count = 0;
            qCurrent.normalize(qCurrent);
        }

        // update BodyAxes
        ssMatrix = qCurrent.toMatrix(ssMatrix);
        sets[BODY_AXES][0].x = ssMatrix.m00;
        sets[BODY_AXES][0].y = ssMatrix.m01;
        sets[BODY_AXES][0].z = ssMatrix.m02;

        sets[BODY_AXES][1].x = ssMatrix.m10;
        sets[BODY_AXES][1].y = ssMatrix.m11;
        sets[BODY_AXES][1].z = ssMatrix.m12;

        sets[BODY_AXES][2].x = ssMatrix.m20;
        sets[BODY_AXES][2].y = ssMatrix.m21;
        sets[BODY_AXES][2].z = ssMatrix.m22;
    }

    /**
     * @return the size of the ArcBall.
     */
    public double getBallSize() {
        return size;
    }

    private Matrix4 identity = Matrix4.identity();
    private Ortho ortho = new Ortho(-1, 1, -1, 1, 0, 0);
    private Scale scale;
//    private Translate translate = new Translate(0.2, 0.2, 0);
    private Polyline3 cameraCircle;
//    private Polyline3 modelCircle;
    private Polyline3 resultArc;
    private Polyline3 dragArc;
    private Vector out = new Vector(25);

    /**
     * @return an enumeration of drawables of the controller with all its (constraint) arcs.
     */
    public Enumeration getDrawables(Matrix4 m) {
        out.setSize(0);
        out.addElement(identity);
        out.addElement(ortho);
        out.addElement(scale);
        out.addElement(RIMCOLOR);
        out.addElement(cameraCircle = Polyline3.circle(1.0, cameraCircle));
//        out.addElement(translate);
//        out.addElement(modelCircle = Polyline3.circle(1.0, modelCircle));

        // resulting arc
        if (showResult) {
            out.addElement(RESCOLOR);
            out.addElement(resultArc = getAnyArc(fromResult, toResult, resultArc));
        }

        // contraining arcs
        addConstraints(out);

        // dragging arc
        if (dragging) {
            out.addElement(DRAGCOLOR);
            out.addElement(dragArc = getAnyArc(from, to, dragArc));
        }

        return out.elements();
    }

    /**
     * add all constraint arcs.
     */
    private void addConstraints(Vector out) {
        if (axisSet != NO_AXES) {
            Vector3[] set = sets[axisSet];
            for (int i=0; i<sets[axisSet].length; i++) {
        	    if (i != axisIndex) {
        	        if (dragging) {
        	            continue;
        	        }
        	        out.addElement(FARCOLOR);
        	    } else {
        	        out.addElement(NEARCOLOR);
        	    }
        	    Vector3 axis = sets[axisSet][i];
        	    if (axis != null) {
            	    if ((axis.x == 0.0) && (axis.y == 0.0)) {
            	        out.addElement(Polyline3.circle(1.0, null));
            	    } else {
            	        out.addElement(getHalfArc(axis, null));
            	    }
            	}
            }
        }
    }


    private static final int arcPoints = 17;

    /**
     * @return an arc defined by its ends. The Polyline3 result is contained in the first 17 entries.
     */
    private Polyline3 getAnyArc(Vector3 from, Vector3 to, Polyline3 r) {
        if (r == null) r = new Polyline3(arcPoints);
        int n = r.size();
        if (n < arcPoints) {
            for (int i=n; i<arcPoints; i++) {
                r.add(0, 0, 0);
            }
            n = arcPoints;
        }
        double length = from.length();
        from.normalize(r.p[0]);
        to.normalize(r.p[1]);
        r.p[n-1].set(r.p[0].x, r.p[0].y, r.p[0].z);
        for (int i=0; i<(n-1)/((n-1) >>> 2); i++) {
            r.p[0].bisect(r.p[1], r.p[1]);
        }
        double dot = 2.0*r.p[0].dot(r.p[1]);
        for (int i=2; i<n; i++) {
            r.p[i-1].scale(dot, r.p[i]);
    	    r.p[i].sub(r.p[i-2], r.p[i]);
        }

        return r;
    }

    private Vector3 ghaP = new Vector3();
    private Vector3 ghaCross = new Vector3();
    private Polyline3 ghaF;
    private Polyline3 ghaS;
    /**
     * @return the arc of a semi-circle defined by its axis.
     */
    private Polyline3 getHalfArc(Vector3 n, Polyline3 r) {
        if (r == null) r = new Polyline3(arcPoints*2);
        if (n.z != 1.0) {
    	    ghaP.set(n.y, -n.x, 0.0);
    	    ghaP.normalize(ghaP);
        } else {
    	    ghaP.set(0.0, 1.0, 0.0);
        }
        ghaP.cross(n, ghaCross);
        ghaF = getAnyArc(ghaP, ghaCross, ghaF);
        ghaP.negate(ghaP);
        ghaS = getAnyArc(ghaCross, ghaP, ghaS);
        r.clear();
        r.add(ghaF);
        r.add(ghaS);
        return r;
    }

    /**
     * @return a matrix representation of the current quaternion
     */
    public Matrix4 toMatrix() {
        return qCurrent.toMatrix(null);
    }

    /**
     * @return the current quaternion
     */
    public Quaternion toQuaternion() {
        return qCurrent;
    }
}
