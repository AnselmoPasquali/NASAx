package com.example.nasax.ui.archive;

import android.graphics.Typeface;
import android.os.Bundle;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.nasax.R;
import com.example.nasax.databinding.FragmentArchiveBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ArchiveFragment extends Fragment {

    private FragmentArchiveBinding binding;
    private ArchiveViewModel viewModel;
    private ArchiveAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentArchiveBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ArchiveViewModel.class);

        Typeface nasa = ResourcesCompat.getFont(requireContext(), R.font.nasa_font);
        if (nasa != null) binding.tvArchiveTitle.setTypeface(nasa);

        setupRecyclerView();
        setupNavigation();
        setupObservers();
    }

    private void setupRecyclerView() {
        adapter = new ArchiveAdapter();
        binding.recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.recyclerView.setAdapter(adapter);

        adapter.setOnApodClickListener((apod, imageView) -> {
            ApodDetailFragment detail = ApodDetailFragment.newInstance(apod);

            // Shared element transition
            detail.setSharedElementEnterTransition(
                    TransitionInflater.from(requireContext())
                            .inflateTransition(android.R.transition.move));

            String transitionName = "archive_image_" + apod.getDate();
            imageView.setTransitionName(transitionName);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .addSharedElement(imageView, transitionName)
                    .replace(R.id.fragment_container, detail)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void setupNavigation() {
        binding.btnPrevMonth.setOnClickListener(v -> viewModel.previousMonth());
        binding.btnNextMonth.setOnClickListener(v -> viewModel.nextMonth());
    }

    private void setupObservers() {
        viewModel.getMonthLabel().observe(getViewLifecycleOwner(),
                label -> binding.tvArchiveMonth.setText(label));

        viewModel.getCanGoPrev().observe(getViewLifecycleOwner(), can -> {
            binding.btnPrevMonth.setAlpha(Boolean.TRUE.equals(can) ? 1f : 0.3f);
            binding.btnPrevMonth.setClickable(Boolean.TRUE.equals(can));
        });

        viewModel.getCanGoNext().observe(getViewLifecycleOwner(), can -> {
            binding.btnNextMonth.setAlpha(Boolean.TRUE.equals(can) ? 1f : 0.3f);
            binding.btnNextMonth.setClickable(Boolean.TRUE.equals(can));
        });

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
                } else if (msg != null && (msg.contains("Unable to resolve") || msg.contains("timeout"))) {
                    binding.tvError.setText(getString(R.string.error_offline_no_cache));
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
