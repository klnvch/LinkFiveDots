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

package by.klnvch.link5dots.multiplayer.adapters;

import static com.google.common.base.Preconditions.checkNotNull;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseError;

import by.klnvch.link5dots.R;
import by.klnvch.link5dots.domain.models.NetworkRoom;
import by.klnvch.link5dots.multiplayer.services.GameServiceOnline;
import by.klnvch.link5dots.multiplayer.targets.TargetOnline;
import by.klnvch.link5dots.multiplayer.utils.online.FirebaseHelper;

public class PickerAdapterOnline extends FirebaseRecyclerAdapter<NetworkRoom, TargetHolder>
        implements TargetAdapterInterface, ScannerInterface {

    private OnItemClickListener mOnItemClickListener = null;
    private OnEmptyStateListener mOnEmptyStateListener = null;
    private OnScanStoppedListener mOnScanStoppedListener;

    private PickerAdapterOnline(@NonNull FirebaseRecyclerOptions<NetworkRoom> options) {
        super(options);
    }

    @NonNull
    public static PickerAdapterOnline createAdapter() {
        final FirebaseRecyclerOptions<NetworkRoom> options =
                new FirebaseRecyclerOptions.Builder<NetworkRoom>()
                        .setQuery(FirebaseHelper.getRoomsQuery(), NetworkRoom.class)
                        .build();

        return new PickerAdapterOnline(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull TargetHolder holder, int position, @NonNull NetworkRoom room) {
        holder.setDestination(new TargetOnline(room));
    }

    @NonNull
    @Override
    public TargetHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_room, parent, false);

        return new TargetHolder(v, this);
    }

    @Override
    public void onDataChanged() {
        final int count = getItemCount();
        Log.d(GameServiceOnline.TAG, "PickerAdapter.onDataChanged: " + count);

        if (mOnEmptyStateListener != null) {
            mOnEmptyStateListener.onEmptyState(count == 0);
        }
    }

    @Override
    public void onError(@NonNull DatabaseError e) {
        if (mOnScanStoppedListener != null) {
            mOnScanStoppedListener.onScanStopped(e.toException());
        }
    }

    @Override
    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    @NonNull
    @Override
    public OnItemClickListener getOnItemClickListener() {
        return mOnItemClickListener;
    }

    @Override
    public void setOnItemClickListener(@Nullable OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.Adapter getAdapter() {
        return this;
    }

    @Override
    public void startScan(@NonNull OnScanStoppedListener listener) {
        checkNotNull(listener);

        mOnScanStoppedListener = listener;
        startListening();
    }

    @Override
    public void stopScan() {
        mOnScanStoppedListener = null;
        stopListening();
    }

    @Override
    public boolean isScanning() {
        return mOnScanStoppedListener != null;
    }

    @Override
    public void setOnEmptyStateListener(@Nullable OnEmptyStateListener listener) {
        this.mOnEmptyStateListener = listener;
    }
}