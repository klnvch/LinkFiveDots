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

package by.klnvch.link5dots.settings;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.plus.PlusOneButton;

import by.klnvch.link5dots.R;

public class InfoActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "InfoActivity";

    // The request code must be 0 or greater.
    private static final int PLUS_ONE_REQUEST_CODE = 0;
    private static final String WEB_PAGE_LINK = "https://play.google.com/store/apps/details?id=by.klnvch.link5dots";
    private static final String ANDROID_APP_LINK = "market://details?id=by.klnvch.link5dots";
    private static final String GITHUB_LINK = "https://github.com/klnvch/LinkFiveDots";

    private PlusOneButton mPlusOneButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_activity);

        // set version
        String versionName = "?";
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        TextView versionTextView = findViewById(R.id.version);
        versionTextView.setText(getString(R.string.version_text, versionName));

        // init plus button
        mPlusOneButton = findViewById(R.id.plus_one_button);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the state of the +1 button each time the activity receives focus.
        mPlusOneButton.initialize(WEB_PAGE_LINK, PLUS_ONE_REQUEST_CODE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.github:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_LINK)));
                break;
            case R.id.rate_this_app:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ANDROID_APP_LINK)));
                break;
            case R.id.share:
                startActivity(new Intent(Intent.ACTION_SEND)
                        .putExtra(Intent.EXTRA_TEXT, WEB_PAGE_LINK)
                        .setType("text/plain"));
                break;
            case R.id.send_feedback:
                Intent feedBackIntent = new Intent(Intent.ACTION_SENDTO)
                        .setType("message/rfc822")
                        .setData(Uri.parse("mailto:link5dots@gmail.com"))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(Intent.createChooser(feedBackIntent,
                            getString(R.string.device_feedback)));
                } catch (ActivityNotFoundException e) {
                    Log.e(TAG, e.getMessage());
                }
                break;
        }
    }
}