package com.example.nasax.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.nasax.R;
import com.example.nasax.databinding.FragmentLoginBinding;
import com.example.nasax.ui.home.HomeFragment;
import com.example.nasax.ui.favorite.FavoriteViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private LoginViewModel viewModel;
    private FavoriteViewModel favoriteViewModel;

    private final ActivityResultLauncher<Intent> signInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    viewModel.handleSignInResult(result.getData());
                } else {
                    binding.btnGoogleSignIn.setEnabled(true);
                    binding.progressBar.setVisibility(View.GONE);
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel         = new ViewModelProvider(this).get(LoginViewModel.class);
        favoriteViewModel = new ViewModelProvider(requireActivity()).get(FavoriteViewModel.class);

        binding.btnGoogleSignIn.setOnClickListener(v -> {
            binding.btnGoogleSignIn.setEnabled(false);
            binding.progressBar.setVisibility(View.VISIBLE);
            signInLauncher.launch(viewModel.getSignInIntent(requireContext()));
        });

        viewModel.getLoginState().observe(getViewLifecycleOwner(), state -> {
            if (state.isLoading()) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.btnGoogleSignIn.setEnabled(false);
            } else if (state.isSuccess()) {
                // Pull Firestore favorites into local DB, then go to home
                favoriteViewModel.syncFromFirestore();
                navigateToHome();
            } else if (state.isError()) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnGoogleSignIn.setEnabled(true);
                Toast.makeText(requireContext(), state.getError(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void navigateToHome() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
