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

package by.klnvch.link5dots.multiplayer.targets;

import androidx.annotation.NonNull;

import by.klnvch.link5dots.domain.models.NetworkRoom;
import by.klnvch.link5dots.domain.models.NetworkUser;
import by.klnvch.link5dots.utils.FormatUtils;

public final class TargetOnline extends Target<NetworkRoom> {

    public TargetOnline(@NonNull NetworkRoom target) {
        super(target);
    }

    @NonNull
    @Override
    public String getShortName() {
        final NetworkUser user = getTarget().getUser1();
        if (user != null) return user.getName();
        else return "-";
    }

    @NonNull
    @Override
    public String getLongName() {
        final NetworkRoom room = getTarget();
        final String time = FormatUtils.formatDateTime(room.getTimestamp());
        final String name;
        if (room.getUser1() != null) name = room.getUser1().getName();
        else name = "-";
        return time + '\n' + name;
    }
}