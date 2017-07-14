package com.muntako.radio.decoration;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by Lenovo on 21/06/2016.
 */
public class LayoutSquare extends LinearLayout{
    public LayoutSquare(Context context) {
        super(context);
    }

    public LayoutSquare(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public LayoutSquare(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int ignoredHeightMeasureSpec) {
        int newHeightMeasureSpec = widthMeasureSpec;
//        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        super.onMeasure(ignoredHeightMeasureSpec,ignoredHeightMeasureSpec);
    }
}
