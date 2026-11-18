package com.chepelayo.thelastone;

import android.app.Application;
import com.chepelayo.thelastone.utils.ThemeHelper;

/** Se ejecuta al arrancar el proceso: aplica el tema (claro/oscuro/sistema) guardado. */
public class TheLastOneApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ThemeHelper.applySavedTheme(this);
    }
}