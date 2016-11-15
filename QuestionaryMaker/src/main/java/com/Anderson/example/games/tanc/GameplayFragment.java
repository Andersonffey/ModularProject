/*
 * Copyright (C) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.Anderson.example.games.tanc;


import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.util.Base64InputStream;
import android.util.Log;

import java.io.InputStream;
import android.util.Base64;
import java.net.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveApi.DriveIdResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;

import android.widget.TextView;

/**
 * Fragment for the gameplay portion of the game. It shows the keypad
 * where the user can request their score.
 *
 * @author Bruno Oliveira (Google)
 *
 */
public class GameplayFragment extends Fragment implements OnClickListener {
    int mRequestedScore = 0;
    int mCurrentQuestion = 0;
    String mCurrentAnswer = "";
    TextView mTimer;
    CountDownTimer myCDT;
    Questionary mQuestionary = new Questionary();
    boolean isMultiple = false;
    boolean isTimer = false;
    int mTimerScore = 0;

    static int[] MY_BUTTONS = {
        R.id.option_button_1, R.id.option_button_2,
        R.id.option_button_3, R.id.option_button_4,
        R.id.ok_score_button
    };

    public interface Listener {
        public void onEnteredScore(int score);
        public String onQuestionaryAsk();
        public void onButtonClick();
    }

    Listener mListener = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d("I", "Quase la");
        View v = inflater.inflate(R.layout.fragment_gameplay, container, false);
        mTimer = (TextView) v.findViewById(R.id.timer_field);
        Log.d("I",mTimer.getText().toString() + "WW");
        ObjectMapper mapper = new ObjectMapper();
        mCurrentQuestion = 0;
        Log.d("I", "Quase la2");
        try {
            InputStream myFile = getActivity().getAssets().open("texts/"+ mListener.onQuestionaryAsk());
            Log.d("I", "Quase la3");
            Base64InputStream bs = new Base64InputStream(myFile,80);
            JsonNode actualObj = mapper.readTree(bs);
            AssignToQuestionary(actualObj);
            Log.d("I",actualObj.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
        if(mListener.onQuestionaryAsk().contains("Timer")) {
            SetTimer();
            myCDT.start();
            isTimer= true;
        }
        if(mListener.onQuestionaryAsk().contains("Multiple")) {
            isMultiple = true;
            mRequestedScore = 20;
            TextView scoreInput = ((TextView) getActivity().findViewById(R.id.score_text));
            if (scoreInput != null) scoreInput.setText("Score: "+ 20);
        }
        ManageButtons(v);
        return v;
    }

    void SetTimer(){
            myCDT =  new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                mTimer.setText("Timer: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                updateUi();
            }

        };

    }

    void AssignToQuestionary(JsonNode jNode){
        ArrayNode slaidsNode = (ArrayNode) jNode.get("questions");
        Iterator<JsonNode> questionsIterator = slaidsNode.elements();

        int temp = 0;
        while (questionsIterator.hasNext()) {
            JsonNode questions = questionsIterator.next();
            mQuestionary.mQuestions[temp] = new Question();
            mQuestionary.mQuestions[temp].setCorrectAnswer(questions.get("answer").asText());
            mQuestionary.mQuestions[temp].mAnswers.add(mQuestionary.mQuestions[temp].CorrectAnswer);
            mQuestionary.mQuestions[temp].mAnswers.add(questions.get("choice1").asText());
            mQuestionary.mQuestions[temp].mAnswers.add(questions.get("choice2").asText());
            mQuestionary.mQuestions[temp].mAnswers.add(questions.get("choice3").asText());
            Collections.shuffle(mQuestionary.mQuestions[temp].mAnswers);
            mQuestionary.mQuestions[temp].setDescription(questions.get("description").asText());
            temp++;
        }
    }

    void AssignQuestions(){

    }

    void ManageButtons(View v){

        for (int i : MY_BUTTONS) {
            Button button =  ((Button) v.findViewById(i));
            button.setOnClickListener(this);

        }
        TextView tv = (TextView) v.findViewById(R.id.question_desc);
        tv.setText(mQuestionary.mQuestions[mCurrentQuestion].getDescription());
        Button button =  ((Button) v.findViewById(R.id.option_button_1));
        button.setSelected(false);
        button.setText(mQuestionary.mQuestions[mCurrentQuestion].mAnswers.get(0));
        button =  ((Button) v.findViewById(R.id.option_button_2));
        button.setSelected(false);
        button.setText(mQuestionary.mQuestions[mCurrentQuestion].mAnswers.get(1));
        button =  ((Button) v.findViewById(R.id.option_button_3));
        button.setSelected(false);
        button.setText(mQuestionary.mQuestions[mCurrentQuestion].mAnswers.get(2));
        button =  ((Button) v.findViewById(R.id.option_button_4));
        button.setSelected(false);
        button.setText(mQuestionary.mQuestions[mCurrentQuestion].mAnswers.get(3));
    }



    public void setListener(Listener l) {
        mListener = l;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    void updateUi() {
        if (getActivity() == null) return;
        Log.d("I", mCurrentQuestion + "");
                Log.d("I", "Question Answer: " + mCurrentAnswer + "  " + mQuestionary.mQuestions[mCurrentQuestion].CorrectAnswer);
        if(!isMultiple) {
            if (mCurrentAnswer == mQuestionary.mQuestions[mCurrentQuestion].CorrectAnswer) {
                mRequestedScore += 1;
                if(isTimer){
                    mTimerScore += Integer.parseInt(mTimer.getText().toString().split(" ")[1]);
                }
            }
            TextView scoreInput = ((TextView) getActivity().findViewById(R.id.score_text));
            if (scoreInput != null) scoreInput.setText("Score: "+mRequestedScore+ "/10");
        }else {

            mRequestedScore += 20;
            TextView scoreInput = ((TextView) getActivity().findViewById(R.id.score_text));
            if (scoreInput != null) scoreInput.setText("Score: "+mRequestedScore);
        }
        mCurrentAnswer = "";
        mCurrentQuestion += 1;
        if(mCurrentQuestion != 10) {
            ManageButtons(getView());
        }
        Log.d("I", "" + mRequestedScore);

        if(mCurrentQuestion == 10){
            if(isMultiple) {
                mListener.onEnteredScore(mRequestedScore);
            }
            else if(isTimer){
                mListener.onEnteredScore(mRequestedScore*10+mTimerScore);
            }
            else{
                mListener.onEnteredScore(mRequestedScore*10);
            }
        }
        if(isTimer == true) {
            myCDT.cancel();
            SetTimer();
            myCDT.start();
        }

    }

    @Override
    public void onClick(View view) {
        mListener.onButtonClick();
        switch (view.getId()) {
        case R.id.option_button_1:
        case R.id.option_button_2:
        case R.id.option_button_3:
        case R.id.option_button_4:
            Button tv = (Button) view;
            mCurrentAnswer = tv.getText().toString();
            Log.d("I", tv.getText().toString());
            tv.setSelected(true);
            SetColor();
            tv.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
            break;
        case R.id.ok_score_button:
           // mListener.onEnteredScore(mRequestedScore);
            if(isMultiple){
                if(mCurrentAnswer != "" && mCurrentAnswer == mQuestionary.mQuestions[mCurrentQuestion].CorrectAnswer){
                    updateUi();
                }
                else{
                    mRequestedScore -= 2;
                    TextView scoreInput = ((TextView) getActivity().findViewById(R.id.score_text));
                    if (scoreInput != null) scoreInput.setText("Score: "+mRequestedScore);
                }
            }
            else if(mCurrentAnswer != "") {
                updateUi();
            }
            break;
        }
    }

    void SetColor(){

        Button b1 = (Button) getView().findViewById(R.id.option_button_1);
        Button b2 = (Button) getView().findViewById(R.id.option_button_2);
        Button b3 = (Button) getView().findViewById(R.id.option_button_3);
        Button b4 = (Button) getView().findViewById(R.id.option_button_4);
        b1.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        b2.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        b3.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        b4.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);


    }


}
