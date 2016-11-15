package com.Anderson.example.games.tanc;


import java.util.ArrayList;

public class Question {
    public String getDescription() {
        return Description;
    }

    public String Description = "";
    public String CorrectAnswer = "";
    public ArrayList<String> mAnswers = new ArrayList<String>();



    public void setDescription(String description) {
        Description = description;
    }

    public String getCorrectAnswer() {
        return CorrectAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        CorrectAnswer = correctAnswer;
    }
}
