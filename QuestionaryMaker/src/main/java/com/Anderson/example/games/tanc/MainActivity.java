/*
 * Copyright (C) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.Anderson.example.games.tanc;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.google.example.games.basegameutils.BaseGameUtils;

import java.io.IOException;


/**
 * Our main activity for the game.
 *
 * IMPORTANT: Before attempting to run this sample, please change
 * the package name to your own package name (not com.android.*) and
 * replace the IDs on res/values/ids.xml by your own IDs (you must
 * create a game in the developer console to get those IDs).
 *
 * This is a very simple game where the user selects "easy mode" or
 * "hard mode" and then the "gameplay" consists of inputting the
 * desired score (0 to 9999). In easy mode, you get the score you
 * request; in hard mode, you get half.
 *
 * @author Bruno Oliveira
 */
public class MainActivity extends FragmentActivity
        implements MainMenuFragment.Listener,
        GameplayFragment.Listener, WinFragment.Listener, com.Anderson.example.games.tanc.ListFragment.Listener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // Fragments
    MainMenuFragment mMainMenuFragment;
    GameplayFragment mGameplayFragment;
    com.Anderson.example.games.tanc.ListFragment mListFragment;
    WinFragment mWinFragment;

    protected static final int REQUEST_CODE_RESOLUTION = 1;
    protected static final int NEXT_AVAILABLE_REQUEST_CODE = 2;
    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingConnectionFailure = false;
    private boolean mSignInClicked = false;
    final MediaPlayer bg = new MediaPlayer();
    final MediaPlayer mp = new MediaPlayer();
    private boolean mAutoStartSignInFlow = true;
    private static final int RC_RESOLVE = 5000;
    private static final int RC_UNUSED = 5001;
    private static final int RC_SIGN_IN = 9001;
    final boolean ENABLE_DEBUG = true;
    final String TAG = "TanC";
    boolean mHardMode = false;
    AccomplishmentsOutbox mOutbox = new AccomplishmentsOutbox();
    String mCurrentQuestionary;
    String mCurrentMode;
    int mCurrentLeaderBoardScore = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        // Create the Google API Client with access to Games
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();
       
        mGoogleApiClient.connect();
        mMainMenuFragment = new MainMenuFragment();
        mGameplayFragment = new GameplayFragment();
        mWinFragment = new WinFragment();
        mListFragment = new com.Anderson.example.games.tanc.ListFragment();
        // listen to fragment events
        mListFragment.setListener(this);
        mMainMenuFragment.setListener(this);
        mGameplayFragment.setListener(this);
        mWinFragment.setListener(this);
        // add initial fragment (welcome fragment)
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,
                mMainMenuFragment).commit();
        try {
            AssetFileDescriptor afd = getAssets().openFd("sounds/backgroundSound.mp3");
            bg.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            bg.prepare();
            bg.setLooping(true);
            bg.setVolume(20,20);
            bg.start();
            afd = getAssets().openFd("sounds/buttonSound.wav");
            mp.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
            mp.prepare();
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        // IMPORTANT: if this Activity supported rotation, we'd have to be
        // more careful about adding the fragment, since the fragment would
        // already be there after rotation and trying to add it again would
        // result in overlapping fragments. But since we don't support rotation,
        // we don't deal with that for code simplicity.
    }

    public void onButtonClick(){
        if(mp != null) {
            mp.start();
        }
    }

    // Switch UI to the given fragment
    void switchToFragment(Fragment newFrag) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, newFrag)
                .commit();
    }

    private boolean isSignedIn() {

        return (mGoogleApiClient != null && mGoogleApiClient.isConnected());
    }

    void GetLeaderBoardScore(){
        Games.Leaderboards.loadCurrentPlayerLeaderboardScore(getGoogleApiClient(),getString(R.string.leaderboard_test_ladder), LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC).setResultCallback(new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {

            @Override
            public void onResult(Leaderboards.LoadPlayerScoreResult arg0) {
                Log.d("I","myScore is " + arg0.getScore().getRawScore());
                mCurrentLeaderBoardScore = (int)(arg0.getScore().getRawScore());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart(): connecting");
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop(): disconnecting");
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onStartGameRequested(boolean hardMode) {
        startGame();
        Log.d("I", "Estou Antes");
    }

    public void onListRequested() {
        startList();
        Log.d("I", "Listando");
    }

    @Override
    public void onShowAchievementsRequested() {
        if (isSignedIn()) {
            startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient),
                    RC_UNUSED);
        } else {
            BaseGameUtils.makeSimpleDialog(this, getString(R.string.achievements_not_available)).show();
        }
    }

    @Override
    public void onShowLeaderboardsRequested() {
        if (isSignedIn()) {
            startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(mGoogleApiClient),
                    RC_UNUSED);
        } else {
            BaseGameUtils.makeSimpleDialog(this, getString(R.string.leaderboards_not_available)).show();
        }
    }

    /**
     * Start gameplay. This means updating some status variables and switching
     * to the "gameplay" screen (the screen where the user types the score they want).
     *
     */
    void startGame() {
        Log.d("I", "Estou Aqui");
        switchToFragment(mGameplayFragment);
    }

    void startList()
    {
        Log.d("I", "Estou Aqui");
        switchToFragment(mListFragment);
    }

    @Override
    public void onEnteredScore(int requestedScore) {
        int finalScore = requestedScore;

        mWinFragment.setFinalScore(finalScore);
        // check for achievements
        checkForAchievements(requestedScore, finalScore);

        // update leaderboards
        updateLeaderboards(requestedScore);

        // push those accomplishments to the cloud, if signed in
        pushAccomplishments();

        // switch to the exciting "you won" screen
        switchToFragment(mWinFragment);
    }

    // Checks if n is prime. We don't consider 0 and 1 to be prime.
    // This is not an implementation we are mathematically proud of, but it gets the job done.
    boolean isPrime(int n) {
        int i;
        if (n == 0 || n == 1) return false;
        for (i = 2; i <= n / 2; i++) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check for achievements and unlock the appropriate ones.
     *
     * @param requestedScore the score the user requested.
     * @param finalScore the score the user got.
     */
    void checkForAchievements(int requestedScore, int finalScore) {
        // Check if each condition is met; if so, unlock the corresponding
        // achievement.
            mOutbox.mOneAchiev = true;
            achievementToast(getString(R.string.achievement_your_first_test));
            mOutbox.mFiveAchiev = true;
            achievementToast(getString(R.string.achievement_high_five));
            mOutbox.mTenAchiev = true;
            achievementToast(getString(R.string.achievement_hungry_student));

        mOutbox.mScoreAchiev = requestedScore;

    }

    void unlockAchievement(int achievementId, String fallbackString) {
        if (isSignedIn()) {
            Games.Achievements.unlock(mGoogleApiClient, getString(achievementId));
        } else {
            Toast.makeText(this, getString(R.string.achievement) + ": " + fallbackString,
                    Toast.LENGTH_LONG).show();
        }
    }

    public void onQuestionaryCall(String s){
        mCurrentQuestionary = s;
        String[] temp = s.split("-");
        mCurrentMode = temp[temp.length-1];
        startGame();
    }

    public String onQuestionaryAsk(){
        return mCurrentQuestionary;
    }

    void achievementToast(String achievement) {
        // Only show toast if not signed in. If signed in, the standard Google Play
        // toasts will appear, so we don't need to show our own.
        if (!isSignedIn()) {
            Toast.makeText(this, getString(R.string.achievement) + ": " + achievement,
                    Toast.LENGTH_LONG).show();
        }
    }

    void pushAccomplishments() {
        if (mOutbox.mOneAchiev) {
            Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_your_first_test));
            mOutbox.mOneAchiev = false;
        }
        if (mOutbox.mFiveAchiev) {
            Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_high_five),
                    1);
        }
        if (mOutbox.mTenAchiev) {
            Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_hungry_student),
                    1);
        }
        if (mOutbox.mScoreAchiev > 0) {
            Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_genius),
                    mOutbox.mScoreAchiev);

        }
        if(mOutbox.mLeaderBoard > 0){
            Games.Leaderboards.submitScore(mGoogleApiClient, getString(R.string.leaderboard_test_ladder),
                    mOutbox.mLeaderBoard);
        }

    }
    /**
     * Update leaderboards with the user's score.
     *
     * @param finalScore The score the user got.
     */
    void updateLeaderboards(int finalScore) {
        mOutbox.mLeaderBoard = mCurrentLeaderBoardScore + finalScore;
    }

    @Override
    public void onWinScreenDismissed() {
        switchToFragment(mMainMenuFragment);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == RC_SIGN_IN) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                BaseGameUtils.showActivityResultError(this, requestCode, resultCode, R.string.signin_other_error);
            }
        }
        if (requestCode == REQUEST_CODE_RESOLUTION && resultCode == RESULT_OK) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected(): connected to Google APIs");
        // Show sign-out button on main menu
        mMainMenuFragment.setShowSignInButton(false);
        // Show "you are signed in" message on win screen, with no sign in button.
        mWinFragment.setShowSignInButton(false);

        // Set the greeting appropriately on main menu
        Player p = Games.Players.getCurrentPlayer(mGoogleApiClient);
        String displayName;
        if (p == null) {
            Log.w(TAG, "mGamesClient.getCurrentPlayer() is NULL!");
            displayName = "???";
        } else {
            displayName = p.getDisplayName();
        }
        Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_you_got_in));
        mOutbox.mFirstAchiev = false;
        GetLeaderBoardScore();
        mMainMenuFragment.setGreeting("Hello, " + displayName);
        // if we have accomplishments to push, push them
        if (!mOutbox.isEmpty()) {
            pushAccomplishments();
            Toast.makeText(this, getString(R.string.your_progress_will_be_uploaded),
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended(): attempting to connect");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed(): attempting to resolve");
        if (mResolvingConnectionFailure) {
            Log.d(TAG, "onConnectionFailed(): already resolving");
            return;
        }

        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;
            if (!BaseGameUtils.resolveConnectionFailure(this, mGoogleApiClient, connectionResult,
                    RC_SIGN_IN, getString(R.string.signin_other_error))) {
                mResolvingConnectionFailure = false;
            }
        }
        if (!connectionResult.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, connectionResult.getErrorCode(), 0).show();
            return;
        }
        try {
            connectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }

        // Sign-in failed, so show sign-in button on main menu
        mMainMenuFragment.setGreeting(getString(R.string.signed_out_greeting));
        mMainMenuFragment.setShowSignInButton(true);
        mWinFragment.setShowSignInButton(true);
    }

    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSignInButtonClicked() {
                // start the sign-in flow
        mSignInClicked = true;
        mGoogleApiClient.connect();
    }

    @Override
    public void onSignOutButtonClicked() {
        mSignInClicked = false;
        Games.signOut(mGoogleApiClient);
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        mMainMenuFragment.setGreeting(getString(R.string.signed_out_greeting));
        mMainMenuFragment.setShowSignInButton(true);
        mWinFragment.setShowSignInButton(true);
    }

    class AccomplishmentsOutbox {
        boolean mFirstAchiev = false;
        boolean mOneAchiev = false;
        boolean mFiveAchiev = false;
        boolean mTenAchiev = false;
        int mScoreAchiev = 0;
        int mBoredSteps = 0;
        int mLeaderBoard;


        boolean isEmpty() {
            return !mFirstAchiev && !mOneAchiev && !mFiveAchiev &&
                    !mTenAchiev && mScoreAchiev == 0 && mBoredSteps == 0 && mLeaderBoard == 0;
        }


    }

    @Override
    public void onWinScreenSignInClicked() {
        mSignInClicked = true;
        mGoogleApiClient.connect();
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }
}
