package by.klnvch.link5dots.utils;

import static com.google.android.gms.common.internal.Preconditions.checkNotNull;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

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
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }
}
