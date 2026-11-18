package com.chepelayo.thelastone.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.chepelayo.thelastone.R;
import com.chepelayo.thelastone.data.rawg.RawgModels;
import com.chepelayo.thelastone.databinding.ItemRawgResultBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/** Lista de resultados de búsqueda de RAWG. */
public class RawgResultAdapter extends RecyclerView.Adapter<RawgResultAdapter.ResultViewHolder> {

    private final List<RawgModels.RawgGame> items = new ArrayList<>();
    private final Consumer<RawgModels.RawgGame> onClick;

    public RawgResultAdapter(Consumer<RawgModels.RawgGame> onClick) {
        this.onClick = onClick;
    }

    public void setItems(List<RawgModels.RawgGame> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ResultViewHolder(ItemRawgResultBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        RawgModels.RawgGame game = items.get(position);
        ItemRawgResultBinding b = holder.binding;

        b.tvTitle.setText(game.name);
        String released = game.released == null ? b.getRoot().getContext().getString(R.string.no_data_dash) : game.released;
        String genres = game.genres == null ? "" : game.genres.stream()
                .map(g -> g.name)
                .collect(Collectors.joining(", "));
        b.tvSubtitle.setText(genres.isEmpty() ? released : released + "\n" + genres);

        Glide.with(b.ivCover).load(game.backgroundImage)
                .placeholder(R.drawable.bg_cover_placeholder)
                .error(R.drawable.bg_cover_placeholder)
                .fallback(R.drawable.bg_cover_placeholder)
                .centerCrop().into(b.ivCover);

        b.getRoot().setOnClickListener(v -> onClick.accept(game));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ResultViewHolder extends RecyclerView.ViewHolder {
        final ItemRawgResultBinding binding;
        ResultViewHolder(ItemRawgResultBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}