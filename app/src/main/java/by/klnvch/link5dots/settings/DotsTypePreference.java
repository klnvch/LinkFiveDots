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

package by.klnvch.link5dots.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import by.klnvch.link5dots.R;

@SuppressWarnings({"unused"})
public class DotsTypePreference extends Preference {

    private static final int DEFAULT_VALUE = SettingsUtils.DOTS_TYPE_ORIGINAL;

    private int mCurrentValue;

    public DotsTypePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public DotsTypePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public DotsTypePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DotsTypePreference(Context context) {
        super(context);
        init();
    }

    private void init() {
        setWidgetLayoutResource(R.layout.switch_dots_type);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        final ImageView dot1 = (ImageView) holder.findViewById(R.id.dot_1);
        final ImageView dot2 = (ImageView) holder.findViewById(R.id.dot_2);
        if (mCurrentValue == SettingsUtils.DOTS_TYPE_ORIGINAL) {
            dot1.setImageResource(R.drawable.game_dot_circle_red);
            dot2.setImageResource(R.drawable.game_dot_circle_blue);
        } else {
            dot1.setImageResource(R.drawable.game_dot_cross_red);
            dot2.setImageResource(R.drawable.game_dot_ring_blue);
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            mCurrentValue = this.getPersistedInt(DEFAULT_VALUE);
        } else {
            mCurrentValue = (Integer) defaultValue;
            persistInt(mCurrentValue);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, DEFAULT_VALUE);
    }

    @Override
    protected void onClick() {
        final int newValue;
        if (mCurrentValue == SettingsUtils.DOTS_TYPE_ORIGINAL) {
            newValue = SettingsUtils.DOTS_TYPE_CROSS_AND_RING;
        } else {
            newValue = SettingsUtils.DOTS_TYPE_ORIGINAL;
        }

        if (!callChangeListener(newValue)) {
            return;
        }

        mCurrentValue = newValue;
        persistInt(mCurrentValue);
        notifyChanged();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.value = mCurrentValue;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }

        // Restore the instance state
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        mCurrentValue = myState.value;
        notifyChanged();
    }

    private static class SavedState extends BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
        int value;

        SavedState(Parcel source) {
            super(source);
            value = source.readInt();
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(value);
        }
    }
}