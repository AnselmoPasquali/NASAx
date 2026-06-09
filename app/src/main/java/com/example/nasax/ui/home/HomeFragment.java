package com.example.nasax.ui.home;

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

import com.example.nasax.databinding.FragmentHomeBinding;
import com.example.nasax.ui.apod.ApodFragment;
import com.example.nasax.ui.archive.ArchiveFragment;
import com.example.nasax.ui.explore.ExploreFragment;
import com.example.nasax.ui.favorite.FavoriteFragment;
import com.example.nasax.ui.quiz.QuizFragment;
import com.example.nasax.ui.news.NewsFragment;
import com.example.nasax.R;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.cardApod.setOnClickListener(v -> openApodFragment());
        binding.cardFavorites.setOnClickListener(v -> openFavoriteFragment());
        binding.cardArchive.setOnClickListener(v -> openArchiveFragment());
        binding.cardQuiz.setOnClickListener(v -> openQuizFragment());
        binding.cardExplore.setOnClickListener(v -> openExploreFragment());
        binding.cardNews.setOnClickListener(v -> openNewsFragment());

        Typeface customFontNasa = ResourcesCompat.getFont(requireContext(), R.font.nasa_font);
        if (customFontNasa != null) {
            binding.txTitle.setTypeface(customFontNasa);
            binding.txAPOD.setTypeface(customFontNasa);
            binding.txFavorites.setTypeface(customFontNasa);
            binding.txArchive.setTypeface(customFontNasa);
            binding.txQuiz.setTypeface(customFontNasa);
            binding.txExplore.setTypeface(customFontNasa);
            binding.txNews.setTypeface(customFontNasa);
        }
    }

    private void openApodFragment() {
        try {
            FragmentTransaction transaction = requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.fragment_container, new ApodFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openFavoriteFragment() {
        try {
            FragmentTransaction transaction = requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.fragment_container, new FavoriteFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openArchiveFragment() {
        try {
            FragmentTransaction transaction = requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.fragment_container, new ArchiveFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openQuizFragment() {
        try {
            FragmentTransaction transaction = requireActivity()
                    .getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new QuizFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void openExploreFragment() {
        try {
            FragmentTransaction transaction = requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.fragment_container, new ExploreFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openNewsFragment() {
        try {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new NewsFragment())
                    .addToBackStack(null)
                    .commit();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}