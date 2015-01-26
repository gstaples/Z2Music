package com.lldogg.avrz2irp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Hashtable;


public class MainActivity extends ActionBarActivity {

    public static String port;
    public static String ipaddr;
    public static int statussleeptime;

    private final String z2pwron = "/MainZone/index.put.asp?cmd0=PutZone_OnOff/ON&cmd1=osThreadSleep/50&ZoneName=ZONE2";
    private final String z2pwroff = "/MainZone/index.put.asp?cmd0=PutZone_OnOff/OFF&ZoneName=ZONE2";

    private final String z2irpfav = "/NetAudio/index.put.asp?cmd0=PutZone_InputFunction/FAVORITES&cmd1=osThreadSleep/50&ZoneName=ZONE2";
    private final String z2irppand = "/NetAudio/index.put.asp?cmd0=PutZone_InputFunction/PANDORA&cmd1=osThreadSleep/100&ZoneName=ZONE2";

    private final String z2irpCurLeft = "/NetAudio/index.put.asp?cmd0=PutNetAudioCommand/CurLeft&ZoneName=ZONE2";

    private final String z2irpCurRight = "/NetAudio/index.put.asp?cmd0=PutNetAudioCommand/CurRight&ZoneName=ZONE2";
    private final String z2irpCurUp = "/NetAudio/index.put.asp?cmd0=PutNetAudioCommand/CurUp&cmd1=osThreadSleep/50&ZoneName=ZONE2";
    private final String z2irpCurDown = "/NetAudio/index.put.asp?cmd0=PutNetAudioCommand/CurDown&cmd1=osThreadSleep/50&ZoneName=ZONE2";

    private final String z2irpstop = "/NetAudio/index.put.asp?cmd0=PutNetAudioCommand/CmdStop&cmd1=osThreadSleep/50";

    private final String z2irpstatus = "/goform/formNetAudio_StatusXml.xml?&ZoneName=ZONE2";
    private final String z2irpVolMute = "/NetAudio/index.put.asp?cmd0=PutVolumeMute/TOGGLE&ZoneName=ZONE2";
    private final String z2irpVolDown = "/NetAudio/index.put.asp?cmd0=PutMasterVolumeBtn/<&ZoneName=ZONE2";
    private final String z2irpVolUp = "/NetAudio/index.put.asp?cmd0=PutMasterVolumeBtn/>&ZoneName=ZONE2";
    private final String z2irpVolSet = "/goform/formiPhoneAppVolume.xml?2+";


    public static final String mainZoneStatus = "/goform/formMainZone_MainZoneXml.xml?ZoneName=ZONE2";

    private showstatus backgroundtask;
    private Animation animAlpha2;
    private static Context context;
    //private AudioManager am;
    private SeekBar volumeControl = null;

    private boolean show_favorites_button;
    private boolean show_pandora_button;
    private boolean show_volume_controls;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        ipaddr = prefs.getString("pref_ipaddr", this.getString(R.string.pref_ipaddr_def));
        port = prefs.getString("pref_port", this.getString(R.string.pref_port_def));
        statussleeptime = Integer.parseInt(prefs.getString("pref_sleeptime", this.getString(R.string.pref_sleeptime_def)));


        animAlpha2 = AnimationUtils.loadAnimation(this, R.anim.anim_alpha);
        context = getApplicationContext();

        volumeControl = (SeekBar) findViewById(R.id.seek1);

        volumeControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChanged = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                progressChanged = progress;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                int newVolSet = progressChanged - 80;
                String cmd = makeUrl(z2irpVolSet + Integer.toString(newVolSet));
                new sendcmd().execute(cmd);
                System.out.printf(cmd);


            }
        });

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
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        if (id == R.id.action_about) {
            Intent AboutIntent = new Intent(this, AboutActivity.class);
            startActivity(AboutIntent);
            return true;
        }
         if (id == R.id.action_favorites) {
            action_favorites();
            return true;
        }
        if (id == R.id.action_pandora) {
            action_pandora();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.
        System.out.printf("~~~~STARTING~~~~\n");

        backgroundtask = new showstatus("http://" + ipaddr + ":" + port + z2irpstatus);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            backgroundtask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            backgroundtask.execute();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        show_favorites_button = prefs.getBoolean("pref_showfavorites", true);
        show_pandora_button = prefs.getBoolean("pref_showpandora", true);
        show_volume_controls = prefs.getBoolean("pref_showvolume", true);
        Button favbutton = (Button) findViewById(R.id.button_favorite);
        Button panbutton = (Button) findViewById(R.id.button_pandora);
        LinearLayout vollayout = (LinearLayout) findViewById(R.id.volLayout);

        if (show_favorites_button) {
            favbutton.setVisibility(View.VISIBLE);
        } else {
            favbutton.setVisibility(View.GONE);

        }
        if (show_pandora_button) {
            panbutton.setVisibility(View.VISIBLE);
        } else {
            panbutton.setVisibility(View.GONE);

        }
        //if (show_volume_controls) {
        //    vollayout.setVisibility(View.VISIBLE);
        //} else {
        //    vollayout.setVisibility(View.GONE);

        //}

    }

    @Override
    protected void onStop() {
        super.onStop();
        // The activity is no longer visible (it is now "stopped")
        System.out.printf("~~~~Cancelling~~~~\n");
        backgroundtask.cancel(true);
    }

    String makeUrl(String cmd) {
        return "http://" + ipaddr + ":" + port + cmd;
    }

    public void action_favorites() {

        new sendcmd().execute(makeUrl(z2pwron),
                makeUrl(z2irpfav),
                makeUrl(z2irpCurRight));
    }

    public void action_pandora() {

        new sendcmd().execute(makeUrl(z2pwron),
                makeUrl(z2irppand));
    }
    public void irp_favorites(View view) {

        view.startAnimation(animAlpha2);

        new sendcmd().execute(makeUrl(z2pwron),
                makeUrl(z2irpfav),
                makeUrl(z2irpCurRight));
    }

    public void irp_pandora(View view) {

        view.startAnimation(animAlpha2);

        new sendcmd().execute(makeUrl(z2pwron),
                makeUrl(z2irppand));
    }

    void irp_poweroff(View view) {
        view.startAnimation(animAlpha2);

        new sendcmd().execute(makeUrl(z2irpstop),
                makeUrl(z2pwroff));
    }
    void irp_power_toggle() {

        new sendcmd().execute(makeUrl(z2irpstop),
                makeUrl(z2pwroff));
    }
    void irp_poweron(View view) {
        view.startAnimation(animAlpha2);

        new sendcmd().execute(makeUrl(z2pwron));
    }

    // cursor direction commands
    public void irp_back(View view) {

        new sendcmd().execute(makeUrl(z2irpCurLeft));
    }

    public void irp_up(View view) {

        new sendcmd().execute(makeUrl(z2irpCurUp));
    }

    public void irp_down(View view) {

        new sendcmd().execute(makeUrl(z2irpCurDown));
    }

    public void irp_right(View view) {

        new sendcmd().execute(makeUrl(z2irpCurRight));
    }

    //FIXME: make this green when muted '<Mute><value>on</value></Mute>'
    public void irp_mute(View view) {

        new sendcmd().execute(makeUrl(z2irpVolMute));
    }
    public void irp_voldown(View view) {

        new sendcmd().execute(makeUrl(z2irpVolDown));
    }
    public void irp_volup(View view) {

        new sendcmd().execute(makeUrl(z2irpVolUp));
    }


    // asyntask to keep the output display updated
    private class showstatus extends AsyncTask<Void, Hashtable<String, String>, Integer> {
        final String url;

        public showstatus(String target) {
            url = target;
        }

        protected Integer doInBackground(Void... params) {
            InputStream stream;
            Hashtable<String, String> results = new Hashtable<>();

            while (!isCancelled()) {
                if ((!isConnectedViaWifi() && (!Build.PRODUCT.matches(".*_?sdk_?.*")))) {
                    results.put("OUTPUT", "Wifi is not enabled.");
                    publishProgress(results);
                    return (0);
                }
                Calendar c = Calendar.getInstance();
                int seconds = c.get(Calendar.SECOND);

                try {
                    Thread.sleep(statussleeptime);
                } catch (InterruptedException e) {
                }
                if (isCancelled()) {
                    results.put("OUTPUT", "Refreshing....");
                    publishProgress(results);
                    return (0);
                }
                
                //System.out.printf("%s (sleeptime: %d)\n", "calling status downloader", statussleeptime);

                try {
                    String cmdurl = makeUrl(mainZoneStatus);
                    System.out.printf("cmdurl: %s (sleeptime: %d)\n", cmdurl, statussleeptime);
                    URL u1 = new URL(cmdurl);
                    try {
                        stream = u1.openStream();
                    } catch (Exception e) {

                        results.put("OUTPUT", "Can't open stream to receiver. Please check your settings.\n");
                        publishProgress(results);
                        return(1);
                    }
                    results = MyXMLparser.parse(stream, MyXMLparser.XMLP_MAINSTATUS);
                    stream.close();
                    publishProgress(results);

                    if ((results.get("POWER") != null) && (results.get("POWER").equals("ON"))) {
                        URL u2 = new URL(url);
                        stream = u2.openStream();
                        results = MyXMLparser.parse(stream, MyXMLparser.XMLP_PLAYEROUTPUT);
                        stream.close();
                        publishProgress(results);

                    } else {
                        results.put("OUTPUT", "Power is Off\n\nHere's an idea...\n" +
                                "Push one of the giant buttons below!\n");
                        publishProgress(results);

                    }
                } catch (XmlPullParserException e) {
                    System.out.printf("%s", "XML Exception\n");
                    results.put("OUTPUT", "XML parse failed " + seconds);
                    publishProgress(results);
                } catch (IOException e) {
                    results.put("OUTPUT", "Unable to retrieve web page. Check the settings? Wifi enabled? Receiver on the network?");
                    publishProgress(results);
                }
            }
            return (1);
        }


        @SafeVarargs
        protected final void onProgressUpdate(Hashtable<String, String>... values) {
            TextView textView;
            TextView volView;
            Button buttonpower;
            ImageButton buttonmute;
            textView = (TextView) findViewById(R.id.edit_message);

            LinearLayout navLayout = (LinearLayout) findViewById(R.id.navLayout);
            LinearLayout volLayout = (LinearLayout) findViewById(R.id.volLayout);
            LinearLayout shortCutLayout = (LinearLayout) findViewById(R.id.shortCutLayout);


            // I don't want to maintain multiple layout xml files. That's obnoxious.
            // I should probably put all of this in a function or something, but meh.
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            final float density = getResources().getDisplayMetrics().density;

            final float width = size.x / density;
            final float height = size.y /density;
            //System.out.printf(" width: %f, height: %f, density: %f\n",width,height,density);

            boolean ShowVolumeOrNot;
            boolean ShowShortcutsOrNot;
            boolean IsVolumeCompact;
            boolean UseLargeText;

                // maybe
                if ((getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT) &&
                        (width < 600)) {
                    // small screen, portrait, compact volume allowed
                    ShowVolumeOrNot=true;
                    IsVolumeCompact=true;
                    UseLargeText=false;
                    ShowShortcutsOrNot=true;
                } else if ((getResources().getConfiguration().orientation== Configuration.ORIENTATION_LANDSCAPE) &&
                        (height < 600)) {
                    // small screen, landscape, full volume controls, but hide showrtcuts
                    ShowVolumeOrNot=true;
                    IsVolumeCompact=false;
                    UseLargeText=false;
                    ShowShortcutsOrNot=false;
                } else if ((getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT) &&
                        (width >= 600)) {
                    // larger screen, portrait, full volume controls
                    ShowVolumeOrNot=true;
                    IsVolumeCompact=false;
                    UseLargeText=true;
                    ShowShortcutsOrNot=true;
                } else {
                    // larger screen, landscape, full volume controls
                    ShowVolumeOrNot=true;
                    IsVolumeCompact=false;
                    UseLargeText=true;
                    ShowShortcutsOrNot=true;
                }

            if (!show_volume_controls) {
                //No
                ShowVolumeOrNot=false;
                IsVolumeCompact=false;

            }
            if (values[0].get("POWER") != null) {
                if (values[0].get("POWER").equals("ON")) {
                    // make button for power off

                    buttonpower = (Button) findViewById(R.id.button_power);
                    buttonmute = (ImageButton) findViewById(R.id.button_mute);

                    buttonpower.setBackgroundResource(R.drawable.power_on);
                    buttonpower.setEnabled(true);
                    buttonpower.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            irp_poweroff(v);
                        }
                    });

                    navLayout.setVisibility(View.VISIBLE);



                    if (ShowVolumeOrNot) {
                        Button volbutton1 = (Button) findViewById(R.id.button_volup);
                        Button volbutton2 = (Button) findViewById(R.id.button_voldown);

                        volLayout.setVisibility(View.VISIBLE);

                        if (IsVolumeCompact) {
                            volbutton1.setVisibility(View.GONE);
                            volbutton2.setVisibility(View.GONE);
                        } else {
                            volbutton1.setVisibility(View.VISIBLE);
                            volbutton2.setVisibility(View.VISIBLE);
                        }
                    } else {
                        volLayout.setVisibility(View.GONE);
                    }

                    if (ShowShortcutsOrNot) {
                        shortCutLayout.setVisibility(View.VISIBLE);
                    } else {
                        shortCutLayout.setVisibility(View.GONE);

                    }

                    if (values[0].get("MASTERVOLUME") != null) {
                        String volstring = values[0].get("MASTERVOLUME").replaceAll("\\s","");
                        int volume = Integer.parseInt(volstring);
                        volume += 80;

                        volView = (TextView) findViewById(R.id.vol_level);
                        volView.setText(Integer.toString(volume));
                        volumeControl.setProgress(volume);
                    }
                    if (values[0].get("MUTE") != null) {
                        if (values[0].get("MUTE").equals("off")) {
                            buttonmute.setImageResource(R.drawable.ic_action_volume_on);
                            //@drawable/ic_action_volume_on
                            //buttonmute.setTextColor(0xffffffff);
                        } else {
                            buttonmute.setImageResource(R.drawable.ic_action_volume_muted);

                            //buttonmute.setTextColor(0xff00ff00);

                        }
                    }

                } else if (values[0].get("POWER").equals("OFF")) {
                    // make button for power on

                    buttonpower = (Button) findViewById(R.id.button_power);
                    buttonpower.setBackgroundResource(R.drawable.power_off);
                    buttonpower.setEnabled(true);
                    buttonpower.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            irp_poweron(v);
                        }
                    });

                    navLayout.setVisibility(View.INVISIBLE);
                    volLayout.setVisibility(View.INVISIBLE);
                }

            } else if (values[0].get("OUTPUT") != null)  {

                boolean inMenu = values[0].get("OUTPUT").contains("Select one");
                setNavInMenu(inMenu);

                //System.out.printf("%s", values[0].get("OUTPUT"));
                if (UseLargeText) {
                    textView.setTextAppearance(context,R.style.Base_TextAppearance_AppCompat_Large);
                } else {
                    textView.setTextAppearance(context,R.style.Base_TextAppearance_AppCompat_Small);

                }
                textView.setText(values[0].get("OUTPUT"));
            }

        }

        protected void onPostExecute(Integer result) {

        }

    }

    void setNavInMenu(boolean inMenu) {
        Button buttonBack = (Button) findViewById(R.id.button_back);
        ImageButton buttonDown = (ImageButton) findViewById(R.id.button_down);
        ImageButton buttonUp = (ImageButton) findViewById(R.id.button_up);
        ImageButton buttonRight = (ImageButton) findViewById(R.id.button_right);
        if (inMenu) {


            buttonBack.setEnabled(false);
            buttonUp.setEnabled(true);
            buttonDown.setEnabled(true);
            buttonRight.setEnabled(true);
            buttonUp.setImageResource(R.drawable.ic_action_collapse);
            buttonDown.setImageResource(R.drawable.ic_action_expand);
            buttonRight.setImageResource(R.drawable.ic_action_next_item);

        } else {

            buttonBack.setEnabled(true);
            buttonUp.setEnabled(false);
            buttonDown.setEnabled(false);
            buttonRight.setEnabled(false);
            buttonUp.setImageResource(R.drawable.ic_action_collapse_inactive);
            buttonDown.setImageResource(R.drawable.ic_action_expand_inactive);
            buttonRight.setImageResource(R.drawable.ic_action_next_item_inactive);
        }

    }

    private class sendcmd extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls) {
            for (String url : urls) {

                try {
                    downloadUrl(url);
                } catch (IOException e) {
                    return "Unable to retrieve web page. URL may be invalid.";
                }
            }
            return "Success";
        }

        protected void onPostExecute(String result) {

        }


    }


    private void downloadUrl(String myurl) throws IOException {

        InputStream is = null;

        try {
            URL url = new URL(myurl);
            System.out.printf("Fetching: %s\n", url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(5000 /* milliseconds */);
            conn.setConnectTimeout(5000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            conn.getResponseCode();
            is = conn.getInputStream();

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }


    public static String getLocalWifiIpAddress() {
        WifiManager myWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (myWifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
            return "";
        }
        WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();
        DhcpInfo dhcp = myWifiManager.getDhcpInfo();
        int ipAddress = myWifiInfo.getIpAddress();

        return String.format("%d.%d.%d.%d/%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff),
                dhcp.netmask);
    }

    public static DhcpInfo getWifiDhcpInfo() {
        WifiManager myWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (myWifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
            return null;
        }

        return myWifiManager.getDhcpInfo();
    }

    private boolean isConnectedViaWifi() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }


}
