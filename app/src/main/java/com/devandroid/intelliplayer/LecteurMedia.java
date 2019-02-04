package com.devandroid.intelliplayer;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.VideoView;

public class LecteurMedia extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecteur_media);

        VideoView video = (VideoView) findViewById(R.id.videoView);
        video.setVideoURI(Uri.parse(Environment.getExternalStorageDirectory().getPath() + "/small.mp4"));

        video.start();

    }
}
