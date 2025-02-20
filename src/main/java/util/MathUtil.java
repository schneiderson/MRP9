package util;

import org.locationtech.jts.geom.Coordinate;

import java.util.*;

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

    public static <T> Iterator<T> cycle(List<T> list) {
        Iterator x = new Iterator<T>() {
            int count = -1;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public T next() {
                count++;
                count = count % list.size();
                return list.get(count);
            }
        };
        return x;
    }

    public static <T> Iterator<T> cycle(T[] array){
        return cycle(Arrays.asList(array));
    }
}
