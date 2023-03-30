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

package by.klnvch.link5dots.ui

import android.content.*
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import by.klnvch.link5dots.BuildConfig
import by.klnvch.link5dots.R
import by.klnvch.link5dots.databinding.ActivityInfoBinding
import com.google.firebase.crashlytics.FirebaseCrashlytics


class InfoActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setTitle(R.string.application_info_label)

        // set version
        binding.textViewAppVersion.text = getString(R.string.version_text, BuildConfig.VERSION_NAME)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.buttonInfoCode -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_LINK))
                launchIntent(intent, GITHUB_LINK)
            }
            R.id.buttonInfoRate -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ANDROID_APP_LINK))
                launchIntent(intent, WEB_PAGE_LINK)
            }
            R.id.buttonInfoShare -> {
                val intent = Intent(Intent.ACTION_SEND)
                    .putExtra(Intent.EXTRA_TEXT, WEB_PAGE_LINK)
                    .setType("text/plain")
                launchIntent(intent, WEB_PAGE_LINK)
            }
            R.id.buttonInfoFeedback -> {
                val intent = Intent.createChooser(
                    Intent(Intent.ACTION_SENDTO)
                        .setDataAndType(Uri.parse(URI_MAIL_DATA), "message/rfc822")
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    getString(R.string.connection_error_message)
                )
                launchIntent(intent, MAIL)
            }
        }
    }

    private fun launchIntent(intent: Intent, errorMsg: String) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            FirebaseCrashlytics.getInstance().recordException(e)

            AlertDialog.Builder(this)
                .setMessage(errorMsg)
                .setPositiveButton(R.string.okay, null)
                .setNeutralButton(R.string.copy_text) { _, _ -> copyToClipboard(errorMsg) }
                .show()
        }
    }

    private fun copyToClipboard(msg: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(msg, msg)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, R.string.toast_text_copied, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val WEB_PAGE_LINK =
            "https://play.google.com/store/apps/details?id=by.klnvch.link5dots"
        private const val ANDROID_APP_LINK = "market://details?id=by.klnvch.link5dots"
        private const val GITHUB_LINK = "https://github.com/klnvch/LinkFiveDots"
        private const val MAIL = "link5dots@gmail.com"
        private const val URI_MAIL_DATA = "mailto:$MAIL"
    }
}
