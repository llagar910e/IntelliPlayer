package com.devandroid.intelliplayer;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Accueil extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_accueil);

        TextView bienvenue = (TextView)findViewById(R.id.bienvenue);
        TextView messageAccueil = (TextView)findViewById(R.id.messageAccueil);
        TextView prets = (TextView)findViewById(R.id.prets);
        Button cestParti = (Button)findViewById(R.id.boutonCestParti);

        cestParti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent distanceImageActivity = new Intent(Accueil.this, Mediatheque.class);
                startActivity(distanceImageActivity);
            }
        });
    }
}
