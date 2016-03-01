package by.klnvch.link5dots;

import org.json.JSONException;
import org.json.JSONObject;

public class HighScore {

    public static final long WON = 1;
    public static final long LOST = 2;

    public static final String GAME_STATUS = "GAME_STATUS";
    public static final String NUMBER_OF_MOVES = "NUMBER_OF_MOVES";
    public static final String ELAPSED_TIME = "ELAPSED_TIME";

    private static final String DEVICE_ID = "id";
    private static final String USER_NAME = "username";
    private static final String SCORE = "score";

    private String id;
    private String username;
    private long score;
    private long time;
    private long status;

    public HighScore(String id, String username, long score, long time, long status) {

        this.id = id;
        this.username = username;
        this.score = score;
        this.time = time;
        this.status = status;
    }

    public HighScore(long score, long time, long status) {

        this.score = score;
        this.time = time;
        this.status = status;
    }

    public HighScore(long score) {

        decode(score);
    }

    public HighScore(JSONObject jsonObject) throws JSONException {

        if (jsonObject.has(DEVICE_ID)) {
            id = jsonObject.getString(DEVICE_ID);
        }

        if (jsonObject.has(USER_NAME)) {
            username = jsonObject.getString(USER_NAME);
        }

        if (jsonObject.has(SCORE)) {
            long score = jsonObject.getLong(SCORE);
            decode(score);
        }
    }

    public String getId() {
        return id;
    }

    public String getUserName() {
        return username;
    }

    /*
    public void setUserName(String username) {
        this.username = username;
    }
    */
    /*
    public void setDeviceId(String deviceId) {
		this.id = deviceId;
	}
	*/
    public long getScore() {
        return score;
    }

    public long getTime() {
        return time;
    }

    public long getStatus() {
        return status;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject result = new JSONObject();
        result.put(DEVICE_ID, id);
        result.put(USER_NAME, username);
        result.put(SCORE, code());
        return result;
    }
	/*
	public int compareTo(HighScore another) {
		return (int)(this.code() - another.code());
	}
	*/

    private static final long L_1 = 1;
    private static final long L_2000 = 2000;
    private static final long L_4294 = 4294;
    private static final long L_967295 = 967295;
    private static final long L_1000000 = 1000000;

    //
    //this function is needed for JSON objects
    //
    //max long value is 4294967295
    //
    //for won games           score =        s1*1000000 + time
    //for lost and unfinished score = (4294-s2)*1000000 + time
    //
    // 1 <= time <= 967295
    // 1 <= s1   <= 2000
    // 1 <= s2   <= 2000
    //
    long code() {

        if (time < L_1) time = L_1;
        if (time > L_967295) time = L_967295;
        if (score < L_1) score = L_1;
        if (score > L_2000) score = L_2000;

        if (status == WON) {
            return score * L_1000000 + time;
        } else {
            return (L_4294 - score) * L_1000000 + time;
        }
    }

    //Initialize: score
    //            time
    //            status
    private void decode(long score) {

        time = score % L_1000000;

        long temp = (score - time) / L_1000000;
        if (temp > L_2000) {
            status = LOST;
            this.score = L_4294 - temp;
        } else {
            status = WON;
            this.score = temp;
        }
    }
}
