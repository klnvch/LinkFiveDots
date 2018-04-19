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

import android.support.annotation.NonNull;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.google.android.gms.common.internal.Preconditions.checkNotNull;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Dot implements Serializable {

    public static final int EMPTY = 1;
    public static final int HOST = 2;
    public static final int GUEST = 4;

    private int x;
    private int y;
    private int id;
    private int type;
    private long timestamp;

    public Dot(int x, int y) {
        this.x = x;
        this.y = y;
        this.type = EMPTY;
        this.id = -1;
    }

    static Dot copyDot(@NonNull Dot dot) {
        checkNotNull(dot);

        final Dot result = new Dot();
        result.x = dot.x;
        result.y = dot.y;
        result.type = dot.type;
        result.id = dot.id;
        result.timestamp = dot.timestamp;
        return result;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}