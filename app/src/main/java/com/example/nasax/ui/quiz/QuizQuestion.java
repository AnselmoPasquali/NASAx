package com.example.nasax.ui.quiz;

import java.util.List;

public class QuizQuestion {
    private final String       question;
    private final String       correctAnswer;
    private final List<String> shuffledAnswers; // all 4, already shuffled
    private final String       difficulty;

    public QuizQuestion(String question, String correctAnswer,
                        List<String> shuffledAnswers, String difficulty) {
        this.question        = question;
        this.correctAnswer   = correctAnswer;
        this.shuffledAnswers = shuffledAnswers;
        this.difficulty      = difficulty;
    }

    public String       getQuestion()        { return question;        }
    public String       getCorrectAnswer()   { return correctAnswer;   }
    public List<String> getShuffledAnswers() { return shuffledAnswers; }
    public String       getDifficulty()      { return difficulty;      }
}
