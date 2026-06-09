package com.example.nasax.ui.login;

import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.nasax.R;
import com.example.nasax.data.repository.AuthRepository;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.GoogleAuthProvider;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoginViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final MutableLiveData<LoginState> loginState = new MutableLiveData<>(LoginState.idle());

    @Inject
    public LoginViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public LiveData<LoginState> getLoginState() { return loginState; }

    /** Builds the Google Sign-In intent to be launched by the Fragment. */
    public Intent getSignInIntent(Context context) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient client = GoogleSignIn.getClient(context, gso);
        client.signOut(); // ensure a fresh account picker shows each time
        return client.getSignInIntent();
    }

    /** Called from the Fragment after the sign-in Activity returns a result. */
    public void handleSignInResult(Intent data) {
        loginState.setValue(LoginState.loading());
        try {
            GoogleSignInAccount account = GoogleSignIn
                    .getSignedInAccountFromIntent(data)
                    .getResult(ApiException.class);

            authRepository
                    .signInWithCredential(GoogleAuthProvider.getCredential(account.getIdToken(), null))
                    .addOnSuccessListener(result -> loginState.setValue(LoginState.success()))
                    .addOnFailureListener(e  -> loginState.setValue(LoginState.error(e.getMessage())));

        } catch (ApiException e) {
            loginState.setValue(LoginState.error("Google sign-in failed: " + e.getStatusCode()));
        }
    }
}
