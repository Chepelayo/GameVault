package com.chepelayo.thelastone.utils;

import android.util.Patterns;
import android.widget.TextView;
import java.util.Calendar;

/** Validaciones y conversiones seguras (sin lanzar excepciones al formulario). */
public final class Validators {

    public static final int MIN_YEAR = 1950;

    private Validators() { }

    /** Texto de un campo, nunca null. */
    public static String textOf(TextView view) {
        CharSequence text = view.getText();
        return text == null ? "" : text.toString();
    }

    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /** Firebase exige un mínimo de 6 caracteres. */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public static boolean isValidUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"))
                && Patterns.WEB_URL.matcher(url).matches();
    }

    /** Devuelve null si el texto no es un entero válido. */
    public static Integer parseIntOrNull(String text) {
        try {
            return Integer.valueOf(text.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    /** Acepta coma o punto decimal. Devuelve null si no es un número. */
    public static Double parseDoubleOrNull(String text) {
        try {
            return Double.valueOf(text.trim().replace(',', '.'));
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    public static int currentYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }
}