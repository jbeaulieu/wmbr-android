package com.jbproductions.wmbr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FragmentManager fragmentManager = getSupportFragmentManager();
    private static final int REQUEST_CALL_PHONE_PERMISSION = 429;
    private static final int REQUEST_SEND_SMS_PERMISSION = 501;
    private static final int REQUEST_SEND_EMAIL_PERMISSION = 394;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        HomeFragment homeFragment = new HomeFragment();
        fragmentManager.beginTransaction().replace(R.id.defaultLayout, homeFragment, homeFragment.getTag()).commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks
        int id = item.getItemId();

        if (id == R.id.nav_live_stream) {
            StreamFragment streamFragment = new StreamFragment();
            fragmentManager.beginTransaction().replace(R.id.defaultLayout, streamFragment, streamFragment.getTag()).commit();
        } else if (id == R.id.nav_schedule) {
            ScheduleFragment scheduleFragment = new ScheduleFragment();
            fragmentManager.beginTransaction().replace(R.id.defaultLayout, scheduleFragment, scheduleFragment.getTag()).commit();
        } else if (id == R.id.nav_archives) {
            ArchiveFragment archiveFragment = new ArchiveFragment();
            fragmentManager.beginTransaction().replace(R.id.defaultLayout, archiveFragment, archiveFragment.getTag()).commit();
        } else if (id == R.id.nav_event_cal) {
            EventsFragment eventsFragment = new EventsFragment();
            fragmentManager.beginTransaction().replace(R.id.defaultLayout, eventsFragment, eventsFragment.getTag()).commit();
        } else if (id == R.id.nav_contact) {
            ContactFragment contactFragment = new ContactFragment();
            fragmentManager.beginTransaction().replace(R.id.defaultLayout, contactFragment, contactFragment.getTag()).commit();
        } else if (id == R.id.nav_settings) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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
}
