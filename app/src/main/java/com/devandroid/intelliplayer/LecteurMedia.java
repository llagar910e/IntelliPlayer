package com.devandroid.intelliplayer;

import android.app.Activity;
import android.content.Context;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class LecteurMedia extends Activity {

    // DONNEES MEMBRES
    private boolean m_pleinEcran = true;

    // METHODES
    public boolean getPleinEcran(){

        return this.m_pleinEcran;
    }

    public void setPleinEcran(boolean pleinEcran){

        this.m_pleinEcran = pleinEcran;
    }

    //Affiche la video en plein ecran
    public void affichePleinEcran(VideoView video){
        ConstraintLayout mediaLayout = (ConstraintLayout) findViewById(R.id.mediaLayout);
        video.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.constrainHeight(video.getId(), ConstraintSet.WRAP_CONTENT);
        constraintSet.constrainWidth(video.getId(), ConstraintSet.WRAP_CONTENT);
        constraintSet.connect(video.getId(), ConstraintSet.TOP, mediaLayout.getId(), ConstraintSet.TOP, 0);
        constraintSet.connect(video.getId(), ConstraintSet.BOTTOM, mediaLayout.getId(), ConstraintSet.BOTTOM, 0);
        constraintSet.setVerticalBias(video.getId(), (float) 0.5);
        constraintSet.applyTo(mediaLayout);

    }

    //Affiche la video en taille reduite
    public void affichePetiteTaille(VideoView video, String path){
        video.setLayoutParams(new ConstraintLayout.LayoutParams(1200, 1200));
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
        int unit =  1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre =  "KMGTPE".charAt(exp-1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    //Cherche les metadonnées de la video située dans path et les stocke dans metadata
    void chercherMetadata(TextView metadata, String path){
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
            metadata.setText(metadata.getText()+nom);
        }

        if (titre != null) {
            titre = "Titre : \n" + titre + "\n";
            metadata.setText(metadata.getText()+titre);
        }

        if (artiste != null) {
            artiste = "Artiste : \n" + artiste + "\n";
            metadata.setText(metadata.getText()+artiste);
        }

        if (annee != null) {
            annee = "Année : \n" + annee + "\n";
            metadata.setText(metadata.getText()+annee);
        }

        if (duree != null) {
            duree = "Duree : " + convertirMs(Long.parseLong(duree)) + "\n";
            metadata.setText(metadata.getText()+duree);
        }

        if (width != null && height != null){
            resolution = "Résolution :" + width + "x" + height + "\n";
            metadata.setText(metadata.getText()+resolution);
        }

        if (taille != null){
            taille = "Taille : " + taille + "\n";
            metadata.setText(metadata.getText()+taille);
        }
    }

    //Affiche les metadonnées metadata en haut à droite du layout mediaLayout
    void afficherMetadata(ConstraintLayout mediaLayout, TextView metadata) {

        metadata.setLayoutParams(new ConstraintLayout.LayoutParams(500, 500));

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.constrainHeight(metadata.getId(), ConstraintSet.WRAP_CONTENT);
        constraintSet.constrainWidth(metadata.getId(), ConstraintSet.WRAP_CONTENT);
        constraintSet.connect(metadata.getId(), ConstraintSet.RIGHT, mediaLayout.getId(), ConstraintSet.RIGHT, 0);
        constraintSet.connect(metadata.getId(), ConstraintSet.LEFT, mediaLayout.getId(), ConstraintSet.LEFT, 0);
        constraintSet.connect(metadata.getId(), ConstraintSet.TOP, mediaLayout.getId(), ConstraintSet.TOP, 40);
        constraintSet.setHorizontalBias(metadata.getId(), (float) 0.88);
        constraintSet.applyTo(mediaLayout);
    }

    // Retire les  métadonnées metadata du Layout mediaLayout
    void retirerMetadata(ConstraintLayout mediaLayout, TextView metadata){
        metadata.setLayoutParams(new ConstraintLayout.LayoutParams(0, 0));
    }

    // Calcule la taille en bytes du fichier situé dans path
    long calculerTaille(String path){
        File file = new File(path);
        long lenght = file.length();
        return lenght;
    }

    // Retourne le nom du fichier situé dans path
    String nomFichier (String path){
        File file = new File(path);
        String name = file.getName();
        return name;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecteur_media);


        Button boutonMetadata = (Button) findViewById(R.id.boutonMetadata);
        VideoView video = (VideoView) findViewById(R.id.videoView);
        String path = Environment.getExternalStorageDirectory().getPath() + "/Deadpool2.mkv";

        TextView metadata = (TextView)findViewById(R.id.metadata);
        chercherMetadata(metadata,path);

        MediaController mediaController = new MediaController(this);
        video.setVideoURI(Uri.parse(path));
        video.setMediaController(mediaController);
        affichePleinEcran(video);
        video.start();

        boutonMetadata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ConstraintLayout mediaLayout = (ConstraintLayout) findViewById(R.id.mediaLayout);
                VideoView video = (VideoView) findViewById(R.id.videoView);
                String path = Environment.getExternalStorageDirectory().getPath() + "/Deadpool2.mkv";
                TextView metadata = (TextView)findViewById(R.id.metadata);

                if (getPleinEcran()==true) {
                    affichePetiteTaille(video,path);
                    afficherMetadata(mediaLayout,metadata);
                    setPleinEcran(false);
                }
                else {
                    affichePleinEcran(video);
                    retirerMetadata(mediaLayout, metadata);
                    setPleinEcran(true);
                }
            }
        });

    }
}
