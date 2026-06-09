package com.example.nasax.ui.archive;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nasax.databinding.ItemArchiveBinding;
import com.example.nasax.domain.model.Apod;

import java.util.ArrayList;
import java.util.List;

import coil.Coil;
import coil.request.ImageRequest;

public class ArchiveAdapter extends RecyclerView.Adapter<ArchiveAdapter.ViewHolder> {

    private List<Apod> apods = new ArrayList<>();
    private OnApodClickListener listener;

    public interface OnApodClickListener {
        void onApodClick(Apod apod, ImageView sharedImage);
    }

    public void setOnApodClickListener(OnApodClickListener listener) {
        this.listener = listener;
    }

    public void updateApods(List<Apod> newApods) {
        this.apods.clear();
        if (newApods != null) this.apods.addAll(newApods);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemArchiveBinding binding = ItemArchiveBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Apod apod = apods.get(position);

        // Unique transition name per item
        String transitionName = "archive_image_" + apod.getDate();
        holder.binding.ivArchiveImage.setTransitionName(transitionName);

        holder.binding.tvArchiveDate.setText(apod.getDate());
        holder.binding.tvArchiveTitle.setText(apod.getTitle());

        if (apod.isVideo()) {
            holder.binding.ivArchiveImage.setImageResource(com.example.nasax.R.drawable.viedo_image);
            holder.binding.tvVideoLabel.setVisibility(android.view.View.VISIBLE);
        } else {
            holder.binding.tvVideoLabel.setVisibility(android.view.View.GONE);
            String imageUrl = apod.getUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                ImageRequest request = new ImageRequest.Builder(holder.itemView.getContext())
                        .data(imageUrl)
                        .crossfade(true)
                        .target(holder.binding.ivArchiveImage)
                        .build();
                Coil.imageLoader(holder.itemView.getContext()).enqueue(request);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onApodClick(apod, holder.binding.ivArchiveImage);
        });
    }

    @Override
    public int getItemCount() { return apods.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemArchiveBinding binding;

        ViewHolder(ItemArchiveBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
