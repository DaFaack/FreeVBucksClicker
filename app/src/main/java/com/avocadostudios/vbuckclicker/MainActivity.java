package com.avocadostudios.vbuckclicker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.penta.games.handynummernvonyoutuber.R;

public class MainActivity extends AppCompatActivity {



    TextView zahlenfeld;
    int klickzahl;
    int normalbild, kleinbild, bildID;
    String zahlname;
    ImageView image;
    RelativeLayout relativeLayout;
    MediaPlayer mp;
    TextView plottwist;
    ImageView rewardButton;
    ImageView ratingButton;
    public RewardedVideoAd mAd;

    int gesamtklicks;
    public static int klicksound = R.raw.push;
    public static int backsound = R.raw.pull;
    public static int lotofklicks = R.raw.rewardsound;


    public static int clickzahl;


    public static int adMargin;

    public static boolean reward;
    public static String rewardtext;
    public static String ratingtext;

    public static long lastAdTime;

    public static int klicksForRating;
    public static int klicksForAd;

    public static boolean userRatedUs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.youtuber_layout);

        klicksForAd=3000;
        klicksForRating = 10000;

        rewardtext = "3,000 clicks have been added to your progress!";
        ratingtext = "10,000 clicks have been added to your progress!";

        if (internetAvailabel()) {
            adMargin = 150;
        } else {
            adMargin = 0;
        }


        gesamtklicks = 100000;


        SharedPreferences prefs = getSharedPreferences("werte", 0);
        clickzahl = prefs.getInt("clickzahl", gesamtklicks);
        userRatedUs = prefs.getBoolean("userRatedUs", false);






        bildID = R.id.image;


                klickzahl= clickzahl;
                zahlname="clickzahl";
                normalbild=R.drawable.buck;
                kleinbild=R.drawable.buckklein;





        //Initialisierung
        plottwist = (TextView)findViewById(R.id.plottwist);
        rewardButton = (ImageView)findViewById(R.id.rewardButton);
        ratingButton = (ImageView)findViewById(R.id.ratingButton);
        zahlenfeld = (TextView)findViewById(R.id.zahlenfeld);
        image = (ImageView)findViewById(bildID);


        firstRunDialog();
        click();
        rating();
        rewardAd();
        adListener();
        loadAd();
        ifGameIsOver();

        if(MainActivity.userRatedUs){
            ratingButton.setVisibility(View.INVISIBLE);
        }
    }



    public boolean internetAvailabel() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }


    public static void cleanUpMediaPlayer(MediaPlayer mp) {
        if (mp != null) {
            try {
                mp.stop();
                mp.release();
                mp = null;
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }

    public void firstRunDialog() {
        SharedPreferences prefs = getSharedPreferences("werte", 0);
        boolean b = prefs.getBoolean("firstrun", true);

        if (b) {
            AlertDialog.Builder a_builder = new AlertDialog.Builder(this);
            a_builder.setMessage(R.string.begruessungsText)
                    .setCancelable(true)
                    .setPositiveButton("I don‘t get it... ", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert = a_builder.create();
            alert.setTitle(R.string.begeruessungsTitle);
            alert.show();
        }


        SharedPreferences sh = getSharedPreferences("werte", 0);
        SharedPreferences.Editor editor = sh.edit();
        editor.putBoolean("firstrun", false);
        editor.commit();
    }

    public void ifGameIsOver(){
        ImageView normalbild = (ImageView)findViewById(bildID);
        if(klickzahl <= 0){
            normalbild.setVisibility(View.INVISIBLE);
            zahlenfeld.setVisibility(View.INVISIBLE);
            rewardButton.setVisibility(View.INVISIBLE);
            ratingButton.setVisibility(View.INVISIBLE);



            plottwist.setVisibility(View.VISIBLE);

        }
    }

    //Click auf den Kopf vom YouTuber
    public void click(){
        zahlenfeld.setText(klickzahl+"");

        image.setImageResource(normalbild);
        image.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        image.setImageResource(kleinbild);
                        if(klickzahl>0){
                            klickzahl--;
                        }

                        ifGameIsOver();

                        MainActivity.cleanUpMediaPlayer(mp);
                        mp = MediaPlayer.create(getApplication(), MainActivity.klicksound);
                        mp.start();

                        zahlenfeld.setText(klickzahl+"");
                        break;
                    case MotionEvent.ACTION_UP:


                        image.setImageResource(normalbild);

                        SharedPreferences prefs = getSharedPreferences("werte", 0);
                        SharedPreferences.Editor editor =prefs.edit();
                        editor.putInt(zahlname, klickzahl);
                        editor.commit();
                        break;
                }

                return true;
            }
        });
    }

    //Rating Button (Google Play Store)
    public void rating (){

        ratingButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        //Setzung des Startbildes vom Rating Button
                        ratingButton.setImageResource(R.drawable.rating3);

                        // Button Click Sound
                        MainActivity.cleanUpMediaPlayer(mp);
                        mp = MediaPlayer.create(getApplication(), MainActivity.klicksound);
                        mp.start();



                        break;

                    case MotionEvent.ACTION_UP:


                        ratingButton.setImageResource(R.drawable.ratingone);

                        if(internetAvailabel()){
                            //Intent zum Google Play store
                            String url = "https://play.google.com/store/apps/details?id=com.avocadostudios.youtuberinreallife&hl=de";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(url));
                            onPause();
                            startActivity(intent);



                            //Handler für verzögerung des Dialogs
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {


                                    //Dialog
                                    AlertDialog.Builder a_builder = new AlertDialog.Builder(MainActivity.this);
                                    a_builder.setMessage(R.string.ratingText)
                                            .setCancelable(false)
                                            .setPositiveButton("Claim your reward!", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int i) {

                                                    //Abzug und speicherung der Punkte
                                                    dialog.cancel();
                                                    Toast.makeText(getApplicationContext(), MainActivity.ratingtext, Toast.LENGTH_SHORT).show();
                                                    MainActivity.cleanUpMediaPlayer(mp);
                                                    mp = MediaPlayer.create(getApplication(), MainActivity.lotofklicks);
                                                    mp.start();
                                                    if(klickzahl>MainActivity.klicksForRating){
                                                        klickzahl = klickzahl-MainActivity.klicksForRating;
                                                    }else{
                                                        klickzahl=0;
                                                    }




                                                    ifGameIsOver();

                                                    zahlenfeld.setText(klickzahl+"");
                                                    SharedPreferences prefs = getSharedPreferences("werte", 0);
                                                    SharedPreferences.Editor editor =prefs.edit();
                                                    editor.putInt(zahlname, klickzahl);
                                                    editor.putBoolean("userRatedUs", true);
                                                    editor.commit();

                                                    MainActivity.userRatedUs=true;
                                                    ratingButton.setVisibility(View.INVISIBLE);

                                                }
                                            });

                                    AlertDialog alert = a_builder.create();
                                    alert.setTitle(R.string.ratingTitle);
                                    alert.show();


                                }

                                //Delay Zeit in Millisekunden
                            }, 1000);
                        }else{
                            Toast.makeText(getApplicationContext(), R.string.checkInternetConnection, Toast.LENGTH_SHORT).show();
                        }




                        break;
                }

                return true;
            }
        });





    }

    //RewardAd Button
    public void rewardAd(){


        MainActivity.reward=false;

        rewardButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        rewardButton.setImageResource(R.drawable.reward3);

                        MainActivity.cleanUpMediaPlayer(mp);
                        mp = MediaPlayer.create(getApplication(), MainActivity.klicksound);
                        mp.start();



                        if(mAd.isLoaded()){
                            startAd();
                        }else if(internetAvailabel()){
                            Toast.makeText(MainActivity.this, "Werbung noch nicht geladen, versuche es später erneut", Toast.LENGTH_SHORT).show();


                        }else{
                            Toast.makeText(MainActivity.this, "Check your internet connection!", Toast.LENGTH_SHORT).show();
                        }


                        break;
                    case MotionEvent.ACTION_UP:

                        rewardButton.setImageResource(R.drawable.rewardone);

                        break;
                }

                return true;
            }
        });





}

    //Anzeigen des Ads
    public void startAd(){
        mAd.show();
        MainActivity.lastAdTime=System.currentTimeMillis();
        loadAd();
    }

    //Laden des Ads und MarginTOP setzung
    public void adListener(){

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        relativeLayout = (RelativeLayout)findViewById(R.id.relativ);
        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        param.setMargins(0, MainActivity.adMargin, 0, 0);
        relativeLayout.setLayoutParams(param);
    }

    //Laden des Ads + AdListener
    public void loadAd(){

        AdRequest adRequest = new AdRequest.Builder().build();
        mAd = MobileAds.getRewardedVideoAdInstance(MainActivity.this);
        mAd.loadAd("ca-app-pub-8919538156550588/1410808346", adRequest);

        mAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {

            }

            @Override
            public void onRewardedVideoAdOpened() {

            }

            @Override
            public void onRewardedVideoStarted() {

            }

            @Override
            public void onRewardedVideoAdClosed() {
                if(MainActivity.reward){
                    MainActivity.reward=false;
                    Toast.makeText(getApplicationContext(), MainActivity.rewardtext, Toast.LENGTH_SHORT).show();
                    MainActivity.cleanUpMediaPlayer(mp);
                    mp = MediaPlayer.create(getApplication(), MainActivity.lotofklicks);
                    mp.start();
                    if(klickzahl>MainActivity.klicksForAd){
                        klickzahl = klickzahl-MainActivity.klicksForAd;
                    }else{
                        klickzahl=0;
                    }

                    zahlenfeld.setText(klickzahl+"");

                    ifGameIsOver();

                    SharedPreferences prefs = getSharedPreferences("werte", 0);
                    SharedPreferences.Editor editor =prefs.edit();
                    editor.putInt(zahlname, klickzahl);
                    editor.commit();
                    loadAd();
                }

            }

            @Override
            public void onRewarded(RewardItem rewardItem) {
                MainActivity.reward=true;



            }

            @Override
            public void onRewardedVideoAdLeftApplication() {
                loadAd();
            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {

            }
        });

    }

}

