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

package by.klnvch.link5dots.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.TextView

import by.klnvch.link5dots.R

class InfoActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.info_activity)
        setTitle(R.string.application_info_label)

        // set version
        var versionName = "?"
        try {
            versionName = packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        val versionTextView = findViewById<TextView>(R.id.version)
        versionTextView.text = getString(R.string.version_text, versionName)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.github -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_LINK)))
            R.id.rate_this_app -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(ANDROID_APP_LINK)))
            R.id.share -> startActivity(Intent(Intent.ACTION_SEND)
                    .putExtra(Intent.EXTRA_TEXT, WEB_PAGE_LINK)
                    .setType("text/plain"))
            R.id.send_feedback -> {
                val feedBackIntent = Intent(Intent.ACTION_SENDTO)
                        .setType("message/rfc822")
                        .setData(Uri.parse("mailto:link5dots@gmail.com"))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                try {
                    startActivity(Intent.createChooser(feedBackIntent,
                            getString(R.string.device_feedback)))
                } catch (e: ActivityNotFoundException) {
                    Log.e(TAG, e.message)
                }

            }
        }
    }

    companion object {
        private val TAG = "InfoActivity"

        private val WEB_PAGE_LINK = "https://play.google.com/store/apps/details?id=by.klnvch.link5dots"
        private val ANDROID_APP_LINK = "market://details?id=by.klnvch.link5dots"
        private val GITHUB_LINK = "https://github.com/klnvch/LinkFiveDots"
    }
}