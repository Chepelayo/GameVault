package com.chepelayo.thelastone.utils;

import android.content.Context;
import com.chepelayo.thelastone.R;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.FirebaseFirestoreException;

/** Traduce excepciones de Firebase a mensajes (de strings.xml) comprensibles. */
public final class ErrorMessages {

    private ErrorMessages() { }

    public static String fromAuth(Context ctx, Exception e) {
        android.util.Log.e("GameVault", "Error de autenticación", e);
        if (e instanceof FirebaseNetworkException) return ctx.getString(R.string.error_network);
        if (e instanceof FirebaseAuthUserCollisionException) return ctx.getString(R.string.error_email_in_use);
        // WeakPassword hereda de InvalidCredentials: hay que comprobarla antes
        if (e instanceof FirebaseAuthWeakPasswordException) return ctx.getString(R.string.error_weak_password);
        if (e instanceof FirebaseAuthInvalidUserException
                || e instanceof FirebaseAuthInvalidCredentialsException) {
            return ctx.getString(R.string.error_invalid_credentials);
        }
        if (e != null && e.getMessage() != null && e.getMessage().contains("blocked")) {
            return ctx.getString(R.string.error_auth_blocked);
        }
        return ctx.getString(R.string.error_generic);
    }

    public static String fromFirestore(Context ctx, Exception e) {
        if (e instanceof FirebaseFirestoreException) {
            switch (((FirebaseFirestoreException) e).getCode()) {
                case PERMISSION_DENIED: return ctx.getString(R.string.error_permission);
                case UNAVAILABLE: return ctx.getString(R.string.error_network);
                default: break;
            }
        }
        return ctx.getString(R.string.error_generic);
    }
}