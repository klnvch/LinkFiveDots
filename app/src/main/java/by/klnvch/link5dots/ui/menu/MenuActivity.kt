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

package by.klnvch.link5dots.ui.menu

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import by.klnvch.link5dots.*
import by.klnvch.link5dots.multiplayer.activities.GameActivityBluetooth
import by.klnvch.link5dots.multiplayer.activities.GameActivityNsd
import by.klnvch.link5dots.multiplayer.activities.GameActivityOnline
import by.klnvch.link5dots.multiplayer.utils.bluetooth.BluetoothHelper
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.android.support.DaggerAppCompatActivity

class MenuActivity : DaggerAppCompatActivity(), View.OnClickListener {
    private lateinit var appBarConfiguration: AppBarConfiguration

    private var mIsBluetoothEnabled = false

    private val startOnlineGame =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != RESULT_OK) {
                showErrorDialog()
            }
        }

    private val startNsdGame =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != RESULT_OK) {
                showErrorDialog()
            }
        }

    private val startBluetoothGame =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (!mIsBluetoothEnabled) {
                BluetoothHelper.disable()
            }
            if (it.resultCode != RESULT_OK) {
                showErrorDialog()
            }
        }

    private val enableBluetooth =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                startBluetoothActivity()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        setTitle(R.string.app_name)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        if (savedInstanceState != null) {
            mIsBluetoothEnabled = savedInstanceState.getBoolean(KEY_IS_BLUETOOTH_ENABLED)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(KEY_IS_BLUETOOTH_ENABLED, mIsBluetoothEnabled)
        super.onSaveInstanceState(outState)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    @SuppressLint("ObsoleteSdkInt")
    override fun onClick(v: View) {
        val nav = v.findNavController()
        when (v.id) {
            R.id.main_menu_single_player ->
                nav.navigate(MainMenuFragmentDirections.actionMainMenuFragmentToMainActivity())
            R.id.main_menu_multi_player ->
                nav.navigate(MainMenuFragmentDirections.actionMainMenuFragmentToMultiplayerMenuFragment())
            R.id.main_menu_scores ->
                nav.navigate(MainMenuFragmentDirections.actionMainMenuFragmentToScoresActivity())
            R.id.main_menu_how_to ->
                nav.navigate(MainMenuFragmentDirections.actionMainMenuFragmentToHowToActivity())
            R.id.main_menu_about ->
                nav.navigate(MainMenuFragmentDirections.actionMainMenuFragmentToInfoActivity())
            R.id.main_menu_settings ->
                nav.navigate(MainMenuFragmentDirections.actionMainMenuFragmentToSettingsActivity())
            R.id.multi_player_two_players ->
                nav.navigate(MultiplayerMenuFragmentDirections.actionMultiplayerMenuFragmentToTwoPlayersActivity())
            R.id.multi_player_online ->
                startOnlineGame.launch(Intent(this, GameActivityOnline::class.java))
            R.id.multi_player_lan ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    startNsdGame.launch(Intent(this, GameActivityNsd::class.java))
                else
                    showErrorDialog()
            R.id.multi_player_bluetooth ->
                if (BluetoothHelper.isSupported) {
                    mIsBluetoothEnabled = BluetoothHelper.isEnabled
                    if (mIsBluetoothEnabled) {
                        startBluetoothActivity()
                    } else {
                        enableBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                    }
                } else {
                    showErrorDialog()
                }
        }
    }

    private fun startBluetoothActivity() {
        startBluetoothGame.launch(Intent(this, GameActivityBluetooth::class.java))
    }

    private fun showErrorDialog() {
        FirebaseCrashlytics.getInstance().log("feature not supported")
        AlertDialog.Builder(this)
            .setMessage(R.string.error_feature_not_available)
            .setPositiveButton(R.string.okay, null)
            .show()
    }

    companion object {
        private const val KEY_IS_BLUETOOTH_ENABLED = "KEY_IS_BLUETOOTH_ENABLED"
    }
}
