package com.devandroid.intelliplayer;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.util.concurrent.TimeUnit;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;

import Proxemic.Distance;
import Proxemic.ProxZone;

public class LecteurMedia extends Activity {


    //La camera utilisée par le face detector
    CameraSource cameraSource;

    //View réduite à 1dp de côté (invisible) requise pour l'utilisation du face detector
    SurfaceView cameraView;

    //Requis pour demander la permission d'utiliser la caméra
    final int RequestCameraPermissionId = 1001;

    //Renvoie vrai si la vidéo en cours de lecture est affichée en plein écran
    private boolean m_pleinEcran = true;

    //Le layout de l'activité
    ConstraintLayout mediaLayout;

    //Le chemin du fichier sur le stockage de l'appareil
    String path = Environment.getExternalStorageDirectory().getPath() + "/Deadpool2.mp4";

    //Les métadonnées de la vidéo
    TextView metadata;

    //La vidéo lue par le lecteur
    VideoView video;

    //Stocke les visages détéctés par la camera
    SparseArray<Face> faces;

    //Utilisé pour modifier le volume de l'appareil en fonction de la distance
    AudioManager audioManager;

    //Thread permettant de gérer l'affichage de la vidéo et des métadonnées
    Runnable changerAffichage;

    //Permet de définir les zones  proxémiques
    ProxZone proxzone;
    String zoneProxemique;

    //Siun utilisateur, la distance séparant l'utilisateur et l'appareil
    Distance distance;

    //Si deux utilisateurs, les distances séparant les utilisateurs et l'appareil
    Distance distanceVisageUn;
    Distance distanceVisageDeux;

    //Le volume de l'appareil
    int volume = 0;

    //Utilisés pour éviter que la vidéo ne se mette en pause suite à un bref défaut de détection
    int compteurPauseSeul;
    int compteurPauseDeux;

    //Utilisé pour éviter que l'appareil ne détecte qu'une personne suite à un défaut de détection
    boolean deuxPersonnes;


    //Renvoie true si la vidéo est en plein écran
    public boolean getPleinEcran() {

        return this.m_pleinEcran;
    }

    //Permet d'indiquer si la vidéo est en plein écran
    public void setPleinEcran(boolean pleinEcran) {

        this.m_pleinEcran = pleinEcran;
    }

    //Affiche la video en plein ecran
    public void affichePleinEcran(VideoView video) {
        ConstraintLayout mediaLayout = (ConstraintLayout) findViewById(R.id.mediaLayout);
        video.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.constrainHeight(video.getId(), ConstraintSet.WRAP_CONTENT);
        constraintSet.constrainWidth(video.getId(), ConstraintSet.WRAP_CONTENT);
        constraintSet.connect(video.getId(), ConstraintSet.TOP, mediaLayout.getId(), ConstraintSet.TOP, 0);
        constraintSet.connect(video.getId(), ConstraintSet.BOTTOM, mediaLayout.getId(), ConstraintSet.BOTTOM, 0);
        constraintSet.setVerticalBias(video.getId(), (float) 0.5);
        constraintSet.connect(video.getId(), ConstraintSet.LEFT, mediaLayout.getId(), ConstraintSet.LEFT, 0);
        constraintSet.connect(video.getId(), ConstraintSet.RIGHT, mediaLayout.getId(), ConstraintSet.RIGHT, 0);
        constraintSet.applyTo(mediaLayout);

    }

    //Affiche la video en taille reduite en haut à gauche de l'écran et baisse le volume à 50%
    public void affichePetiteTailleGauche(VideoView video) {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.constrainHeight(video.getId(), 1200);
        constraintSet.constrainWidth(video.getId(), 1200);
        constraintSet.connect(video.getId(), ConstraintSet.TOP, mediaLayout.getId(), ConstraintSet.TOP, 0);
        constraintSet.connect(video.getId(), ConstraintSet.LEFT, mediaLayout.getId(), ConstraintSet.LEFT, 0);
        constraintSet.applyTo(mediaLayout);
    }

    //Affiche la video en taille reduite en haut à droite de l'écran et baisse le volume à 50%
    public void affichePetiteTailleDroite(VideoView video) {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.constrainHeight(video.getId(), 1200);
        constraintSet.constrainWidth(video.getId(), 1200);
        constraintSet.connect(video.getId(), ConstraintSet.TOP, mediaLayout.getId(), ConstraintSet.TOP, 0);
        constraintSet.connect(video.getId(), ConstraintSet.RIGHT, mediaLayout.getId(), ConstraintSet.RIGHT, 0);
        constraintSet.applyTo(mediaLayout);
    }

    //Convertit les ms au format hh:mm:ss
    String convertirMs(long duree) {
        String dureeFormatee = String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(duree),
                TimeUnit.MILLISECONDS.toMinutes(duree) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duree)),
                TimeUnit.MILLISECONDS.toSeconds(duree) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duree)));
        return dureeFormatee;
    }

    //Rend les bytes "humman readable"
    String convertirBytes(long bytes) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    //Cherche les metadonnées de la video située dans path et les stocke dans metadata
    void chercherMetadata(TextView metadata, String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);
        String titre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String artiste = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        String annee = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR);
        String duree = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        String resolution = "";
        String nom = nomFichier(path);
        String taille = String.valueOf(convertirBytes(calculerTaille(path)));

        if (nom != null) {
            nom = "Titre : " + nom + "\n";
            metadata.setText(metadata.getText() + nom);
        }

        if (titre != null) {
            titre = "Titre : \n" + titre + "\n";
            metadata.setText(metadata.getText() + titre);
        }

        if (artiste != null) {
            artiste = "Artiste : \n" + artiste + "\n";
            metadata.setText(metadata.getText() + artiste);
        }

        if (annee != null) {
            annee = "Année : \n" + annee + "\n";
            metadata.setText(metadata.getText() + annee);
        }

        if (duree != null) {
            duree = "Duree : " + convertirMs(Long.parseLong(duree)) + "\n";
            metadata.setText(metadata.getText() + duree);
        }

        if (width != null && height != null) {
            resolution = "Résolution :" + width + "x" + height + "\n";
            metadata.setText(metadata.getText() + resolution);
        }

        if (taille != null) {
            taille = "Taille : " + taille + "\n";
            metadata.setText(metadata.getText() + taille);
        }
    }

    //Affiche les metadonnées (metadata) en haut à droite du layout mediaLayout
    void afficherMetadataDroite(ConstraintLayout mediaLayout, TextView metadata) {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.constrainHeight(metadata.getId(), ConstraintSet.WRAP_CONTENT);
        constraintSet.constrainWidth(metadata.getId(), ConstraintSet.WRAP_CONTENT);
        constraintSet.connect(metadata.getId(), ConstraintSet.RIGHT, mediaLayout.getId(), ConstraintSet.RIGHT, 0);
        constraintSet.connect(metadata.getId(), ConstraintSet.LEFT, mediaLayout.getId(), ConstraintSet.LEFT, 0);
        constraintSet.connect(metadata.getId(), ConstraintSet.TOP, mediaLayout.getId(), ConstraintSet.TOP, 40);
        constraintSet.setHorizontalBias(metadata.getId(), (float) 0.88);
        constraintSet.applyTo(mediaLayout);
    }

    //Affiche les metadonnées (metadata) en haut à gauche du layout mediaLayout
    void afficherMetadataGauche(ConstraintLayout mediaLayout, TextView metadata) {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.constrainHeight(metadata.getId(), ConstraintSet.WRAP_CONTENT);
        constraintSet.constrainWidth(metadata.getId(), ConstraintSet.WRAP_CONTENT);
        constraintSet.connect(metadata.getId(), ConstraintSet.RIGHT, mediaLayout.getId(), ConstraintSet.RIGHT, 0);
        constraintSet.connect(metadata.getId(), ConstraintSet.LEFT, mediaLayout.getId(), ConstraintSet.LEFT, 0);
        constraintSet.connect(metadata.getId(), ConstraintSet.TOP, mediaLayout.getId(), ConstraintSet.TOP, 40);
        constraintSet.setHorizontalBias(metadata.getId(), (float) 0.12);
        constraintSet.applyTo(mediaLayout);
    }

    // Retire les  métadonnées metadata du Layout mediaLayout
    void retirerMetadata(ConstraintLayout mediaLayout, TextView metadata) {
        metadata.setLayoutParams(new ConstraintLayout.LayoutParams(0, 0));
    }

    // Calcule la taille en bytes du fichier situé dans path
    long calculerTaille(String path) {
        File file = new File(path);
        long lenght = file.length();
        return lenght;
    }

    // Retourne le nom du fichier situé dans path
    String nomFichier(String path) {
        File file = new File(path);
        String name = file.getName();
        return name;
    }

    //Change le volume du lecteur en fonction de la zone proxémique
    void changerVolume(String zoneProxemique) {
        if (zoneProxemique == "intimiZone") {
            //Volume à 25%
            volume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 4;
        } else if (zoneProxemique == "personalZone") {
            //Volume à 50%
            volume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2;
        }
        //Volume à 75%
        else if (zoneProxemique == "socialZone") {
            volume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 4 * 3;
        }
        //Volume à 100%
        else if (zoneProxemique == "publicZone") {
            volume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        }

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
    }


    // Invoquée lors de  l'appel à la méthode ActivityCompat.requestPermissions(android.app.Activity, String[], int)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestCameraPermissionId: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    try {
                        cameraSource.start(cameraView.getHolder());
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
        setContentView(R.layout.activity_lecteur_media);

        //Initialisation des distances
        distance = new Distance();
        distanceVisageUn = new Distance();
        distanceVisageDeux = new Distance();

        //Définition des zones proxémiques utilisées
        proxzone = new ProxZone(0.25D, 0.45D, 1.0D, 2.0D);
        zoneProxemique = new String();

        //Le preview de la camera réduit à 1 pixel nécessaire pour utiliser la détection de visages
        cameraView = (SurfaceView) findViewById(R.id.cameraView);

        metadata = (TextView) findViewById(R.id.metadata);

        mediaLayout = (ConstraintLayout) findViewById(R.id.mediaLayout);

        video = (VideoView) findViewById(R.id.videoView);

        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        //Recherche les métadonnées de la vidéo
        chercherMetadata(metadata, path);

        //Association d'une barre de contrôle à la vidéo et lancement de la lecture
        MediaController mediaController = new MediaController(this);
        video.setVideoURI(Uri.parse(path));
        video.setMediaController(mediaController);
        affichePleinEcran(video);
        mediaController.setAnchorView(video);
        video.start();

        changerAffichage = new Runnable() {
            @Override
            public void run() {
                if (faces.size() == 1) {
                    if (getPleinEcran() == false) {
                        affichePleinEcran(video);
                        retirerMetadata(mediaLayout, metadata);
                        setPleinEcran(true);
                    }
                } else if (faces.size() == 2 && getPleinEcran() == true) {
                    distanceVisageUn.setfaceHeight(faces.valueAt(0).getHeight());
                    distanceVisageDeux.setfaceHeight(faces.valueAt(1).getHeight());
                    if (distanceVisageUn.getDistance() < distanceVisageDeux.getDistance()) {
                        if (distanceVisageUn.getDistance() <= 1) {
                            changerVolume("personnelle");
                            if (faces.valueAt(0).getPosition().x < 650) {
                                affichePetiteTailleGauche(video);
                                afficherMetadataDroite(mediaLayout, metadata);
                                setPleinEcran(false);
                            } else {
                                affichePetiteTailleDroite(video);
                                afficherMetadataGauche(mediaLayout, metadata);
                                setPleinEcran(false);
                            }
                        }
                    } else {
                        if (distanceVisageDeux.getDistance() <= 1) {
                            changerVolume("personnelle");
                            if (faces.valueAt(1).getPosition().x < 650) {
                                affichePetiteTailleGauche(video);
                                afficherMetadataDroite(mediaLayout, metadata);
                                setPleinEcran(false);
                            } else {
                                affichePetiteTailleDroite(video);
                                afficherMetadataGauche(mediaLayout, metadata);
                                setPleinEcran(false);
                            }
                        }

                    }
                }
            }
        };


        //L'objet permettant de détecter les visages
        FaceDetector detector = new FaceDetector.Builder(this)
                .setTrackingEnabled(true)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .build();
        if (!detector.isOperational()) {
            Log.w("MainActivity", "FaceDetector dependencies not available");
        } else {
            //Les paramètres de la camera
            cameraSource = new CameraSource.Builder(getApplicationContext(), detector)
                    .setFacing(CameraSource.CAMERA_FACING_FRONT)
                    .setRequestedFps(30.0f)
                    .setRequestedPreviewSize(1920, 1080)
                    .setAutoFocusEnabled(true)
                    .build();
            cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                //A la création du preview de la caméra -> demander l'autorisation d'utiliser  la caméra
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(LecteurMedia.this, new String[]{Manifest.permission.CAMERA}, RequestCameraPermissionId);
                            return;
                        }
                        cameraSource.start(cameraView.getHolder());
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

                //Lorsque le détecteur de visages effectue ses détections
                @Override
                public void receiveDetections(Detector.Detections<Face> detections) {

                    faces = detections.getDetectedItems();

                    //Lance le thread permettant de changer l'affichage de la video et des métadonnées
                    runOnUiThread(changerAffichage);
                    Log.v("NombreVisages", Integer.toString(compteurPauseSeul));

                    if (faces.size() == 1) {
                        compteurPauseSeul = 0;
                        if (compteurPauseDeux < 100) compteurPauseDeux++;
                        if (compteurPauseDeux == 100) deuxPersonnes = false;
                        //Lance la lecture si une personne regarde la vidéo
                        if (video.isPlaying() == false) video.start();

                        //Change le volume en fonction de la distance
                        distance.setfaceHeight(faces.valueAt(0).getHeight());
                        Log.v("Distance", Double.toString(distance.getDistance()));
                        zoneProxemique = proxzone.setDistanceofEntity(distance.getDistance());
                        changerVolume(zoneProxemique);
                        Log.v("Faces", "Coordonnées : " + faces.valueAt(0).getPosition());
                    }

                    else if (faces.size() == 0) {
                        //Met en pause la vidéo si  personne ne regarde
                        if (compteurPauseSeul < 30) compteurPauseSeul++;
                        if (compteurPauseSeul == 30) video.pause();
                        if (deuxPersonnes == true) video.pause();
                    }

                    else if (faces.size() == 2) {
                        compteurPauseDeux = 0;
                        deuxPersonnes = true;
                    }
                }

            });

        }
    }
}

