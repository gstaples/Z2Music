package com.lldogg.avrz2irp;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class AboutActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }


    @Override
    protected void onStart() {
        super.onStart();

        String version;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
            version = pInfo.versionName + " (versionCode: " + Integer.toString(pInfo.versionCode)+ ")";
        } catch (PackageManager.NameNotFoundException e1) {
            version = "Unknown";
        }

        TextView about_output = (TextView) findViewById(R.id.about_output);
        about_output.setText(this.getString(R.string.app_name) + " is written by Garrick Staples, garrick.staples@gmail.com, Copyright 2015.\n");
        about_output.append("This is version " + version +".\n");
        about_output.append("\n" + this.getString(R.string.app_name) + " is for very quickly playing Internet Radio and Pandora on Zone2 of Marantz and Denon receivers. It does not replace the full-featured official apps or AVR-Remote. It was extensively tested with a Marantz NR-1605. Please let me know if this app works for your receiver.\n");
        about_output.append("\n" + this.getString(R.string.app_name) + " requires your receiver's IP in the settings. Please use the built-in scanner to find it. (Seriously, use the scanner. It took me 3 days to write the damn thing just so you don't have to type an IP address!)\n");
        about_output.append("\n" + this.getString(R.string.app_name) + " and Garrick Staples are not affiliated with Marantz, Denon, D+M Group, or Pandora in any way. I assume all of those capitalized names are copyrighted and/or trademarked by their respective owners, or not, IANAL.");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_about, menu);
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
}
