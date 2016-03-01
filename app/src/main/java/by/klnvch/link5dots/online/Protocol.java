package by.klnvch.link5dots.online;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import by.klnvch.link5dots.Dot;

public class Protocol {

    public static final int TYPE_INIT = 1;
    public static final int TYPE_DOT = 2;

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
