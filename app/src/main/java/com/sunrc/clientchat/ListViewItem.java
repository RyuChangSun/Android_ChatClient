package com.sunrc.clientchat;

import android.graphics.drawable.Drawable;

/**
 * Created by ryuchangsun on 2016. 12. 7..
 */

public class ListViewItem {
    private Drawable iconDrawable ;
    private String textStr ;

    public void setIcon(Drawable icon) {
        iconDrawable = icon ;
    }
    public void setText(String text) {
        textStr = text ;
    }

    public Drawable getIcon() {
        return this.iconDrawable ;
    }
    public String getText() {
        return this.textStr ;
    }
}
