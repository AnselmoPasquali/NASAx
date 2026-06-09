package com.example.nasax.ui.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nasax.R;
import com.example.nasax.data.local.entity.QuizScoreEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class QuizStatsFragment extends Fragment {

    private QuizStatsViewModel viewModel;
    private ScoreBarChartView  chartView;
    private RecyclerView       recyclerView;
    private TextView           tvEmpty, tvAvg, tvTotal;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chartView    = view.findViewById(R.id.chartView);
        recyclerView = view.findViewById(R.id.recyclerView);
        tvEmpty      = view.findViewById(R.id.tvEmpty);
        tvAvg        = view.findViewById(R.id.tvAvg);
        tvTotal      = view.findViewById(R.id.tvTotal);

        ScoreHistoryAdapter adapter = new ScoreHistoryAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(QuizStatsViewModel.class);
        viewModel.getScores().observe(getViewLifecycleOwner(), scores -> {
            if (scores == null || scores.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                chartView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                tvAvg.setVisibility(View.GONE);
                tvTotal.setVisibility(View.GONE);
                return;
            }

            tvEmpty.setVisibility(View.GONE);
            chartView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            tvAvg.setVisibility(View.VISIBLE);
            tvTotal.setVisibility(View.VISIBLE);

            // Chart data: last 10, oldest→newest (left→right)
            int chartCount = Math.min(scores.size(), 10);
            List<QuizScoreEntity> chartSlice = scores.subList(0, chartCount);
            List<float[]> chartData = new ArrayList<>();
            for (int i = chartSlice.size() - 1; i >= 0; i--) {
                QuizScoreEntity s = chartSlice.get(i);
                chartData.add(new float[]{s.score, s.total});
            }
            chartView.setData(chartData);

            // Stats
            int total = scores.size();
            double sum = 0;
            for (QuizScoreEntity s : scores) {
                if (s.total > 0) sum += (double) s.score / s.total;
            }
            int avgPct = total > 0 ? (int) Math.round(sum / total * 100) : 0;

            tvTotal.setText(getString(R.string.stats_games_played, total));
            tvAvg.setText(getString(R.string.stats_avg_score, avgPct));

            adapter.updateScores(scores);
        });
    }
}
