package com.hellothxsorry.guessthegame;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private ImageView imageViewGame;
    private Button button0;
    private Button button1;
    private Button button2;
    private Button button3;

    private ArrayList<String> titles;
    private ArrayList<String> imgUrls;
    private ArrayList<Button> buttons;
    private int numberOfQuestion;
    private int numberOfCorrectAnswer;

    private String resource = "https://www.ign.com/lists/top-100-games/71";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageViewGame = findViewById(R.id.imageViewGame);
        button0 = findViewById(R.id.button0);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        titles = new ArrayList<>();
        imgUrls = new ArrayList<>();
        buttons = new ArrayList<>();
        buttons.add(button0);
        buttons.add(button1);
        buttons.add(button2);
        buttons.add(button3);
        GetContent();
        PlayQuiz();
    }

    public void GetContent() {
        DownloadLinesTask downloadLinesTask = new DownloadLinesTask();
        try {
            String getLinesData = downloadLinesTask.execute(resource).get();
            Log.i("data", getLinesData);
            String beginPoint = "class=\"item item-2 item-rank-71 has-labels  no-sub-heading  insert-ad \"";
            String endPoint = "Super Mario World is the best-selling SNES game, selling over 20 million copies";
            Pattern pattern = Pattern.compile(beginPoint + "(.*?)" + endPoint);
            Matcher matcher = pattern.matcher(getLinesData);
            String filteredData = "";
            while (matcher.find()) {
                filteredData = matcher.group(1);
            }
            Pattern patternImg = Pattern.compile("data-original=\"(.*?)\"");
            Pattern patternTitle = Pattern.compile("data-heading=\"(.*?)\"");
            Matcher imgMatcher = patternImg.matcher(filteredData);
            while (imgMatcher.find()) {
                imgUrls.add(imgMatcher.group(1));
            }
            Matcher titleMatcher = patternTitle.matcher(filteredData);
            while (titleMatcher.find()) {
                titles.add(titleMatcher.group(1));
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void onClickTriggering(View view) {
        Button button = (Button) view;
        String tag = button.getTag().toString();
        if (Integer.parseInt(tag) == numberOfCorrectAnswer) {
            Toast toastCorrect = Toast.makeText(this, "CORRECT +1!", Toast.LENGTH_SHORT);
            toastCorrect.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
            toastCorrect.show();
        } else {
            Toast toastWrong = Toast.makeText(this, "WRONG! This is " + titles.get(numberOfQuestion), Toast.LENGTH_SHORT);
            toastWrong.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
            toastWrong.show();
        }
        PlayQuiz();
    }

    private void GenerateQuestion() {
        numberOfQuestion = (int) (Math.random() * imgUrls.size());
        numberOfCorrectAnswer = (int) (Math.random() * buttons.size());
    }

    private int GenerateWrongAnswer() {
        return (int) (Math.random() * titles.size());
    }

    private void PlayQuiz() {
        GenerateQuestion();
        DownloadImageTask imageTask = new DownloadImageTask();
        try {
            Bitmap imageToGuess = imageTask.execute(imgUrls.get(numberOfQuestion)).get();
            if (imageToGuess != null) {
                imageViewGame.setImageBitmap(imageToGuess);
                for (int i = 0; i < buttons.size(); i++) {
                    if (i == numberOfCorrectAnswer) {
                        buttons.get(i).setText(titles.get(numberOfQuestion));
                    } else {
                        int wrongAnswer = GenerateWrongAnswer();
                        buttons.get(i).setText(titles.get(wrongAnswer));
                    }
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            URL url = null;
            HttpURLConnection httpURLConnection = null;
            StringBuilder result = new StringBuilder();
            try {
                url = new URL(strings[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }

            return null;
        }
    }

    private static class DownloadLinesTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            URL url = null;
            HttpURLConnection httpURLConnection = null;
            StringBuilder result = new StringBuilder();
            try {
                url = new URL(strings[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line = bufferedReader.readLine();
                while (line != null) {
                    result.append(line);
                    line = bufferedReader.readLine();
                }
                return result.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }
            return null;
        }
    }
}
