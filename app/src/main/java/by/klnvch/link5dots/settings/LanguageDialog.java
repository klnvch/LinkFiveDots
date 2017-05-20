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

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import by.klnvch.link5dots.R;

public class LanguageDialog extends DialogFragment implements DialogInterface.OnClickListener {

    private static final String LANGUAGE_EN = "en";
    private static final String LANGUAGE_BE = "be";
    private static final String LANGUAGE_DE = "de";
    private static final String LANGUAGE_ES = "es";
    private static final String LANGUAGE_FR = "fr";
    private static final String LANGUAGE_IT = "it";
    private static final String LANGUAGE_PL = "pl";
    private static final String LANGUAGE_RU = "ru";
    private static final String LANGUAGE_UK = "uk";
    private static final String LANGUAGE_FA = "fa";

    private OnLanguageChangedListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String currentLanguage = getResources().getConfiguration().locale.getLanguage();
        final int currentItem;
        switch (currentLanguage) {
            case LANGUAGE_DE:
                currentItem = 0;
                break;
            case LANGUAGE_ES:
                currentItem = 2;
                break;
            case LANGUAGE_FR:
                currentItem = 3;
                break;
            case LANGUAGE_IT:
                currentItem = 4;
                break;
            case LANGUAGE_PL:
                currentItem = 5;
                break;
            case LANGUAGE_BE:
                currentItem = 6;
                break;
            case LANGUAGE_RU:
                currentItem = 7;
                break;
            case LANGUAGE_UK:
                currentItem = 8;
                break;
            case LANGUAGE_FA:
                currentItem = 9;
                break;
            default:
                currentItem = 1;
                break;
        }

        return new AlertDialog.Builder(getActivity())
                .setCancelable(false)
                .setSingleChoiceItems(R.array.languages, currentItem, this)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        final String language;
        switch (which) {
            case 0:
                language = LANGUAGE_DE;
                break;
            case 2:
                language = LANGUAGE_ES;
                break;
            case 3:
                language = LANGUAGE_FR;
                break;
            case 4:
                language = LANGUAGE_IT;
                break;
            case 5:
                language = LANGUAGE_PL;
                break;
            case 6:
                language = LANGUAGE_BE;
                break;
            case 7:
                language = LANGUAGE_RU;
                break;
            case 8:
                language = LANGUAGE_UK;
                break;
            case 9:
                language = LANGUAGE_FA;
                break;
            default:
                language = LANGUAGE_EN;
        }
        //
        PreferenceManager
                .getDefaultSharedPreferences(getActivity())
                .edit()
                .putString(SettingsUtils.APP_LANGUAGE, language)
                .apply();
        //
        SettingsUtils.changeLanguage(getContext(), language);
        //
        if (listener != null) {
            listener.onLanguageChanged();
        }
        //
        dismiss();
    }

    @NonNull
    public LanguageDialog setOnLanguageChangedListener(@NonNull OnLanguageChangedListener listener) {
        this.listener = listener;
        return this;
    }

    public interface OnLanguageChangedListener {
        void onLanguageChanged();
    }
}