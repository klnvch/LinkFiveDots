package by.klnvch.link5dots;

public class Dot {

    public static final String TYPE = "TYPE";
    public static final String TIMESTAMP = "TIMESTAMP";
    public static final int EMPTY = 1;
    public static final int USER = 2;
    public static final int OPPONENT = 4;
    private final int x;
    private final int y;
    private int type;
    private int number;
    private long timestamp;

    public Dot(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Dot(int x, int y, int type, long timestamp) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.timestamp = timestamp;
    }

    public static Dot parseString(String str) {
        String[] tokens = str.split(",");
        int x = Integer.parseInt(tokens[0]);
        int y = Integer.parseInt(tokens[1]);
        return new Dot(x, y);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void changeStatus(int type, int number) {
        this.type = type;
        this.number = number;
        this.timestamp = System.currentTimeMillis();
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getNumber() {
        return number;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return x + "," + y;
    }

    public Dot copy() {
        Dot dot = new Dot(x, y);
        dot.type = type;
        dot.number = number;
        return dot;
    }
}