package by.klnvch.link5dots;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

class Game {
	
	private static final String START_TIME = "START_TIME";
	private static final String MOVES_DONE = "MOVES_DONE";
    private static final String HIGH_SCORE = "HIGH_SCORE";
	
	private final int n = 20;
	private final int m = 20;
	public final Offset[][] net;
	
	private HighScore currentScore = null;
	
	private ArrayList<Offset> winningLine = null;
	
	private int movesDone = 0;
	
	private long startTime;
	
	//masks
	
	public Game() {
		
		net = new Offset[n][m];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				net[i][j] = new Offset(i, j);
			}
		}
	}
	
	public void restore(SharedPreferences pref){
		//
		startTime = pref.getLong(START_TIME, 0);
		//
		movesDone = pref.getInt(MOVES_DONE, 0);
		//restore dots table
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				int type = pref.getInt("dottype(" + Integer.toString(i)+ "," + Integer.toString(j) + ")", Offset.EMPTY);
				int number = pref.getInt("dotnum(" + Integer.toString(i)+ "," + Integer.toString(j) + ")", -1);
				
				net[i][j].changeStatus(type, number);
			}
		}
        //
        final long currentScoreCode = pref.getLong(HIGH_SCORE, -1);
        if(currentScoreCode != -1){
            currentScore = new HighScore(currentScoreCode);
        }
		
		//other
		winningLine = isOver();
	}
	public void save(SharedPreferences pref){
		Editor editor = pref.edit();
		//
		editor.putLong(START_TIME, startTime);
		//
		editor.putInt(MOVES_DONE, movesDone);
		//save info about dots
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				editor.putInt("dottype(" + Integer.toString(i)+ "," + Integer.toString(j) + ")", net[i][j].getType());
				editor.putInt("dotnum(" + Integer.toString(i)+ "," + Integer.toString(j) + ")", net[i][j].getNumber());
			}
		}
        //
        if(currentScore != null) {
            editor.putLong(HIGH_SCORE, currentScore.code());
        }else{
            editor.putLong(HIGH_SCORE, -1);
        }
		//
		editor.apply();
	}
	public void reset() {
		
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				net[i][j].changeStatus(Offset.EMPTY, -1);
			}
		}
		
		movesDone = 0;
		
		winningLine = null;
	}
	private void prepareScore(){
		long time = System.currentTimeMillis()/1000 - startTime;
		
		if(winningLine != null){
			
			if(winningLine.get(0).getType() == Offset.USER){
				currentScore = new HighScore(movesDone, time, HighScore.WON);
			}else{
				currentScore = new HighScore(getNumberOfMoves(), time, HighScore.LOST);
			}
			
		}
	}

	public void setDot(int x, int y, int type){

        Offset theLastDot = getLastDot();
        if (!checkCorrectness(x, y) || (theLastDot != null && theLastDot.getType() == type)) {
            return;
        }

        if(getNumberOfMoves() == 0){//it is the first move, start stop watch
			startTime = System.currentTimeMillis() / 1000;
		}
		if(type == Offset.USER){
			movesDone++;
		}
		net[x][y].changeStatus(type, getNumberOfMoves());
		winningLine = isOver();
			
		if(winningLine != null){
			prepareScore();
		}
	}
	private int getNumberOfMoves(){
		Offset offset = getLastDot();
		if(offset != null){
			return offset.getNumber() + 1;
		}
		return 0;
	}
    public boolean checkCorrectness(int x, int y) {
        return isInBound(x, y) && net[x][y].getType() == Offset.EMPTY && winningLine == null;
    }
	private boolean isInBound(int x,int y) {
		return x>=0 && y>=0 && x<n && y<m;
	}
	private ArrayList<Offset> getDotsNumber(Offset dot,int dx,int dy){
		
		int x = dot.getX();
		int y = dot.getY();
		
		ArrayList<Offset> result = new ArrayList<>();
		result.add(dot);
		
		for (int k = 1; (k < 5)&&isInBound(x+dx*k, y+dy*k)&&net[x+dx*k][y+dy*k].getType()==dot.getType(); k++) {
            result.add(net[x+dx*k][y+dy*k]);
        }
		for (int k = 1; (k < 5)&&isInBound(x-dx*k, y-dy*k)&&net[x-dx*k][y-dy*k].getType()==dot.getType(); k++){
            result.add(0, net[x-dx*k][y-dy*k]);
        }
		
		return result;
	}
	/**
	 * Checks if five dots line has been built
	 * 
	 * @return null or array of five dots
	 * 
	 */
	public ArrayList<Offset> isOver(){
		
		if(winningLine == null){
			if(getNumberOfMoves() < 5)	return null;
		
			Offset lastDot = getLastDot();
			
			if(lastDot == null)	return null;

			ArrayList<Offset> result;
		
			result = getDotsNumber(lastDot, 1, 0);
			if (result.size() >= 5){
				winningLine = result;
				return result;
			}
		
			result = getDotsNumber(lastDot, 1, 1);
			if (result.size() >= 5){
				winningLine = result;
				return result;
			}
		
			result = getDotsNumber(lastDot, 0, 1);
			if (result.size() >= 5){
				winningLine = result;
				return result;
			}
		
			result = getDotsNumber(lastDot, -1, 1);
			if (result.size() >= 5){
				winningLine = result;
				return result;
			}
			
			return null;
		}else{
			return winningLine;
		}
	}
	public void undo(int moves) {
		
		for (int i=0; i!= moves; ++i) {
			Offset d = getLastDot();
            if (d != null) {
                d.changeStatus(Offset.EMPTY, -1);
            }
		}
			
		winningLine = null;
		winningLine = isOver();
			
	}
	public HighScore getCurrentScore(){
		return currentScore;
	}
	public Offset getLastDot(){
		
		Offset result = net[0][0];
		
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				if(result.getNumber() < net[i][j].getNumber()){
					result = net[i][j];
				}
			}
		}
		
		if(result.getNumber() == -1)	return null;
		else							return result;
	}

	@Override
	public String toString() {
		String result = "";
    	for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				result += net[i][j] + " ";
			}
			result += "\n";
    	}
		return result;
	}
}
