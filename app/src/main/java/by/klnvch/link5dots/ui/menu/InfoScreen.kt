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

package by.klnvch.link5dots.ui.menu

import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import by.klnvch.link5dots.BuildConfig
import by.klnvch.link5dots.R
import by.klnvch.link5dots.ui.common.CustomButtonWithText
import by.klnvch.link5dots.ui.common.TextNoSurface
import by.klnvch.link5dots.ui.common.adaptive.adaptiveIconSize

@Composable
fun InfoScreen() {
    val configuration = LocalConfiguration.current
    when (configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> InfoScreenPortrait()
        else -> InfoScreenScreenLandscape()
    }
}

@Composable
private fun InfoScreenPortrait() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        Column1()
        Column2()
    }
}

@Composable
private fun InfoScreenScreenLandscape() {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        Column1()
        Column2()
    }
}

@Composable
private fun Column1() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ContextCompat.getDrawable(LocalContext.current, R.mipmap.ic_launcher)?.let {
            val size = adaptiveIconSize(64.dp)
            Image(
                modifier = Modifier.size(size),
                bitmap = it.toBitmap().asImageBitmap(),
                contentDescription = stringResource(id = R.string.app_name)
            )
        }
        TextNoSurface(
            text = stringResource(R.string.version_text, BuildConfig.VERSION_NAME),
        )
    }
}

@Composable
private fun Column2() {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val appName = stringResource(R.string.app_name)

    Column {
        CustomButtonWithText(
            onClick = { uriHandler.openUri("https://github.com/klnvch/LinkFiveDots") },
            textId = R.string.btn_github,
        )
        CustomButtonWithText(
            onClick = { uriHandler.openUri("market://details?id=by.klnvch.link5dots") },
            textId = R.string.rate_this_app,
        )
        CustomButtonWithText(
            onClick = {
                val target = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("link5dots@gmail.com"))
                    putExtra(Intent.EXTRA_SUBJECT, appName)
                    type = "message/rfc822"
                }
                context.startActivity(Intent.createChooser(target, null))
            },
            textId = R.string.send_mail,
        )
        CustomButtonWithText(
            onClick = {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "https://play.google.com/store/apps/details?id=by.klnvch.link5dots"
                    )
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, null))
            },
            textId = R.string.share,
        )
    }
}
