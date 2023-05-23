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

import android.os.Build
import android.util.Log
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.test.InstrumentationRegistry.getInstrumentation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiSelector
import by.klnvch.link5dots.ui.menu.MenuActivity
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@LargeTest
@RunWith(AndroidJUnit4::class)
class MultiplayerTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MenuActivity::class.java)

    @Test
    fun testBluetoothNone() {
        Thread.sleep(2000)
        clickMenuButton(R.id.multi_player_bluetooth)
        checkIdleState(R.string.bluetooth)
        pressBack()
    }

    @Test
    fun testBluetoothCreate() {
        Thread.sleep(2000)
        clickMenuButton(R.id.multi_player_bluetooth)
        checkIdleState(R.string.bluetooth)
        clickCreateButton()
        checkCreatedState()
        pressBack()
    }

    @Test
    fun testBluetoothCreateDelete() {
        Thread.sleep(2000)
        clickMenuButton(R.id.multi_player_bluetooth)
        checkIdleState(R.string.bluetooth)
        clickCreateButton()
        checkCreatedState()
        clickCreateButton()
        checkIdleState(R.string.bluetooth)
        pressBack()
    }

    @Test
    fun testBluetoothScan() {
        Thread.sleep(2000)
        clickMenuButton(R.id.multi_player_bluetooth)
        checkIdleState(R.string.bluetooth)
        clickScanButton()
        checkScanningState()
        pressBack()
    }

    @Test
    fun testBluetoothScanCancel() {
        Thread.sleep(2000)
        clickMenuButton(R.id.multi_player_bluetooth)
        checkIdleState(R.string.bluetooth)
        clickScanButton()
        checkScanningState()
        clickScanButton()
        checkIdleState(R.string.bluetooth)
        pressBack()
    }

    @Test
    fun testBluetoothAll() {
        Thread.sleep(2000)
        clickMenuButton(R.id.multi_player_bluetooth)
        checkIdleState(R.string.bluetooth)
        clickCreateButton()
        checkCreatedState()
        clickCreateButton()
        checkIdleState(R.string.bluetooth)
        clickScanButton()
        checkScanningState()
        clickScanButton()
        checkIdleState(R.string.bluetooth)
        pressBack()
    }


    @Test
    fun testNsdNone() {
        Thread.sleep(2000)
        clickMenuButton(R.id.multi_player_lan)
        checkIdleState(R.string.menu_local_network)
        pressBack()
    }

    @Test
    fun testNsdCreate() {
        Thread.sleep(2000)
        clickMenuButton(R.id.multi_player_lan)
        checkIdleState(R.string.menu_local_network)
        clickCreateButton()
        checkCreatedState()
        pressBack()
    }

    @Test
    fun testNsdCreateDelete() {
        Thread.sleep(2000)
        clickMenuButton(R.id.multi_player_lan)
        checkIdleState(R.string.menu_local_network)
        clickCreateButton()
        checkCreatedState()
        clickCreateButton()
        checkIdleState(R.string.menu_local_network)
        pressBack()
    }

    @Test
    fun testNsdScan() {
        Thread.sleep(2000)
        clickMenuButton(R.id.multi_player_lan)
        checkIdleState(R.string.menu_local_network)
        clickScanButton()
        checkScanningState()
        pressBack()
    }

    @Test
    fun testNsdScanCancel() {
        Thread.sleep(2000)
        clickMenuButton(R.id.multi_player_lan)
        checkIdleState(R.string.menu_local_network)
        clickScanButton()
        checkScanningState()
        clickScanButton()
        checkIdleState(R.string.menu_local_network)
        pressBack()
    }

    @Test
    fun testNsdAll() {
        Thread.sleep(2000)
        clickMenuButton(R.id.multi_player_lan)
        checkIdleState(R.string.menu_local_network)
        clickCreateButton()
        checkCreatedState()
        clickCreateButton()
        checkIdleState(R.string.menu_local_network)
        clickScanButton()
        checkScanningState()
        clickScanButton()
        checkIdleState(R.string.menu_local_network)
        pressBack()
    }

    @Test
    fun testOnlineNone() {
        Thread.sleep(2000)
        clickMenuButton(R.id.multi_player_online)
        checkIdleState(R.string.menu_online_game)
        pressBack()
    }

    @Test
    fun testOnlineCreate() {
        Thread.sleep(2000)
        clickMenuButton(R.id.multi_player_online)
        checkIdleState(R.string.menu_online_game)
        clickCreateButton()
        checkCreatedState()
        pressBack()
    }

    @Test
    fun testOnlineCreateDelete() {
        Thread.sleep(2000)
        clickMenuButton(R.id.multi_player_online)
        checkIdleState(R.string.menu_online_game)
        clickCreateButton()
        checkCreatedState()
        clickCreateButton()
        checkIdleState(R.string.menu_online_game)
        pressBack()
    }

    @Test
    fun testOnlineScan() {
        Thread.sleep(2000)
        clickMenuButton(R.id.multi_player_online)
        checkIdleState(R.string.menu_online_game)
        clickScanButton()
        checkScanningState()
        pressBack()
    }

    @Test
    fun testOnlineScanCancel() {
        Thread.sleep(2000)
        clickMenuButton(R.id.multi_player_online)
        checkIdleState(R.string.menu_online_game)
        clickScanButton()
        checkScanningState()
        clickScanButton()
        checkIdleState(R.string.menu_online_game)
        pressBack()
    }

    @Test
    fun testOnlineAll() {
        Thread.sleep(2000)
        clickMenuButton(R.id.multi_player_online)
        checkIdleState(R.string.menu_online_game)
        clickCreateButton()
        checkCreatedState()
        clickCreateButton()
        checkIdleState(R.string.menu_online_game)
        clickScanButton()
        checkScanningState()
        clickScanButton()
        checkIdleState(R.string.menu_online_game)
        pressBack()
    }

    private fun clickMenuButton(buttonId: Int) {
        onView(withId(buttonId))
                .check(matches(isDisplayed()))
                .perform(click())
        Thread.sleep(2000)
        allowPermissionsIfNeeded()
    }

    private fun clickCreateButton() {
        onView(withId(R.id.buttonCreate)).perform(click())
        Thread.sleep(2000)
        allowPermissionsIfNeeded()
    }

    private fun clickScanButton() {
        onView(withId(R.id.buttonScan)).perform(click())
        Thread.sleep(2000)
        allowPermissionsIfNeeded()
    }

    private fun checkIdleState(titleId: Int) {
        onView(allOf(isAssignableFrom(TextView::class.java),
                withParent(isAssignableFrom(Toolbar::class.java))))
                .check(matches(withText(titleId)))

        onView(withId(R.id.buttonCreate))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()))
                .check(matches(isNotChecked()))
                .check(matches(withText(R.string.create)))

        onView(withId(R.id.textStatusLabel))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()))

        onView(withId(R.id.textStatusValue))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()))
                .check(matches(withText(R.string.name_not_set)))

        onView(withId(R.id.progressCreate))
                .check(matches(not(isDisplayed())))

        onView(withId(R.id.buttonScan))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()))
                .check(matches(isNotChecked()))
                .check(matches(withText(R.string.scan)))

        onView(withId(R.id.progressScan))
                .check(matches(not(isDisplayed())))

        onView(withId(R.id.listTargets))
                .check(matches(not(isDisplayed())))

        onView(withId(R.id.textWarningEmpty))
                .check(matches(not(isDisplayed())))
    }

    private fun checkCreatedState() {
        onView(allOf(isAssignableFrom(TextView::class.java),
                withParent(isAssignableFrom(Toolbar::class.java))))
                .check(matches(withText(R.string.loading)))

        onView(withId(R.id.buttonCreate))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()))
                .check(matches(isChecked()))
                .check(matches(withText(R.string.delete)))

        onView(withId(R.id.textStatusLabel))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()))

        onView(withId(R.id.textStatusValue))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()))
                .check(matches(not(withText(R.string.name_not_set))))

        onView(withId(R.id.progressCreate))
                .check(matches(not(isDisplayed())))

        onView(withId(R.id.buttonScan))
                .check(matches(isDisplayed()))
                .check(matches(not(isEnabled())))
                .check(matches(isNotChecked()))
                .check(matches(withText(R.string.scan)))

        onView(withId(R.id.progressScan))
                .check(matches(not(isDisplayed())))

        onView(withId(R.id.listTargets))
                .check(matches(not(isDisplayed())))

        onView(withId(R.id.textWarningEmpty))
                .check(matches(not(isDisplayed())))
    }

    private fun checkScanningState() {
        onView(allOf(isAssignableFrom(TextView::class.java),
                withParent(isAssignableFrom(Toolbar::class.java))))
                .check(matches(withText(R.string.searching)))

        onView(withId(R.id.buttonCreate))
                .check(matches(isDisplayed()))
                .check(matches(not(isEnabled())))
                .check(matches(isNotChecked()))
                .check(matches(withText(R.string.create)))

        onView(withId(R.id.textStatusLabel))
                .check(matches(isDisplayed()))
                .check(matches(not(isEnabled())))

        onView(withId(R.id.textStatusValue))
                .check(matches(isDisplayed()))
                .check(matches(not(isEnabled())))
                .check(matches(withText(R.string.name_not_set)))

        onView(withId(R.id.progressCreate))
                .check(matches(not(isDisplayed())))

        onView(withId(R.id.buttonScan))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()))
                .check(matches(isChecked()))
                .check(matches(withText(R.string.cancel)))

        onView(withId(R.id.progressScan))
                .check(matches(isDisplayed()))

        onView(withId(R.id.listTargets))
                .check(matches(isDisplayed()))

        //onView(withId(R.id.textWarningEmpty))
        //        .check(matches(not(isDisplayed())))
    }

    private fun allowPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= 23) {
            val device = UiDevice.getInstance(getInstrumentation())
            val allowPermissions = device.findObject(UiSelector().text("ALLOW"))
            if (allowPermissions.exists()) {
                try {
                    allowPermissions.click()
                    Thread.sleep(2000)
                } catch (e: UiObjectNotFoundException) {
                    Log.e("TEST", "There is no permissions dialog to interact with ", e)
                }

            }
        }
    }
}