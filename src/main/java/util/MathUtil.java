package util;

import org.locationtech.jts.geom.Coordinate;

public class MathUtil {

    public static double round(double number, double decimals){
        return Math.round(number * Math.pow(10d, decimals)) /  Math.pow(10d, decimals);
    }

    public static Coordinate roundCoordinate(Coordinate coordinate, double decimals){
        Coordinate result = new Coordinate();
        result.x = round(coordinate.x, decimals);
        result.y = round(coordinate.y, decimals);
        return result;
    }
}
