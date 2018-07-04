package by.klnvch.link5dots.utils;

import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.crashlytics.android.Crashlytics;

import static com.google.android.gms.common.internal.Preconditions.checkNotNull;

public class ActivityUtils {
    public static void showDialog(@NonNull FragmentManager manager,
                                  @NonNull DialogFragment dialog,
                                  @NonNull String tag) {
        checkNotNull(manager);
        checkNotNull(dialog);
        checkNotNull(tag);

        try {
            final FragmentTransaction ft = manager.beginTransaction();
            final Fragment dialogPrevious = manager.findFragmentByTag(tag);
            if (dialogPrevious != null) ft.remove(dialogPrevious);
            dialog.show(ft, tag);
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }
}
