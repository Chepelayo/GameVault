package com.chepelayo.thelastone.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.chepelayo.thelastone.R;
import com.chepelayo.thelastone.databinding.ItemGameBinding;
import com.chepelayo.thelastone.model.Game;
import com.chepelayo.thelastone.model.GameStatus;
import java.util.Objects;
import java.util.function.Consumer;

/** Adaptador del listado de juegos. Recibe lambdas para el clic y el favorito. */
public class GameAdapter extends ListAdapter<Game, GameAdapter.GameViewHolder> {

    private static final DiffUtil.ItemCallback<Game> DIFF = new DiffUtil.ItemCallback<Game>() {
        @Override
        public boolean areItemsTheSame(@NonNull Game a, @NonNull Game b) {
            return Objects.equals(a.getId(), b.getId());
        }
        @Override
        public boolean areContentsTheSame(@NonNull Game a, @NonNull Game b) {
            return a.sameContentAs(b);
        }
    };

    private final Consumer<Game> onClick;
    private final Consumer<Game> onFavorite;

    public GameAdapter(Consumer<Game> onClick, Consumer<Game> onFavorite) {
        super(DIFF);
        this.onClick = onClick;
        this.onFavorite = onFavorite;
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GameViewHolder(ItemGameBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        Game game = getItem(position);
        ItemGameBinding b = holder.binding;
        Context ctx = b.getRoot().getContext();

        b.tvTitle.setText(game.getTitle());
        b.tvDeveloperYear.setText(ctx.getString(R.string.developer_year_format, game.getDeveloper(), game.getReleaseYear()));
        b.tvPlatformGenre.setText(ctx.getString(R.string.platform_genre_format, game.getPlatform(), game.getGenre()));

        // Estado como "pastilla" de color
        GameStatus status = game.getStatusEnum();
        b.tvStatus.setText(status.getLabelRes());
        b.tvStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx, status.getColorRes())));

        // Año jugado y puntuación solo existen si el juego no está pendiente
        if (game.getPlayedYear() != null) {
            b.tvPlayedYear.setVisibility(View.VISIBLE);
            b.tvPlayedYear.setText(ctx.getString(R.string.played_year_format, game.getPlayedYear()));
        } else {
            b.tvPlayedYear.setVisibility(View.GONE);
        }
        if (game.getScore() != null) {
            b.tvScore.setVisibility(View.VISIBLE);
            b.tvScore.setText(game.getScoreText());
            b.tvScore.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx, game.getScoreColorRes())));
        } else {
            b.tvScore.setVisibility(View.INVISIBLE);
        }

        b.ivFavorite.setImageResource(game.isFavorite() ? R.drawable.ic_favorite_on : R.drawable.ic_favorite_off);

        Glide.with(b.ivCover)
                .load(game.getCoverUrl())
                .placeholder(R.drawable.bg_cover_placeholder)
                .error(R.drawable.bg_cover_placeholder)
                .fallback(R.drawable.bg_cover_placeholder)
                .centerCrop()
                .into(b.ivCover);

        // Lambdas para los clics
        b.getRoot().setOnClickListener(v -> onClick.accept(game));
        b.ivFavorite.setOnClickListener(v -> onFavorite.accept(game));
    }

    static class GameViewHolder extends RecyclerView.ViewHolder {
        final ItemGameBinding binding;
        GameViewHolder(ItemGameBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}