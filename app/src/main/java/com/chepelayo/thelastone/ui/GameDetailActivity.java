package com.chepelayo.thelastone.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.chepelayo.thelastone.R;
import com.chepelayo.thelastone.data.GameRepository;
import com.chepelayo.thelastone.databinding.ActivityGameDetailBinding;
import com.chepelayo.thelastone.model.Game;
import com.chepelayo.thelastone.model.GameStatus;
import com.chepelayo.thelastone.utils.ErrorMessages;
import com.chepelayo.thelastone.utils.ShareHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

/** Ficha de un juego. Escucha el documento en tiempo real, así se actualiza tras editarlo. */
public class GameDetailActivity extends AppCompatActivity {

    public static final String EXTRA_GAME_ID = "extra_game_id";

    private ActivityGameDetailBinding binding;
    private final GameRepository repository = new GameRepository();
    private ListenerRegistration registration;
    private Game game;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String gameId = getIntent().getStringExtra(EXTRA_GAME_ID);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (gameId == null || user == null) { finish(); return; }
        uid = user.getUid();

        binding.toolbar.inflateMenu(R.menu.menu_game_detail);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.toolbar.setOnMenuItemClickListener(this::onMenuItemClick);
        binding.btnFavorite.setOnClickListener(v -> toggleFavorite());

        registration = repository.listenGame(uid, gameId, this::render, this::showError);
    }

    private boolean onMenuItemClick(MenuItem item) {
        if (game == null) return false;
        int id = item.getItemId();
        if (id == R.id.action_share) {
            ShareHelper.shareGame(this, game);
            return true;
        }
        if (id == R.id.action_edit) {
            Intent intent = new Intent(this, GameFormActivity.class);
            intent.putExtra(GameFormActivity.EXTRA_GAME, game);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_delete) {
            confirmDelete();
            return true;
        }
        return false;
    }

    /** Pinta todos los datos del juego. Si llega null, el juego se ha borrado. */
    private void render(Game g) {
        if (g == null) { finish(); return; }
        game = g;

        binding.toolbar.setTitle(g.getTitle());
        binding.tvTitle.setText(g.getTitle());
        binding.tvDeveloperYear.setText(getString(R.string.developer_year_format, g.getDeveloper(), g.getReleaseYear()));
        binding.chipPlatform.setText(g.getPlatform());
        binding.chipGenre.setText(g.getGenre());

        GameStatus status = g.getStatusEnum();
        binding.chipStatus.setText(status.getLabelRes());
        binding.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this, status.getColorRes())));

        if (g.getScore() != null) {
            binding.tvScore.setText(g.getScoreText());
            binding.tvScore.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, g.getScoreColorRes())));
            binding.tvScore.setVisibility(View.VISIBLE);
        } else {
            binding.tvScore.setVisibility(View.GONE);
        }
        binding.tvPlayedYear.setText(g.getPlayedYear() != null
                ? getString(R.string.played_year_format, g.getPlayedYear())
                : getString(R.string.detail_not_played));

        binding.tvNotes.setText(TextUtils.isEmpty(g.getNotes()) ? getString(R.string.detail_no_notes) : g.getNotes());

        binding.btnFavorite.setText(g.isFavorite() ? R.string.fav_remove : R.string.fav_add);
        binding.btnFavorite.setIconResource(g.isFavorite() ? R.drawable.ic_favorite_on : R.drawable.ic_favorite_off);

        Glide.with(this).load(g.getCoverUrl())
                .placeholder(R.drawable.bg_cover_placeholder)
                .error(R.drawable.bg_cover_placeholder)
                .fallback(R.drawable.bg_cover_placeholder)
                .centerCrop().into(binding.ivCover);
    }

    private void toggleFavorite() {
        if (game == null) return;
        repository.setFavorite(uid, game.getId(), !game.isFavorite()).addOnFailureListener(this::showError);
    }

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_title)
                .setMessage(getString(R.string.delete_message, game.getTitle()))
                .setPositiveButton(R.string.delete, (dialog, which) ->
                        repository.deleteGame(uid, game.getId())
                                .addOnSuccessListener(unused -> finish())
                                .addOnFailureListener(this::showError))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showError(Exception e) {
        Snackbar.make(binding.getRoot(), ErrorMessages.fromFirestore(this, e), Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (registration != null) registration.remove();
    }
}