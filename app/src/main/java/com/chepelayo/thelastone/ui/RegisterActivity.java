package com.chepelayo.thelastone.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.chepelayo.thelastone.R;
import com.chepelayo.thelastone.databinding.ActivityRegisterBinding;
import com.chepelayo.thelastone.utils.ErrorMessages;
import com.chepelayo.thelastone.utils.Validators;
import com.chepelayo.thelastone.utils.Watchers;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

/** Registro de usuario nuevo. Guarda el nombre como displayName del perfil de Firebase. */
public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();

        binding.etName.addTextChangedListener(Watchers.afterChange(() -> binding.tilName.setError(null)));
        binding.etEmail.addTextChangedListener(Watchers.afterChange(() -> binding.tilEmail.setError(null)));
        binding.etPassword.addTextChangedListener(Watchers.afterChange(() -> binding.tilPassword.setError(null)));
        binding.etConfirm.addTextChangedListener(Watchers.afterChange(() -> binding.tilConfirm.setError(null)));

        binding.btnRegister.setOnClickListener(v -> attemptRegister());
        binding.tvGoLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String name = Validators.textOf(binding.etName).trim();
        String email = Validators.textOf(binding.etEmail).trim();
        String password = Validators.textOf(binding.etPassword);
        String confirm = Validators.textOf(binding.etConfirm);

        boolean valid = true;
        if (name.isEmpty()) {
            binding.tilName.setError(getString(R.string.error_name_required));
            valid = false;
        }
        if (!Validators.isValidEmail(email)) {
            binding.tilEmail.setError(getString(R.string.error_email_invalid));
            valid = false;
        }
        if (!Validators.isValidPassword(password)) {
            binding.tilPassword.setError(getString(R.string.error_password_short));
            valid = false;
        }
        if (!password.equals(confirm)) {
            binding.tilConfirm.setError(getString(R.string.error_password_mismatch));
            valid = false;
        }
        if (!valid) return;

        setLoading(true);
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) { goToMain(); return; }
                    UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name).build();
                    // Pase lo que pase con el nombre, entramos en la app
                    user.updateProfile(request).addOnCompleteListener(task -> goToMain());
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Snackbar.make(binding.getRoot(), ErrorMessages.fromAuth(this, e), Snackbar.LENGTH_LONG).show();
                });
    }

    private void setLoading(boolean loading) {
        binding.progress.setVisibility(loading ? View.VISIBLE : View.INVISIBLE);
        binding.btnRegister.setEnabled(!loading);
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}