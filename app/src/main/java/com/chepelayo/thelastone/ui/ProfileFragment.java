package com.chepelayo.thelastone.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import com.chepelayo.thelastone.R;
import com.chepelayo.thelastone.databinding.FragmentProfileBinding;
import com.chepelayo.thelastone.utils.ThemeHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.Locale;

/** Perfil del usuario, selector de tema y cierre de sesión. */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String name = (user != null && !TextUtils.isEmpty(user.getDisplayName()))
                ? user.getDisplayName() : getString(R.string.profile_default_name);
        binding.tvName.setText(name);
        binding.tvEmail.setText(user != null && user.getEmail() != null ? user.getEmail() : "");
        binding.tvAvatar.setText(name.substring(0, 1).toUpperCase(Locale.getDefault()));

        setupThemeSelector();

        binding.btnLogout.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.logout_title)
                .setMessage(R.string.logout_message)
                .setPositiveButton(R.string.btn_logout, (dialog, which) -> logout())
                .setNegativeButton(R.string.cancel, null)
                .show());
    }

    /** Selector Sistema / Claro / Oscuro. */
    private void setupThemeSelector() {
        int saved = ThemeHelper.getSavedMode(requireContext());
        int checked = R.id.btnThemeSystem;
        if (saved == AppCompatDelegate.MODE_NIGHT_NO) checked = R.id.btnThemeLight;
        if (saved == AppCompatDelegate.MODE_NIGHT_YES) checked = R.id.btnThemeDark;
        binding.toggleTheme.check(checked);

        // El listener se añade DESPUÉS de marcar el botón para no disparar un cambio de tema
        binding.toggleTheme.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            int mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            if (checkedId == R.id.btnThemeLight) mode = AppCompatDelegate.MODE_NIGHT_NO;
            if (checkedId == R.id.btnThemeDark) mode = AppCompatDelegate.MODE_NIGHT_YES;
            ThemeHelper.saveAndApply(requireContext(), mode);
        });
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}