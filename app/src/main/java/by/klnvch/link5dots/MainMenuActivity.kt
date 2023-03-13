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

package by.klnvch.link5dots

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import by.klnvch.link5dots.databinding.ActivityMainMenuBinding
import by.klnvch.link5dots.domain.repositories.RoomDao
import by.klnvch.link5dots.domain.repositories.Settings
import by.klnvch.link5dots.domain.usecases.GetNightModeChangesUseCase
import by.klnvch.link5dots.models.Room
import by.klnvch.link5dots.network.NetworkService
import by.klnvch.link5dots.scores.ScoresActivity
import by.klnvch.link5dots.ui.main.UsernameDialog
import by.klnvch.link5dots.ui.settings.SettingsActivity
import by.klnvch.link5dots.utils.ContextExt.isTestDevice
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainMenuActivity : DaggerAppCompatActivity(), View.OnClickListener, View.OnLongClickListener,
    UsernameDialog.OnUsernameChangedListener {

    private val mDisposables = CompositeDisposable()

    private lateinit var binding: ActivityMainMenuBinding

    @Inject
    lateinit var networkService: NetworkService

    @Inject
    lateinit var roomDao: RoomDao

    //@Inject
    //lateinit var languageChangesUseCase: GetLanguageChangesUseCase

    @Inject
    lateinit var nightModeChangesUseCase: GetNightModeChangesUseCase

    @Inject
    lateinit var settings: Settings

    private val startSettingsForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            recreate()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setTitle(R.string.app_name)

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
        }

        binding.textViewGreeting.setOnLongClickListener(this)

        lifecycleScope.launch {
            val isTheFirstRun = settings.isFirstRun()
            checkTheFirstRun(isTheFirstRun)
        }

        //lifecycleScope.launch {
        //    languageChangesUseCase.isChanged().collect {
        //        withContext(Dispatchers.Main) {
        //            changeConfiguration(it)
        //        }
        //    }
        //}

        lifecycleScope.launch {
            nightModeChangesUseCase.isChanged().collect()
        }
    }

    override fun onResume() {
        super.onResume()

        mDisposables.add(roomDao.loadAll()
            .take(1)
            .flatMapIterable { it }
            .filter { !it.isSend }
            .flatMapSingle { updateIsTest(it) }
            .flatMapSingle { networkService.addRoom(HISTORY_TABLE, it.key, it) }
            .flatMapCompletable { update(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ Log.d(TAG, MSG_SENT) }, { onError(it) })
        )
    }

    private fun updateIsTest(room: Room): Single<Room> {
        return Single.fromCallable {
            room.isTest = isTestDevice()
            roomDao.updateRoom(room)
            room
        }
    }

    private fun update(room: Room): Completable {
        return Completable.fromAction {
            room.isSend = true
            roomDao.updateRoom(room)
        }
    }

    private fun onError(throwable: Throwable) {
        Log.e(TAG, MSG_FAIL, throwable)
        FirebaseCrashlytics.getInstance().recordException(throwable)
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
                startSettingsForResult.launch(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun onLongClick(v: View): Boolean {
        return when (v.id) {
            R.id.textViewGreeting -> showUsernameDialog()
            else -> false
        }
    }

    private fun showUsernameDialog(): Boolean {
        UsernameDialog().show(supportFragmentManager, UsernameDialog.TAG)
        return true
    }

    override fun onUsernameChanged(username: String) {
        setUsername(username)
    }

    private suspend fun checkTheFirstRun(isTheFirstRun: Boolean) {
        if (isTheFirstRun) {
            settings.setFirstRun()
        }
    }

    private fun setUsername(username: String) {
        if (username.isEmpty()) {
            binding.textViewGreeting.visibility = View.GONE
        } else {
            binding.textViewGreeting.visibility = View.VISIBLE
            binding.textViewGreeting.text = getString(R.string.greetings, username)
        }
    }

    private fun changeConfiguration(isChanged: Boolean) {
        if (isChanged) recreate()
    }

    companion object {
        private const val TAG = "MainMenuActivity"
        private const val MSG_SENT = "history updated successfully"
        private const val MSG_FAIL = "history update failed"
        private val HISTORY_TABLE = if (BuildConfig.DEBUG) "history_debug" else "history"
    }
}
