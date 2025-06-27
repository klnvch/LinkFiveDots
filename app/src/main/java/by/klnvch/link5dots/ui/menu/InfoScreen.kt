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

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import by.klnvch.link5dots.BuildConfig
import by.klnvch.link5dots.R
import by.klnvch.link5dots.ui.common.TextNoSurface

@Composable
fun InfoScreen(onNavigate: (Screen) -> Unit) {
    val configuration = LocalConfiguration.current
    when (configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> InfoScreenPortrait(onNavigate)
        else -> InfoScreenScreenLandscape(onNavigate)
    }
}

@Composable
private fun InfoScreenPortrait(onNavigate: (Screen) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        Column1()
        Column2(onNavigate)
    }
}

@Composable
private fun InfoScreenScreenLandscape(onNavigate: (Screen) -> Unit) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        Column1()
        Column2(onNavigate)
    }
}

@Composable
private fun Column1() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ContextCompat.getDrawable(LocalContext.current, R.mipmap.ic_launcher)?.let {
            Image(
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
private fun Column2(onNavigate: (Screen) -> Unit) {
    Column {
        MenuTextButton(
            onClick = { onNavigate(Screen.SourceCode) },
            textId = R.string.btn_github,
        )
        MenuTextButton(
            onClick = { onNavigate(Screen.RateApp) },
            textId = R.string.rate_this_app,
        )
        MenuTextButton(
            onClick = { onNavigate(Screen.Feedback) },
            textId = R.string.send_mail,
        )
        MenuTextButton(
            onClick = { onNavigate(Screen.ShareApp) },
            textId = R.string.share,
        )
    }
}

@Composable
private fun MenuTextButton(
    onClick: () -> Unit,
    @StringRes textId: Int,
) {
    ElevatedButton(
        onClick = onClick,
        modifier = Modifier.widthIn(0.dp, 320.dp),
    ) {
        Text(
            text = stringResource(textId).uppercase(),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
    }
}
