package com.chepelayo.thelastone.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import com.chepelayo.thelastone.R;
import com.chepelayo.thelastone.adapter.GameAdapter;
import com.chepelayo.thelastone.databinding.FragmentCatalogBinding;
import com.chepelayo.thelastone.model.Game;
import com.chepelayo.thelastone.model.GameStatus;
import com.chepelayo.thelastone.utils.ShareHelper;
import com.chepelayo.thelastone.utils.Validators;
import com.chepelayo.thelastone.utils.Watchers;
import com.chepelayo.thelastone.viewmodel.GamesViewModel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Listado de juegos con búsqueda, filtro por estado y ordenación.
 * Se reutiliza para "Catálogo" (todos) y "Favoritos" (onlyFavorites = true).
 */
public class CatalogFragment extends Fragment {

    private static final String ARG_ONLY_FAVORITES = "only_favorites";
    private static final String STATE_SORT = "state_sort";

    private FragmentCatalogBinding binding;
    private GameAdapter adapter;
    private boolean onlyFavorites;
    private int sortMode = 0;
    private List<Game> allGames = new ArrayList<>();
    private List<Game> visibleGames = new ArrayList<>();   // lo que se ve (para compartir)

    public static CatalogFragment newInstance(boolean onlyFavorites) {
        CatalogFragment fragment = new CatalogFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_ONLY_FAVORITES, onlyFavorites);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onlyFavorites = getArguments() != null && getArguments().getBoolean(ARG_ONLY_FAVORITES);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCatalogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) sortMode = savedInstanceState.getInt(STATE_SORT, 0);

        GamesViewModel viewModel = new ViewModelProvider(requireActivity()).get(GamesViewModel.class);

        // Lista: 1 columna en vertical, más en horizontal/tablet (valor en res/values*/integers.xml)
        adapter = new GameAdapter(this::openDetail, viewModel::toggleFavorite);
        binding.rvGames.setLayoutManager(new GridLayoutManager(requireContext(), getResources().getInteger(R.integer.grid_columns)));
        binding.rvGames.setAdapter(adapter);

        setupSortDropdown();

        // Filtros: cualquier cambio vuelve a calcular la lista
        binding.etSearch.addTextChangedListener(Watchers.afterChange(this::applyFilters));
        binding.chipGroupStatus.setOnCheckedStateChangeListener((group, ids) -> applyFilters());

        binding.fabAdd.setOnClickListener(v -> startActivity(new Intent(requireContext(), GameFormActivity.class)));
        binding.btnShareList.setOnClickListener(v -> ShareHelper.shareList(requireContext(), visibleGames));

        // Cada vez que Firestore cambia, llega la lista nueva
        viewModel.getGames().observe(getViewLifecycleOwner(), games -> {
            allGames = games;
            applyFilters();
        });
    }

    private void setupSortDropdown() {
        String[] options = getResources().getStringArray(R.array.sort_options);
        binding.actSort.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, options));
        binding.actSort.setText(options[sortMode], false);
        binding.actSort.setOnItemClickListener((parent, v, position, id) -> {
            sortMode = position;
            applyFilters();
        });
    }

    /** Aplica favoritos + búsqueda + estado + orden con streams y lambdas. */
    private void applyFilters() {
        if (binding == null) return;
        String query = Validators.textOf(binding.etSearch).trim().toLowerCase(Locale.getDefault());
        GameStatus status = selectedStatus();

        List<Game> result = allGames.stream()
                .filter(g -> !onlyFavorites || g.isFavorite())
                .filter(g -> status == null || g.getStatusEnum() == status)
                .filter(g -> query.isEmpty() || lower(g.getTitle()).contains(query) || lower(g.getDeveloper()).contains(query))
                .sorted(comparatorFor(sortMode))
                .collect(Collectors.toList());

        visibleGames = result;
        adapter.submitList(result);
        updateEmptyState(result.isEmpty());
    }

    /** null significa "Todos". */
    private GameStatus selectedStatus() {
        int id = binding.chipGroupStatus.getCheckedChipId();
        if (id == R.id.chipPending) return GameStatus.PENDING;
        if (id == R.id.chipPlaying) return GameStatus.PLAYING;
        if (id == R.id.chipCompleted) return GameStatus.COMPLETED;
        if (id == R.id.chipAbandoned) return GameStatus.ABANDONED;
        return null;
    }

    /** Comparadores como lambdas. */
    private Comparator<Game> comparatorFor(int mode) {
        switch (mode) {
            case 1:  return (a, b) -> Double.compare(scoreOf(b), scoreOf(a));                 // mejor nota primero
            case 2:  return (a, b) -> Integer.compare(b.getReleaseYear(), a.getReleaseYear()); // más reciente primero
            case 3:  return (a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt());        // añadidos recientemente
            default: return (a, b) -> String.CASE_INSENSITIVE_ORDER.compare(str(a.getTitle()), str(b.getTitle()));
        }
    }

    private void updateEmptyState(boolean empty) {
        binding.tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        if (!empty) return;
        boolean hasBaseGames = allGames.stream().anyMatch(g -> !onlyFavorites || g.isFavorite());
        if (!hasBaseGames) {
            binding.tvEmpty.setText(onlyFavorites ? R.string.empty_favorites : R.string.empty_catalog);
        } else {
            binding.tvEmpty.setText(R.string.empty_no_results);
        }
    }

    private void openDetail(Game game) {
        Intent intent = new Intent(requireContext(), GameDetailActivity.class);
        intent.putExtra(GameDetailActivity.EXTRA_GAME_ID, game.getId());
        startActivity(intent);
    }

    private static double scoreOf(Game g) { return g.getScore() == null ? -1 : g.getScore(); }
    private static String str(String s) { return s == null ? "" : s; }
    private static String lower(String s) { return str(s).toLowerCase(Locale.getDefault()); }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SORT, sortMode);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;   // evita fugas de memoria
    }
}