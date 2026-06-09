package com.example.nasax.data.repository;

import android.text.Html;

import com.example.nasax.ui.quiz.QuizQuestion;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Fetches and filters astronomy quiz questions from the Open Trivia Database.
 *
 * Science &amp; Nature category (17): fetches 50 questions per request and keeps
 * only those that contain at least one astronomy-related keyword.
 */
@Singleton
public class QuizRepository {

    private static final String TRIVIA_URL =
            "https://opentdb.com/api.php?amount=50&category=17&type=multiple";

    private static final int TARGET_QUESTIONS = 10;
    private static final int MAX_ATTEMPTS = 4;

    private static final String[] ASTRO_KEYWORDS = {
        "space", "star", "stars", "planet", "planets", "moon", "sun", "solar",
        "galaxy", "galaxies", "universe", "nebula", "astronaut", "nasa", "orbit",
        "comet", "asteroid", "telescope", "cosmic", "cosmos", "lunar", "mars",
        "jupiter", "saturn", "venus", "mercury", "neptune", "uranus", "pluto",
        "milky way", "black hole", "supernova", "quasar", "pulsar", "light-year",
        "lightyear", "astronomical", "astronomy", "astrophysics", "spacecraft",
        "satellite", "atmosphere", "gravity", "eclipse", "meteor", "meteorite",
        "constellation", "hubble", "apollo", "exoplanet", "big bang", "dark matter",
        "dark energy", "neutron star", "white dwarf", "red giant", "cosmonaut",
        "launch", "rocket", "iss", "international space", "skylab", "voyager",
        "cassini", "curiosity", "perseverance", "james webb", "andromeda",
        "parallax", "zenith", "equinox", "solstice", "perihelion", "aphelion",
        "corona", "photosphere", "chromosphere", "heliosphere", "magnetosphere"
    };

    private final OkHttpClient httpClient;

    @Inject
    public QuizRepository(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Fetches up to {@link #TARGET_QUESTIONS} astronomy questions.
     * Blocks the calling thread — must be called on a background thread.
     *
     * @throws Exception on network or parse errors
     */
    public List<QuizQuestion> fetchAstronomyQuestions() throws Exception {
        List<QuizQuestion> collected = new ArrayList<>();
        int attempts = 0;

        while (collected.size() < TARGET_QUESTIONS && attempts < MAX_ATTEMPTS) {
            attempts++;
            Request req = new Request.Builder().url(TRIVIA_URL).build();
            Response resp = httpClient.newCall(req).execute();
            if (!resp.isSuccessful() || resp.body() == null) {
                throw new Exception("API error " + resp.code());
            }
            List<QuizQuestion> batch = filterAstronomy(parseQuestions(resp.body().string()));
            for (QuizQuestion q : batch) {
                if (collected.size() >= TARGET_QUESTIONS) break;
                collected.add(q);
            }
        }

        if (collected.isEmpty()) {
            throw new Exception("No space questions available. Try again later.");
        }
        return collected;
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private List<QuizQuestion> filterAstronomy(List<QuizQuestion> all) {
        List<QuizQuestion> result = new ArrayList<>();
        for (QuizQuestion q : all) {
            String combined = (q.getQuestion() + " " +
                    String.join(" ", q.getShuffledAnswers())).toLowerCase(Locale.US);
            for (String kw : ASTRO_KEYWORDS) {
                if (combined.contains(kw)) { result.add(q); break; }
            }
        }
        Collections.shuffle(result);
        return result;
    }

    private List<QuizQuestion> parseQuestions(String json) throws Exception {
        List<QuizQuestion> list = new ArrayList<>();
        JSONObject root    = new JSONObject(json);
        JSONArray  results = root.getJSONArray("results");

        for (int i = 0; i < results.length(); i++) {
            JSONObject obj  = results.getJSONObject(i);
            String question = decode(obj.getString("question"));
            String correct  = decode(obj.getString("correct_answer"));
            String diff     = obj.optString("difficulty", "medium");

            JSONArray wrongArr = obj.getJSONArray("incorrect_answers");
            List<String> all   = new ArrayList<>();
            all.add(correct);
            for (int j = 0; j < wrongArr.length(); j++) all.add(decode(wrongArr.getString(j)));
            Collections.shuffle(all);

            list.add(new QuizQuestion(question, correct, all, diff));
        }
        return list;
    }

    @SuppressWarnings("deprecation")
    private String decode(String html) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString();
        else
            return Html.fromHtml(html).toString();
    }
}
