package com.devandroid.intelliplayer;

import android.app.Activity;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

import Proxemic.Distance;

public class DistanceImage extends Activity {

    final Distance uneDistance = new Distance();

    // CHANGE LA TAILLE DES IMAGES CONTENUES DANS LA LISTE "listeImages" EN FONCTION DE LA DISTANCE
    void changerTaille(ArrayList<ImageView> listeImages) {
        for (int i = 0; i < listeImages.size(); i++) {
            listeImages.get(i).setScaleY((float) uneDistance.getDistance());
            listeImages.get(i).setScaleX((float) uneDistance.getDistance());
        }
    }

    // CHANGE LA VALEUR DU MARGIN DES IMAGES POUR AVOIR UN LAYOUT COHERENT
    void changerMargins(ImageView imageView, int nbImages) {
        if (nbImages == 6) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(imageView.getWidth(), imageView.getHeight());
            layoutParams.setMargins(0, -50, 0, 0);
            imageView.setLayoutParams(layoutParams);
        } else {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(imageView.getWidth(), imageView.getHeight());
            layoutParams.setMargins(0, 45, 0, 0);
            imageView.setLayoutParams(layoutParams);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distance_image);

        //CONTAINERS VERTICAUX CONTENANT LES IMAGES
        final LinearLayout containerGauche = (LinearLayout) findViewById(R.id.containerGauche);
        final LinearLayout containerDroit = (LinearLayout) findViewById(R.id.containerDroit);

        //IMAGES AFFICHEES LORS DE LA CREATION DE L'ACTIVITE
        final ImageView aquaman = (ImageView) findViewById(R.id.aquaman);
        final ImageView ironman = (ImageView) findViewById(R.id.ironman);
        final ImageView deadpool = (ImageView) findViewById(R.id.deadpool);
        final ImageView badboys = (ImageView) findViewById(R.id.badboys);


        //CREATION DES IMAGES 5 et 6
        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(510, 510);

        final ImageView lalaland = new ImageView(this);
        lalaland.setImageResource(R.drawable.lalaland);
        lalaland.setLayoutParams(layoutParams);


        final ImageView livebynight = new ImageView(this);
        livebynight.setImageResource(R.drawable.livebynight);
        livebynight.setLayoutParams(layoutParams);


        //BOUTONS SIMULANT LE CHANGEMENT DE DISTANCE
        final Button augmenterTaille = (Button) findViewById(R.id.augmenterTaille);
        final Button diminuerTaille = (Button) findViewById(R.id.diminuerTaille);


        //LISTE D'IMAGES
        final ArrayList<ImageView> listeImages = new ArrayList<ImageView>();
        listeImages.add(aquaman);
        listeImages.add(deadpool);
        listeImages.add(ironman);
        listeImages.add(badboys);


        //INITIALISATION DE LA DISTANCE A LA CREATION
        uneDistance.setDistance((float) 1);


        //CLIC BOUTON AUGMENTER TAILLE (AGRANDIR LA DISTANCE)
        augmenterTaille.setOnTouchListener(new View.OnTouchListener() {
            boolean changementIHM=false;
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                // SI 4 IMAGES
                if (listeImages.size() == 4) {
                    //SI IMAGES ASSEZ PETITES ET IMAGES JAMAIS AFFICHEES PAR 3*2 -> AUGMENTER TAILLE
                    if(changementIHM==false) {
                        if (listeImages.get(0).getScaleX() < (1.17)) {
                            uneDistance.setDistance(uneDistance.getDistance() + 0.005);
                            changerTaille(listeImages);
                        }
                    }
                    //SI IMAGES ASSEZ PETITES ET IMAGES DEJA AFFICHEES PAR 3*2 -> AUGMENTER TAILLE
                    else if (listeImages.get(0).getScaleX() < (1)) {
                        uneDistance.setDistance(uneDistance.getDistance() + 0.005);
                        changerTaille(listeImages);
                    }
                }
                //SI 6 IMAGES
                else if (listeImages.size() == 6) {
                    //SI IMAGES TROP GRANDES -> ENLEVER IMAGES
                    if (listeImages.get(0).getScaleX() > (0.794)) {
                        changementIHM=true;
                        containerGauche.removeView(listeImages.get(4));
                        containerDroit.removeView(listeImages.get(5));
                        listeImages.remove(lalaland);
                        listeImages.remove(livebynight);
                        changerMargins(listeImages.get(0), listeImages.size());
                        changerMargins(listeImages.get(1), listeImages.size());
                        changerMargins(listeImages.get(2), listeImages.size());
                        changerMargins(listeImages.get(3), listeImages.size());
                    }
                    //SI IMAGES ASSEZ PETITES -> AUGMENTER TAILLE
                    else {
                        uneDistance.setDistance(uneDistance.getDistance() + 0.005);
                        changerTaille(listeImages);
                    }
                }
                return false;
            }
        });


        //CLIC BOUTON DIMINUER TAILLE (RETRECIR LA DISTANCE)
        diminuerTaille.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //SI 4 IMAGES
                if (listeImages.size() == 4) {
                    //SI IMAGES ASSEZ GRANDES -> DIMINUER TAILLE
                    if (listeImages.get(0).getScaleX() > 0.794) {
                        uneDistance.setDistance(uneDistance.getDistance() - 0.005);
                        changerTaille(listeImages);
                    }
                    //SI IMAGES TROP PETITES -> AJOUTER IMAGES
                    else {
                        listeImages.add(lalaland);
                        listeImages.add(livebynight);
                        containerGauche.addView(listeImages.get(4));
                        containerDroit.addView(listeImages.get(5));
                        changerMargins(listeImages.get(0), listeImages.size());
                        changerMargins(listeImages.get(1), listeImages.size());
                        changerMargins(listeImages.get(2), listeImages.size());
                        changerMargins(listeImages.get(3), listeImages.size());
                    }
                }
                //SI 6 IMAGES
                if (listeImages.size() == 6) {
                    //SI IMAGES ASSEZ GRANDES -> DIMINUER TAILLE
                    if (listeImages.get(0).getScaleX() > 0.6) {
                        uneDistance.setDistance(uneDistance.getDistance() - 0.005);
                        changerTaille(listeImages);
                    }
                }
                return false;
            }
        });


        deadpool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent lecteurMediaActivity = new Intent(DistanceImage.this, LecteurMedia.class);
                startActivity(lecteurMediaActivity);
            }
        });

    }
}
