/*
 * MIT License
 *
 * Copyright (c) 2023 klnvch
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

package by.klnvch.link5dots.ui.settings.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import by.klnvch.link5dots.R;
import by.klnvch.link5dots.domain.models.DotsType;

@SuppressWarnings({"unused"})
public class DotsTypePreference extends Preference {

    private static final int DEFAULT_VALUE = DotsType.ORIGINAL;

    private int mCurrentValue;
    private boolean mCurrentValueSet;

    public DotsTypePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setWidgetLayoutResource(R.layout.switch_dots_type);
    }

    public DotsTypePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWidgetLayoutResource(R.layout.switch_dots_type);
    }

    public DotsTypePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.switch_dots_type);
    }

    public DotsTypePreference(Context context) {
        super(context);
        setWidgetLayoutResource(R.layout.switch_dots_type);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        final ImageView dot1 = (ImageView) holder.findViewById(R.id.dot_1);
        final ImageView dot2 = (ImageView) holder.findViewById(R.id.dot_2);
        switch (mCurrentValue) {
            case DotsType.CROSS_AND_RING:
                dot1.setImageResource(R.drawable.game_dot_cross_red);
                dot2.setImageResource(R.drawable.game_dot_ring_blue);
                break;
            case DotsType.ORIGINAL:
            default:
                dot1.setImageResource(R.drawable.game_dot_circle_red);
                dot2.setImageResource(R.drawable.game_dot_circle_blue);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, DEFAULT_VALUE);
    }

    @Override
    protected void onSetInitialValue(@Nullable Object defaultValue) {
        if (defaultValue == null) {
            defaultValue = DEFAULT_VALUE;
        }
        setCurrentValue(getPersistedInt((Integer) defaultValue));
    }

    @Override
    protected void onClick() {
        final int newValue;
        switch (mCurrentValue) {
            case DotsType.CROSS_AND_RING:
                newValue = DotsType.ORIGINAL;
                break;
            case DotsType.ORIGINAL:
            default:
                newValue = DotsType.CROSS_AND_RING;
        }

        if (callChangeListener(newValue)) {
            setCurrentValue(newValue);
        }
    }

    private void setCurrentValue(int currentValue) {
        final boolean changed = mCurrentValue != currentValue;
        if (changed || !mCurrentValueSet) {
            mCurrentValue = currentValue;
            mCurrentValueSet = true;
            persistInt(currentValue);
            if (changed) {
                notifyDependencyChange(shouldDisableDependents());
                notifyChanged();
            }
        }
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
        if (state != null && !state.getClass().equals(SavedState.class)) {
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
