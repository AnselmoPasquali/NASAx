package com.example.nasax.data.repository;

import android.util.Log;

import com.example.nasax.domain.model.FavoritePhoto;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Handles all Firestore read/write operations.
 *
 * Firestore structure:
 *   users/{uid}/favorites/{apodDate}   → FavoritePhoto fields
 *   users/{uid}/quizScores/{autoId}    → { score, total, timestamp }
 */
@Singleton
public class FirestoreRepository {

    private static final String TAG = "FirestoreRepository";

    private static final String COL_USERS       = "users";
    private static final String COL_FAVORITES   = "favorites";
    private static final String COL_QUIZ_SCORES = "quizScores";

    private final FirebaseFirestore db;
    private final AuthRepository authRepository;

    @Inject
    public FirestoreRepository(FirebaseFirestore db, AuthRepository authRepository) {
        this.db = db;
        this.authRepository = authRepository;
    }

    // ── Favorites ─────────────────────────────────────────────────────────────

    /** Fire-and-forget: saves a favorite for the current user. No-op if not logged in. */
    public void saveFavorite(FavoritePhoto photo) {
        String uid = authRepository.getCurrentUid();
        if (uid == null) return;
        db.collection(COL_USERS).document(uid)
          .collection(COL_FAVORITES).document(photo.getId())
          .set(photoToMap(photo))
          .addOnFailureListener(e -> Log.e(TAG, "saveFavorite failed", e));
    }

    /** Fire-and-forget: deletes a favorite for the current user. No-op if not logged in. */
    public void deleteFavorite(String id) {
        String uid = authRepository.getCurrentUid();
        if (uid == null) return;
        db.collection(COL_USERS).document(uid)
          .collection(COL_FAVORITES).document(id)
          .delete()
          .addOnFailureListener(e -> Log.e(TAG, "deleteFavorite failed", e));
    }

    /**
     * Synchronously fetches all favorites for the current user from Firestore.
     * Must be called on a background thread (uses Tasks.await).
     */
    public List<FavoritePhoto> fetchFavoritesSync() throws Exception {
        String uid = authRepository.getCurrentUid();
        if (uid == null) return Collections.emptyList();

        QuerySnapshot snapshot = Tasks.await(
            db.collection(COL_USERS).document(uid).collection(COL_FAVORITES).get()
        );

        List<FavoritePhoto> result = new ArrayList<>();
        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            FavoritePhoto photo = mapToPhoto(doc);
            if (photo != null) result.add(photo);
        }
        return result;
    }

    // ── Quiz Scores ───────────────────────────────────────────────────────────

    /** Fire-and-forget: saves a quiz score for the current user. No-op if not logged in. */
    public void saveQuizScore(int score, int total, long timestamp) {
        String uid = authRepository.getCurrentUid();
        if (uid == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("score", score);
        data.put("total", total);
        data.put("timestamp", timestamp);
        db.collection(COL_USERS).document(uid)
          .collection(COL_QUIZ_SCORES).add(data)
          .addOnFailureListener(e -> Log.e(TAG, "saveQuizScore failed", e));
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private Map<String, Object> photoToMap(FavoritePhoto p) {
        Map<String, Object> m = new HashMap<>();
        m.put("id",          p.getId());
        m.put("title",       p.getTitle());
        m.put("date",        p.getDate());
        m.put("imageUrl",    p.getImageUrl());
        m.put("explanation", p.getExplanation());
        m.put("mediaType",   p.getMediaType());
        m.put("videoUrl",    p.getVideoUrl());
        m.put("type",        p.getType());
        return m;
    }

    private FavoritePhoto mapToPhoto(DocumentSnapshot doc) {
        try {
            String id    = doc.getString("id");
            String title = doc.getString("title");
            String date  = doc.getString("date");
            String url   = doc.getString("imageUrl");
            String expl  = doc.getString("explanation");
            if (id == null) return null;

            FavoritePhoto photo = new FavoritePhoto(id, title, date, url, expl);
            String mt = doc.getString("mediaType");
            photo.setMediaType(mt != null ? mt : "image");
            photo.setVideoUrl(doc.getString("videoUrl"));
            photo.setType(doc.getString("type"));
            return photo;
        } catch (Exception e) {
            Log.e(TAG, "mapToPhoto failed for doc " + doc.getId(), e);
            return null;
        }
    }
}
