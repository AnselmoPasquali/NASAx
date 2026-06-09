package com.example.nasax.ui.news;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nasax.R;
import android.widget.ProgressBar;
import android.widget.TextView;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NewsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar  progressBar;
    private TextView     tvError;
    private NewsViewModel viewModel;
    private NewsAdapter   adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_news, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar  = view.findViewById(R.id.progressBar);
        tvError      = view.findViewById(R.id.tvError);

        adapter = new NewsAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(NewsViewModel.class);
        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            tvError.setVisibility(View.GONE);

            if (state.isLoading()) {
                progressBar.setVisibility(View.VISIBLE);
            } else if (state.isSuccess()) {
                recyclerView.setVisibility(View.VISIBLE);
                adapter.updateItems(state.getItems());
            } else {
                tvError.setVisibility(View.VISIBLE);
                tvError.setText(getString(R.string.error_generic_prefix) + state.getErrorMessage());
            }
        });

        // Pull-to-refresh via tap sul titolo (semplice)
        view.findViewById(R.id.tvNewsTitle).setOnClickListener(v -> viewModel.refresh());
    }
}
