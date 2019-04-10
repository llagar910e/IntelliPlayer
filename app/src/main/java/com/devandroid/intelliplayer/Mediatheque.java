package com.devandroid.intelliplayer;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;
import java.util.ArrayList;
import Proxemic.Distance;

import static com.google.android.gms.vision.face.FaceDetector.FAST_MODE;

//Vitrine d'une mediathèque avec une seule vignette clickable
public class Mediatheque extends Activity {

    //La distance entre l'utilisateur et l'appareil
    final Distance uneDistance = new Distance();

    //L'ancienne distance de l'utilisateur utilisée pour lisser le changement de taille des images
    final Distance ancienneDistance = new Distance();

    //Coefficient utilisé pour lisser le changement de taille des images
    static final float ALPHA = 0.01f;

    //Thread pour orienter les images en fonction de la position de l'utilisateur
    Runnable runnableOrientationImages;

    //Thread pour changer la taille des images en fonction de la distance entre l'utilisateur et l'appareil
    Runnable runnableTailleImages;

    //Thread pour changer  le nombre d'images affichées en fonction de la distance entre l'utilisateur et l'appareil
    Runnable runnableNombreImages;

    //Permettent d'utiliser la caméra servant à repérer l'utilisateur
    CameraSource cameraSource;
    SurfaceView cameraView;
    final int RequestCameraPermissionId = 1001;

    //Utilisé pour changer l'orientation des images
    ObjectAnimator flip;

    //Stocke les visages détéctés par la camera
    SparseArray<Face> faces;

    //Stocke les coordonnées X précédente et actuelle des images pour les animer
    Float[] tabCoordX = new Float[]{0f, 0f};


    //Ajoute 2 images à la médiathèque
    void ajouterImages(ConstraintLayout mediasContainer, ArrayList<ImageView> listeImages, ImageView image1, ImageView image2){

        image1.setLayoutParams(new ConstraintLayout.LayoutParams(listeImages.get(4).getWidth(),listeImages.get(4).getHeight()));
        image2.setLayoutParams(new ConstraintLayout.LayoutParams(listeImages.get(5).getWidth(),listeImages.get(5).getHeight()));

        image1.setId(View.generateViewId());
        image2.setId(View.generateViewId());

        listeImages.add(image1);
        listeImages.add(image2);

        mediasContainer.addView(image1);
        mediasContainer.addView(image2);


        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(mediasContainer);
        constraintSet.connect(listeImages.get(6).getId(),ConstraintSet.TOP,listeImages.get(4).getId(),ConstraintSet.BOTTOM,Math.round(50*getResources().getDisplayMetrics().density));
        constraintSet.connect(listeImages.get(6).getId(),ConstraintSet.LEFT,listeImages.get(4).getId(),ConstraintSet.LEFT,0);
        constraintSet.connect(listeImages.get(7).getId(),ConstraintSet.TOP,listeImages.get(5).getId(),ConstraintSet.BOTTOM,Math.round(50*getResources().getDisplayMetrics().density));
        constraintSet.connect(listeImages.get(7).getId(),ConstraintSet.LEFT,listeImages.get(5).getId(),ConstraintSet.LEFT,0);

        constraintSet.applyTo(mediasContainer);

    }


    //Change la taille des images contenues dans la liste "listeImages" en fonction de la distance
    void changerTaille(ArrayList<ImageView> listeImages) {
        if(uneDistance.getDistance()<1.6) {
            for (int i = 0; i < listeImages.size(); i++) {
                ViewGroup.LayoutParams nouveauParam = listeImages.get(i).getLayoutParams();
                nouveauParam.width = (int) (150 * (1.5 + uneDistance.getDistance()));
                nouveauParam.height = (int) (150 * (1.5 + uneDistance.getDistance()));
                listeImages.get(i).setLayoutParams(nouveauParam);

            }
        }
    }

    //Utilisé pour vérifier l'atorisation d'utiliser  la caméra
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestCameraPermissionId: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    try {
                        cameraSource.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mediatheque);

        //Images affichées lors de la création de l'activité
        final ImageView aquaman = (ImageView) findViewById(R.id.aquaman);
        final ImageView ironman = (ImageView) findViewById(R.id.ironman);
        final ImageView deadpool = (ImageView) findViewById(R.id.deadpool);
        final ImageView badboys = (ImageView) findViewById(R.id.badboys);
        final ImageView lalaland = (ImageView) findViewById(R.id.lalaland);
        final ImageView livebynight = (ImageView) findViewById(R.id.livebynight);


        //Container principal
        final ConstraintLayout mediasContainer =  (ConstraintLayout)findViewById(R.id.mediasContainer);


        //Instanciation des images 7 et 8
        final ImageView sauverryan = new ImageView(this);
        sauverryan.setImageResource(R.drawable.sauverryan);
        sauverryan.setLayoutParams(mediasContainer.getLayoutParams());
        final ImageView forestgump = new ImageView(this);
        forestgump.setImageResource(R.drawable.forestgump);
        forestgump.setLayoutParams(mediasContainer.getLayoutParams());

        //Liste des images affichées
        final ArrayList<ImageView> listeImages = new ArrayList<ImageView>();
        listeImages.add(aquaman);
        listeImages.add(deadpool);
        listeImages.add(ironman);
        listeImages.add(badboys);
        listeImages.add(lalaland);
        listeImages.add(livebynight);

        //Le preview de la camera réduit à 1 pixel nécessaire pour utiliser la détection de visages
        cameraView = (SurfaceView) findViewById(R.id.cameraView);


        //Initialisation de la distance à la création
        uneDistance.setDistance((float) 1);

        //L'objet permettant de détecter les visages
        FaceDetector detector = new FaceDetector.Builder(this)
                .setTrackingEnabled(true)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FAST_MODE)
                .build();

        if (!detector.isOperational()) {
            Log.w("MainActivity", "FaceDetector dependencies not available");
        } else {
            //Les paramètres de la camera
            cameraSource = new CameraSource.Builder(getApplicationContext(), detector)
                    .setFacing(CameraSource.CAMERA_FACING_FRONT)
                    .setRequestedFps(60.0f)
                    .setRequestedPreviewSize(1920, 1080)
                    .setAutoFocusEnabled(true)
                    .build();
            cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {

                //A la création du preview de la caméra -> demander l'autorisation d'utiliser  la caméra
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(Mediatheque.this, new String[]{Manifest.permission.CAMERA}, RequestCameraPermissionId);
                            return;
                        }
                        cameraSource.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                }

                //A la destruction du preview de la caméra -> arrêter la caméra
                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    cameraSource.stop();
                }
            });

            //La détection de visages
            detector.setProcessor(new Detector.Processor<Face>() {
                @Override
                public void release() {

                }

                //Lorsque le détecteur de visages effectue ses détections, stockage des visages dans "faces" et lancement des threads
                //de manipulation des images
                @Override
                public void receiveDetections(Detector.Detections<Face> detections) {
                    faces = detections.getDetectedItems();
                    Log.v("Faces", Integer.toString(faces.size()));
                    runOnUiThread(runnableOrientationImages);
                    runOnUiThread(runnableTailleImages);
                    runOnUiThread(runnableNombreImages);
                }
            });

        }


        //Permet d'animer  les images en fonction de la position du visage de l'utilisateur
        runnableOrientationImages = new Runnable() {
            @Override
            public void run() {
                if (faces.size() != 0) {
                    tabCoordX[1] = tabCoordX[0];
                    //Permet d'avoir la coordonnée horizontale du centre du visage
                    tabCoordX[0] = faces.valueAt(0).getPosition().x
                            - ((faces.valueAt(0).getWidth()
                            - faces.valueAt(0).getPosition().x) / 2);
                    if (-tabCoordX[0] / 20 <= 25) {
                        for (int i = 0; i < listeImages.size(); i++) {
                            flip = ObjectAnimator.ofFloat(listeImages.get(i), "rotationY", -tabCoordX[1] / 20, -tabCoordX[0] / 20);
                            flip.setDuration(1);
                            flip.start();
                        }
                    }

                }
                else { }
            }
        };

        //Permet de faire varier la taille des images en fonction de la distance
        runnableTailleImages = new Runnable() {
            @Override
            public void run() {
                if(faces.size()!=0) {
                    ancienneDistance.setDistance(uneDistance.getDistance());
                    uneDistance.setDistance(1/faces.valueAt(0).getHeight() * 600);

                    //Lisse le changement de taille des images
                    uneDistance.setDistance(uneDistance.getDistance() + ALPHA * (uneDistance.getDistance()-ancienneDistance.getDistance()));

                    Log.v("Distance", Double.toString(uneDistance.getDistance()));
                    changerTaille(listeImages);
                }
            }
        };

        //Permet de changer le nombre d'images en fonction de la distance
        runnableNombreImages = new Runnable() {
            @Override
            public void run() {
                //Si 8 images
                if (listeImages.size() == 8) {
                    //Si images trop grandes -> retirer images
                    if (listeImages.get(0).getWidth() > (380)) {
                        mediasContainer.removeView(listeImages.get(6));
                        mediasContainer.removeView(listeImages.get(7));
                        listeImages.remove(forestgump);
                        listeImages.remove(sauverryan);
                    }
                }
                //Si 6 images
                else if (listeImages.size() == 6) {

                    //Si images trop petites -> ajouter images
                    if (listeImages.get(0).getWidth() < 380) {
                        ajouterImages(mediasContainer, listeImages, sauverryan, forestgump);
                    }
                }
            }
        };

        //Si click sur l'image -> lancement de l'activité "LecteurMedia"
        deadpool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent lecteurMediaActivity = new Intent(Mediatheque.this, LecteurMedia.class);
                startActivity(lecteurMediaActivity);
            }
        });
    }
}


