package com.noahhendrickson.bot.questions;

public class Question {

    private String questionText;
    private String correctAnswer;

    public Question() {
    }

    public Question(String questionText, String correctAnswer) {
        this.questionText = questionText;
        this.correctAnswer = correctAnswer;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }
}
