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

package by.klnvch.link5dots.multiplayer.nsd;

import android.annotation.TargetApi;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

import by.klnvch.link5dots.R;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
class NsdListAdapter extends RecyclerView.Adapter<NsdListAdapter.ViewHolder> {

    private final ArrayList<NsdServiceInfo> mDataset = new ArrayList<>();
    private OnItemClickListener mListener = null;

    NsdListAdapter(@NonNull OnItemClickListener listener) {
        this.mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TextView v = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.device_name, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mTextView.setText(mDataset.get(position).getServiceName());
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    void replaceAll(@NonNull Collection<NsdServiceInfo> services) {
        mDataset.clear();
        mDataset.addAll(services);
        notifyDataSetChanged();
    }

    void clear() {
        mDataset.clear();
        notifyDataSetChanged();
    }

    interface OnItemClickListener {
        void onServiceSelected(@NonNull NsdServiceInfo info);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView mTextView;

        private ViewHolder(TextView v) {
            super(v);
            mTextView = v;
            mTextView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            if (mListener != null) {
                mListener.onServiceSelected(mDataset.get(pos));
            }
        }
    }
}