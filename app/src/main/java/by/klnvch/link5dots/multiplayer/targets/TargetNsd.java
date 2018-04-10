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

import android.annotation.TargetApi;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.support.annotation.NonNull;

import java.net.InetAddress;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class TargetNsd extends Target<NsdServiceInfo> {
    public TargetNsd(@NonNull NsdServiceInfo target) {
        super(target);
    }

    @Override
    public String toString() {
        final NsdServiceInfo serviceInfo = getTarget();
        final String serviceName = serviceInfo.getServiceName();
        final int port = serviceInfo.getPort();
        final InetAddress inetAddress = serviceInfo.getHost();
        if (port != 0 && inetAddress != null) {
            return serviceName + '\n' + inetAddress + ':' + port;
        } else {
            return serviceName;
        }
    }

    @NonNull
    @Override
    public String getShortName() {
        return getTarget().getServiceName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null) {
            final NsdServiceInfo serviceInfo = getTarget();
            final String serviceName = serviceInfo.getServiceName();

            if (obj instanceof NsdServiceInfo) {
                final NsdServiceInfo info = (NsdServiceInfo) obj;
                return info.getServiceName().equals(serviceName);
            }

            if (obj instanceof TargetNsd) {
                final NsdServiceInfo info = ((TargetNsd) obj).getTarget();
                return info.getServiceName().equals(serviceName);
            }
        }
        return false;
    }
}