package com.chepelayo.thelastone.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

/** Guarda y aplica la preferencia de tema: sistema, claro u oscuro. */
public final class ThemeHelper {

    private static final String PREFS = "settings";
    private static final String KEY_THEME = "theme_mode";

    private ThemeHelper() { }

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static int getSavedMode(Context c) {
        return prefs(c).getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public static void applySavedTheme(Context c) {
        AppCompatDelegate.setDefaultNightMode(getSavedMode(c));
    }

    public static void saveAndApply(Context c, int mode) {
        prefs(c).edit().putInt(KEY_THEME, mode).apply();
        AppCompatDelegate.setDefaultNightMode(mode);   // recrea las Activities con el nuevo tema
    }
}