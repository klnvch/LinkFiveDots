/*
 * MIT License
 *
 * Copyright (c) 2025 klnvch
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

package by.klnvch.link5dots.ui.game.picker

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.core.content.ContextCompat

//////////////// Create //////////////////////////////////////////////////////////

private fun Context.isCreatePermissionGranted() =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.S || hasPermission(Manifest.permission.BLUETOOTH_CONNECT)

fun Context.createPermissionGuard(
    launcher: ManagedActivityResultLauncher<String, Boolean>,
    action: () -> Unit,
) {
    if (isCreatePermissionGranted()) {
        action()
    } else {
        launcher.launch(Manifest.permission.BLUETOOTH_CONNECT)
    }
}

//////////////// Scan //////////////////////////////////////////////////////////

private val scanPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
    )
} else {
    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
}

private fun Context.isScanPermissionGranted() = scanPermissions.all { hasPermission(it) }

fun Context.scanPermissionGuard(
    launcher: ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>>,
    action: () -> Unit,
) {
    if (isScanPermissionGranted()) {
        action()
    } else {
        launcher.launch(scanPermissions)
    }
}

//////////////// Discovery //////////////////////////////////////////////////////////
fun Context.discoveryPermissionGuard(
    permissionLauncher: ManagedActivityResultLauncher<String, Boolean>,
    discoverLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            discoverLauncher.launch(discoverableIntent)
        } else {
            permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
        }
    } else {
        discoverLauncher.launch(discoverableIntent)
    }
}

//////////////// Common //////////////////////////////////////////////////////////

fun Context.hasPermission(permission: String) = ContextCompat.checkSelfPermission(
    this,
    permission
) == PackageManager.PERMISSION_GRANTED

private const val DISCOVERABLE_DURATION_SECONDS = 30
val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
    putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABLE_DURATION_SECONDS)
}
