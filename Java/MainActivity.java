package com.smartlock.smartlock;

//imports:
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice = null;
    InputStream mmInputStream;  //

    final byte delimiter = 33;
    int readBufferPosition = 0;
    TextView status_text;

    public void sendBtMsg(String msg2send){

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
        //UUID uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee"); //Standard SerialPortService ID

        try {

            //mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            mmSocket = mmDevice.createInsecureRfcommSocketToServiceRecord(uuid);

            if (!mmSocket.isConnected()){
                mmSocket.connect();
            }

            String msg = msg2send;
            //msg += "\n";
            OutputStream mmOutputStream = mmSocket.getOutputStream();
            mmOutputStream.write(msg.getBytes());

        }

        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        status_text = (TextView)findViewById(R.id.status_textview_main);
        status_text.setText("null status");
        final Handler handler = new Handler();
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        final Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        final ImageView lockImage = (ImageView) findViewById(R.id.lockImage_main);



        final class workerThread implements Runnable {

            private String btMsg;

            public workerThread(String msg) {
                btMsg = msg;
                Log.e("BTMsg = msg", "done");
            }

            // Run method

            public void run()
            {
                Log.e("run reached", "done.");
                sendBtMsg(btMsg);
                while(!Thread.currentThread().isInterrupted())
                {
                    int bytesAvailable;
                    boolean workDone = false;

                    try {



                        final InputStream mmInputStream;
                        mmInputStream = mmSocket.getInputStream();
                        bytesAvailable = mmInputStream.available();

                        if(bytesAvailable > 0)
                        {

                            byte[] packetBytes = new byte[bytesAvailable];
                            Log.e("smartlock recv bt", "bytes available");
                            byte[] readBuffer = new byte[1024];
                            mmInputStream.read(packetBytes);

                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    //The variable data now contains our full command
                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            status_text.setText(data);
                                        }
                                    });

                                    workDone = true;
                                    break;


                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }

                            if (workDone == true){
                                mmSocket.close();
                                break;
                            }

                        }
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }
        };


        //get status



        //Lock Button
        Button lockButton = (Button) findViewById(R.id.lock_button_main);
        lockButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {

                        /*-----------------------------------------------------------------*/
                        // add here all the code to send the request to the raspberry pi   //
                        /*-----------------------------------------------------------------*/

                        /*if(pairedDevices.size() > 0)
                        {
                            for(BluetoothDevice device : pairedDevices)
                            {
                                if(device.getName().equals("wright")) //raspberry pie name.
                                {
                                    Log.e("smartlock 101",device.getName());
                                    mmDevice = device;
                                    (new Thread(new workerThread("get!"))).start();
                                    //lockImage.setImageResource(R.drawable.locked);
                                    //lockImage.setColorFilter(Color.argb(255, 39, 196, 8));
                                    //status_text.setText("Status: LOCKED");
                                    break;
                                }
                                //else Toast.makeText(MainActivity.this, "Smartlock out of reach!", Toast.LENGTH_SHORT).show();
                            }
                        }*/

                        /*if (status_text.getText() == "locked!"){
                            Toast.makeText(MainActivity.this, "SmartLock already locked", Toast.LENGTH_SHORT).show();
                        }*/
                        if (pairedDevices.size() > 0)
                        {
                            for(BluetoothDevice device : pairedDevices)
                            {
                                if(device.getName().equals("wright")) //raspberry pie name.
                                {
                                    Log.e("smartlock 101",device.getName());
                                    mmDevice = device;
                                    (new Thread(new workerThread("lock!"))).start();
                                    lockImage.setImageResource(R.drawable.locked);
                                    lockImage.setColorFilter(Color.argb(255, 39, 196, 8));
                                    //status_text.setText("Status: LOCKED");
                                    break;
                                }
                                //else Toast.makeText(MainActivity.this, "Smartlock out of reach!", Toast.LENGTH_SHORT).show();
                            }
                        }
                        //(new Thread(new workerThread("lock!"))).start();

                    }
                }

        );


        //unlock Button
        Button unlockButton = (Button) findViewById(R.id.u_button_main);
        unlockButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {

                       // ImageView lockImage = (ImageView) findViewById(R.id.lockImage_main);

                        /*---------------------------------------------------------------------*/
                        // add here all the code to send the UNLOCK request to the raspberry pi//
                        /*---------------------------------------------------------------------*/

                        /*if(pairedDevices.size() > 0)
                        {
                            for(BluetoothDevice device : pairedDevices)
                            {
                                if(device.getName().equals("wright")) //raspberry pie name.
                                {
                                    Log.e("state get",device.getName());
                                    mmDevice = device;
                                    (new Thread(new workerThread("get!"))).start();
                                    //lockImage.setImageResource(R.drawable.locked);
                                    //lockImage.setColorFilter(Color.argb(255, 39, 196, 8));
                                    //status_text.setText("Status: LOCKED");
                                    break;
                                }
                                //else Toast.makeText(MainActivity.this, "Smartlock out of reach!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        if (status_text.getText() == "unlocked!"){
                            Toast.makeText(MainActivity.this, "SmartLock already unlocked", Toast.LENGTH_SHORT).show();
                        }*/
                        if(pairedDevices.size() > 0)
                        {
                            for(BluetoothDevice device : pairedDevices)
                            {
                                if(device.getName().equals("wright")) //raspberry pie name.
                                {
                                    Log.e("smartlock 101",device.getName());
                                    mmDevice = device;
                                    lockImage.setImageResource(R.drawable.unlocked);
                                    lockImage.setColorFilter(Color.argb(255, 207, 12, 29));
                                    //status_text.setText("Status: UNLOCKED");
                                    (new Thread(new workerThread("unlock!"))).start();
                                    break;
                                }
                            }
                        }

                    }
                }
        );

        //unlock Button
        Button statusButton = (Button) findViewById(R.id.status_button_main);
        statusButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {


                        if(pairedDevices.size() > 0)
                        {
                            for(BluetoothDevice device : pairedDevices)
                            {
                                if(device.getName().equals("wright")) //raspberry pie name.
                                {
                                    Log.e("status button check",device.getName());
                                    mmDevice = device;
                                    //lockImage.setImageResource(R.drawable.unlocked);
                                    //lockImage.setColorFilter(Color.argb(255, 207, 12, 29));
                                    //status_text.setText("Status: UNLOCKED");
                                    (new Thread(new workerThread("get!"))).start();
                                    break;
                                }
                            }
                        }

                    }
                }
        );


        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }



        //Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().equals("wright")) //raspberry pie name.
                {
                    Log.e("smartlock 101",device.getName());
                    mmDevice = device;
                    break;
                }
            }
        }

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
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
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.smartlock.smartlock/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.smartlock.smartlock/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

}
