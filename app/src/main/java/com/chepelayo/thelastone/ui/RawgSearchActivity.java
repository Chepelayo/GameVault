package com.chepelayo.thelastone.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import com.chepelayo.thelastone.BuildConfig;
import com.chepelayo.thelastone.R;
import com.chepelayo.thelastone.adapter.RawgResultAdapter;
import com.chepelayo.thelastone.data.rawg.RawgClient;
import com.chepelayo.thelastone.data.rawg.RawgModels;
import com.chepelayo.thelastone.databinding.ActivityRawgSearchBinding;
import com.chepelayo.thelastone.utils.Validators;
import com.google.android.material.snackbar.Snackbar;
import java.io.IOException;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Busca juegos en la API de RAWG. Al elegir uno, pide su detalle (para obtener la desarrolladora)
 * y devuelve un Game a GameFormActivity para autocompletar el formulario.
 */
public class RawgSearchActivity extends AppCompatActivity {

    public static final String EXTRA_RESULT = "extra_rawg_result";

    private ActivityRawgSearchBinding binding;
    private RawgResultAdapter adapter;
    private Call<RawgModels.SearchResponse> currentSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRawgSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        adapter = new RawgResultAdapter(this::onResultSelected);
        binding.rvResults.setLayoutManager(new GridLayoutManager(this, getResources().getInteger(R.integer.grid_columns)));
        binding.rvResults.setAdapter(adapter);

        binding.btnSearch.setOnClickListener(v -> search());
        binding.etQuery.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) { search(); return true; }
            return false;
        });
    }

    private void search() {
        String query = Validators.textOf(binding.etQuery).trim();
        if (query.length() < 2) {
            binding.tilQuery.setError(getString(R.string.error_query_short));
            return;
        }
        binding.tilQuery.setError(null);

        // Sin API key configurada no tiene sentido llamar a la API
        if (BuildConfig.RAWG_API_KEY.isEmpty()) {
            showMessage(R.string.error_rawg_key_missing);
            return;
        }

        setLoading(true);
        if (currentSearch != null) currentSearch.cancel();
        currentSearch = RawgClient.getApi().searchGames(BuildConfig.RAWG_API_KEY, query, 20);
        currentSearch.enqueue(new Callback<RawgModels.SearchResponse>() {
            @Override
            public void onResponse(Call<RawgModels.SearchResponse> call, Response<RawgModels.SearchResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().results != null) {
                    List<RawgModels.RawgGame> results = response.body().results;
                    adapter.setItems(results);
                    binding.tvEmpty.setText(R.string.rawg_empty);
                    binding.tvEmpty.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    showMessage(R.string.error_rawg_http);
                }
            }

            @Override
            public void onFailure(Call<RawgModels.SearchResponse> call, Throwable t) {
                if (call.isCanceled()) return;               // búsqueda sustituida por otra
                setLoading(false);
                showMessage(t instanceof IOException ? R.string.error_network : R.string.error_generic);
            }
        });
    }

    /** El listado de RAWG no incluye desarrolladoras: pedimos el detalle del juego elegido. */
    private void onResultSelected(RawgModels.RawgGame item) {
        setLoading(true);
        RawgClient.getApi().getGame(item.id, BuildConfig.RAWG_API_KEY).enqueue(new Callback<RawgModels.RawgGame>() {
            @Override
            public void onResponse(Call<RawgModels.RawgGame> call, Response<RawgModels.RawgGame> response) {
                setLoading(false);
                // Si el detalle falla, usamos los datos del listado (sin desarrolladora)
                returnResult(response.isSuccessful() && response.body() != null ? response.body() : item);
            }

            @Override
            public void onFailure(Call<RawgModels.RawgGame> call, Throwable t) {
                setLoading(false);
                returnResult(item);
            }
        });
    }

    private void returnResult(RawgModels.RawgGame rawgGame) {
        Intent data = new Intent().putExtra(EXTRA_RESULT, rawgGame.toGame());
        setResult(RESULT_OK, data);
        finish();
    }

    private void setLoading(boolean loading) {
        binding.progress.setVisibility(loading ? View.VISIBLE : View.INVISIBLE);
        binding.btnSearch.setEnabled(!loading);
    }

    private void showMessage(@StringRes int message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentSearch != null) currentSearch.cancel();
    }
}