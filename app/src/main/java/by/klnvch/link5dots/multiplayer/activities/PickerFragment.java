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

package by.klnvch.link5dots.multiplayer.activities;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import by.klnvch.link5dots.R;
import by.klnvch.link5dots.multiplayer.adapters.OnEmptyStateListener;
import by.klnvch.link5dots.multiplayer.adapters.OnItemClickListener;
import by.klnvch.link5dots.multiplayer.adapters.TargetAdapterInterface;
import by.klnvch.link5dots.multiplayer.targets.Target;
import by.klnvch.link5dots.multiplayer.utils.GameState;

import static com.google.common.base.Preconditions.checkNotNull;

public class PickerFragment extends Fragment implements View.OnClickListener,
        OnItemClickListener, OnEmptyStateListener {

    public static final String TAG = "OnlineGamePickerFr";

    OnPickerListener mListener;
    private ToggleButton mButtonCreate;
    ToggleButton mButtonScan;
    private TextView mCreateStatusValue;
    private View mCreateStatusColon;
    private View mCreateStatusLabel;
    private View mProgressCreate;
    private View mProgressScan;
    private RecyclerView mRecyclerView;
    private View mScanWarning;
    private boolean isClickable = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnPickerListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnPickerListener");
        }
    }

    @CallSuper
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        final View view = inflater.inflate(R.layout.fragment_game_picker, container, false);

        mButtonCreate = view.findViewById(R.id.buttonCreate);
        mButtonCreate.setOnClickListener(this);

        mButtonScan = view.findViewById(R.id.buttonScan);
        mButtonScan.setOnClickListener(this);

        mCreateStatusLabel = view.findViewById(R.id.textStatusLabel);
        mCreateStatusColon = view.findViewById(R.id.textStatusColon);
        mCreateStatusValue = view.findViewById(R.id.textStatusValue);
        mProgressCreate = view.findViewById(R.id.progressCreate);

        mProgressScan = view.findViewById(R.id.progressScan);

        mRecyclerView = view.findViewById(R.id.listTargets);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mScanWarning = view.findViewById(R.id.textWarningEmpty);

        return view;
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView");
        mRecyclerView.setAdapter(null); // prevent memory leak
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonCreate:
                onCreateButtonClicked();
                break;
            case R.id.buttonScan:
                onScanButtonClicked();
                break;
        }
    }

    @Override
    public void onItemSelected(@NonNull Target target) {
        if (isClickable) {
            Log.d(TAG, "onItemSelected: " + target.toString());

            checkNotNull(getContext());

            final String msg = getString(R.string.connection_dialog_text, target.getShortName());
            new AlertDialog.Builder(getContext())
                    .setMessage(msg)
                    .setPositiveButton(R.string.yes, (dialog, which) -> mListener.onConnect(target))
                    .setNegativeButton(R.string.no, null)
                    .show();
        } else {
            Log.w(TAG, "picker fragment is not clickable");
        }
    }

    @Override
    public void onEmptyState(boolean isEmpty) {
        if (isEmpty) {
            mScanWarning.setVisibility(View.VISIBLE);
        } else {
            mScanWarning.setVisibility(View.INVISIBLE);
        }
    }

    public void setState(@NonNull GameState state) {
        checkNotNull(state);

        if (getView() == null) return;

        switch (state.getTargetState()) {
            case GameState.STATE_NONE:
                mButtonCreate.setEnabled(false);
                mButtonCreate.setChecked(false);
                mCreateStatusValue.setText(R.string.name_not_set);
                mCreateStatusValue.setEnabled(false);
                mCreateStatusValue.setVisibility(View.VISIBLE);
                mCreateStatusLabel.setEnabled(false);
                mCreateStatusLabel.setVisibility(View.VISIBLE);
                mCreateStatusColon.setEnabled(false);
                mCreateStatusColon.setVisibility(View.VISIBLE);
                mProgressCreate.setVisibility(View.INVISIBLE);
                break;
            case GameState.STATE_TARGET_DELETED:
                mButtonCreate.setEnabled(true);
                mButtonCreate.setChecked(false);
                mCreateStatusValue.setText(R.string.name_not_set);
                mCreateStatusValue.setEnabled(true);
                mCreateStatusValue.setVisibility(View.VISIBLE);
                mCreateStatusLabel.setEnabled(true);
                mCreateStatusLabel.setVisibility(View.VISIBLE);
                mCreateStatusColon.setEnabled(true);
                mCreateStatusColon.setVisibility(View.VISIBLE);
                mProgressCreate.setVisibility(View.INVISIBLE);
                break;
            case GameState.STATE_TARGET_CREATING:
                mButtonCreate.setEnabled(false);
                mButtonCreate.setChecked(false);
                mCreateStatusValue.setText(R.string.name_not_set);
                mCreateStatusValue.setEnabled(false);
                mCreateStatusValue.setVisibility(View.INVISIBLE);
                mCreateStatusLabel.setEnabled(false);
                mCreateStatusLabel.setVisibility(View.INVISIBLE);
                mCreateStatusColon.setEnabled(false);
                mCreateStatusColon.setVisibility(View.INVISIBLE);
                mProgressCreate.setVisibility(View.VISIBLE);
                break;
            case GameState.STATE_TARGET_CREATED:
                mButtonCreate.setEnabled(true);
                mButtonCreate.setChecked(true);
                mCreateStatusValue.setText(mListener.getTargetLongName());
                mCreateStatusValue.setEnabled(true);
                mCreateStatusValue.setVisibility(View.VISIBLE);
                mCreateStatusLabel.setEnabled(true);
                mCreateStatusLabel.setVisibility(View.VISIBLE);
                mCreateStatusColon.setEnabled(true);
                mCreateStatusColon.setVisibility(View.VISIBLE);
                mProgressCreate.setVisibility(View.INVISIBLE);
                break;
            case GameState.STATE_TARGET_DELETING:
                mButtonCreate.setEnabled(false);
                mButtonCreate.setChecked(true);
                mCreateStatusValue.setText(R.string.name_not_set);
                mCreateStatusValue.setEnabled(false);
                mCreateStatusValue.setVisibility(View.INVISIBLE);
                mCreateStatusLabel.setEnabled(false);
                mCreateStatusLabel.setVisibility(View.INVISIBLE);
                mCreateStatusColon.setEnabled(false);
                mCreateStatusColon.setVisibility(View.INVISIBLE);
                mProgressCreate.setVisibility(View.VISIBLE);
                break;
        }

        final TargetAdapterInterface adapter = mListener.getAdapter();
        checkNotNull(adapter);

        switch (state.getScanState()) {
            case GameState.STATE_NONE:
                mButtonScan.setEnabled(false);
                mButtonScan.setChecked(false);
                mProgressScan.setVisibility(View.INVISIBLE);
                adapter.setOnItemClickListener(null);
                adapter.setOnEmptyStateListener(null);
                mRecyclerView.setVisibility(View.INVISIBLE);
                mRecyclerView.setAdapter(null);
                mScanWarning.setVisibility(View.INVISIBLE);
                break;
            case GameState.STATE_SCAN_OFF:
                mButtonScan.setEnabled(true);
                mButtonScan.setChecked(false);
                mProgressScan.setVisibility(View.INVISIBLE);
                adapter.setOnItemClickListener(null);
                adapter.setOnEmptyStateListener(null);
                mRecyclerView.setVisibility(View.INVISIBLE);
                mRecyclerView.setAdapter(null);
                mScanWarning.setVisibility(View.INVISIBLE);
                break;
            case GameState.STATE_SCAN_ON:
                mButtonScan.setEnabled(true);
                mButtonScan.setChecked(true);
                mProgressScan.setVisibility(View.VISIBLE);
                adapter.setOnItemClickListener(this);
                adapter.setOnEmptyStateListener(this);
                onEmptyState(adapter.isEmpty());
                mRecyclerView.setVisibility(View.VISIBLE);
                mRecyclerView.setAdapter(adapter.getAdapter());
                break;
            case GameState.STATE_SCAN_DONE:
                mButtonScan.setEnabled(true);
                mButtonScan.setChecked(false);
                mProgressScan.setVisibility(View.INVISIBLE);
                adapter.setOnItemClickListener(this);
                adapter.setOnEmptyStateListener(null);
                onEmptyState(adapter.isEmpty());
                mRecyclerView.setVisibility(View.VISIBLE);
                mRecyclerView.setAdapter(adapter.getAdapter());
                break;
        }

        if (state.getConnectState() != GameState.STATE_NONE) {
            isClickable = false;
            mButtonCreate.setEnabled(false);
            mCreateStatusValue.setEnabled(false);
            mButtonScan.setEnabled(false);
            mCreateStatusLabel.setEnabled(false);
            mCreateStatusColon.setEnabled(false);
        } else {
            isClickable = true;
        }
    }

    private void onCreateButtonClicked() {
        if (mButtonCreate.isChecked())
            mListener.onCreateRoom();
        else
            mListener.onDeleteRoom();
    }

    void onScanButtonClicked() {
        if (mButtonScan.isChecked())
            mListener.onStartScan();
        else
            mListener.onStopScan();
    }

    public interface OnPickerListener {
        void onCreateRoom();

        void onDeleteRoom();

        void onStartScan();

        void onStopScan();

        void onConnect(@NonNull Target destination);

        @NonNull
        String getTargetLongName();

        @NonNull
        TargetAdapterInterface getAdapter();
    }
}