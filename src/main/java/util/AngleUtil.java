package util;

import static java.lang.Math.PI;

public class AngleUtil {

    /**
     * Returns the rescaled angle in radian
     *
     * @param angle Angle to be rescaled between ( Pi, 2Pi ].
     * @return angle in radian
     */
    public static Double getAngleRadiansRescaled(Double angle){
        if(angle < 0){
            angle = 2 * PI + angle;
        }
        return angle;
    }

}
