package com.wifianalyseur2.fr.wifianalyseur2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    TextView m_affichage = null;
    ImageView m_imgCroix = null;
    ImageView m_imgIUT = null;
    TextView m_textViewListWifi = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_affichage = (TextView)findViewById(R.id.textViewAffichage);           //Associe une variable à l'objet physique qu'elle représente. Affiche X et Y.
        m_textViewListWifi = (TextView)findViewById(R.id.textViewListeWifi);
        m_imgCroix = (ImageView)findViewById(R.id.imageViewCroix);
        m_imgIUT = (ImageView)findViewById(R.id.imageViewIUT);
        m_imgIUT.setOnTouchListener(event);                                     //Lance la fonction "event" au toucher de l'image
    }

    /**
     * Fonction qui se lance au toucher de l'image
     */
    public View.OnTouchListener event = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            final WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            /**
             * Vérification de l'activation du wifi. Dans le cas contraire, affichage d'un toast
             */
            if(wifi.isWifiEnabled()) {
                int m_touchX = (int) event.getX();
                int m_touchY = (int) event.getY();
                m_affichage.setText("X : " + m_touchX + " ; Y : " + m_touchY);
                m_imgCroix.setVisibility(View.INVISIBLE);
                placeCroix(m_touchX, m_touchY);             // Appel de la fonction placeCroix

                registerReceiver(new BroadcastReceiver()
                {
                    @Override
                    public void onReceive(Context c, Intent intent)
                    {
                        List<ScanResult> results = wifi.getScanResults();
                        int rssi = 100;
                        String mac = "";
                        int channel = -1;

                        for (ScanResult s : results)    {
                            if(s.SSID.equals("Etudiants-Paris12")&Math.abs(s.level)<rssi){
                                rssi = Math.abs(s.level);
                                mac = s.BSSID;
                                channel = s.frequency;
                            }
                            DecimalFormat df = new DecimalFormat("#.##");       //Permet de définir un format décimal
                            m_textViewListWifi.setText("Etudiants-Paris12" + "\n" + "BSSID : "+ "\n" + mac + "\n" + "RSSI : " + rssi + " dB" + "\n" + "Distance : " + df.format(calculateDistance((double)rssi, channel)) + " m" + "\n" + "Canal : " + channel + " MHz"); //Concatener les informations wifi récupérées par le wifi manager

                        }
                    }
                }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

                wifi.startScan();

            } else {
               Toast.makeText(MainActivity.this, "WiFi non actif", Toast.LENGTH_SHORT).show();      //Affiche un toast si le wifi n'est pas activé
            }

            return false;
        }
    };

    /**
     * Fonction permettant de placer une image de croix à l'endroit du clic
     * @param X = position x du clic
     * @param Y = position y du clic
     */
    private void placeCroix (int X, int Y) {
        int touchX = (int) X;
        int touchY = (int) Y;
        // mettre croix milieu du touch
        int viewWidth = m_imgCroix.getWidth();
        int viewHeight = m_imgCroix.getHeight();
        viewHeight = viewHeight/2;
        viewWidth = viewWidth/2;
        RelativeLayout.LayoutParams r = (RelativeLayout.LayoutParams )m_imgCroix.getLayoutParams();
        r.setMargins(touchX - viewWidth, touchY - viewHeight,0,0);
        m_imgCroix.setVisibility(View.VISIBLE);
    }

    /**
     * Fonction permettant de calculer la distance entre le téléphone et le routeur
     * @param levelInDb = RSSI du réseau wifi (puissance du signal en Db)
     * @param freqInMHz = Fréquence du réseau wifi (channel du signal en MHz)
     * @return = La distance en mètre
     */
    public double calculateDistance(double levelInDb, double freqInMHz)    {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(levelInDb)) / 20.0;
        return Math.pow(10.0, exp);
    }
}

