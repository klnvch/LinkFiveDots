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

package by.klnvch.link5dots.multiplayer;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;

import by.klnvch.link5dots.models.Dot;

public class GameMessage {

    static final int MSG_NEW_GAME = 1;
    static final int MSG_USERNAME = 2;
    static final int MSG_DOT = 3;
    private static final int MSG_INVALID = 0;
    private final int msg;
    private transient Object obj;
    private String jsonObj;

    GameMessage(int msg) {
        this.msg = msg;
        this.obj = null;
    }

    GameMessage(int msg, @NonNull Object obj) {
        this.msg = msg;
        this.obj = obj;
    }

    @NonNull
    static GameMessage fromBytes(@Nullable byte[] bytes, int byteCount) {
        if (bytes == null) return new GameMessage(MSG_INVALID);

        String json = new String(bytes, 0, byteCount);
        GameMessage gameMessage = new Gson().fromJson(json, GameMessage.class);
        switch (gameMessage.msg) {
            case MSG_NEW_GAME:
                gameMessage.obj = null;
                break;
            case MSG_USERNAME:
                gameMessage.obj = new Gson().fromJson(gameMessage.jsonObj, String.class);
                break;
            case MSG_DOT:
                gameMessage.obj = new Gson().fromJson(gameMessage.jsonObj, Dot.class);
                break;
        }
        gameMessage.jsonObj = null;
        return gameMessage;
    }

    public int getMsg() {
        return msg;
    }

    @NonNull
    Object getObj() {
        if (obj != null) {
            return obj;
        } else {
            throw new RuntimeException();
        }
    }

    @NonNull
    byte[] toBytes() {
        this.jsonObj = new Gson().toJson(obj);
        String json = new Gson().toJson(this);
        return json.getBytes();
    }
}