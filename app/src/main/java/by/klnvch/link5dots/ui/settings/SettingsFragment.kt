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

package by.klnvch.link5dots.ui.settings

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.preference.*
import by.klnvch.link5dots.R
import by.klnvch.link5dots.domain.repositories.Settings
import by.klnvch.link5dots.ui.settings.preferences.DeletePreference
import by.klnvch.link5dots.ui.settings.preferences.DeletePreferenceDialog
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var viewModel: SettingsViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var pref: PreferenceDataStore

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = pref            // must be before the next line
        setPreferencesFromResource(R.xml.preferences, rootKey)  // to set initial values

        val userNamePreference: EditTextPreference? = findPreference(Settings.KEY_USER_NAME)
        userNamePreference?.summaryProvider =
            Preference.SummaryProvider<EditTextPreference> { preference ->
                if (preference.text.isNullOrEmpty()) getString(R.string.unknown)
                else preference.text
            }

        val nightModePreference: ListPreference? = findPreference(Settings.KEY_NIGHT_MODE)
        nightModePreference?.summaryProvider =
            Preference.SummaryProvider<ListPreference> { preference ->
                if (preference.entry.isNullOrEmpty()) getString(R.string.settings_system)
                else preference.entry
            }

        val languagePreference: ListPreference? = findPreference(Settings.KEY_LANGUAGE)
        languagePreference?.summaryProvider =
            Preference.SummaryProvider<ListPreference> { preference ->
                if (preference.entry.isNullOrEmpty()) getString(R.string.settings_system)
                else preference.entry
            }

        viewModel =
            ViewModelProvider(requireActivity(), viewModelFactory)[SettingsViewModel::class.java]
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference is DeletePreference) {
            val f = DeletePreferenceDialog.newInstance(preference.getKey())
            f.setTargetFragment(this, 0)
            f.show(parentFragmentManager, DIALOG_FRAGMENT_TAG)
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    companion object {
        private const val DIALOG_FRAGMENT_TAG = "SettingsFragment.DIALOG"
    }
}
