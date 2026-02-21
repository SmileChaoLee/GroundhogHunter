package com.smile.groundhoghunter.ArrayAdapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.smile.smilelibraries.utilities.ScreenUtil;
import java.util.List;

public class TwoPlayerListAdapter extends ArrayAdapter {
    private final int mTextViewResourceId;
    private final float textFontSize;

    @SuppressWarnings("unchecked")
    public TwoPlayerListAdapter(@NonNull Context context, int resource,
                                int textViewResourceId, @NonNull List objects,
                                float textSize) {
        super(context, resource, textViewResourceId, objects);
        mTextViewResourceId = textViewResourceId;
        textFontSize = textSize;
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        return super.getItem(position);
    }

    @SuppressWarnings("unchecked")
    @Override
    public int getPosition(@Nullable Object item) {
        return super.getPosition(item);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        if (getCount() == 0) {
            return view;
        }
        TextView itemTextView = view.findViewById(mTextViewResourceId);
        ScreenUtil.resizeTextSize(itemTextView, textFontSize);
        return view;
    }

    @SuppressWarnings("unchecked")
    public void updateData(List newData) {
        clear();
        addAll(newData);
        notifyDataSetChanged();
    }
}
