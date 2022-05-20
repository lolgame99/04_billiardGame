package at.fhv.sysarch.lab4.game;

public class Cue {
    private final double startX;
    private final double startY;
    private double endX;
    private double endY;

    public Cue(double startX, double startY) {
        this.startX = startX;
        this.startY = startY;
        this.endY = startY;
        this.endX = startX;
    }

    public void setEnd(double x, double y){
        this.endX = x;
        this.endY = y;
    }

    public double getStartX() {
        return startX;
    }

    public double getStartY() {
        return startY;
    }

    public double getEndX() {
        return endX;
    }

    public double getEndY() {
        return endY;
    }
}
