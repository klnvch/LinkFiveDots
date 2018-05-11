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

import java.io.Serializable;

@SuppressWarnings({"unused"})
public class HighScore implements Serializable {

    public static final String TAG = "HighScore";

    public static final int WON = 1;
    public static final int LOST = 2;

    public static final String SCORE = "score";
    private static final long L_1 = 1;
    private static final long L_2000 = 2000;
    private static final long L_4294 = 4294;
    private static final long L_967295 = 967295;
    private static final long L_1000000 = 1000000;

    private String userId;
    private String androidId;
    private String username;
    private long score;
    private int moves;
    private long time;
    private int status;
    private long timestamp;

    public HighScore() {

    }

    public HighScore(int moves, long time, long status) {
        this.moves = moves;
        this.time = time;
        this.status = (int) status;
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getMoves() {
        return moves;
    }

    public void setMoves(int moves) {
        this.moves = moves;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAndroidId() {
        return androidId;
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
    public void code() {
        final long tempTime;
        if (time < L_1) tempTime = L_1;
        else if (time > L_967295) tempTime = L_967295;
        else tempTime = time;

        final long tempMoves;
        if (moves < L_1) tempMoves = L_1;
        else if (moves > L_2000) tempMoves = L_2000;
        else tempMoves = moves;

        if (status == WON) {
            score = tempMoves * L_1000000 + tempTime;
        } else {
            score = (L_4294 - tempMoves) * L_1000000 + tempTime;
        }
    }
/*
    //Initialize: score
    //            time
    //            status
    public void decode() {
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
*/
}