package com.example.nasax.ui.favorite;

import android.graphics.Typeface;
import android.os.Bundle;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.nasax.R;
import com.example.nasax.databinding.FragmentFavoriteBinding;
import com.example.nasax.domain.model.FavoritePhoto;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FavoriteFragment extends Fragment {

    private FragmentFavoriteBinding binding;
    private FavoriteViewModel viewModel;
    private FavoriteAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFavoriteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(FavoriteViewModel.class);

        Typeface nasa = ResourcesCompat.getFont(requireContext(), R.font.nasa_font);
        if (nasa != null) binding.tvFavoritesTitle.setTypeface(nasa);

        setupRecyclerView();
        setupObservers();
    }

    private void setupRecyclerView() {
        adapter = new FavoriteAdapter();
        binding.recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.recyclerView.setAdapter(adapter);

        adapter.setOnFavoriteClickListener(new FavoriteAdapter.OnFavoriteClickListener() {
            @Override
            public void onPhotoClick(FavoritePhoto photo, android.widget.ImageView sharedImage) {
                FavoriteDetailFragment detail = FavoriteDetailFragment.newInstance(photo);

                detail.setSharedElementEnterTransition(
                        TransitionInflater.from(requireContext())
                                .inflateTransition(android.R.transition.move));

                String transitionName = "favorite_image_" + photo.getId();
                sharedImage.setTransitionName(transitionName);

                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .addSharedElement(sharedImage, transitionName)
                        .replace(R.id.fragment_container, detail)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onDeleteClick(FavoritePhoto photo) {
                viewModel.removeFromFavorites(photo);
            }
        });
    }

    private void setupObservers() {
        viewModel.getFavoritesLiveData().observe(getViewLifecycleOwner(), favorites -> {
            if (favorites != null && !favorites.isEmpty()) {
                binding.tvEmpty.setVisibility(View.GONE);
                adapter.updateFavorites(favorites);
            } else {
                binding.tvEmpty.setVisibility(View.VISIBLE);
                adapter.updateFavorites(favorites);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
