package com.example.nasax.ui.news;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nasax.R;

import java.util.ArrayList;
import java.util.List;

import coil.Coil;
import coil.request.ImageRequest;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private List<NewsItem> items = new ArrayList<>();

    public void updateItems(List<NewsItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NewsItem item = items.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvDate.setText(item.getPubDate());

        if (item.getDescription() != null && !item.getDescription().isEmpty()) {
            holder.tvDescription.setText(item.getDescription());
            holder.tvDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            holder.ivThumb.setVisibility(View.VISIBLE);
            ImageRequest request = new ImageRequest.Builder(holder.itemView.getContext())
                    .data(item.getImageUrl())
                    .crossfade(true)
                    .target(holder.ivThumb)
                    .build();
            Coil.imageLoader(holder.itemView.getContext()).enqueue(request);
        } else {
            holder.ivThumb.setVisibility(View.GONE);
        }

        // Tap → apre l'articolo nel browser
        if (item.getLink() != null && !item.getLink().isEmpty()) {
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getLink()));
                holder.itemView.getContext().startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivThumb;
        final TextView  tvTitle, tvDate, tvDescription;

        ViewHolder(@NonNull View v) {
            super(v);
            ivThumb       = v.findViewById(R.id.ivNewsThumb);
            tvTitle       = v.findViewById(R.id.tvNewsTitle);
            tvDate        = v.findViewById(R.id.tvNewsDate);
            tvDescription = v.findViewById(R.id.tvNewsDescription);
        }
    }
}
