package com.devandroid.intelliplayer;

import android.media.MediaPlayer;
import android.support.annotation.RawRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class LecteurMedia extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecteur_media);

        MediaPlayer lecteurVideo = new MediaPlayer();
        lecteurVideo.setDataSource();
int i = 0;
    }
}
