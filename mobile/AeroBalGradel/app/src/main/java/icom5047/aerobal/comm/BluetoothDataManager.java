package icom5047.aerobal.comm;

import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import icom5047.aerobal.callback.AeroCallback;

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
    public BluetoothDataManager(BluetoothSocket btSocket){
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

    //Sender Command
    public void send(String command, boolean addTerminator){

        if(addTerminator)
            command += "\r\n";

        try{
            mOutputStream.write(command.getBytes());
            mOutputStream.flush();

            //Flush
        }catch (IOException e){};
        try{
            Thread.sleep(300);
        } catch (Exception e){
            e.printStackTrace();
        }
    }



    //Callback Setter
    public void setCallback(AeroCallback callback){
        this.callback = callback;
    }

    public boolean isCallbackSet(){
        return this.callback != null;
    }




    @Override
    protected String doInBackground(Void... params) {
        loopReceiver();
        return null;
    }


    //Listening Loop
    private void loopReceiver() {
        BufferedReader br = new BufferedReader(new InputStreamReader(mInputStream));


        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                String line = br.readLine();
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

       //Callback Map
       Map<String, Object> map = new HashMap<String, Object>();
       map.put(RECEIVER_KEY, values[0]);
       callback.callback(map);

    }





    //Close Socket when Called
    @Override
    protected void onCancelled() {
        try {
            mSocket.close();
        } catch (IOException e) {};
    }
}
