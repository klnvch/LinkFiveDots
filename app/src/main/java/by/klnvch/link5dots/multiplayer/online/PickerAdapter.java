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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import by.klnvch.link5dots.R;

public class PickerAdapter extends FirebaseRecyclerAdapter<Room, PickerAdapter.RoomHolder> {

    private OnItemClickListener mOnItemClickListener = null;
    private OnEmptyStateListener mOnEmptyStateListener = null;

    private PickerAdapter(@NonNull FirebaseRecyclerOptions<Room> options) {
        super(options);
    }

    static PickerAdapter createAdapter(@NonNull DatabaseReference database) {
        Query query = database
                .child(Room.CHILD_ROOM)
                .orderByChild(Room.CHILD_STATE)
                .equalTo(Room.STATE_CREATED);

        FirebaseRecyclerOptions<Room> options =
                new FirebaseRecyclerOptions.Builder<Room>()
                        .setQuery(query, Room.class)
                        .build();

        return new PickerAdapter(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull RoomHolder holder, int position, @NonNull Room model) {
        holder.mTextView.setText(model.toString());
    }

    @Override
    public RoomHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_room, parent, false);

        return new RoomHolder(v);
    }

    @Override
    public void onDataChanged() {
        final int count = getItemCount();
        Log.d(OnlineService.TAG, "PickerAdapter.onDataChanged: " + count);

        if (mOnEmptyStateListener != null) {
            mOnEmptyStateListener.onEmptyState(count == 0);
        }
    }

    @Override
    public void onError(@NonNull DatabaseError e) {
        Log.d(OnlineService.TAG, "PickerAdapter.onError", e.toException());
    }

    boolean isEmpty() {
        return getItemCount() == 0;
    }

    void setOnItemClickListener(@Nullable OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    void setOnEmptyStateListener(@Nullable OnEmptyStateListener listener) {
        this.mOnEmptyStateListener = listener;
    }

    interface OnItemClickListener {
        void onItemSelected(@NonNull Room room);
    }

    interface OnEmptyStateListener {
        void onEmptyState(boolean isEmpty);
    }

    class RoomHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView mTextView;

        private RoomHolder(View v) {
            super(v);
            mTextView = v.findViewById(R.id.text);
            mTextView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mOnItemClickListener != null) {
                int pos = getAdapterPosition();
                Room room = getItem(pos);
                mOnItemClickListener.onItemSelected(room);
            }
        }
    }
}