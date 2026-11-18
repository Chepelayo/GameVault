package com.chepelayo.thelastone.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.chepelayo.thelastone.R;
import com.chepelayo.thelastone.databinding.FragmentStatsBinding;
import com.chepelayo.thelastone.databinding.ItemStatBarBinding;
import com.chepelayo.thelastone.model.Game;
import com.chepelayo.thelastone.model.GameStatus;
import com.chepelayo.thelastone.viewmodel.GamesViewModel;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Estadísticas calculadas en el dispositivo con streams y lambdas. */
public class StatsFragment extends Fragment {

    private FragmentStatsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GamesViewModel viewModel = new ViewModelProvider(requireActivity()).get(GamesViewModel.class);
        viewModel.getGames().observe(getViewLifecycleOwner(), this::render);
    }

    private void render(List<Game> games) {
        boolean empty = games.isEmpty();
        binding.tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.contentStats.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) return;

        int total = games.size();
        String dash = getString(R.string.no_data_dash);

        // Nota media (solo juegos puntuados)
        OptionalDouble average = games.stream()
                .filter(g -> g.getScore() != null)
                .mapToDouble(Game::getScore)
                .average();

        // Porcentaje de completados
        long completed = games.stream().filter(g -> g.getStatusEnum() == GameStatus.COMPLETED).count();
        int percent = Math.round(completed * 100f / total);

        Map<String, Long> byGenre = countBy(games, Game::getGenre, Integer.MAX_VALUE);
        Map<String, Long> byPlatform = countBy(games, Game::getPlatform, 6);

        binding.tvTotal.setText(String.valueOf(total));
        binding.tvAverage.setText(average.isPresent()
                ? String.format(Locale.getDefault(), "%.1f", average.getAsDouble()) : dash);
        binding.tvCompleted.setText(getString(R.string.percent_format, percent));
        binding.tvTopGenre.setText(byGenre.keySet().stream().findFirst().orElse(dash));   // el primero es el más frecuente

        // Juegos por estado
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (GameStatus s : GameStatus.values()) {
            byStatus.put(getString(s.getLabelRes()), games.stream().filter(g -> g.getStatusEnum() == s).count());
        }
        fillBars(binding.llStatus, byStatus);
        fillBars(binding.llPlatform, byPlatform);

        // Top 5 por puntuación
        List<Game> top = games.stream()
                .filter(g -> g.getScore() != null)
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(5)
                .collect(Collectors.toList());
        binding.cardTop.setVisibility(top.isEmpty() ? View.GONE : View.VISIBLE);
        binding.llTop.removeAllViews();
        top.forEach(g -> addBar(binding.llTop, g.getTitle(), g.getScoreText(), (int) (g.getScore() * 10)));
    }

    /** Cuenta cuántos juegos hay por clave (género, plataforma...) ordenado de mayor a menor. */
    private Map<String, Long> countBy(List<Game> games, Function<Game, String> key, int limit) {
        return games.stream()
                .map(key)
                .filter(v -> v != null && !v.isEmpty())
                .collect(Collectors.groupingBy(v -> v, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }

    private void fillBars(LinearLayout container, Map<String, Long> data) {
        container.removeAllViews();
        long max = Math.max(1, data.values().stream().mapToLong(Long::longValue).max().orElse(1));
        data.forEach((label, value) -> addBar(container, label, String.valueOf(value), (int) (value * 100 / max)));
    }

    private void addBar(LinearLayout container, String label, String valueText, int progress) {
        ItemStatBarBinding row = ItemStatBarBinding.inflate(getLayoutInflater(), container, false);
        row.tvLabel.setText(label);
        row.tvValue.setText(valueText);
        row.progress.setProgressCompat(progress, true);
        container.addView(row.getRoot());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}