package com.andrewtorr.highfive;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.andrewtorr.highfive.Models.Encrypter;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends ActionBarActivity {
    @Bind(R.id.get_lvl3)
    ImageView get_lvl3;
    @Bind(R.id.level_two)
    ScrollView level_two;
    @Bind(R.id.hand)
    ImageView hand;

    private String TAG = "main activity";
    private SensorManager sm;
    private float mAccel = 0;
    private float mAccelLast = 0;
    private float mAccelCurrent = 0;

    private boolean isClapping = false;

    MediaPlayer mp;
    int fiveCount = 0;
    int level = 1;
    byte[] settingsData;
    JSONObject settings = new JSONObject();
    boolean earth = false;
    boolean air = false;
    boolean water = false;
    boolean fire = false;

    private final SensorEventListener sel = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent se) {
            float x = se.values[0];
            float y = se.values[1];
            float z = se.values[2];
            Log.d("Acceleration", "x: " + x + ", y: " + y + ", z: " + z);
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;

            if (mAccel > 9) {
                if (!isClapping) {
                    mp = MediaPlayer.create(getApplicationContext(), R.raw.clap);
                    mp.start();

                    isClapping = true;
                    ++fiveCount;

                    if (level == 1 && fiveCount >= 5) {
                        level_two.setVisibility(View.VISIBLE);
                        level_two.scrollTo(0,0);
                        levelUp();
                    } else if (level == 3 && fiveCount >= 25) {
                        levelUp();
                    }

                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            isClapping = false;
                        }
                    }, 1000);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {}
    };

    @OnClick(R.id.get_lvl3)
    public void getLvl3() {
        earth = true;
        Toast.makeText(getApplicationContext(), "Earth acquired", Toast.LENGTH_LONG).show();
        level_two.setVisibility(View.GONE);
        hand.setImageResource(R.drawable.dirtyhand);

        levelUp();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sm.registerListener(sel, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        settingsData = readSavedData();
        if (settingsData != null) {
            try {
                settings = new JSONObject(new String(settingsData));
            } catch (Exception e) {
                Log.e("Settings read error", e.getLocalizedMessage());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void writeData(byte[] data) {
        try {
            FileOutputStream fOut = openFileOutput("settings.dat", MODE_PRIVATE);
            fOut.write(Encrypter.encrypt(data));
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] readSavedData() {
        //StringBuffer datax = new StringBuffer("");
        byte[] data;
        try {
            FileInputStream fIn = openFileInput("settings.dat");
            BufferedInputStream buffStream = new BufferedInputStream(fIn);
            data = IOUtils.toByteArray(buffStream);
            return Encrypter.decrypt(data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        Log.d("Activity", "destroying...");
        super.onDestroy();
    }

    private void levelUp() {
        ++level;
        Log.d("Level up", "level " + level + " reached!");
        Toast.makeText(getApplicationContext(), "Level " + level + " Unlocked!", Toast.LENGTH_LONG).show();
    }
}