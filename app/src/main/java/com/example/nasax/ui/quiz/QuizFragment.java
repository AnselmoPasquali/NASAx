package com.example.nasax.ui.quiz;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.nasax.R;
import com.example.nasax.databinding.FragmentQuizBinding;
import com.example.nasax.ui.stats.QuizStatsFragment;
import com.example.nasax.ui.stats.QuizStatsViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class QuizFragment extends Fragment {

    private FragmentQuizBinding binding;
    private QuizViewModel       viewModel;
    private QuizStatsViewModel  statsViewModel;

    private int colorDefault;
    private int colorCorrect;
    private int colorWrong;

    // Salviamo il punteggio una sola volta per partita
    private boolean scoreSaved = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentQuizBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel      = new ViewModelProvider(this).get(QuizViewModel.class);
        statsViewModel = new ViewModelProvider(this).get(QuizStatsViewModel.class);

        colorDefault = 0xFF281A08;
        colorCorrect = 0xFF2E7D32;
        colorWrong   = 0xFFC62828;

        Typeface nasa = ResourcesCompat.getFont(requireContext(), R.font.nasa_font);
        if (nasa != null) binding.tvQuizTitle.setTypeface(nasa);

        for (MaterialButton btn : answerButtons()) {
            btn.setOnClickListener(v -> viewModel.submitAnswer(btn.getText().toString()));
        }

        binding.btnNext.setOnClickListener(v -> viewModel.nextQuestion());

        binding.btnPlayAgain.setOnClickListener(v -> {
            scoreSaved = false;
            viewModel.restart();
        });

        binding.btnStats.setOnClickListener(v -> openStats());

        viewModel.getState().observe(getViewLifecycleOwner(), this::render);
    }

    // ── rendering ─────────────────────────────────────────────────────────────

    private void render(QuizUiState state) {
        binding.progressBar.setVisibility(View.GONE);
        binding.tvError.setVisibility(View.GONE);
        binding.layoutQuiz.setVisibility(View.GONE);
        binding.layoutGameOver.setVisibility(View.GONE);

        switch (state.getPhase()) {
            case LOADING:
                binding.progressBar.setVisibility(View.VISIBLE);
                break;
            case ERROR:
                binding.tvError.setVisibility(View.VISIBLE);
                binding.tvError.setText(getString(R.string.quiz_error_prefix) + state.getError());
                break;
            case QUESTION:
                binding.layoutQuiz.setVisibility(View.VISIBLE);
                renderQuestion(state, false, null);
                break;
            case ANSWERED:
                binding.layoutQuiz.setVisibility(View.VISIBLE);
                renderQuestion(state, true, state.getSelectedAnswer());
                break;
            case GAME_OVER:
                binding.layoutGameOver.setVisibility(View.VISIBLE);
                renderGameOver(state.getScore(), state.totalQuestions());
                break;
        }
    }

    private void renderQuestion(QuizUiState state, boolean answered, String selected) {
        QuizQuestion q = state.currentQuestion();
        if (q == null) return;

        int idx   = state.getCurrentIndex();
        int total = state.totalQuestions();

        binding.tvProgress.setText(getString(R.string.quiz_progress, idx + 1, total));
        binding.tvScore.setText(getString(R.string.quiz_score_format, state.getScore(), total));
        binding.tvQuestion.setText(q.getQuestion());

        String diff = q.getDifficulty();
        int diffColor;
        switch (diff.toLowerCase(Locale.US)) {
            case "easy": diffColor = 0xFF388E3C; break;
            case "hard": diffColor = 0xFFD32F2F; break;
            default:     diffColor = 0xFFF57C00; break;
        }
        binding.tvDifficulty.setBackgroundColor(diffColor);
        String diffLabel;
        switch (diff.toLowerCase(Locale.US)) {
            case "easy": diffLabel = getString(R.string.quiz_difficulty_easy); break;
            case "hard": diffLabel = getString(R.string.quiz_difficulty_hard); break;
            default:     diffLabel = getString(R.string.quiz_difficulty_medium); break;
        }
        binding.tvDifficulty.setText(diffLabel);

        List<MaterialButton> btns    = answerButtons();
        List<String>         answers = q.getShuffledAnswers();
        for (int i = 0; i < btns.size(); i++) {
            MaterialButton btn = btns.get(i);
            String         ans = i < answers.size() ? answers.get(i) : "";
            btn.setText(ans);
            btn.setEnabled(!answered);
            if (answered) {
                boolean correct  = ans.equals(q.getCorrectAnswer());
                boolean selected2 = ans.equals(selected);
                if      (correct)   btn.setBackgroundColor(colorCorrect);
                else if (selected2) btn.setBackgroundColor(colorWrong);
                else                btn.setBackgroundColor(0xFF1E1408);
            } else {
                btn.setBackgroundColor(colorDefault);
            }
        }

        binding.btnNext.setVisibility(answered ? View.VISIBLE : View.GONE);
        boolean isLast = idx == total - 1;
        binding.btnNext.setText(isLast
                ? getString(R.string.quiz_see_results)
                : getString(R.string.quiz_next));
    }

    private void renderGameOver(int score, int total) {
        // Save score to Room — only once per game
        if (!scoreSaved) {
            statsViewModel.saveScore(score, total);
            scoreSaved = true;
        }

        binding.tvFinalScore.setText(getString(R.string.quiz_score_format, score, total));
        double pct = total > 0 ? (double) score / total : 0;
        int msgRes;
        if      (pct >= 0.9) msgRes = R.string.quiz_msg_expert;
        else if (pct >= 0.6) msgRes = R.string.quiz_msg_good;
        else if (pct >= 0.3) msgRes = R.string.quiz_msg_keep_going;
        else                 msgRes = R.string.quiz_msg_try_again;
        binding.tvFinalMessage.setText(getString(msgRes));
    }

    private void openStats() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new QuizStatsFragment())
                .addToBackStack(null)
                .commit();
    }

    private List<MaterialButton> answerButtons() {
        return Arrays.asList(binding.btnA, binding.btnB, binding.btnC, binding.btnD);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
