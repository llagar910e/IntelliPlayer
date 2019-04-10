package com.devandroid.intelliplayer;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.animation.ObjectAnimator;
import android.os.Bundle;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

import Proxemic.Distance;

import static com.google.android.gms.vision.face.FaceDetector.FAST_MODE;


//Indique à l'utilisateur le fonctionnement de l'application
public class Accueil extends Activity {

    TextView indications;
    Button cestParti;


      @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accueil);

        cestParti = (Button)findViewById(R.id.cestParti);



        cestParti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent distanceImageActivity = new Intent(Accueil.this, Mediatheque.class);
                startActivity(distanceImageActivity);
            }
        });


    }
}
