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

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import by.klnvch.link5dots.R;
import by.klnvch.link5dots.multiplayer.targets.Target;

public class TargetHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final TextView mTextView;
    private final TargetAdapterInterface mAdapter;
    private Target mTarget;

    public TargetHolder(@NonNull View v, @NonNull TargetAdapterInterface adapter) {
        super(v);
        mAdapter = adapter;
        mTextView = v.findViewById(R.id.text);
        mTextView.setOnClickListener(this);
    }

    public void setDestination(@NonNull Target target) {
        mTarget = target;
        mTextView.setText(target.getLongName());
    }

    @Override
    public void onClick(View view) {
        mAdapter.getOnItemClickListener().onItemSelected(mTarget);
    }
}