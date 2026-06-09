package com.example.nasax.ui.archive;

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
import com.example.nasax.databinding.FragmentApodDetailBinding;
import com.example.nasax.domain.model.Apod;
import com.example.nasax.domain.model.FavoritePhoto;
import com.example.nasax.ui.detail.FullScreenImageActivity;
import com.example.nasax.ui.detail.VideoPlayerActivity;
import com.example.nasax.ui.favorite.FavoriteViewModel;
import com.example.nasax.util.ShareHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import coil.Coil;
import coil.request.ImageRequest;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ApodDetailFragment extends Fragment {

    private static final String ARG_DATE        = "date";
    private static final String ARG_TITLE       = "title";
    private static final String ARG_EXPLANATION = "explanation";
    private static final String ARG_URL         = "url";
    private static final String ARG_HD_URL      = "hdUrl";
    private static final String ARG_MEDIA_TYPE  = "mediaType";
    private static final String ARG_THUMB_URL   = "thumbnailUrl";

    private FragmentApodDetailBinding binding;
    private FavoriteViewModel favoriteViewModel;
    private Apod apod;
    private boolean isFavorite = false;
    private boolean isExpanded = false;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static ApodDetailFragment newInstance(Apod apod) {
        ApodDetailFragment fragment = new ApodDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DATE,        apod.getDate());
        args.putString(ARG_TITLE,       apod.getTitle());
        args.putString(ARG_EXPLANATION, apod.getExplanation());
        args.putString(ARG_URL,         apod.getUrl());
        args.putString(ARG_HD_URL,      apod.getHdUrl());
        args.putString(ARG_MEDIA_TYPE,  apod.getMediaType());
        args.putString(ARG_THUMB_URL,   apod.getThumbnailUrl());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentApodDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        favoriteViewModel = new ViewModelProvider(this).get(FavoriteViewModel.class);

        Bundle args = requireArguments();
        apod = new Apod();
        apod.setDate(args.getString(ARG_DATE));
        apod.setTitle(args.getString(ARG_TITLE));
        apod.setExplanation(args.getString(ARG_EXPLANATION));
        apod.setUrl(args.getString(ARG_URL));
        apod.setHdUrl(args.getString(ARG_HD_URL));
        apod.setMediaType(args.getString(ARG_MEDIA_TYPE));
        apod.setThumbnailUrl(args.getString(ARG_THUMB_URL));

        binding.ivImage.setTransitionName("archive_image_" + apod.getDate());

        populateViews();
        syncFavoriteState();
        setupListeners();
    }

    private void populateViews() {
        binding.tvTitle.setText(apod.getTitle());
        binding.tvDate.setText(apod.getDate());
        binding.tvExplanation.setText(apod.getExplanation());
        binding.tvExplanation.setMaxLines(8);
        binding.ivPlayButton.setVisibility(apod.isVideo() ? View.VISIBLE : View.GONE);

        if (apod.isVideo()) {
            binding.ivImage.setImageResource(com.example.nasax.R.drawable.viedo_image);
            startPostponedEnterTransition();
        } else {
            String imageUrl = apod.getUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                postponeEnterTransition();
                ImageRequest request = new ImageRequest.Builder(requireContext())
                        .data(imageUrl)
                        .crossfade(true)
                        .target(binding.ivImage)
                        .build();
                Coil.imageLoader(requireContext()).enqueue(request);
                binding.ivImage.postDelayed(() -> startPostponedEnterTransition(), 250);
            }
        }
    }

    private void syncFavoriteState() {
        favoriteViewModel.checkIsFavorite(apod.getDate()).observe(getViewLifecycleOwner(), favorite -> {
            isFavorite = favorite != null && favorite;
            updateHeartIcon();
        });
    }

    private void updateHeartIcon() {
        binding.btnFavorite.setImageResource(
                isFavorite ? R.drawable.ic_menu_heart : R.drawable.ic_heart_outline);
    }

    private void setupListeners() {

        binding.ivImage.setOnClickListener(v -> {
            if (apod.isVideo()) {
                startActivity(new Intent(requireContext(), VideoPlayerActivity.class)
                        .putExtra(VideoPlayerActivity.EXTRA_VIDEO_URL, apod.getUrl()));
            } else {
                String url = apod.getDisplayUrl();
                if (url != null) {
                    startActivity(new Intent(requireContext(), FullScreenImageActivity.class)
                            .putExtra(FullScreenImageActivity.EXTRA_IMAGE_URL, url)
                            .putExtra(FullScreenImageActivity.EXTRA_IMAGE_TITLE, apod.getTitle()));
                }
            }
        });

        binding.btnFavorite.setOnClickListener(v -> {
            String favImageUrl = apod.isVideo() ? apod.getDisplayUrl() : apod.getUrl();
            FavoritePhoto favorite = new FavoritePhoto(
                    apod.getDate(), apod.getTitle(), apod.getDate(),
                    favImageUrl, apod.getExplanation());
            favorite.setMediaType(apod.getMediaType());
            favorite.setVideoUrl(apod.getUrl());

            if (!isFavorite) {
                favoriteViewModel.addToFavorites(favorite);
                isFavorite = true;
                Toast.makeText(requireContext(), getString(R.string.added_to_favorites), Toast.LENGTH_SHORT).show();
                binding.btnFavorite.animate()
                        .scaleX(1.35f).scaleY(1.35f).setDuration(120)
                        .withEndAction(() -> binding.btnFavorite.animate()
                                .scaleX(1f).scaleY(1f).setDuration(120).start())
                        .start();
            } else {
                favoriteViewModel.removeFromFavorites(favorite);
                isFavorite = false;
                Toast.makeText(requireContext(), getString(R.string.removed_from_favorites), Toast.LENGTH_SHORT).show();
            }
            updateHeartIcon();
        });

        binding.btnShare.setOnClickListener(v -> {
            String shareUrl = apod.isVideo() ? apod.getDisplayUrl() : apod.getUrl();
            ShareHelper.share(this, shareUrl, apod.getTitle(), executor);
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
