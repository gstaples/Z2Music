package com.lldogg.avrz2irp;

import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


public class ReceiverScanner extends ActionBarActivity {
    private ProgressBar spinner;
    private TextView scan_output;
    private LinearLayout scannerLayout;
    private Context context;
    private static int currentIP = 0;
    @SuppressWarnings("CanBeFinal")
    private static ArrayList<String> servers = new ArrayList<>();
    private scan scantask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.printf("onCreate(): called\n");

        setContentView(R.layout.activity_marantz_scanner);

        scan_output = (TextView) findViewById(R.id.scan_output);

        spinner = (ProgressBar) findViewById(R.id.progressBar1);
        spinner.setVisibility(View.GONE);

        context = getApplication();

        scannerLayout = (LinearLayout) findViewById(R.id.scannerlayout);


    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.printf("onStart(): called\n");

        scan_output = (TextView) findViewById(R.id.scan_output);
        spinner = (ProgressBar) findViewById(R.id.progressBar1);
        context = getApplication();
        scannerLayout = (LinearLayout) findViewById(R.id.scannerlayout);


        spinner.setVisibility(View.VISIBLE);
        scan_output.setText("Scanning...\n");

        scantask = new scan();
        scantask.execute();

    }
    @Override
    protected void onStop() {
        super.onStop();
        System.out.printf("onStop(): called\n");
        scantask.cancel(true);
    }
    @Override
    protected void onPause() {
        super.onPause();
        System.out.printf("onPause(): called\n");

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.printf("onDestroy(): called\n");
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        System.out.printf("onRestart(): called\n");

    }
    @Override
    protected void onResume() {
        super.onResume();
        System.out.printf("onResume(): called\n");


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_marantz_scanner, menu);
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

    private class scan extends AsyncTask<Void, String, List<String>> {
        protected List<String> doInBackground(Void... params) {
            Hashtable<String, String> results = null;

            // Get the IP of the local machine
            if (isCancelled()) {
                return servers;
            }


            DhcpInfo dhcp;
            int myIPAddr;
            int myNetmaskbits;
            int networkbits;
            System.out.printf("doInBackground started.\n");

            dhcp = MainActivity.getWifiDhcpInfo();
            if (dhcp == null) {
                return null;
            }
            myIPAddr = dhcp.ipAddress;
            myNetmaskbits = dhcp.netmask;
            networkbits = myIPAddr & myNetmaskbits;
            int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
            int firstIP = IPincr(networkbits);
            int lastIP = IPdecr(broadcast);
/*
            for (int i=0; i<100; i++) {
                firstIP = IPincr(firstIP);
            }
            for (int i=0; i<130; i++) {
                lastIP = IPdecr(lastIP);
            }
*/
            if (currentIP != 0) {
                firstIP = currentIP;
            }
            System.out.printf("%s %s\n", "Found local IP:", IPinttostring(myIPAddr));
            System.out.printf("%s %s\n", "Found local netmask:", IPinttostring(myNetmaskbits));
            System.out.printf("%s %s\n", "Found local network:", IPinttostring(networkbits));
            System.out.printf("%s %s\n", "Found local broadcast:", IPinttostring(broadcast));
            System.out.printf("%s %s\n", "Found firstIP:", IPinttostring(firstIP));
            System.out.printf("%s %s\n", "Found lastIP:", IPinttostring(lastIP));

            // Loop to scan each address on the local subnet
            for (int testip = firstIP; testip != lastIP; testip = IPincr(testip)) {
                if (isCancelled()) {
                    return servers;
                }
                String output = "";
                InputStream stream;
                currentIP = testip;

                String ipaddr = IPinttostring(testip);
                System.out.printf("%s %s\n", "Testing:", ipaddr);

                try {
                    String mainZoneStatus = "http://" + ipaddr + ":" + MainActivity.port + MainActivity.mainZoneStatus;
                    URLConnection u1 = new URL(mainZoneStatus).openConnection();
                    u1.setConnectTimeout(200);
                    u1.setReadTimeout(200);
                    //URL u1 = new URL("http://" + ipaddr + ":" + MainActivity.port + MainActivity.mainZoneStatus);
                    stream = u1.getInputStream();
                    results = MyXMLparser.parse(stream, MyXMLparser.XMLP_FRIENDLYNAME);
                    stream.close();

                } catch (IOException e) {
                    //System.out.printf("%s %s\n", "IO Exception", ipaddr);
                } catch (XmlPullParserException e) {
                    System.out.printf("%s %s\n", "XML Exception", ipaddr);
                }
                if ((results != null) && (results.get("FRIENDLYNAME") != null)) {
                    output = results.get("FRIENDLYNAME");
                }

                if (output.length() > 0) {
                    System.out.printf("Found %s at %s!\n", output, ipaddr);
                    publishProgress(output, ipaddr);
                    if (!servers.contains(ipaddr)) {
                        servers.add(ipaddr);
                    }
                }
                results=null;
            }
            return servers;

        }

        protected void onProgressUpdate(String... values) {
            scan_output.append("Found " + values[0] + " at " + values[1] + "...\n");
        }

        protected void onPostExecute(List<String> results) {
            spinner.setVisibility(View.GONE);

            //FIXME: need to set both the friendlyname and the IP in servers list
            if (servers == null) {
                scan_output.setText("Scan failed. Is Wifi enabled?\n");
            } else if (servers.isEmpty()) {
                scan_output.setText("No Receivers found.\n");
            } else {
                scan_output.append("Scan complete.\n");
                scan_output.append("Select a receiver:\n");
                for (String ipaddr : servers) {
                    System.out.printf("Making button for IPaddr: %s\n", ipaddr);

                    Button b1 = new Button(context);
                    b1.setText(ipaddr);
                    b1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Button b = (Button) v;
                            String buttonText = b.getText().toString();
                            System.out.printf("button text: %s\n", buttonText);

                            MainActivity.ipaddr = buttonText;

                            ReceiverScanner.currentIP = 0;

                            // send the ipaddr back to settingsactivity
                            Intent intent = new Intent();
                            intent.putExtra("ipaddr", buttonText);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    });
                    scannerLayout.addView(b1);

                }
            }


        }
    } // END scan class


    String IPinttostring(int ipAddress) {
        return String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
    }

    int IPincr(int ipAddress) {
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) (ipAddress >> (k * 8) & 0xff);

        if (quads[3] == 255) {
            quads[3] = 0;
            if (quads[2] == 255) {
                quads[2] = 0;
                if (quads[1] == 255) {
                    quads[1] = 0;
                    if (quads[0] == 255) {
                        return -1;
                    } else {
                        quads[0]++;
                    }
                } else {
                    quads[1]++;
                }
            } else {
                quads[2]++;
            }
        } else {
            quads[3]++;
        }

        return ((quads[3] << 24) & 0xff000000) |
                ((quads[2] << 16) & 0xff0000) |
                ((quads[1] << 8) & 0xff00) |
                (quads[0] & 0xff);

    }

    int IPdecr(int ipAddress) {
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) (ipAddress >> (k * 8) & 0xff);

        if (quads[3] == 0) {
            quads[3] = (byte) 255;
            if (quads[2] == 0) {
                quads[2] = (byte) 255;
                if (quads[1] == 0) {
                    quads[1] = (byte) 255;
                    if (quads[0] == 0) {
                        return -1;
                    } else {
                        quads[0]--;
                    }
                } else {
                    quads[1]--;
                }
            } else {
                quads[2]--;
            }
        } else {
            quads[3]--;
        }
        return ((quads[3] << 24) & 0xff000000) |
                ((quads[2] << 16) & 0xff0000) |
                ((quads[1] << 8) & 0xff00) |
                (quads[0] & 0xff);

    }
}
