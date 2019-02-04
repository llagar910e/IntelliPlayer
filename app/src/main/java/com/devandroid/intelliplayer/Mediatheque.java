package com.devandroid.intelliplayer;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;

public class Mediatheque extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mediatheque);

        /*GridView myGridView = (GridView)findViewById(R.id.myGridView);
        myGridView.setHorizontalSpacing(14);
        myGridView.setVerticalSpacing(14);*/
    }
}
