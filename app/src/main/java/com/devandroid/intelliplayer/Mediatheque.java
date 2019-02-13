package com.devandroid.intelliplayer;

import android.app.Activity;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import java.util.ArrayList;
import Proxemic.Distance;

public class Mediatheque extends Activity {

    final Distance uneDistance = new Distance();


    void ajouterImages(ConstraintLayout mediasContainer, ArrayList<ImageView> listeImages, ImageView image1, ImageView image2){

        image1.setLayoutParams(new ConstraintLayout.LayoutParams(listeImages.get(2).getWidth(),listeImages.get(2).getHeight()));
        image2.setLayoutParams(new ConstraintLayout.LayoutParams(listeImages.get(3).getWidth(),listeImages.get(3).getHeight()));

        image1.setId(View.generateViewId());
        image2.setId(View.generateViewId());

        listeImages.add(image1);
        listeImages.add(image2);

        mediasContainer.addView(image1);
        mediasContainer.addView(image2);


        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(mediasContainer);
        constraintSet.connect(listeImages.get(4).getId(),ConstraintSet.TOP,listeImages.get(2).getId(),ConstraintSet.BOTTOM,Math.round(50*getResources().getDisplayMetrics().density));
        constraintSet.connect(listeImages.get(4).getId(),ConstraintSet.LEFT,listeImages.get(2).getId(),ConstraintSet.LEFT,0);
        constraintSet.connect(listeImages.get(5).getId(),ConstraintSet.TOP,listeImages.get(3).getId(),ConstraintSet.BOTTOM,Math.round(50*getResources().getDisplayMetrics().density));
        constraintSet.connect(listeImages.get(5).getId(),ConstraintSet.LEFT,listeImages.get(3).getId(),ConstraintSet.LEFT,0);

        constraintSet.applyTo(mediasContainer);

    }



    // CHANGE LA TAILLE DES IMAGES CONTENUES DANS LA LISTE "listeImages" EN FONCTION DE LA DISTANCE
    void changerTaille(ArrayList<ImageView> listeImages) {
        for (int i = 0; i < listeImages.size(); i++) {

            ViewGroup.LayoutParams nouveauParam = listeImages.get(i).getLayoutParams();
            nouveauParam.width = (int)Math.round(500 * (uneDistance.getDistance()*100))/100;
            nouveauParam.height = (int)Math.round(500 * (uneDistance.getDistance()*100))/100;
            listeImages.get(i).setLayoutParams(nouveauParam);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mediatheque);

        //IMAGES AFFICHEES LORS DE LA CREATION DE L'ACTIVITE
        final ImageView aquaman = (ImageView) findViewById(R.id.aquaman);
        final ImageView ironman = (ImageView) findViewById(R.id.ironman);
        final ImageView deadpool = (ImageView) findViewById(R.id.deadpool);
        final ImageView badboys = (ImageView) findViewById(R.id.badboys);


        //CONTAINER PRINCIPAL
        final ConstraintLayout mediasContainer =  (ConstraintLayout)findViewById(R.id.mediasContainer);


        //CREATION DES IMAGES 5 et 6
        final ImageView lalaland = new ImageView(this);
        lalaland.setImageResource(R.drawable.lalaland);
        lalaland.setLayoutParams(mediasContainer.getLayoutParams());
        final ImageView livebynight = new ImageView(this);
        livebynight.setImageResource(R.drawable.livebynight);
        livebynight.setLayoutParams(mediasContainer.getLayoutParams());


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

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                // SI 4 IMAGES
                if (listeImages.size() == 4) {
                    //SI IMAGES ASSEZ PETITES -> AUGMENTER TAILLE
                    if (listeImages.get(0).getWidth() < (500)) {
                            uneDistance.setDistance(uneDistance.getDistance() + 0.005);
                            changerTaille(listeImages);
                        }
                }
                //SI 6 IMAGES
                else if (listeImages.size() == 6) {
                    //SI IMAGES TROP GRANDES -> ENLEVER IMAGES
                    if (listeImages.get(0).getWidth() > (400)) {
                        mediasContainer.removeView(listeImages.get(4));
                        mediasContainer.removeView(listeImages.get(5));
                        listeImages.remove(lalaland);
                        listeImages.remove(livebynight);
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
                    if (listeImages.get(0).getWidth() > 400) {
                        uneDistance.setDistance(uneDistance.getDistance() - 0.005);
                        changerTaille(listeImages);
                   }
                    //SI IMAGES TROP PETITES -> AJOUTER IMAGES
                    else {
                        ajouterImages(mediasContainer, listeImages, lalaland, livebynight);
                   }
                }
                //SI 6 IMAGES
                if (listeImages.size() == 6) {
                    //SI IMAGES ASSEZ GRANDES -> DIMINUER TAILLE
                    if (listeImages.get(0).getWidth() > 300) {
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
                Intent lecteurMediaActivity = new Intent(Mediatheque.this, LecteurMedia.class);
                startActivity(lecteurMediaActivity);
            }
        });
    }
}


