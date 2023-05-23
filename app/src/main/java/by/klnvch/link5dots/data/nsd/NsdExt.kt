/*
 * MIT License
 *
 * Copyright (c) 2023 klnvch
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
package by.klnvch.link5dots.data.nsd

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo

object NsdExt {
    private fun createNsdServiceInfo(port: Int): NsdServiceInfo {
        val serviceInfo = NsdServiceInfo()
        serviceInfo.port = port
        serviceInfo.serviceName = NsdParams.SERVICE_NAME
        serviceInfo.serviceType = NsdParams.SERVICE_TYPE
        return serviceInfo
    }

    fun NsdServiceInfo.isValid(): Boolean {
        return if (serviceType != NsdParams.SERVICE_TYPE) false
        else serviceName.contains(NsdParams.SERVICE_NAME)
    }

    fun NsdManager.registerService(port: Int, listener: NsdManager.RegistrationListener) =
        registerService(createNsdServiceInfo(port), NsdManager.PROTOCOL_DNS_SD, listener)
}
