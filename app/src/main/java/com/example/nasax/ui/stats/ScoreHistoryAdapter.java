package com.example.nasax.ui.stats;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nasax.R;
import com.example.nasax.data.local.entity.QuizScoreEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScoreHistoryAdapter extends RecyclerView.Adapter<ScoreHistoryAdapter.VH> {

    private List<QuizScoreEntity> items = new ArrayList<>();
    private final SimpleDateFormat sdf =
            new SimpleDateFormat("dd MMM yyyy  HH:mm", Locale.ITALIAN);

    public void updateScores(List<QuizScoreEntity> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quiz_score, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        QuizScoreEntity s = items.get(position);
        int pct = s.total > 0 ? (int) Math.round((double) s.score / s.total * 100) : 0;
        holder.tvScore.setText(s.score + " / " + s.total + "  (" + pct + "%)");
        holder.tvDate.setText(sdf.format(new Date(s.timestamp)));

        int color;
        if      (pct >= 90) color = 0xFF388E3C; // verde
        else if (pct >= 60) color = 0xFFFF9100; // arancione
        else                color = 0xFFD32F2F; // rosso
        holder.tvScore.setTextColor(color);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvScore, tvDate;
        VH(View v) {
            super(v);
            tvScore = v.findViewById(R.id.tvScoreValue);
            tvDate  = v.findViewById(R.id.tvScoreDate);
        }
    }
}
