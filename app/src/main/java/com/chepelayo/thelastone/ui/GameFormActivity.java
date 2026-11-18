package com.chepelayo.thelastone.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.IntentCompat;
import com.bumptech.glide.Glide;
import com.chepelayo.thelastone.R;
import com.chepelayo.thelastone.data.GameRepository;
import com.chepelayo.thelastone.databinding.ActivityGameFormBinding;
import com.chepelayo.thelastone.model.Game;
import com.chepelayo.thelastone.model.GameStatus;
import com.chepelayo.thelastone.utils.ErrorMessages;
import com.chepelayo.thelastone.utils.Validators;
import com.chepelayo.thelastone.utils.Watchers;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.chepelayo.thelastone.utils.Validators.textOf;

/**
 * Formulario para añadir o editar un juego.
 * - Si recibe EXTRA_GAME, está en modo edición.
 * - Si el estado es "Pendiente", año jugado y puntuación se deshabilitan.
 */
public class GameFormActivity extends AppCompatActivity {

    public static final String EXTRA_GAME = "extra_game";
    private static final String STATE_STATUS = "state_status";
    private static final String STATE_GENRE = "state_genre";
    private static final String STATE_PLATFORM = "state_platform";
    private static final int MAX_TITLE = 100;
    private static final int MAX_NOTES = 500;

    private ActivityGameFormBinding binding;
    private final GameRepository repository = new GameRepository();
    private Game editingGame;                                    // null = juego nuevo
    private GameStatus selectedStatus = GameStatus.PENDING;

    /** Recibe el juego elegido en la búsqueda de RAWG y rellena el formulario. */
    private final ActivityResultLauncher<Intent> rawgLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Game prefill = IntentCompat.getSerializableExtra(
                            result.getData(), RawgSearchActivity.EXTRA_RESULT, Game.class);
                    if (prefill != null) applyPrefill(prefill);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        editingGame = IntentCompat.getSerializableExtra(getIntent(), EXTRA_GAME, Game.class);
        binding.toolbar.setTitle(editingGame == null ? R.string.form_title_new : R.string.form_title_edit);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupDropdowns();
        setupErrorClearing();

        binding.btnAutofill.setOnClickListener(v -> rawgLauncher.launch(new Intent(this, RawgSearchActivity.class)));
        binding.btnSave.setOnClickListener(v -> saveGame());
        binding.btnCancel.setOnClickListener(v -> finish());

        // Estado inicial: rotación → restaurar; edición → rellenar; nuevo → pendiente
        if (savedInstanceState != null) {
            binding.actGenre.setText(savedInstanceState.getString(STATE_GENRE, ""), false);
            binding.actPlatform.setText(savedInstanceState.getString(STATE_PLATFORM, ""), false);
            applyStatus(GameStatus.fromKey(savedInstanceState.getString(STATE_STATUS)));
        } else if (editingGame != null) {
            fillForm(editingGame);
        } else {
            applyStatus(GameStatus.PENDING);
        }
    }

    // ---------------------------------------------------------------- configuración

    private void setupDropdowns() {
        binding.actGenre.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.genres)));
        binding.actPlatform.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.platforms)));

        // Etiquetas de estado generadas con un stream
        List<String> labels = Arrays.stream(GameStatus.values())
                .map(s -> getString(s.getLabelRes()))
                .collect(Collectors.toList());
        binding.actStatus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, labels));
        binding.actStatus.setOnItemClickListener((parent, v, position, id) -> applyStatus(GameStatus.values()[position]));
    }

    /** Cada error desaparece en cuanto el usuario corrige el campo. */
    private void setupErrorClearing() {
        binding.etTitle.addTextChangedListener(Watchers.afterChange(() -> binding.tilTitle.setError(null)));
        binding.etDeveloper.addTextChangedListener(Watchers.afterChange(() -> binding.tilDeveloper.setError(null)));
        binding.etReleaseYear.addTextChangedListener(Watchers.afterChange(() -> binding.tilReleaseYear.setError(null)));
        binding.etPlayedYear.addTextChangedListener(Watchers.afterChange(() -> binding.tilPlayedYear.setError(null)));
        binding.etScore.addTextChangedListener(Watchers.afterChange(() -> binding.tilScore.setError(null)));
        binding.etNotes.addTextChangedListener(Watchers.afterChange(() -> binding.tilNotes.setError(null)));
        binding.etCover.addTextChangedListener(Watchers.afterChange(() -> {
            binding.tilCover.setError(null);
            updateCoverPreview();
        }));
        binding.actGenre.setOnItemClickListener((p, v, pos, id) -> binding.tilGenre.setError(null));
        binding.actPlatform.setOnItemClickListener((p, v, pos, id) -> binding.tilPlatform.setError(null));
    }

    /** Aplica el estado elegido: si es pendiente, bloquea año jugado y puntuación. */
    private void applyStatus(GameStatus status) {
        selectedStatus = status;
        binding.actStatus.setText(getString(status.getLabelRes()), false);
        binding.tilStatus.setError(null);

        boolean played = status.isPlayed();
        binding.tilPlayedYear.setEnabled(played);
        binding.tilScore.setEnabled(played);
        if (!played) {
            binding.etPlayedYear.setText("");
            binding.etScore.setText("");
            binding.tilPlayedYear.setError(null);
            binding.tilScore.setError(null);
        }
    }

    private void fillForm(Game g) {
        binding.etTitle.setText(g.getTitle());
        binding.actGenre.setText(g.getGenre(), false);
        binding.etDeveloper.setText(g.getDeveloper());
        binding.actPlatform.setText(g.getPlatform(), false);
        binding.etReleaseYear.setText(String.valueOf(g.getReleaseYear()));
        applyStatus(g.getStatusEnum());                       // antes de rellenar año/nota
        if (g.getPlayedYear() != null) binding.etPlayedYear.setText(String.valueOf(g.getPlayedYear()));
        if (g.getScore() != null) binding.etScore.setText(g.getScoreText());
        binding.etNotes.setText(g.getNotes());
        binding.etCover.setText(g.getCoverUrl());
        binding.switchFavorite.setChecked(g.isFavorite());
    }

    /** Datos importados de RAWG: solo rellena lo que venga informado. */
    private void applyPrefill(Game p) {
        if (!TextUtils.isEmpty(p.getTitle())) binding.etTitle.setText(p.getTitle());
        if (!TextUtils.isEmpty(p.getGenre())) binding.actGenre.setText(p.getGenre(), false);
        if (!TextUtils.isEmpty(p.getDeveloper())) binding.etDeveloper.setText(p.getDeveloper());
        if (!TextUtils.isEmpty(p.getPlatform())) binding.actPlatform.setText(p.getPlatform(), false);
        if (p.getReleaseYear() > 0) binding.etReleaseYear.setText(String.valueOf(p.getReleaseYear()));
        if (!TextUtils.isEmpty(p.getCoverUrl())) binding.etCover.setText(p.getCoverUrl());
        Snackbar.make(binding.getRoot(), R.string.form_autofilled, Snackbar.LENGTH_LONG).show();
    }

    private void updateCoverPreview() {
        String url = textOf(binding.etCover).trim();
        if (url.isEmpty() || !Validators.isValidUrl(url)) {
            binding.ivCoverPreview.setVisibility(View.GONE);
            return;
        }
        binding.ivCoverPreview.setVisibility(View.VISIBLE);
        Glide.with(this).load(url).centerCrop().into(binding.ivCoverPreview);
    }

    // ---------------------------------------------------------------- validación y guardado

    /** Valida todos los campos. Marca cada error en su TextInputLayout. */
    private boolean validateForm() {
        boolean valid = true;
        int currentYear = Validators.currentYear();
        int maxReleaseYear = currentYear + 5;

        String title = textOf(binding.etTitle).trim();
        if (title.isEmpty()) {
            binding.tilTitle.setError(getString(R.string.error_title_required));
            valid = false;
        } else if (title.length() > MAX_TITLE) {
            binding.tilTitle.setError(getString(R.string.error_title_too_long));
            valid = false;
        }

        if (textOf(binding.actGenre).trim().isEmpty()) {
            binding.tilGenre.setError(getString(R.string.error_genre_required));
            valid = false;
        }
        if (textOf(binding.etDeveloper).trim().isEmpty()) {
            binding.tilDeveloper.setError(getString(R.string.error_developer_required));
            valid = false;
        }
        if (textOf(binding.actPlatform).trim().isEmpty()) {
            binding.tilPlatform.setError(getString(R.string.error_platform_required));
            valid = false;
        }

        Integer releaseYear = Validators.parseIntOrNull(textOf(binding.etReleaseYear));
        if (releaseYear == null || releaseYear < Validators.MIN_YEAR || releaseYear > maxReleaseYear) {
            binding.tilReleaseYear.setError(getString(R.string.error_release_year, Validators.MIN_YEAR, maxReleaseYear));
            valid = false;
        }

        // Año jugado y puntuación: opcionales aunque el juego ya se haya jugado.
        // Si el usuario introduce datos, se valida que estén en el rango correcto.
        if (selectedStatus.isPlayed()) {
            String playedYearStr = textOf(binding.etPlayedYear).trim();
            if (!playedYearStr.isEmpty()) {
                Integer playedYear = Validators.parseIntOrNull(playedYearStr);
                int minPlayed = releaseYear != null ? releaseYear : Validators.MIN_YEAR;
                if (playedYear == null || playedYear < minPlayed || playedYear > currentYear) {
                    binding.tilPlayedYear.setError(getString(R.string.error_played_year_range, minPlayed, currentYear));
                    valid = false;
                }
            }

            String scoreStr = textOf(binding.etScore).trim();
            if (!scoreStr.isEmpty()) {
                Double score = Validators.parseDoubleOrNull(scoreStr);
                if (score == null || score < 0 || score > 10) {
                    binding.tilScore.setError(getString(R.string.error_score_range));
                    valid = false;
                }
            }
        }

        if (textOf(binding.etNotes).length() > MAX_NOTES) {
            binding.tilNotes.setError(getString(R.string.error_notes_too_long));
            valid = false;
        }

        String cover = textOf(binding.etCover).trim();
        if (!cover.isEmpty() && !Validators.isValidUrl(cover)) {
            binding.tilCover.setError(getString(R.string.error_cover_invalid));
            valid = false;
        }

        if (!valid) {
            Snackbar.make(binding.getRoot(), R.string.form_review_fields, Snackbar.LENGTH_SHORT).show();
        }
        return valid;
    }

    private void saveGame() {
        if (!validateForm()) return;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { finish(); return; }

        setSaving(true);
        repository.saveGame(user.getUid(), buildGame())
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, R.string.form_saved, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    setSaving(false);
                    Snackbar.make(binding.getRoot(), ErrorMessages.fromFirestore(this, e), Snackbar.LENGTH_LONG).show();
                });
    }

    /** Construye el Game a partir del formulario (ya validado). */
    private Game buildGame() {
        Game g = new Game();
        if (editingGame != null) {                              // conservar id y fecha de creación
            g.setId(editingGame.getId());
            g.setCreatedAt(editingGame.getCreatedAt());
        } else {
            g.setCreatedAt(System.currentTimeMillis());
        }
        g.setTitle(textOf(binding.etTitle).trim());
        g.setGenre(textOf(binding.actGenre).trim());
        g.setDeveloper(textOf(binding.etDeveloper).trim());
        g.setPlatform(textOf(binding.actPlatform).trim());
        g.setReleaseYear(Validators.parseIntOrNull(textOf(binding.etReleaseYear)));
        g.setStatus(selectedStatus.name());
        if (selectedStatus.isPlayed()) {
            g.setPlayedYear(Validators.parseIntOrNull(textOf(binding.etPlayedYear)));
            g.setScore(Validators.parseDoubleOrNull(textOf(binding.etScore)));
        } else {
            g.setPlayedYear(null);
            g.setScore(null);
        }
        g.setNotes(textOf(binding.etNotes).trim());
        g.setFavorite(binding.switchFavorite.isChecked());
        g.setCoverUrl(textOf(binding.etCover).trim());
        return g;
    }

    private void setSaving(boolean saving) {
        binding.progress.setVisibility(saving ? View.VISIBLE : View.GONE);
        binding.btnSave.setEnabled(!saving);
        binding.btnCancel.setEnabled(!saving);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_STATUS, selectedStatus.name());
        outState.putString(STATE_GENRE, textOf(binding.actGenre));
        outState.putString(STATE_PLATFORM, textOf(binding.actPlatform));
    }
}