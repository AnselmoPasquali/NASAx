package com.example.nasax.data.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuthRepository {

    private final FirebaseAuth auth;

    @Inject
    public AuthRepository(FirebaseAuth auth) {
        this.auth = auth;
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    /** Returns the UID of the current user, or null if not logged in. */
    public String getCurrentUid() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public String getDisplayName() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getDisplayName() : null;
    }

    public Task<AuthResult> signInWithCredential(AuthCredential credential) {
        return auth.signInWithCredential(credential);
    }

    public void signOut() {
        auth.signOut();
    }
}
