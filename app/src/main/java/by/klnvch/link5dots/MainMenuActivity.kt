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

package by.klnvch.link5dots

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.support.v7.app.AppCompatActivity
import android.view.View

import by.klnvch.link5dots.scores.ScoresActivity
import by.klnvch.link5dots.settings.InfoActivity
import by.klnvch.link5dots.settings.SettingsActivity
import by.klnvch.link5dots.settings.SettingsUtils
import by.klnvch.link5dots.settings.UsernameDialog
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main_menu.*

class MainMenuActivity : AppCompatActivity(), View.OnClickListener, View.OnLongClickListener {

    private val mDisposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
        setTitle(R.string.app_name)

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build())
        }

        textViewGreeting.setOnLongClickListener(this)

        mDisposables.add(Observable.fromCallable { SettingsUtils.isTheFirstRun(this) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { checkTheFirstRun(it) })

        mDisposables.add(Observable.fromCallable { SettingsUtils.checkConfiguration(this) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { changeConfiguration(it) })

        mDisposables.add(Observable.fromCallable { SettingsUtils.getUserNameOrEmpty(this) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { setUsername(it) })
    }

    override fun onDestroy() {
        mDisposables.clear()
        super.onDestroy()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.main_menu_single_player ->
                startActivity(Intent(this, MainActivity::class.java))
            R.id.main_menu_multi_player ->
                startActivity(Intent(this, MultiPlayerMenuActivity::class.java))
            R.id.main_menu_scores ->
                startActivity(Intent(this, ScoresActivity::class.java))
            R.id.main_menu_how_to ->
                startActivity(Intent(this, HowToActivity::class.java))
            R.id.main_menu_about ->
                startActivity(Intent(this, InfoActivity::class.java))
            R.id.main_menu_settings ->
                startActivityForResult(Intent(this, SettingsActivity::class.java),
                        RC_SETTINGS)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RC_SETTINGS -> recreate()
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onLongClick(v: View): Boolean {
        return when (v.id) {
            R.id.textViewGreeting -> showUsernameDialog()
            else -> false
        }
    }

    private fun showUsernameDialog(): Boolean {
        UsernameDialog()
                .setOnUsernameChangeListener { setUsername(it) }
                .show(supportFragmentManager, null)
        return true
    }

    private fun checkTheFirstRun(isTheFirstRun: Boolean) {
        if (isTheFirstRun) {
            showUsernameDialog()
            SettingsUtils.setTheFirstRun(this)
        }
    }

    private fun setUsername(username: String) {
        if (username.isEmpty()) {
            textViewGreeting.visibility = View.GONE
        } else {
            textViewGreeting.visibility = View.VISIBLE
            textViewGreeting.text = getString(R.string.greetings, username)
        }
    }

    private fun changeConfiguration(isChanged: Boolean) {
        if (isChanged) recreate()
    }

    companion object {
        private val RC_SETTINGS = 3
    }
}