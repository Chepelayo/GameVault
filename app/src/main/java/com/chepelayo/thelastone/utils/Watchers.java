package com.chepelayo.thelastone.utils;

import android.text.Editable;
import android.text.TextWatcher;

/** Permite usar una lambda donde Android pide un TextWatcher de 3 métodos. */
public final class Watchers {

    private Watchers() { }

    public static TextWatcher afterChange(Runnable action) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable s) { action.run(); }
        };
    }
}