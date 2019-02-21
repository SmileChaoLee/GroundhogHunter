package com.smile.groundhoghunter.Models;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class SmileImageButton extends android.support.v7.widget.AppCompatImageButton implements View.OnTouchListener {

    private final static int colorFilterChanged = Color.argb(100, 155, 155, 155);
    private final static int colorFilterOriginal = Color.argb(0, 155, 155, 155);

    public SmileImageButton(Context context) {
        super(context);
        init(context);
    }

    public SmileImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SmileImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            setColorFilter(colorFilterChanged);
            return true;
        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            view.performClick();
            setColorFilter(null);
            // setColorFilter(colorFilterOriginal); // or null
            return true;
        }
        return false;
    }

    private void init(Context context) {
        setOnTouchListener(this);
    }
}
