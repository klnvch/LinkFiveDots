/*
 * MIT License
 *
 * Copyright (c) 2017 klnvch
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package by.klnvch.link5dots.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class HighScore implements Serializable {

    public static final String TAG = "HighScore";

    public static final int WON = 1;
    public static final int LOST = 2;

    private static final String USER_ID = "userId";
    private static final String ANDROID_ID = "androidId";
    private static final String USER_NAME = "username";
    private static final String SCORE = "score";
    private static final String TIMESTAMP = "timestamp";
    private static final long L_1 = 1;
    private static final long L_2000 = 2000;
    private static final long L_4294 = 4294;
    private static final long L_967295 = 967295;
    private static final long L_1000000 = 1000000;

    private String userId;
    private String androidId;
    private String username;
    private long score;
    private long time;
    private int status;

    public HighScore() {

    }

    HighScore(long score, long time, long status) {
        this.score = score;
        this.time = time;
        this.status = (int) status;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setAndroidId(String androidId) {
        this.androidId = androidId;
    }

    public String getUserName() {
        return username;
    }

    public void setUserName(String userName) {
        this.username = userName;
    }

    public long getScore() {
        return score;
    }

    public long getTime() {
        return time;
    }

    public int getStatus() {
        return status;
    }

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
    private long code() {

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
    public void decode(long score) {

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

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(USER_ID, userId);
        result.put(ANDROID_ID, androidId);
        result.put(USER_NAME, username);
        result.put(SCORE, code());
        result.put(TIMESTAMP, System.currentTimeMillis());
        return result;
    }
}