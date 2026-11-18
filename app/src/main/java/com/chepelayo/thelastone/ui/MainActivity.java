package com.chepelayo.thelastone.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.chepelayo.thelastone.R;
import com.chepelayo.thelastone.databinding.ActivityMainBinding;
import com.chepelayo.thelastone.utils.ErrorMessages;
import com.chepelayo.thelastone.viewmodel.GamesViewModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/** Contenedor principal: barra superior, fragmento actual y BottomNavigationView. */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {                       // sin sesión → al login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ViewModel compartido: empieza a escuchar el catálogo del usuario
        GamesViewModel viewModel = new ViewModelProvider(this).get(GamesViewModel.class);
        viewModel.start(user.getUid());
        viewModel.getError().observe(this, e -> {
            if (e == null) return;
            Snackbar.make(binding.getRoot(), ErrorMessages.fromFirestore(this, e), Snackbar.LENGTH_LONG)
                    .setAnchorView(binding.bottomNav).show();
            viewModel.clearError();
        });

        // Navegación inferior (lambda)
        binding.bottomNav.setOnItemSelectedListener(item -> {
            showSection(item.getItemId());
            return true;
        });

        if (savedInstanceState == null) {
            binding.bottomNav.setSelectedItemId(R.id.nav_catalog);   // dispara el listener
        } else {
            binding.toolbar.setTitle(titleFor(binding.bottomNav.getSelectedItemId()));
        }
    }

    /** Sustituye el fragmento visible según el botón pulsado. */
    private void showSection(int itemId) {
        Fragment fragment;
        if (itemId == R.id.nav_favorites) {
            fragment = CatalogFragment.newInstance(true);
        } else if (itemId == R.id.nav_stats) {
            fragment = new StatsFragment();
        } else if (itemId == R.id.nav_profile) {
            fragment = new ProfileFragment();
        } else {
            fragment = CatalogFragment.newInstance(false);
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
        binding.toolbar.setTitle(titleFor(itemId));
    }

    @StringRes
    private int titleFor(int itemId) {
        if (itemId == R.id.nav_favorites) return R.string.title_favorites;
        if (itemId == R.id.nav_stats) return R.string.title_stats;
        if (itemId == R.id.nav_profile) return R.string.title_profile;
        return R.string.title_catalog;
    }
}