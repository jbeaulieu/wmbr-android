package com.jbproductions.wmbr;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.View;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import static com.jbproductions.wmbr.MediaPlayerService.ACTION_PREPARED;
import static com.jbproductions.wmbr.MediaPlayerService.ACTION_STOP;
import static com.jbproductions.wmbr.MediaPlayerService.ACTION_UNBIND;


public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawer;
    NavigationView navigationView;
    Toolbar toolbar;

    private static final int REQUEST_CALL_PHONE_PERMISSION = 429;
    public static final String PLAY_NEW_AUDIO = "com.jbproductions.wmbr.PLAY_NEW_AUDIO";
    Fragment mFragmentToSet = null;

    MediaPlayerService playerService;
    boolean serviceBound = false;
    Intent playerIntent;

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem menuItem) {
        // Handle navigation view item clicks
        switch (menuItem.getItemId()) {
            case R.id.nav_live_stream:
                mFragmentToSet = new StreamFragment();
                break;
            case R.id.nav_schedule:
                mFragmentToSet = new ScheduleFragment();
                break;
            case R.id.nav_archives:
                mFragmentToSet = new ArchiveFragment();
                break;
            case R.id.nav_event_cal:
                mFragmentToSet = new EventsFragment();
                break;
            case R.id.nav_contact:
                mFragmentToSet = new ContactFragment();
                break;
        }
        drawer.closeDrawer(navigationView);
        return true;
    }

    //Binding this Client to the AudioPlayer Service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            playerService = binder.getService();
            serviceBound = true;

            //Toast.makeText(NavigationActivity.this, "Service Bound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        playerIntent = new Intent(this, MediaPlayerService.class);

        BroadcastReceiver broadcastReceiver = new MediaPlayerBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PREPARED);
        intentFilter.addAction(ACTION_STOP);
        intentFilter.addAction(ACTION_UNBIND);
        registerReceiver(broadcastReceiver, intentFilter);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                super.onDrawerSlide(drawerView, 0); // disables the arrow at opened state
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, 0); // disables the animation
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {}
            @Override public void onDrawerOpened(@NonNull View drawerView) {}
            @Override public void onDrawerStateChanged(int newState) {}

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                //Set your new fragment here
                if (mFragmentToSet != null) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .replace(R.id.defaultLayout, mFragmentToSet)
                            .commitNow();
                    mFragmentToSet = null;
                }
            }
        });

        navigationView.setNavigationItemSelectedListener(this);

        // At the end of setup, load a Stream Fragment, since it functions as the main app screen
        getSupportFragmentManager().beginTransaction().replace(R.id.defaultLayout, new StreamFragment()).commitNow();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(navigationView)) {
            drawer.closeDrawer(navigationView);
        } else {
            super.onBackPressed();
        }
    }

    public void playAudio(String mediaSource, boolean isLiveStream) {
        //Check is service is active
        if (!serviceBound) {
            playerIntent.putExtra("mediaSource", mediaSource);
            playerIntent.putExtra("liveStream", isLiveStream);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            //Service is active
            //Send media with BroadcastReceiver
            Intent broadcastIntent = new Intent(PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
        }
    }

    public void stopAudio() {
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
            playerService.stopSelf();
        }
    }

    public boolean isPlaying() {
        if(serviceBound) {
            Log.d("SERVICE", Boolean.toString(serviceBound));
            return playerService.isPlaying();
        }
        else {
            return false;
        }
    }

    private class MediaPlayerBroadcastReceiver extends android.content.BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BROADCAST RECEIVED", intent.getAction());
            Toast.makeText(getApplicationContext(), intent.getAction(), Toast.LENGTH_SHORT).show();
            switch (intent.getAction()) {
                case ACTION_UNBIND:
                unbindService(serviceConnection);
                //Toast.makeText(getApplicationContext(), "UNBIND ACTIVITY", Toast.LENGTH_SHORT).show();
                serviceBound = false;
                playerService.stopSelf();
                Log.d("SERVICE", "UNBOUND");
                break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("PERMISSION GRANTED", Integer.toString(requestCode));
    }

    public void makeCall(View view) {
        String uri = null;

        switch(view.getTag().toString()) {
            case "request":
                uri = "tel:6172538810";
                break;
            case "business":
                uri = "tel:6172534000";
                break;
        }

        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse(uri));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(NavigationActivity.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PHONE_PERMISSION);
            return;
        }
        startActivity(intent);
    }


    public void locateWMBR(View view) {

        // The below is a hard-encoded URI of WMBR's physical (not mailing) address
        // This allows map applications to pull up a pin for WMBR at Walker Memorial
        Uri geolocation = Uri.parse("geo:0,0?q=WMBR%2088.1%20FM%2C%20142%20Memorial%20Drive%2C%20Cambridge%2C%20MA%2002142");

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geolocation);

        // Before passing the intent, we call resolveActivity make sure that there is at least one activity that can receive it
        // This handles the edge case where a user does not have a map application installed on the phone
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void sendEmail(View view) {

        String uri = null;

        switch(view.getTag().toString()) {
            case "music":
                uri = "mailto:music@wmbr.org";
                break;
            case "news":
                uri = "mailto:press@wmbr.org";
                break;
            case "psa":
                uri = "mailto:psa@wmbr.org";
                break;
            case "guide":
                uri = "mailto:guide@wmbr.org";
                break;
            case "webmaster":
                uri = "mailto:webmaster@wmbr.org";
                break;
            case "management":
                uri = "mailto:management@wmbr.org";
                break;
        }

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse(uri));
        startActivity(intent);
    }

    public void openFacebook(View view) {
        Intent intent;
        try {
            // Check if Facebook app is installed and if so, open it to WMBR's page
            this.getPackageManager().getPackageInfo("com.facebook.katana", 0);
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/22335947090"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } catch (Exception e) {
            // Facebook is not installed, open the browser instead
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/wmbrfm/"));
        }
        this.startActivity(intent);
    }

    public void openTwitter(View view) {
        Intent intent;
        try {
            // Check if Twitter app is installed and if so, open it to WMBR's page
            this.getPackageManager().getPackageInfo("com.twitter.android", 0);
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=wmbr"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } catch (Exception e) {
            // Twitter is not installed, open the browser instead
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/wmbr"));
        }
        this.startActivity(intent);
    }

    public void openInstagram(View view) {
        Intent intent;
        try {
            // Check if IG app is installed and if so, open it to WMBR's page
            this.getPackageManager().getPackageInfo("com.instagram.android", 0);
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("instagram://user?username=wmbrfm"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } catch (Exception e) {
            // IG is not installed, open the browser instead
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/wmbrfm/"));
        }
        this.startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            //service is active
            playerService.stopSelf();
        }
    }
}
