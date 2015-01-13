package com.lldogg.avrz2irp;

import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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


public class MainActivity extends ActionBarActivity {

    String ipaddr = "192.168.42.107";
    String port = "80";
    String z2pwron = "/MainZone/index.put.asp?cmd0=PutZone_OnOff/ON&cmd1=osThreadSleep/50&ZoneName=ZONE2";
    String z2pwroff = "/MainZone/index.put.asp?cmd0=PutZone_OnOff/OFF&ZoneName=ZONE2";

    String z2irpfav = "/NetAudio/index.put.asp?cmd0=PutZone_InputFunction/FAVORITES&cmd1=osThreadSleep/50&ZoneName=ZONE2";
    String z2irppand = "/NetAudio/index.put.asp?cmd0=PutZone_InputFunction/PANDORA&cmd1=osThreadSleep/100&ZoneName=ZONE2";

    String z2irpCurLeft = "/NetAudio/index.put.asp?cmd0=PutNetAudioCommand/CurLeft&ZoneName=ZONE2";

    String z2irpCurRight = "/NetAudio/index.put.asp?cmd0=PutNetAudioCommand/CurRight&ZoneName=ZONE2";
    String z2irpCurUp = "/NetAudio/index.put.asp?cmd0=PutNetAudioCommand/CurUp&cmd1=osThreadSleep/50&ZoneName=ZONE2";
    String z2irpCurDown = "/NetAudio/index.put.asp?cmd0=PutNetAudioCommand/CurDown&cmd1=osThreadSleep/50&ZoneName=ZONE2";

    String z2irpstop = "/NetAudio/index.put.asp?cmd0=PutNetAudioCommand/CmdStop&cmd1=osThreadSleep/50";

    String z2irpstatus = "/goform/formNetAudio_StatusXml.xml?&ZoneName=ZONE2";

    int statussleeptime = 800;
    int runbackground = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    @Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.
        runbackground = 1;
        System.out.printf("~~~~STARTING~~~~\n");

        showstatus task;
        task = new showstatus("http://" + ipaddr + ":" + port + z2irpstatus);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            task.execute();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // The activity is no longer visible (it is now "stopped")
        System.out.printf("~~~~STOPPING~~~~\n");
        runbackground = 0;
    }


    public void irp_on(View view) {

        new sendcmd().execute("http://" + ipaddr + ":" + port + z2pwron,
                "http://" + ipaddr + ":" + port + z2irpfav,
                "http://" + ipaddr + ":" + port + z2irpCurRight);

    }

    public void irp_pandora(View view) {

        new sendcmd().execute("http://" + ipaddr + ":" + port + z2pwron,
                "http://" + ipaddr + ":" + port + z2irppand);
        //,
          //      "http://" + ipaddr + ":" + port + z2irpCurRight);

    }


    public void irp_off(View view) {

        new sendcmd().execute("http://" + ipaddr + ":" + port + z2irpstop,
                "http://" + ipaddr + ":" + port + z2pwroff);

    }

    public void irp_back(View view) {

        new sendcmd().execute("http://" + ipaddr + ":" + port + z2irpCurLeft);
    }

    public void irp_up(View view) {

        new sendcmd().execute("http://" + ipaddr + ":" + port + z2irpCurUp);
    }

    public void irp_down(View view) {

        new sendcmd().execute("http://" + ipaddr + ":" + port + z2irpCurDown);
    }

    public void irp_right(View view) {

        new sendcmd().execute("http://" + ipaddr + ":" + port + z2irpCurRight);
    }


    // asyntask to keep the output display updated
    private class showstatus extends AsyncTask<Void, String, Integer> {
        String url;

        public showstatus(String target) {
            url = target;
        }

        protected Integer doInBackground(Void... params) {
            InputStream stream;
            String output;

            while (runbackground == 1) {
                Calendar c = Calendar.getInstance();
                int seconds = c.get(Calendar.SECOND);

                try {
                    Thread.sleep(statussleeptime);
                } catch (InterruptedException e) {
                }
                if (runbackground == 0) {
                    output = "Refreshing....";
                    publishProgress(output);
                    return (runbackground);
                }
                System.out.printf("%s (sleeptime: %d)\n", "calling status downloader", statussleeptime);

                try {
                    URL u = new URL(url);
                    stream = u.openStream();
                    output = MyXMLparser.parse(stream);
                    stream.close();
                    publishProgress(output);
                } catch (XmlPullParserException e) {
                    System.out.printf("%s", "XML Exception\n");
                    output = "XML parse failed " + seconds;
                    publishProgress(output);
                } catch (IOException e) {
                    output = "Unable to retrieve web page. Wifi enabled? Receiver on the network?";
                    publishProgress(output);
                }
            }
            return (runbackground);
        }


        protected void onProgressUpdate(String... values) {
            TextView textView;
            textView = (TextView) findViewById(R.id.edit_message);

            System.out.printf("%s", values[0]);
            textView.setText(values[0]);
        }

        protected void onPostExecute(Integer result) {

        }

    }


    private class sendcmd extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls) {
            int count = urls.length;
            for (int i = 0; i < count; i++) {

                try {
                    downloadUrl(urls[i]);
                } catch (IOException e) {
                    return "Unable to retrieve web page. URL may be invalid.";
                }
            }
            return "Success";
        }

        protected void onPostExecute(String result) {

        }


    }


    private InputStream downloadUrl(String myurl) throws IOException {

        InputStream is = null;

        try {
            URL url = new URL(myurl);
            System.out.printf("Fetching: %s\n", url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            is = conn.getInputStream();
            return is;

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
}
