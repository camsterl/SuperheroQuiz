package com.example.superheroquiz;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.superheroquiz.model.Heros;
import com.example.superheroquiz.model.JSONLoader;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Superhero Quiz";

    private static final int HEROES_IN_QUIZ = 10;

    private Button[] mButtons = new Button[4];
    private List<Heros> mAllHerosList;  // all the countries loaded from JSON
    private List<Heros> mQuizHerosList; // countries in current quiz (just 10 of them)
    private Heros mCorrectHeros; // correct country for the current question
    private int mTotalGuesses; // number of total guesses made
    private int mCorrectGuesses; // number of correct guesses
    private SecureRandom rng; // used to randomize the quiz
    private Handler handler; // used to delay loading next country

    private TextView mQuestionNumberTextView; // shows current question #
    private ImageView mHerosImageView; // displays a flag
    private TextView mAnswerTextView; // displays correct answer

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mQuizHerosList = new ArrayList<>(HEROES_IN_QUIZ);
        rng = new SecureRandom();
        handler = new Handler();

        // Done: Get references to GUI components (textviews and imageview)
        mQuestionNumberTextView = findViewById(R.id.questionNumberTextView);
        mHerosImageView = findViewById(R.id.heroImageView);
        mAnswerTextView = findViewById(R.id.answerTextView);

        // Done: Put all 4 buttons in the array (mButtons)
        mButtons[0] = findViewById(R.id.button);
        mButtons[1] = findViewById(R.id.button2);
        mButtons[2] = findViewById(R.id.button3);
        mButtons[3] = findViewById(R.id.button4);

        // Done: Set mQuestionNumberTextView's text to the appropriate strings.xml resource
        mQuestionNumberTextView.setText(getString(R.string.question, 1, HEROES_IN_QUIZ));

        // Done: Load all the countries from the JSON file using the JSONLoader

        try {

            mAllHerosList = JSONLoader.loadJSONFromAsset(this);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        // Done: Call the method resetQuiz() to start the quiz.
        resetQuiz();

    }

    /**
     * Sets up and starts a new quiz.
     */
    public void resetQuiz() {

        // Done: Reset the number of correct guesses made
        mCorrectGuesses = 0;
        // Done: Reset the total number of guesses the user made
        mTotalGuesses = 0;
        // Done: Clear list of quiz countries (for prior games played)
        mQuizHerosList.clear();

        // TODO: Randomly add FLAGS_IN_QUIZ (10) countries from the mAllCountriesList into the mQuizCountriesList
        int size = mAllHerosList.size();
        int randomPosition;
        Heros randomCountry;

        while(mQuizHerosList.size() <= HEROES_IN_QUIZ)
        {
            randomPosition = rng.nextInt(size);
            randomCountry = mAllHerosList.get(randomPosition);
            //check for duplicates (use contains)
            //if quiz list doesn't contain randomCountry then add it
            if(!(mQuizHerosList.contains(randomCountry)))
                mQuizHerosList.add(randomCountry);
        }
        // set text of four buttons to first four country names
        // for(int i =0; i < mButtons.length; i++)
        //{
        //    mButtons[i].setText(mQuizCountriesList.get(i).getName());
        //}

        // Done: Ensure no duplicate countries (e.g. don't add a country if it's already in mQuizCountriesList)



        // Done: Start the quiz by calling loadNextFlag
        loadNextHero();
    }

    /**
     * Method initiates the process of loading the next flag for the quiz, showing
     * the flag's image and then 4 buttons, one of which contains the correct answer.
     */
    private void loadNextHero() {
        // Done: Initialize the mCorrectCountry by removing the item at position 0 in the mQuizCountries
        mCorrectHeros = mQuizHerosList.get(0);
        mQuizHerosList.remove(0);
        // Done: Clear the mAnswerTextView so that it doesn't show text from the previous question
        mAnswerTextView.setText("");
        // Done: Display current question number in the mQuestionNumberTextView
        mQuestionNumberTextView.setText(getString(R.string.question, mCorrectGuesses+1, HEROES_IN_QUIZ));

        // Done: Use AssetManager to load next image from assets folder
        AssetManager am = getAssets();

        try {
            InputStream stream = am.open(mCorrectHeros.getFileName());
            Drawable image = Drawable.createFromStream(stream, mCorrectHeros.getName());
            mHerosImageView.setImageDrawable(image);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());

        }



        // Done: Get an InputStream to the asset representing the next flag
        // Done: and try to use the InputStream to create a Drawable
        // Done: The file name can be retrieved from the correct country's file name.
        // Done: Set the image drawable to the correct flag.

        // Done: Shuffle the order of all the countries (use Collections.shuffle)
        do {
            Collections.shuffle(mAllHerosList);
        }
        while((mAllHerosList.subList(0,mButtons.length).contains(mCorrectHeros)));


        // Done: Loop through all 4 buttons, enable them all and set them to the first 4 countries
        // Done: in the all countries list

        for(int i = 0; i < mButtons.length; i++)
        {
            mButtons[i].setEnabled(true);
            mButtons[i].setText(mAllHerosList.get(i).getName());
        }

        // Done: After the loop, randomly replace one of the 4 buttons with the name of the correct country
        mButtons[rng.nextInt(mButtons.length)].setText(mCorrectHeros.getName());

    }

    /**
     * Handles the click event of one of the 4 buttons indicating the guess of a country's name
     * to match the flag image displayed.  If the guess is correct, the country's name (in GREEN) will be shown,
     * followed by a slight delay of 2 seconds, then the next flag will be loaded.  Otherwise, the
     * word "Incorrect Guess" will be shown in RED and the button will be disabled.
     * @param v
     */
    public void makeGuess(View v) {
        mTotalGuesses++;
        // Done: Downcast the View v into a Button (since it's one of the 4 buttons)
        Button clickedButton = (Button) v;
        // Done: Get the country's name from the text of the button
        String guessedName = clickedButton.getText().toString();
        // Done: If the guess matches the correct country's name, increment the number of correct guesses,
        if(guessedName.equalsIgnoreCase(mCorrectHeros.getName())) {

            mCorrectGuesses++;

            if(mCorrectGuesses < HEROES_IN_QUIZ)
            {
                //disable buttons
                for(int i = 0; i < mButtons.length; i++) {
                    mButtons[i].setEnabled(false);
                }
                //change text to correct answer and make it green
                mAnswerTextView.setText(mCorrectHeros.getName());
                mAnswerTextView.setTextColor(getResources().getColor(R.color.correct_answer));

                //Use a handler to delay actions
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadNextHero();
                    }
                }, 2000);




            }
            else{
                //create alert dialouge with some text and a button to reset quiz

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                double percentage = (double) mCorrectGuesses/mTotalGuesses * 100.0;
                builder.setMessage(getString(R.string.results, mTotalGuesses, percentage ));
                builder.setPositiveButton(getString(R.string.reset_quiz), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetQuiz();
                    }
                });
                //disable cancel operation
                builder.setCancelable(false);
                // create diagloue and show
                builder.create();
                builder.show();
            }


            // Done: then display correct answer in green text.  Also, disable all 4 buttons (can't keep guessing once it's correct)

            // TODO: Nested in this decision, if the user has completed all 10 questions, show an AlertDialog
            // TODO: with the statistics and an option to Reset Quiz
        }
        // done: Else, the answer is incorrect, so display "Incorrect Guess!" in red
        else
        {
            //if incorrect disbale that one button
            clickedButton.setEnabled(false);
            mAnswerTextView.setText(getString(R.string.incorrect_answer));
            mAnswerTextView.setTextColor(getResources().getColor(R.color.incorrect_answer));

        }
        // done: and disable just the incorrect button.



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings_xml, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
        return super.onOptionsItemSelected(item);
    }

    SharedPreferences.OnSharedPreferenceChangeListener
            mSharedPreferenceChangeListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                                      String key) {
                    if (key.equals(NAME))
                    {
                        String region = sharedPreferences.getString(NAME,
                                getString(R.string.default_region));
                        updateRegion(region);
                        resetQuiz();
                    }
                    else if (key.equals(CHOICES))
                    {
                        mChoices = Integer.parseInt(sharedPreferences.getString(CHOICES,
                                getString(R.string.default_choices)));
                        updateChoices();
                        resetQuiz();
                    }
                    Toast.makeText(MainActivity.this, R.string.restarting_quiz,
                            Toast.LENGTH_SHORT).show();
                }
            };
}


