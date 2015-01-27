package com.lldogg.avrz2irp;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;


public class NoNetwork extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_network);
        EditText status = (EditText) findViewById(R.id.no_network_status);
        status.setText(this.getString(R.string.no_network1) + "\n");
        status.append(this.getString(R.string.no_network2) + "\n");
        status.append(this.getString(R.string.no_network3) + "\n");
        status.append(this.getString(R.string.no_network4) + "\n");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_no_network, menu);
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
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        if (id == R.id.action_about) {
            Intent AboutIntent = new Intent(this, AboutActivity.class);
            startActivity(AboutIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
