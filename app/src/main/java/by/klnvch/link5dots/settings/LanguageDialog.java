package by.klnvch.link5dots.settings;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import java.util.Locale;

import by.klnvch.link5dots.R;

public class LanguageDialog extends DialogFragment {

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

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);

        String currentLanguage = getResources().getConfiguration().locale.getLanguage();
        int currentItem;

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

        builder.setSingleChoiceItems(R.array.languages, currentItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String language;
                switch (i) {
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
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                prefs.edit().putString(SettingsUtils.APP_LANGUAGE, language).apply();
                //
                Locale locale = new Locale(language);
                Locale.setDefault(locale);
                Configuration config = new Configuration();
                config.locale = locale;
                getActivity().getBaseContext().getResources().updateConfiguration(config, null);
                //
                dismiss();
            }
        });

        return builder.create();
    }
}
