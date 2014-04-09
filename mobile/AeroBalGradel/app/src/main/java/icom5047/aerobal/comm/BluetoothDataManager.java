package icom5047.aerobal.comm;

import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import icom5047.aerobal.interfaces.AeroCallback;

/**
 * Created by enrique on 4/3/14.
 */
public class BluetoothDataManager extends AsyncTask<Void, String, String> {

    //Instance Fields
    private  AeroCallback callback;
    private  BluetoothSocket mSocket;
    private  InputStream mInputStream;
    private  OutputStream mOutputStream;


    public static final String RECEIVER_KEY = "recieverKey";


    //Special Constructor
    public BluetoothDataManager(BluetoothSocket btSocket, AeroCallback callback){
        //Set Callback
        this.callback = callback;

        //Set Socket
        mSocket = btSocket;


        InputStream tmpInputStream = null;
        OutputStream tmpOutputStream = null;

        try{
            tmpInputStream = btSocket.getInputStream();
            tmpOutputStream = btSocket.getOutputStream();
        }catch (IOException e){};

        mInputStream  = tmpInputStream;
        mOutputStream = tmpOutputStream;


    }




    @Override
    protected String doInBackground(Void... params) {
        Log.v("RunningLoop", "Works");
        loopReceiver();
        return null;
    }


    //Listening Loop
    private void loopReceiver() {

        BufferedReader br = new BufferedReader(new InputStreamReader(mInputStream));


        // Keep listening to the InputStream until an exception occurs
        while (true) {
            Log.v("Loop", "looping");
            try {
                String line = br.readLine();
                Log.v("Line Loop", line);
                publishProgress(line);
            } catch (IOException e) {
                //If Error
                break;
            }


        }





    }

    @Override
    protected void onProgressUpdate(String... values) {
       //Assume Value 1
       Log.v("ValueOnProgressUpdate", values[0]);
       //Callback Map
       Map<String, Object> map = new HashMap<String, Object>();
       map.put(RECEIVER_KEY, values[0]);
       callback.callback(map);

    }



    //Use same socket for ease
    public void send(String command, boolean addTerminator){

        if(addTerminator)
            command += "\r\n";

        try{
            mOutputStream.write(command.getBytes());
            mOutputStream.flush();
            //Flush
        }catch (IOException e){};
    }

    //Close Socket when Called


    @Override
    protected void onCancelled() {
        try {
            mSocket.close();
        } catch (IOException e) {};
    }
}
