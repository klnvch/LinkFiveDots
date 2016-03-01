package by.klnvch.link5dots.settings;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import by.klnvch.link5dots.R;

public class UsernameDialog extends DialogFragment {

    public static final String TAG = "UsernameDialog";

    private OnUsernameChangListener listener = null;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);

        View v = View.inflate(getActivity(), R.layout.username, null);

        String username
                = SettingsUtils.getUserName(getContext(), getString(R.string.device_info_default));
        TextView tvUsername = (TextView) v.findViewById(R.id.username);
        tvUsername.setText(username);

        builder.setView(v);

        builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                EditText editText = (EditText) getDialog().findViewById(R.id.username);
                String username = editText.getText().toString().trim();
                if (!username.isEmpty()) {
                    SettingsUtils.setUserName(getContext(), username);
                    //
                    if (listener != null) {
                        listener.onUsernameChanged(username);
                    }
                }
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (listener != null) {
                    listener.onNothingChanged();
                }
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (listener != null) {
                    listener.onNothingChanged();
                }
            }
        });

        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (listener != null) {
            listener.onNothingChanged();
        }
        super.onCancel(dialog);
    }

    public void setOnUsernameChangeListener(OnUsernameChangListener listener) {
        this.listener = listener;
    }

    public interface OnUsernameChangListener {
        void onUsernameChanged(String username);

        void onNothingChanged();
    }

}
