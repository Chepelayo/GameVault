package com.chepelayo.thelastone.model;

import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;
import com.chepelayo.thelastone.R;

/** Estado de un juego en el catálogo. Cada estado conoce su texto y su color. */
public enum GameStatus {
    PENDING(R.string.status_pending, R.color.status_pending),
    PLAYING(R.string.status_playing, R.color.status_playing),
    COMPLETED(R.string.status_completed, R.color.status_completed),
    ABANDONED(R.string.status_abandoned, R.color.status_abandoned);

    private final int labelRes;
    private final int colorRes;

    GameStatus(@StringRes int labelRes, @ColorRes int colorRes) {
        this.labelRes = labelRes;
        this.colorRes = colorRes;
    }

    @StringRes public int getLabelRes() { return labelRes; }
    @ColorRes public int getColorRes() { return colorRes; }

    /** Un juego pendiente no tiene año de juego ni puntuación. */
    public boolean isPlayed() { return this != PENDING; }

    /** Convierte el texto guardado en Firestore al enum; si es inválido, devuelve PENDING. */
    public static GameStatus fromKey(String key) {
        try {
            return GameStatus.valueOf(key);
        } catch (IllegalArgumentException | NullPointerException e) {
            return PENDING;
        }
    }
}