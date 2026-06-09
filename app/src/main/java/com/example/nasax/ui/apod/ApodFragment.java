package com.example.nasax.ui.apod;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.nasax.R;
import com.example.nasax.databinding.FragmentApodBinding;
import com.example.nasax.domain.model.Apod;
import com.example.nasax.domain.model.FavoritePhoto;
import com.example.nasax.ui.detail.FullScreenImageActivity;
import com.example.nasax.ui.detail.VideoPlayerActivity;
import com.example.nasax.ui.favorite.FavoriteViewModel;
import com.example.nasax.util.ShareHelper;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import coil.Coil;
import coil.request.ImageRequest;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ApodFragment extends Fragment {

    private FragmentApodBinding binding;
    private ApodViewModel viewModel;
    private FavoriteViewModel viewModelFavorites;
    private Apod currentApod;
    private boolean isExpanded = false;
    private boolean isFavorite = false;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentApodBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ApodViewModel.class);
        viewModelFavorites = new ViewModelProvider(this).get(FavoriteViewModel.class);

        Typeface customFontNasa = ResourcesCompat.getFont(requireContext(), R.font.nasa_font);
        if (customFontNasa != null) {
            binding.tvTitle.setTypeface(customFontNasa);
            binding.tvAPOD.setTypeface(customFontNasa);
        }

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state.isLoading()) {
                binding.tvTitle.setText(getString(R.string.loading_apod));
                binding.tvExplanation.setText("");
            } else if (state.isSuccess()) {
                currentApod = state.getData();
                displayApod(currentApod);
                syncFavoriteState(currentApod.getDate());
            } else if (state.isError()) {
                handleError(state.getErrorMessage());
            }
        });
    }

    private void syncFavoriteState(String apodId) {
        viewModelFavorites.checkIsFavorite(apodId).observe(getViewLifecycleOwner(), favorite -> {
            isFavorite = favorite != null && favorite;
            updateHeartIcon();
        });
    }

    private void updateHeartIcon() {
        binding.btnFavorites.setImageResource(
                isFavorite ? R.drawable.ic_menu_heart : R.drawable.ic_heart_outline
        );
    }

    private void displayApod(Apod apod) {
        binding.tvTitle.setText(apod.getTitle());
        binding.tvDate.setText(apod.getDate());
        binding.tvExplanation.setText(apod.getExplanation());
        binding.tvExplanation.setMaxLines(12);

        binding.ivPlayButton.setVisibility(apod.isVideo() ? View.VISIBLE : View.GONE);

        String displayUrl = apod.getDisplayUrl();
        if (displayUrl != null) {
            ImageRequest request = new ImageRequest.Builder(requireContext())
                    .data(displayUrl)
                    .crossfade(true)
                    .target(binding.ivApod)
                    .build();
            Coil.imageLoader(requireContext()).enqueue(request);
        }
    }

    private void handleError(String errorMessage) {
        binding.tvTitle.setText(getString(R.string.apod_error_title));
        if (errorMessage != null && errorMessage.contains("429")) {
            binding.tvExplanation.setText(getString(R.string.apod_too_many_requests));
        } else {
            binding.tvExplanation.setText(getString(R.string.apod_error_prefix) + errorMessage);
        }
        binding.tvExplanation.setMaxLines(8);
    }

    private void setupListeners() {

        binding.btnShare.setOnClickListener(v -> {
            if (currentApod == null) return;
            String shareUrl = currentApod.isVideo() ? currentApod.getDisplayUrl() : currentApod.getUrl();
            ShareHelper.share(this, shareUrl, currentApod.getTitle(), executor);
        });

        binding.ivCalendar.setOnClickListener(v -> showDatePicker());

        binding.ivNasaLogo.setOnClickListener(v -> {
            Toast.makeText(requireContext(), getString(R.string.refreshing), Toast.LENGTH_SHORT).show();
            viewModel.refreshApod();
        });

        binding.ivApod.setOnClickListener(v -> {
            if (currentApod == null) return;
            if (currentApod.isVideo()) {
                Intent intent = new Intent(requireContext(), VideoPlayerActivity.class);
                intent.putExtra(VideoPlayerActivity.EXTRA_VIDEO_URL, currentApod.getUrl());
                startActivity(intent);
            } else if (currentApod.getDisplayUrl() != null) {
                Intent intent = new Intent(requireContext(), FullScreenImageActivity.class);
                intent.putExtra(FullScreenImageActivity.EXTRA_IMAGE_URL, currentApod.getDisplayUrl());
                intent.putExtra(FullScreenImageActivity.EXTRA_IMAGE_TITLE, currentApod.getTitle());
                startActivity(intent);
            }
        });

        binding.tvReadMore.setOnClickListener(v -> {
            isExpanded = !isExpanded;
            binding.tvExplanation.setMaxLines(isExpanded ? Integer.MAX_VALUE : 8);
            binding.tvReadMore.setText(isExpanded ? getString(R.string.read_less) : getString(R.string.read_more));
        });

        binding.btnFavorites.setOnClickListener(v -> {
            if (currentApod == null) return;

            FavoritePhoto favorite = new FavoritePhoto(
                    currentApod.getDate(),
                    currentApod.getTitle(),
                    currentApod.getDate(),
                    currentApod.getDisplayUrl(),
                    currentApod.getExplanation()
            );
            favorite.setMediaType(currentApod.getMediaType());
            favorite.setVideoUrl(currentApod.getUrl());

            if (!isFavorite) {
                viewModelFavorites.addToFavorites(favorite);
                isFavorite = true;
                Toast.makeText(requireContext(), getString(R.string.added_to_favorites), Toast.LENGTH_SHORT).show();
                binding.btnFavorites.animate()
                        .scaleX(1.35f).scaleY(1.35f).setDuration(120)
                        .withEndAction(() -> binding.btnFavorites.animate()
                                .scaleX(1f).scaleY(1f).setDuration(120).start())
                        .start();
            } else {
                viewModelFavorites.removeFromFavorites(favorite);
                isFavorite = false;
                Toast.makeText(requireContext(), getString(R.string.removed_from_favorites), Toast.LENGTH_SHORT).show();
            }

            updateHeartIcon();
        });
    }

    private void showDatePicker() {
        Calendar today = Calendar.getInstance();
        Calendar minDate = Calendar.getInstance();
        minDate.set(1995, Calendar.JUNE, 16);

        DatePickerDialog picker = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String date = String.format(Locale.US, "%04d-%02d-%02d",
                            year, month + 1, dayOfMonth);
                    viewModel.loadApodByDate(date);
                },
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH));

        picker.getDatePicker().setMinDate(minDate.getTimeInMillis());
        picker.getDatePicker().setMaxDate(today.getTimeInMillis());
        picker.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        executor.shutdownNow();
        binding = null;
    }
}
