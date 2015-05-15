package by.klnvch.link5dots;

public class Offset{
	
	private final int x;
	private final int y;
	private int type;
	private int number;
	
	public static final int EMPTY = 1;
	public static final int USER = 2;
	public static final int OPPONENT = 4;
	
	public static final String THE_DOT = "the_dot";
	
	public Offset(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public int getX(){
		return x;
	}
	public int getY() {
		return y;
	}
	public void changeStatus(int type, int number){
		this.type = type;
		this.number = number;
	}
	public int getType() {
		return type;
	}
	public int getNumber() {
		return number;
	}
	@Override
	public String toString() {
		return x + "," + y;
	}
	
	public static String toString(int x, int y){
		return x + "," + y;
	}
	
	public static Offset parseString(String str){
		String[] tokens = str.split(",");
		int x = Integer.parseInt(tokens[0]);
		int y = Integer.parseInt(tokens[1]);
		return new Offset(x, y);
	}
}
