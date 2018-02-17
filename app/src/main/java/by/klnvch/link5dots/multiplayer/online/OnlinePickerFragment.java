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

package by.klnvch.link5dots.multiplayer.online;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import by.klnvch.link5dots.R;

public class OnlinePickerFragment extends Fragment implements View.OnClickListener,
        PickerAdapter.OnItemClickListener {

    static final String TAG = "OnlineGamePickerFr";

    private OnPickerListener mListener;
    private ToggleButton mButtonCreate;
    private TextView mCreateStatusValue;
    private View mCreateStatusLabel;
    private View mProgressCreate;
    private ToggleButton mButtonScan;
    private View mProgressScan;
    private RecyclerView mRecyclerView;
    private int mRoomState = OnlineService.STATE_ROOM_DELETED;
    private int mScanState = OnlineService.STATE_SCAN_OFF;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnlineGameActivity) {
            mListener = (OnPickerListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        View view = inflater.inflate(R.layout.activity_nsd_picker, container, false);

        mButtonCreate = view.findViewById(R.id.buttonCreate);
        mButtonCreate.setOnClickListener(this);

        mButtonScan = view.findViewById(R.id.buttonScan);
        mButtonScan.setOnClickListener(this);

        mCreateStatusLabel = view.findViewById(R.id.textStatusLabel);
        mCreateStatusValue = view.findViewById(R.id.textStatusValue);
        mProgressCreate = view.findViewById(R.id.progressCreate);

        mProgressScan = view.findViewById(R.id.progressScan);

        mRecyclerView = view.findViewById(R.id.listDestinations);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

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
        if (mListener != null) {
            switch (v.getId()) {
                case R.id.buttonCreate:
                    if (mButtonCreate.isChecked())
                        mListener.onCreateRoom();
                    else
                        mListener.onDeleteRoom();
                    break;
                case R.id.buttonScan:
                    if (mButtonScan.isChecked())
                        mListener.onStartScan();
                    else
                        mListener.onStopScan();
                    break;
            }
        }
    }

    @Override
    public void onItemSelected(@NonNull Room room) {
        Log.d(TAG, "onItemSelected: " + room.toString());
        if (mListener != null) {
            mListener.onConnect(room);
        }
    }

    void setRoomState(int roomState, @Nullable Room room) {
        mRoomState = roomState;
        updateTitle();

        switch (roomState) {
            case OnlineService.STATE_ROOM_DELETED:
                mButtonCreate.setChecked(false);
                mButtonCreate.setEnabled(true);
                mButtonScan.setEnabled(true);

                mCreateStatusValue.setText(R.string.apn_not_set);
                mCreateStatusValue.setVisibility(View.VISIBLE);
                mCreateStatusLabel.setVisibility(View.VISIBLE);
                mProgressCreate.setVisibility(View.INVISIBLE);
                break;
            case OnlineService.STATE_ROOM_CREATING:
                mButtonCreate.setChecked(false);
                mButtonCreate.setEnabled(false);
                mButtonScan.setEnabled(false);

                mCreateStatusValue.setText(R.string.apn_not_set);
                mCreateStatusValue.setVisibility(View.INVISIBLE);
                mCreateStatusLabel.setVisibility(View.INVISIBLE);
                mProgressCreate.setVisibility(View.VISIBLE);
                break;
            case OnlineService.STATE_ROOM_CREATED:
                mButtonCreate.setChecked(true);
                mButtonCreate.setEnabled(true);
                mButtonScan.setEnabled(false);

                if (room != null) {
                    mCreateStatusValue.setText(room.toString());
                } else {
                    mCreateStatusValue.setText(null);
                }
                mCreateStatusValue.setVisibility(View.VISIBLE);
                mCreateStatusLabel.setVisibility(View.VISIBLE);
                mProgressCreate.setVisibility(View.INVISIBLE);
                break;
            case OnlineService.STATE_ROOM_DELETING:
                mButtonCreate.setChecked(true);
                mButtonCreate.setEnabled(false);
                mButtonScan.setEnabled(false);

                mCreateStatusValue.setText(R.string.apn_not_set);
                mCreateStatusValue.setVisibility(View.INVISIBLE);
                mCreateStatusLabel.setVisibility(View.INVISIBLE);
                mProgressCreate.setVisibility(View.VISIBLE);
                break;
        }
    }

    void setScanState(int scanState, @NonNull PickerAdapter adapter) {
        mScanState = scanState;
        updateTitle();

        switch (scanState) {
            case OnlineService.STATE_SCAN_OFF:
                mButtonCreate.setEnabled(true);
                mButtonScan.setChecked(false);

                mProgressScan.setVisibility(View.INVISIBLE);
                mRecyclerView.setAdapter(null);
                adapter.setOnItemClickListener(null);
                break;
            case OnlineService.STATE_SCAN_ON:
                mButtonCreate.setEnabled(false);
                mButtonScan.setChecked(true);

                mProgressScan.setVisibility(View.VISIBLE);
                mRecyclerView.setAdapter(adapter);
                adapter.setOnItemClickListener(this);
                break;
        }
    }

    private void updateTitle() {
        final int title;
        if (mScanState == OnlineService.STATE_SCAN_ON) {
            title = R.string.searching;
        } else if (mRoomState == OnlineService.STATE_ROOM_CREATED) {
            title = R.string.master_clear_progress_text;
        } else {
            title = R.string.menu_online_game;
        }

        if (getActivity() != null) getActivity().setTitle(title);
    }

    interface OnPickerListener {
        void onCreateRoom();

        void onDeleteRoom();

        void onStartScan();

        void onStopScan();

        void onConnect(@NonNull Room room);
    }
}