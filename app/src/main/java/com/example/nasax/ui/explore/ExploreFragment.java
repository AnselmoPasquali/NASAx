package com.example.nasax.ui.explore;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.nasax.R;
import com.example.nasax.databinding.FragmentExploreBinding;
import com.example.nasax.ui.archive.ApodDetailFragment;
import com.example.nasax.ui.archive.ArchiveAdapter;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ExploreFragment extends Fragment {

    private FragmentExploreBinding binding;
    private ExploreViewModel viewModel;
    private ArchiveAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentExploreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ExploreViewModel.class);

        Typeface nasa = ResourcesCompat.getFont(requireContext(), R.font.nasa_font);
        if (nasa != null) binding.tvExploreTitle.setTypeface(nasa);

        setupRecyclerView();
        setupObservers();

        binding.btnShuffle.setOnClickListener(v -> {
            binding.btnShuffle.animate()
                    .rotationBy(360f).setDuration(400).start();
            viewModel.shuffle();
        });
    }

    private void setupRecyclerView() {
        adapter = new ArchiveAdapter();
        binding.recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.recyclerView.setAdapter(adapter);

        adapter.setOnApodClickListener((apod, sharedImage) -> {
            FragmentTransaction tx = requireActivity()
                    .getSupportFragmentManager().beginTransaction();
            tx.replace(R.id.fragment_container, ApodDetailFragment.newInstance(apod));
            tx.addToBackStack(null);
            tx.commit();
        });
    }

    private void setupObservers() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.recyclerView.setVisibility(View.GONE);
            binding.tvError.setVisibility(View.GONE);

            if (state.isLoading()) {
                binding.progressBar.setVisibility(View.VISIBLE);
            } else if (state.isSuccess()) {
                binding.recyclerView.setVisibility(View.VISIBLE);
                adapter.updateApods(state.getData());
            } else if (state.isError()) {
                binding.tvError.setVisibility(View.VISIBLE);
                String msg = state.getErrorMessage();
                if (msg != null && msg.contains("429")) {
                    binding.tvError.setText(getString(R.string.error_too_many_requests));
                } else {
                    binding.tvError.setText(getString(R.string.error_generic_prefix) + msg);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
