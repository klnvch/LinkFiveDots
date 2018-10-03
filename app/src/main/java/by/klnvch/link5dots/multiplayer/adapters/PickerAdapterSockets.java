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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import by.klnvch.link5dots.R;
import by.klnvch.link5dots.multiplayer.targets.Target;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class PickerAdapterSockets extends RecyclerView.Adapter<TargetHolder>
        implements TargetAdapterInterface, ScannerInterface {

    private final List<Target> mDataset = new ArrayList<>();
    OnScanStoppedListener mOnScanStoppedListener;
    private OnItemClickListener mOnItemClickListener = null;
    private OnEmptyStateListener mOnEmptyStateListener = null;

    PickerAdapterSockets() {
    }

    @NonNull
    @Override
    public TargetHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_room, parent, false);
        return new TargetHolder(v, this);
    }

    @Override
    public void onBindViewHolder(@NonNull TargetHolder holder, int position) {
        holder.setDestination(mDataset.get(position));
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public void setOnEmptyStateListener(@Nullable OnEmptyStateListener listener) {
        this.mOnEmptyStateListener = listener;
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

    void add(@NonNull Target target) {
        mDataset.add(target);
        checksEmptiness();
        notifyDataSetChanged();
    }

    void remove(@NonNull Target target) {
        mDataset.remove(target);
        checksEmptiness();
        notifyDataSetChanged();
    }

    void clear() {
        mDataset.clear();
        checksEmptiness();
        notifyDataSetChanged();
    }

    private void checksEmptiness() {
        if (mOnEmptyStateListener != null) {
            mOnEmptyStateListener.onEmptyState(getItemCount() == 0);
        }
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

    protected abstract void startListening();

    protected abstract void stopListening();

    @Override
    public boolean isScanning() {
        return mOnScanStoppedListener != null;
    }
}
