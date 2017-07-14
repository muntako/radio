package com.muntako.radio.decoration;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.muntako.radio.R;

/**
 * Created by akhmadmuntako on 22/09/2016.
 */
public class IconPreferenceScreen extends Preference {
    private Drawable mIcon;
    
    public IconPreferenceScreen(Context context, AttributeSet attrs) {
        super(context, attrs,0);
    }

    public IconPreferenceScreen(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutResource(R.layout.layout_preference_icon);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.IconPreferenceScreen, defStyleAttr,0);
        mIcon = a.getDrawable(R.styleable.IconPreferenceScreen_icon_);
    }
    @Override
    public void onBindView(View view){
        super.onBindView(view);
        ImageView imageView = (ImageView)view.findViewById(R.id.icon);
        if(imageView !=null&&mIcon!=null){
            imageView.setImageDrawable(mIcon);
        }
    }
    public void setIcon(Drawable icon){
        if((icon==null && mIcon !=null)||(mIcon != null && !mIcon.equals(mIcon))) {
            mIcon = icon;
            notifyChanged();
        }
    }
}
