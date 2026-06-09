package com.example.nasax.ui.favorite;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.nasax.R;
import com.example.nasax.databinding.FragmentFavoriteDetailBinding;
import com.example.nasax.domain.model.FavoritePhoto;
import com.example.nasax.ui.detail.FullScreenImageActivity;
import com.example.nasax.ui.detail.VideoPlayerActivity;
import com.example.nasax.util.ShareHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import coil.Coil;
import coil.request.ImageRequest;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FavoriteDetailFragment extends Fragment {

    private static final String ARG_ID          = "id";
    private static final String ARG_TITLE       = "title";
    private static final String ARG_DATE        = "date";
    private static final String ARG_IMAGE_URL   = "imageUrl";
    private static final String ARG_EXPLANATION = "explanation";
    private static final String ARG_MEDIA_TYPE  = "mediaType";
    private static final String ARG_VIDEO_URL   = "videoUrl";

    private FragmentFavoriteDetailBinding binding;
    private FavoriteViewModel viewModel;
    private FavoritePhoto photo;
    private boolean isExpanded = false;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static FavoriteDetailFragment newInstance(FavoritePhoto photo) {
        FavoriteDetailFragment fragment = new FavoriteDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ID,          photo.getId());
        args.putString(ARG_TITLE,       photo.getTitle());
        args.putString(ARG_DATE,        photo.getDate());
        args.putString(ARG_IMAGE_URL,   photo.getImageUrl());
        args.putString(ARG_EXPLANATION, photo.getExplanation());
        args.putString(ARG_MEDIA_TYPE,  photo.getMediaType());
        args.putString(ARG_VIDEO_URL,   photo.getVideoUrl());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFavoriteDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(FavoriteViewModel.class);

        Bundle args = requireArguments();
        photo = new FavoritePhoto(
                args.getString(ARG_ID), args.getString(ARG_TITLE),
                args.getString(ARG_DATE), args.getString(ARG_IMAGE_URL),
                args.getString(ARG_EXPLANATION));
        photo.setMediaType(args.getString(ARG_MEDIA_TYPE, "image"));
        photo.setVideoUrl(args.getString(ARG_VIDEO_URL));

        binding.ivImage.setTransitionName("favorite_image_" + photo.getId());

        populateViews();
        setupListeners();
    }

    private void populateViews() {
        binding.tvTitle.setText(photo.getTitle());
        binding.tvDate.setText(photo.getDate());
        binding.tvExplanation.setText(photo.getExplanation());
        binding.tvExplanation.setMaxLines(8);
        binding.ivPlayButton.setVisibility(photo.isVideo() ? View.VISIBLE : View.GONE);

        String displayUrl = photo.getDisplayImageUrl();
        if (photo.isVideo()) {
            binding.ivImage.setImageResource(com.example.nasax.R.drawable.viedo_image);
            startPostponedEnterTransition();
        } else if (displayUrl != null && !displayUrl.isEmpty()) {
            postponeEnterTransition();
            ImageRequest request = new ImageRequest.Builder(requireContext())
                    .data(displayUrl)
                    .crossfade(true)
                    .target(binding.ivImage)
                    .build();
            Coil.imageLoader(requireContext()).enqueue(request);
            binding.ivImage.postDelayed(() -> startPostponedEnterTransition(), 250);
        }
    }

    private void setupListeners() {

        binding.ivImage.setOnClickListener(v -> {
            if (photo.isVideo() && photo.getVideoUrl() != null) {
                startActivity(new Intent(requireContext(), VideoPlayerActivity.class)
                        .putExtra(VideoPlayerActivity.EXTRA_VIDEO_URL, photo.getVideoUrl()));
            } else if (photo.getImageUrl() != null) {
                startActivity(new Intent(requireContext(), FullScreenImageActivity.class)
                        .putExtra(FullScreenImageActivity.EXTRA_IMAGE_URL, photo.getImageUrl())
                        .putExtra(FullScreenImageActivity.EXTRA_IMAGE_TITLE, photo.getTitle()));
            }
        });

        binding.btnShare.setOnClickListener(v -> {
            String shareUrl = photo.isVideo() ? photo.getDisplayImageUrl() : photo.getImageUrl();
            ShareHelper.share(this, shareUrl, photo.getTitle(), executor);
        });

        binding.btnDelete.setOnClickListener(v -> {
            viewModel.removeFromFavorites(photo);
            Toast.makeText(requireContext(), getString(R.string.removed_from_favorites), Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        binding.tvReadMore.setOnClickListener(v -> {
            isExpanded = !isExpanded;
            binding.tvExplanation.setMaxLines(isExpanded ? Integer.MAX_VALUE : 8);
            binding.tvReadMore.setText(isExpanded ? getString(R.string.read_less) : getString(R.string.read_more));
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        executor.shutdownNow();
        binding = null;
    }
}
