package com.chepelayo.thelastone.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.chepelayo.thelastone.R;
import com.chepelayo.thelastone.databinding.ActivityLoginBinding;
import com.chepelayo.thelastone.utils.ErrorMessages;
import com.chepelayo.thelastone.utils.Validators;
import com.chepelayo.thelastone.utils.Watchers;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

/** Pantalla de inicio de sesión (Firebase Authentication con email y contraseña). */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();

        // Si ya hay sesión iniciada, saltamos directamente a la app
        if (auth.getCurrentUser() != null) {
            goToMain();
            return;
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Quitar el error en cuanto el usuario escribe
        binding.etEmail.addTextChangedListener(Watchers.afterChange(() -> binding.tilEmail.setError(null)));
        binding.etPassword.addTextChangedListener(Watchers.afterChange(() -> binding.tilPassword.setError(null)));

        binding.btnLogin.setOnClickListener(v -> attemptLogin());
        binding.tvGoRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void attemptLogin() {
        String email = Validators.textOf(binding.etEmail).trim();
        String password = Validators.textOf(binding.etPassword);

        boolean valid = true;
        if (!Validators.isValidEmail(email)) {
            binding.tilEmail.setError(getString(R.string.error_email_invalid));
            valid = false;
        }
        if (password.isEmpty()) {
            binding.tilPassword.setError(getString(R.string.error_password_required));
            valid = false;
        }
        if (!valid) return;

        setLoading(true);
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> goToMain())
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Snackbar.make(binding.getRoot(), ErrorMessages.fromAuth(this, e), Snackbar.LENGTH_LONG).show();
                });
    }

    private void setLoading(boolean loading) {
        binding.progress.setVisibility(loading ? View.VISIBLE : View.INVISIBLE);
        binding.btnLogin.setEnabled(!loading);
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}