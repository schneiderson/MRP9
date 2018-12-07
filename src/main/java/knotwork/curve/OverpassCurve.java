package knotwork.curve;

public class OverpassCurve {

    private Curve curve1;
    private Curve curve2;
    private int id;

    public OverpassCurve(Curve curve1, Curve curve2, int id){
        this.curve1 = curve1;
        this.curve2 = curve2;
        this.id = id;
    }

    public Curve getCurve1() {
        return curve1;
    }

    public Curve getCurve2() {
        return curve2;
    }

    public int getId() {
        return id;
    }
}
