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

package by.klnvch.link5dots.multiplayer.online;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import by.klnvch.link5dots.models.Dot;

class Protocol {

    private static final int TYPE_INIT = 1;
    private static final int TYPE_DOT = 2;

    public static byte[] createInitMessage(@NonNull String name) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", TYPE_INIT);
            jsonObject.put("name", name);
            return jsonObject.toString().getBytes("UTF-8");
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] createDotMessage(@NonNull Dot dot) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", TYPE_DOT);
            jsonObject.put("x", dot.getX());
            jsonObject.put("y", dot.getY());
            return jsonObject.toString().getBytes("UTF-8");
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void parseMessage(@NonNull byte[] message, @NonNull OnParsedListener listener) {
        try {
            String msg = new String(message, "UTF-8");
            JSONObject jsonObject = new JSONObject(msg);
            switch (jsonObject.getInt("type")) {
                case TYPE_INIT:
                    listener.onInitMessage(jsonObject.getString("name"));
                    break;
                case TYPE_DOT:
                    int x = jsonObject.getInt("x");
                    int y = jsonObject.getInt("y");
                    Dot dot = new Dot(x, y);
                    listener.onDotMessage(dot);
                    break;
            }
        } catch (UnsupportedEncodingException | JSONException e) {
            e.printStackTrace();
        }

    }

    public interface OnParsedListener {
        void onInitMessage(@NonNull String name);

        void onDotMessage(@NonNull Dot dot);
    }
}
