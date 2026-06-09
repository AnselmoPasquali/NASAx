package com.example.nasax.ui.favorite;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nasax.databinding.ItemFavoriteBinding;
import com.example.nasax.domain.model.FavoritePhoto;

import java.util.ArrayList;
import java.util.List;

import coil.Coil;
import coil.request.ImageRequest;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {

    private List<FavoritePhoto> favorites = new ArrayList<>();
    private OnFavoriteClickListener listener;

    public interface OnFavoriteClickListener {
        void onPhotoClick(FavoritePhoto photo, ImageView sharedImage);
        void onDeleteClick(FavoritePhoto photo);
    }

    public void setOnFavoriteClickListener(OnFavoriteClickListener listener) {
        this.listener = listener;
    }

    public void updateFavorites(List<FavoritePhoto> newFavorites) {
        this.favorites.clear();
        if (newFavorites != null) this.favorites.addAll(newFavorites);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFavoriteBinding binding = ItemFavoriteBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FavoritePhoto photo = favorites.get(position);

        // Unique transition name per item
        String transitionName = "favorite_image_" + photo.getId();
        holder.binding.ivFavoriteImage.setTransitionName(transitionName);

        if (photo.isVideo()) {
            holder.binding.ivFavoriteImage.setImageResource(com.example.nasax.R.drawable.viedo_image);
            holder.binding.tvVideoLabel.setVisibility(android.view.View.VISIBLE);
        } else {
            holder.binding.tvVideoLabel.setVisibility(android.view.View.GONE);
            String imgUrl = photo.getDisplayImageUrl();
            if (imgUrl != null && !imgUrl.isEmpty()) {
                ImageRequest request = new ImageRequest.Builder(holder.itemView.getContext())
                        .data(imgUrl)
                        .crossfade(true)
                        .target(holder.binding.ivFavoriteImage)
                        .build();
                Coil.imageLoader(holder.itemView.getContext()).enqueue(request);
            }
        }

        holder.binding.tvFavoriteTitle.setText(photo.getTitle());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPhotoClick(photo, holder.binding.ivFavoriteImage);
        });

        holder.binding.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(photo);
        });
    }

    @Override
    public int getItemCount() { return favorites.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemFavoriteBinding binding;

        ViewHolder(ItemFavoriteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
