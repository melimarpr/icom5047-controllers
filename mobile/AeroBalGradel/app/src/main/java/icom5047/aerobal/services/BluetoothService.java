package icom5047.aerobal.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.aerobal.comms.bluetooth.SerialStrings;
import com.aerobal.data.objects.Experiment;
import com.aerobal.data.objects.Measurement;
import com.aerobal.data.objects.Run;
import com.aerobal.data.objects.measurementTypes.Drag;
import com.aerobal.data.objects.measurementTypes.Humidity;
import com.aerobal.data.objects.measurementTypes.Lift;
import com.aerobal.data.objects.measurementTypes.MeasurementTypes;
import com.aerobal.data.objects.measurementTypes.Pressure;
import com.aerobal.data.objects.measurementTypes.Temperature;
import com.aerobal.data.objects.measurementTypes.Tilt;
import com.aerobal.data.objects.measurementTypes.WindDirection;
import com.aerobal.data.objects.measurementTypes.WindSpeed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import icom5047.aerobal.activities.MainActivity;
import icom5047.aerobal.activities.R;
import icom5047.aerobal.callbacks.StateMachineCallback;
import icom5047.aerobal.containers.StateMachineContainer;
import icom5047.aerobal.controllers.BluetoothController;
import icom5047.aerobal.fragments.LoadingSupportFragment;
import icom5047.aerobal.resources.BTResponseCodes;
import icom5047.aerobal.resources.GlobalConstants;

/**
 * Created by enrique on 5/3/14.
 */
public class BluetoothService extends Service {

    //Constants
    public static final String BROADCAST_CODE = "BluetoothServiceCode";

    public static class Keys {

        public static final String BLUETOOTH_DEVICE = "bluetoothDeviceKey";
        public static final String RUN_FINISH = "runFinishKey";
        public static final String ERROR = "errorKey";
        public static final String ERROR_STRING = "errorStrKey";

        public static final int RUNNING_NOTIFICATION = 0;
        public static final int MAIN_FLAG = 1;
        public static final int EMERGENCY = 2;
        public static final int TUNNEL_USER_RESPONSE_INTENT_YES = 3;
        public static final int TUNNEL_USER_RESPONSE_INTENT_NO = 5;
        public static final int TUNNEL_USER_RESPONSE_NOTIFICATION = 4;
        public static final String EMERGENCY_BUNDLE = "emergency";
        public static final String WAITING_FOR_USER = "waitingForUser";
        public static final String WAITING_FOR_USER_FLAG = "waitingForUserFlag";

    }

    public static class Times {
        public static final long BETWEEN_BT_CONNECTION_DELAY = 500; //Delay Between Disconnection and Connection of BT for Run
        public static final long WAIT_BETWEEN_FAN_ON_AND_MEASURES = 2000;
    }
    //Notification KEYS
    private static final int ERROR_NOTIFICATION = 9;
    private static final int SUCCESS_NOTIFICATION = 8;

    //Instances
    private StateMachineThread stateMachineThread;
    private Experiment experiment;

    //State
    public volatile boolean emergency = false;
    public volatile boolean waitingUserResponse = false;
    public volatile boolean isListening = false;
    public volatile boolean ackNeeded = false;

    //Notification
    public Notification runningNotification;

    public BluetoothService() {
        super();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        //Bundles Must Exist
        if (bundle != null) {

            //Check Emergency
            if (bundle.getBoolean(Keys.EMERGENCY_BUNDLE, false)) {
                Log.e("BS:", "Emergency Stop");
                emergency = true; //Set Variable to True is Received

                //State Machine must exist
                if (stateMachineThread != null) {
                    stateMachineThread.emergencyStop(getString(R.string.toast_error_emergency_or_invalid));
                    Log.e("BS:", "Emergency bt:fan=off sent.");
                } else {
                    Log.e("BS:", "State Machine Not Initialized");
                }
                //Publish Error
                publishError(getString(R.string.notification_emergency_stop));

                return Service.START_STICKY;
            }

            if (bundle.getBoolean(Keys.WAITING_FOR_USER_FLAG, false)) {

                Log.v("BS:", "User Respond Notification Running");
                if (bundle.getBoolean(Keys.WAITING_FOR_USER, false)) {
                    Log.v("BS: ", "Yes: Dismiss and Allows Sending");
                    NotificationManager notificationManager =
                            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.cancel(Keys.TUNNEL_USER_RESPONSE_NOTIFICATION);
                    waitingUserResponse = false;
                    return Service.START_STICKY;
                } else {
                    Log.v("BS: ", "No: Not Valid");
                    waitingUserResponse = true;
                    return Service.START_STICKY;
                }
            }

            //Get Instances from Bundles
            BluetoothDevice bluetoothDevice = bundle.getParcelable(Keys.BLUETOOTH_DEVICE);
            experiment = (Experiment) bundle.getSerializable(icom5047.aerobal.resources.Keys.BundleKeys.Experiment);


            //Running Notification

            //Intent if click
            Intent intentMain = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, Keys.MAIN_FLAG, intentMain, PendingIntent.FLAG_UPDATE_CURRENT);

            //Intent Sent if Stop
            Bundle emergencyStop = new Bundle();
            emergencyStop.putBoolean(Keys.EMERGENCY_BUNDLE, true);
            Intent emergencyService = new Intent(this, BluetoothService.class);
            emergencyService.putExtras(emergencyStop);
            PendingIntent pendingIntentStop = PendingIntent.getService(this, Keys.EMERGENCY, emergencyService, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification.Builder builder = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(getString(R.string.notification_running_title))
                    .setContentText(getString(R.string.notification_running_desc))
                    .setContentIntent(pendingIntent)
                    .addAction(R.drawable.ic_emergency, getString(R.string.notification_emergency_stop), pendingIntentStop);


            runningNotification = builder.build();
            runningNotification.flags |= Notification.FLAG_NO_CLEAR;
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(Keys.RUNNING_NOTIFICATION, runningNotification);

            //Set Gears in Motion
            Log.v("BS:", "Stating Everything");
            stateMachineThread = new StateMachineThread(bluetoothDevice);
            stateMachineThread.execute();
            return Service.START_STICKY;
        } else {
            publishError(getString(R.string.toast_error_invalid_service_access));
        }
        return Service.START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("BS:", "Notifications Killed");
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(Keys.TUNNEL_USER_RESPONSE_NOTIFICATION); //Kill All Notification
        notificationManager.cancel(Keys.RUNNING_NOTIFICATION);
        stateMachineThread.cancel(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

/*============== Publish Methods =================*/

    /**
     * Publish Values
     */
    private void publishError(String errorString) {
        Intent intent = new Intent(BROADCAST_CODE);
        Bundle bundle = new Bundle();
        bundle.putBoolean(Keys.ERROR, true);
        bundle.putString(Keys.ERROR_STRING, errorString);
        intent.putExtras(bundle);
        sendBroadcast(intent);

        //Notification
        Notification.Builder builder = new Notification.Builder(this);

        builder.setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(errorString)
                .setContentText(getString(R.string.notification_error_desc));
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(alarmSound);
        builder.setLights(Color.RED, 500, 500);
        long[] pattern = {500,500,500,500,500,500,500,500,500};
        builder.setVibrate(pattern);
        Notification errorNot = builder.build();
        errorNot.flags |= Notification.FLAG_AUTO_CANCEL;
        errorNot.flags |= Notification.FLAG_SHOW_LIGHTS;
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(ERROR_NOTIFICATION, errorNot);

        stopSelf();
    }

    private void publishUpdate(StateMachineThread stateMachine){
        Intent intent = new Intent(LoadingSupportFragment.BROADCAST_CODE);
        intent.putExtras(stateMachine.getUpdateBundle());
        sendBroadcast(intent);

    }


    private void publishResults(StateMachineThread stateMachine) {
        Intent intent = new Intent(BROADCAST_CODE);
        Bundle bundle = new Bundle();
        stateMachine.publishExperiment(experiment); //Append New Experiment
        bundle.putSerializable(icom5047.aerobal.resources.Keys.BundleKeys.Experiment, experiment);
        intent.putExtras(bundle);
        sendBroadcast(intent);


        Intent intentMain = new Intent(this, MainActivity.class);
        intentMain.putExtras(bundle);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, Keys.MAIN_FLAG, intentMain, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this);

        builder.setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getString(R.string.notification_success_title))
                .setContentText(getString(R.string.notification_success_desc))
                .setContentIntent(pendingIntent);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(alarmSound);

        builder.setLights(Color.WHITE, 500, 500);
        long[] pattern = {500,500,500,500,500,500,500,500,500};
        builder.setVibrate(pattern);

        Notification successNot = builder.build();
        successNot.flags |= Notification.FLAG_AUTO_CANCEL;
        successNot.flags |= Notification.FLAG_SHOW_LIGHTS;
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(SUCCESS_NOTIFICATION, successNot);
        stopSelf();

    }






/*============================ State Machine Class ============================*/

    private class StateMachineThread extends AsyncTask<Void, String, Boolean> {



        //Constraints
        private volatile boolean done = false;
        private volatile boolean success = false;
        private volatile boolean isLooping = true;
        private volatile boolean wait = false;

        private long cycles = 0L; //Max Empty Cycles
        private long maxCycles = 1000000L;
        private long ackCounter = 0; //Waitng for Ack
        private final long maxAckCounter = 100000000L;

        private long retryCounter = 0L; //Max Amount of Retry for Errors
        private final long maxRetry = 10L;


        private long noReceptionCounter = 0L; //Amount of Max Empty Cycles Resets Allows
        private final long noReceptionMax = 10L;


        //State Machine Queue and Current
        private StateMachineContainer currentCmd;
        private LinkedList<StateMachineContainer> queue;

        private BluetoothSocket mSocket;
        private InputStream mInputStream;
        private OutputStream mOutputStream;
        private BufferedReader mBufferReader;


        public static final String RECEIVER_KEY = "recieverKey";
        public static final long RECEIVER_DELAY = 1000;


        //New Stuff

        //Store Values
        //{Front, Back, Up, Down, Left, Right }
        double[] emptyTunnelNoWind = new double[6];
        double[] emptyTunnelWithWind = new double[6];
        double[] nonEmptyTunnelNoWind = new double[6];
        public List<Double> sideLeft = new LinkedList<Double>();
        public List<Double> sideRight = new LinkedList<Double>();
        public List<Double> liftUp = new LinkedList<Double>();
        public List<Double> liftDown = new LinkedList<Double>();
        public List<Double> dragFront = new LinkedList<Double>();
        public List<Double> dragBack = new LinkedList<Double>();
        public int runNum = 0;
        private Run run = new Run();

        //Get Values
        public String currState = "Placeholder";
        public double currPressure = 0.0;
        public double currHumidity = 0.0;
        public double currWindSpeed = 0.0;
        public double currWindDir = 0.0;
        public double currTemp = 0.0;
        public double currSide = 0.0;
        public double currDrag = 0.0;
        public double currLift = 0.0;

        public Bundle getUpdateBundle() {
            Bundle bundle = new Bundle();
            bundle.putString(icom5047.aerobal.resources.Keys.UpdateKeys.State, currState);
            bundle.putDouble(icom5047.aerobal.resources.Keys.UpdateKeys.Pressure, currPressure);
            bundle.putDouble(icom5047.aerobal.resources.Keys.UpdateKeys.Humidity, currHumidity);
            bundle.putDouble(icom5047.aerobal.resources.Keys.UpdateKeys.WindSpeed, currWindSpeed);
            bundle.putDouble(icom5047.aerobal.resources.Keys.UpdateKeys.WindDirection, currWindDir);
            bundle.putDouble(icom5047.aerobal.resources.Keys.UpdateKeys.Temperature, currTemp);
            bundle.putDouble(icom5047.aerobal.resources.Keys.UpdateKeys.Side, currSide);
            bundle.putDouble(icom5047.aerobal.resources.Keys.UpdateKeys.Drag, currDrag);
            bundle.putDouble(icom5047.aerobal.resources.Keys.UpdateKeys.Lift, currLift);
            return bundle;
        }


        public StateMachineThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(BluetoothController.AERO_UUID);
            } catch (IOException e) {
                e.printStackTrace();
                publishError(getString(R.string.toast_error_unable_to_bt));
            }
            mSocket = tmp;
            //Wait Some Time
            try {
                Thread.sleep(Times.BETWEEN_BT_CONNECTION_DELAY);
            } catch (InterruptedException e) {
                Log.e("BS:", "Wait Delay Between Connections sleep() failed", e);
                e.printStackTrace();
            }

            try {
                mSocket.connect();
            } catch (IOException e) {
                try {
                    mSocket.close();
                } catch (IOException e1) {
                    Log.e("BS", "close() of connect socket failed", e);
                    e1.printStackTrace();
                    //Close
                    publishError(getString(R.string.toast_error_unable_to_bt));
                    return;
                }

                Log.e("BS:", "connect() of connect socket failed", e);
                Looper.prepare();
                publishError(getString(R.string.toast_error_unable_to_bt));
            }

            InputStream tmpInputStream = null;
            OutputStream tmpOutputStream = null;
            BufferedReader tmpBufferReader = null;

            try {
                tmpInputStream = mSocket.getInputStream();
                tmpOutputStream = mSocket.getOutputStream();
                tmpBufferReader= new BufferedReader(new InputStreamReader(tmpInputStream));
            } catch (IOException e) {
                Log.v("BS:", "Error with IO Stream");
            }
            ;

            mInputStream = tmpInputStream;
            mOutputStream = tmpOutputStream;
            mBufferReader = tmpBufferReader;


            //Queue
            queue = generateCommandOrder();
            currentCmd = queue.getFirst();
            //Init Values
            success = false;
            done = false;
        }

        public void send(String command) {
            try {
                mOutputStream.write(command.getBytes());
                mOutputStream.flush();

                //Flush
            } catch (IOException e) {
                e.printStackTrace();
            }
            ;
            try {
                //Sleep to Wait for Next Command
                Thread.sleep(RECEIVER_DELAY);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {


            //Thread for Looping
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {

                    Log.v("BS:", "State Machine Started");
                    //Run Loop
                    while (!done) {
                        runState();
                        if(wait){
                            try {
                                Log.v("BS:","Sleeping Yay");
                                Thread.sleep(experiment.getFrequency());
                            } catch (InterruptedException e) {
                                Log.v("BS:","Sleeping Error");
                                e.printStackTrace();
                            }
                        }
                    }

                    //Publish is Success
                    if (success){
                        publishResults(StateMachineThread.this);
                    }

                    try {
                        //Close Rx/Tx
                        mInputStream.close();
                        mOutputStream.close();
                        mBufferReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            });
            thread.start();


            // Keep listening to the InputStream until an exception occurs
            while (isLooping) {
                try {
                    String line = mBufferReader.readLine();
                    callback(line);
                } catch (IOException e) {
                    //If Error
                    break;
                }
            }
            Log.v("BS:", "BT Finish");
            return true;
        }

        public void callback(String... values) {
            //Emergency Call Highest Priority
            String value = values[0]; //Get string
            Log.v("BS5: ", "Value Received: "+values[0]);
            if (emergency && value.equals(SerialStrings.disableFan())) {
                Log.v("BS:", "Emergency fan:off received, Thread Still Running");
                isListening = true;
                success = false;
                done = true;
                return;
            }

            //Validate Value Received is What I want
            if (currentCmd.callback.callback(value)) {
                Log.v("BS:", "Valid");

                //Check if Command Requires Acknowledge
                if (ackNeeded) {
                    noReceptionCounter = 0L;
                    retryCounter = 0;
                    return; //Don't Change State
                }

                //Remove Current
                queue.removeFirst();

                //Is Queue Empty, States Complete
                if (queue.isEmpty()) {
                    Log.v("BS:", "Queue is Finish. Success");
                    this.done = true;
                    this.success = true;
                    this.isLooping = false;
                    this.wait = false;
                    return;
                }


                //Other Case
                currentCmd = queue.getFirst(); //Set New State
                isListening = false; //Set Thread to Sending

                //Reset Counters
                Log.v("BS3:", "Reception");
                noReceptionCounter = 0L;
                retryCounter = 0;
                Log.v("BS:", "======================Next Command: " + currentCmd.command);

            }
            //Not Valid
            else {
                Log.v("BS:", "Not Valid");


                if (retryCounter >= maxRetry) { //Retry Allowed Wasted
                    Log.v("BS:", "Current Command " + currentCmd.command + "exceeded amount of retries with Retries: "+retryCounter);
                    emergencyStop(getString(R.string.notification_max_amount_retry));
                    return;
                }

                Log.v("BS", "Retry");
                //Else: Retry Counter Augment and Set to Listing
                retryCounter++;
                noReceptionCounter = 0;
                isListening = false;
            }

            //Redundancy Fail Safe Call
            done = false;

        }


        public void runState() {
            //Don't Send if isListening for Response, Waiting for User Input or Bt Ack
            if (!isListening && !waitingUserResponse && !ackNeeded) {
                //Log.w("BS:", "Is Listening Value:" + isListening + " , Wating User Response Value: " + waitingUserResponse);
                Log.v("BS:", "Command to Send: " + currentCmd.command);
                send(currentCmd.command); //Send
                cycles = 0L; //Reset Cycles in Sent
                if(wait){
                    wait = !wait; //Reset Wait
                }
                isListening = true; //Listening
            } else if (cycles >= maxCycles) { //Error Check for Max Empty Cycles Permitted
                //Overtime Kill Process
                if (noReceptionCounter >= 1) {
                    Log.v("BS:", "Reception Counter"+noReceptionCounter);
                    Log.v("BS:", "Cycles >= maxCycles");
                    Log.v("BS:", "For Current Command: " + currentCmd.command);
                }


                if (ackNeeded) {


                    if (noReceptionCounter >=1)
                        Log.v("BS:", "Waiting for Ack or User Response, Ignore Cycles");
                    cycles = 0L;

                    if(ackCounter == maxAckCounter){
                        emergencyStop(getString(R.string.notification_inactive_bluetooth));
                        return;
                    }

                    ackCounter++;
                    return;
                }

                if (waitingUserResponse) {
                    if (noReceptionCounter >=1)
                        Log.v("BS:", "Waiting for Ack or User Response, Ignore Cycles");
                    cycles = 0L;
                    return;
                }

                if (noReceptionCounter == noReceptionMax) {
                    emergencyStop(getString(R.string.notification_inactive_bluetooth));
                    return; //Return From Stop
                }

                noReceptionCounter += 1; //No Reception Counter
                isListening = false; //Try to Force Response
                cycles = 0L; //Reset Cycles
            } else { //Empty Cycle
                //NoOp
                //OverFlow Check
                if (cycles >= Long.MAX_VALUE - 2) {
                    cycles = 0L;
                }
                cycles++;
            }


        }


        public void emergencyStop(String msg) {
            Log.v("BS:", "Emergency Stop Sent. Error Message: " + msg);
            send(SerialStrings.disableFan());
            publishError(msg);

            emergency = true; //If Threads Alive Change to Finish
            isListening = true;
            done = true;
            isLooping = false;

        }


        //Set Values
        //{Front, Back, Up, Down, Left, Right }
        private void setInitialValues(String[] commands, double[] list) {
            if (commands[0].equals(BTResponseCodes.loadFront)) {
                list[0] = Double.parseDouble(commands[1]);
            } else if (commands[0].equals(BTResponseCodes.loadBack)) {
                list[1] = Double.parseDouble(commands[1]);
            } else if (commands[0].equals(BTResponseCodes.loadUp)) {
                list[2] = Double.parseDouble(commands[1]);
            } else if (commands[0].equals(BTResponseCodes.loadDown)) {
                list[3] = Double.parseDouble(commands[1]);
            } else if (commands[0].equals(BTResponseCodes.loadLeft)) {
                list[4] = Double.parseDouble(commands[1]);
            } else if (commands[0].equals(BTResponseCodes.loadRight)) {
                list[5] = Double.parseDouble(commands[1]);
            }
        }

        private void setMeasurement(String[] commands, int sideKey) {

            double value = Double.parseDouble(commands[1]);

            switch (sideKey){
                case GlobalConstants.Measurements.PressureKey:
                    run.measurements().$plus$eq(new Measurement(MeasurementTypes.getType(Pressure.id()), value));
                    break;
                case GlobalConstants.Measurements.WindSpeedKey:
                    run.measurements().$plus$eq(new Measurement(MeasurementTypes.getType(WindSpeed.id()), value));
                    break;
                case GlobalConstants.Measurements.WindDirectionKey:
                    run.measurements().$plus$eq(new Measurement(MeasurementTypes.getType(WindDirection.id()), value));
                    break;
                case GlobalConstants.Measurements.HumidityKey:
                    run.measurements().$plus$eq(new Measurement(MeasurementTypes.getType(Humidity.id()), value));
                    break;
                case GlobalConstants.Measurements.TemperatureKey:
                    run.measurements().$plus$eq(new Measurement(MeasurementTypes.getType(Temperature.id()), value));
                    break;
                case GlobalConstants.Measurements.LeftKey:
                    //{Front, Back, Up, Down, Left, Right }
                    double adjustmentLeft = (value-nonEmptyTunnelNoWind[4])-(emptyTunnelWithWind[4]-emptyTunnelNoWind[4]);
                    sideLeft.add(adjustmentLeft);
                    break;
                case GlobalConstants.Measurements.RightKey:
                    //{Front, Back, Up, Down, Left, Right }
                    double adjustmentRight = (value-nonEmptyTunnelNoWind[5])-(emptyTunnelWithWind[5]-emptyTunnelNoWind[5]);
                    sideRight.add(adjustmentRight);
                    break;
                case GlobalConstants.Measurements.DownKey:
                    //{Front, Back, Up, Down, Left, Right }
                    double adjustmentDown = (value-nonEmptyTunnelNoWind[3])-(emptyTunnelWithWind[3]-emptyTunnelNoWind[3]);
                    liftDown.add(adjustmentDown);
                    break;
                case GlobalConstants.Measurements.UpKey:
                    //{Front, Back, Up, Down, Left, Right }
                    double adjustmentUp= (value-nonEmptyTunnelNoWind[2])-(emptyTunnelWithWind[2]-emptyTunnelNoWind[2]);
                    liftUp.add(adjustmentUp);
                    break;
                case GlobalConstants.Measurements.BackKey:
                    //{Front, Back, Up, Down, Left, Right }
                    double adjustmentBack= (value-nonEmptyTunnelNoWind[1])-(emptyTunnelWithWind[1]-emptyTunnelNoWind[1]);
                    dragBack.add(adjustmentBack);
                    break;
                case GlobalConstants.Measurements.FrontKey:
                    //{Front, Back, Up, Down, Left, Right }
                    double adjustmentFront= (value-nonEmptyTunnelNoWind[0])-(emptyTunnelWithWind[0]-emptyTunnelNoWind[0]);
                    dragFront.add(adjustmentFront);
                    break;
            }

        }


        @Override
        protected void onPostExecute(Boolean aBoolean) {

            Log.v("BS:", "onPostExecute");
            try {
                mSocket.close();
                mInputStream.close();
                mOutputStream.close();
                mBufferReader.close();
            } catch (IOException e) {
                Log.v("BS:", "BluetoothSocket Close");
                e.printStackTrace();
            } catch (NullPointerException e) {
                Log.v("BS:", "BluetoothSocket Close Null");
                e.printStackTrace();
            }

        }

        @Override
        protected void onCancelled(Boolean aBoolean) {

            Log.v("BS:", "On Cancelled Call");
            try {
                mSocket.close();
                mInputStream.close();
                mOutputStream.close();
                mBufferReader.close();
            } catch (IOException e) {
                Log.v("BS:", "BluetoothSocket Close");
                e.printStackTrace();
            } catch (NullPointerException e) {
                Log.v("BS:", "BluetoothSocket Close Null");
                e.printStackTrace();
            }

        }

        public void publishExperiment(Experiment experiment) {
            addSideDragList();
            experiment.runs().$plus$eq(run);
        }

        private void addSideDragList() {
            if(liftDown.size() == liftUp.size() )
            {
                for(int i=0; i<liftDown.size(); i++){

                    run.measurements().$plus$eq(new Measurement(MeasurementTypes.getType(Lift.id()), liftUp.get(i) - liftDown.get(i)));
                }
            }

            if(dragBack.size() == dragFront.size() ){
                for(int i=0; i<dragBack.size(); i++){
                    run.measurements().$plus$eq(new Measurement(MeasurementTypes.getType(Drag.id()), dragFront.get(i) - dragBack.get(i)));
                }
            }

            if(sideLeft.size() == sideRight.size() ){
                for(int i=0; i<dragBack.size(); i++){
                    run.measurements().$plus$eq(new Measurement(MeasurementTypes.getType(Tilt.id()), sideRight.get(i) - sideLeft.get(i)));
                }
            }



        }

        public LinkedList<StateMachineContainer> generateCommandOrder() {

            LinkedList<StateMachineContainer> queue = new LinkedList<StateMachineContainer>();
            queue.add(
                    //1. bt:ack = Acknowledge Bt Exists
                    new StateMachineContainer("bt:ack\r\n", new StateMachineCallback() {
                        @Override
                        public boolean callback(String receivedString) {
                            if (receivedString.equals(BTResponseCodes.ack)) {

                                Bundle yesBnd = new Bundle();
                                yesBnd.putBoolean(Keys.WAITING_FOR_USER, true);
                                yesBnd.putBoolean(Keys.WAITING_FOR_USER_FLAG, true);

                                Bundle noBnd = new Bundle();
                                noBnd.putBoolean(Keys.WAITING_FOR_USER, false);
                                noBnd.putBoolean(Keys.WAITING_FOR_USER_FLAG, true);

                                Intent yesService = new Intent(BluetoothService.this, BluetoothService.class);
                                yesService.putExtras(yesBnd);
                                PendingIntent yesPIntent = PendingIntent.getService(BluetoothService.this, Keys.TUNNEL_USER_RESPONSE_INTENT_YES, yesService, PendingIntent.FLAG_UPDATE_CURRENT);

                                Intent noService = new Intent(BluetoothService.this, BluetoothService.class);
                                noService.putExtras(noBnd);
                                PendingIntent noPIntent = PendingIntent.getService(BluetoothService.this, Keys.TUNNEL_USER_RESPONSE_INTENT_NO, noService, PendingIntent.FLAG_UPDATE_CURRENT);


                                Notification.Builder builder = new Notification.Builder(BluetoothService.this)
                                        .setSmallIcon(R.drawable.ic_launcher)
                                        .setContentTitle(getString(R.string.notification_empty_tunnel_title))
                                        .setContentText(getString(R.string.notification_empty_tunnel_desc))
                                        .addAction(R.drawable.ic_yes, getString(android.R.string.yes), yesPIntent)
                                        .addAction(R.drawable.ic_no, getString(android.R.string.no), noPIntent);
                                //Sound
                                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                builder.setSound(alarmSound);
                                builder.setLights(Color.BLUE, 500, 500);
                                long[] pattern = {500, 500, 500, 500, 500, 500, 500, 500, 500};
                                builder.setVibrate(pattern);

                                Notification tunnelEmpty = builder.build();
                                tunnelEmpty.flags |= Notification.FLAG_NO_CLEAR;
                                NotificationManager notificationManager =
                                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                notificationManager.notify(Keys.TUNNEL_USER_RESPONSE_NOTIFICATION, tunnelEmpty);

                                //Waiting for User Response
                                waitingUserResponse = true;


                                currState = "Calibration 1: NONW";
                                return true;

                            }
                            return false;
                        }
                    })
            );

            //Get Drag Front
            queue.add(
                    //2. bt:lcf = No Wind Values
                    new StateMachineContainer(SerialStrings.getLoadFront(), new StateMachineCallback() {
                        @Override
                        public boolean callback(String receivedString) {
                            String[] commands = BTResponseCodes.splitCommand(receivedString);
                            if (commands.length == 0) {
                                return false;
                            }
                            if (BTResponseCodes.validForce(commands[0])) {
                                try {
                                    currLift = Double.parseDouble(commands[1]);
                                    setInitialValues(commands, emptyTunnelNoWind);
                                    return true;
                                } catch (NumberFormatException e) {
                                    return false;
                                }
                            }
                            return false;

                        }
                    })
            );
            //Get Drag Back
            queue.add(
                    //3. bt:lcb
                    new StateMachineContainer(SerialStrings.getLoadBack(), new StateMachineCallback() {
                        @Override
                        public boolean callback(String receivedString) {
                            String[] commands = BTResponseCodes.splitCommand(receivedString);
                            if (commands.length == 0) {
                                return false;
                            }
                            if (BTResponseCodes.validForce(commands[0])) {
                                try {
                                    currLift = currLift - Double.parseDouble(commands[1]);
                                    setInitialValues(commands, emptyTunnelNoWind);
                                    return true;
                                } catch (NumberFormatException e) {
                                    return false;
                                }
                            }
                            return false;
                        }
                    })
            );
            //Get Load Up
            queue.add(
                    //4. bt:lcu
                    new StateMachineContainer(SerialStrings.getLoadUp(), new StateMachineCallback() {
                        @Override
                        public boolean callback(String receivedString) {
                            String[] commands = BTResponseCodes.splitCommand(receivedString);
                            if (commands.length == 0) {
                                return false;
                            }
                            if (BTResponseCodes.validForce(commands[0])) {
                                try {
                                    currDrag = Double.parseDouble(commands[1]);
                                    setInitialValues(commands, emptyTunnelNoWind);
                                    return true;
                                } catch (NumberFormatException e) {
                                    return false;
                                }
                            }
                            return false;
                        }
                    })
            );
            //Get Load Down
            queue.add(
                    //5. bt:lcd
                    new StateMachineContainer(SerialStrings.getLoadDown(), new StateMachineCallback() {
                        @Override
                        public boolean callback(String receivedString) {
                            String[] commands = BTResponseCodes.splitCommand(receivedString);
                            if (commands.length == 0) {
                                return false;
                            }
                            if (BTResponseCodes.validForce(commands[0])) {
                                try {
                                    currDrag = currDrag - Double.parseDouble(commands[1]);
                                    setInitialValues(commands, emptyTunnelNoWind);
                                    return true;
                                } catch (NumberFormatException e) {
                                    return false;
                                }
                            }
                            return false;
                        }
                    })
            );
            //Get Load Right
            queue.add(
                    //6. bt:lcr
                    new StateMachineContainer(SerialStrings.getLoadRight(), new StateMachineCallback() {
                        @Override
                        public boolean callback(String receivedString) {
                            String[] commands = BTResponseCodes.splitCommand(receivedString);
                            if (commands.length == 0) {
                                return false;
                            }
                            if (BTResponseCodes.validForce(commands[0])) {
                                try {
                                    currSide = Double.parseDouble(commands[1]);
                                    setInitialValues(commands, emptyTunnelNoWind);
                                    return true;
                                } catch (NumberFormatException e) {
                                    return false;
                                }
                            }
                            return false;
                        }
                    })
            );
            //Get Load Left
            queue.add(
                    //7. bt:lcl
                    new StateMachineContainer(SerialStrings.getLoadLeft(), new StateMachineCallback() {
                        @Override
                        public boolean callback(String receivedString) {
                            String[] commands = BTResponseCodes.splitCommand(receivedString);
                            if (commands.length == 0) {
                                return false;
                            }
                            if (BTResponseCodes.validForce(commands[0])) {
                                try {
                                    currSide = currSide - Double.parseDouble(commands[1]);
                                    setInitialValues(commands, emptyTunnelNoWind);
                                    return true;
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                    return false;
                                }
                            }
                            return false;
                        }
                    })
            );
            queue.add(
                    //Set Wind Speed
                    //8. bt:ws=
                    new StateMachineContainer(SerialStrings.setWindSpeed(experiment.getWindSpeed()), new StateMachineCallback() {
                        @Override
                        public boolean callback(String receivedString) {
                            String[] commands = BTResponseCodes.splitCommand(receivedString);
                            publishUpdate(StateMachineThread.this);
                            return commands[0].equals(BTResponseCodes.windSpeedSet);

                        }
                    })
            );
            queue.add(
                    //9. bt:fan=on
                    new StateMachineContainer(SerialStrings.enableFan(), new StateMachineCallback() {
                        @Override
                        public boolean callback(String receivedString) {
                            if (receivedString.equals(BTResponseCodes.fanOn)) {
                                ackNeeded = true;
                                return true;
                            } else if (receivedString.equals(BTResponseCodes.ack)) {
                                Log.v("BS:", "bt:fan=On acknowledge received");
                                currState = "Calibrated 1: NOW";
                                try {
                                    Thread.sleep(20000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                ackNeeded = false;
                                Log.v("BS:", "Runs Quick");
                                return true;
                            }
                            return false;
                        }
                    })
            );
            //Get Drag Back
            queue.add(
                    //10. bt:lcb
                    new StateMachineContainer(SerialStrings.getLoadBack(), new StateMachineCallback() {
                        @Override
                        public boolean callback(String receivedString) {
                            String[] commands = BTResponseCodes.splitCommand(receivedString);
                            if (commands.length == 0) {
                                return false;
                            }
                            if (BTResponseCodes.validForce(commands[0])) {
                                try {
                                    currLift = Double.parseDouble(commands[1]);
                                    setInitialValues(commands, emptyTunnelWithWind);
                                    return true;
                                } catch (NumberFormatException e) {
                                    return false;
                                }
                            }
                            return false;
                        }
                    })
            );
            //Get Load Up
            queue.add(
                    //11. bt:lcu
                    new StateMachineContainer(SerialStrings.getLoadUp(), new StateMachineCallback() {
                        @Override
                        public boolean callback(String receivedString) {
                            String[] commands = BTResponseCodes.splitCommand(receivedString);
                            if (commands.length == 0) {
                                return false;
                            }
                            if (BTResponseCodes.validForce(commands[0])) {
                                try {
                                    currDrag = Double.parseDouble(commands[1]);
                                    setInitialValues(commands, emptyTunnelWithWind);
                                    return true;
                                } catch (NumberFormatException e) {
                                    return false;
                                }
                            }
                            return false;
                        }
                    })
            );
            //Get Load Down
            queue.add(
                    //12. bt:lcd
                    new StateMachineContainer(SerialStrings.getLoadDown(), new StateMachineCallback() {
                        @Override
                        public boolean callback(String receivedString) {
                            String[] commands = BTResponseCodes.splitCommand(receivedString);
                            if (commands.length == 0) {
                                return false;
                            }
                            if (BTResponseCodes.validForce(commands[0])) {
                                try {
                                    currDrag = currDrag - Double.parseDouble(commands[1]);
                                    setInitialValues(commands, emptyTunnelWithWind);
                                    return true;
                                } catch (NumberFormatException e) {
                                    return false;
                                }
                            }
                            return false;
                        }
                    })
            );
            //Get Load Right
            queue.add(
                    //13. bt:lcr
                    new StateMachineContainer(SerialStrings.getLoadRight(), new StateMachineCallback() {
                        @Override
                        public boolean callback(String receivedString) {
                            String[] commands = BTResponseCodes.splitCommand(receivedString);
                            if (commands.length == 0) {
                                return false;
                            }
                            if (BTResponseCodes.validForce(commands[0])) {
                                try {
                                    currSide = Double.parseDouble(commands[1]);
                                    setInitialValues(commands, emptyTunnelWithWind);
                                    return true;
                                } catch (NumberFormatException e) {
                                    return false;
                                }
                            }
                            return false;
                        }
                    })
            );
            //Get Load Left
            queue.add(
                    //14. bt:lcl
                    new StateMachineContainer(SerialStrings.getLoadLeft(), new StateMachineCallback() {
                        @Override
                        public boolean callback(String receivedString) {
                            String[] commands = BTResponseCodes.splitCommand(receivedString);
                            if (commands.length == 0) {
                                return false;
                            }
                            if (BTResponseCodes.validForce(commands[0])) {
                                try {
                                    currSide = currSide - Double.parseDouble(commands[1]);
                                    setInitialValues(commands, emptyTunnelWithWind);
                                    return true;
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                    return false;
                                }
                            }
                            return false;
                        }
                    })
            );
            queue.add(
                    //15. bt:lcf = No Wind Values
                    new StateMachineContainer(SerialStrings.getLoadFront(), new StateMachineCallback() {
                        @Override
                        public boolean callback(String receivedString) {
                            String[] commands = BTResponseCodes.splitCommand(receivedString);
                            if (commands.length == 0) {
                                return false;
                            }
                            if (BTResponseCodes.validForce(commands[0])) {
                                try {
                                    currSide = Double.parseDouble(commands[1]) - currSide;
                                    setInitialValues(commands, emptyTunnelWithWind);
                                    return true;
                                } catch (NumberFormatException e) {
                                    return false;
                                }
                            }
                            return false;

                        }
                    })
            );

            queue.add(
                    //16. bt:fan=off
                    new StateMachineContainer(SerialStrings.disableFan(), new StateMachineCallback() {
                        @Override
                        public boolean callback(String receivedString) {
                            if (receivedString.equals(BTResponseCodes.fanOff)) {

                                Bundle yesBnd = new Bundle();
                                yesBnd.putBoolean(Keys.WAITING_FOR_USER, true);
                                yesBnd.putBoolean(Keys.WAITING_FOR_USER_FLAG, true);

                                Bundle noBnd = new Bundle();
                                noBnd.putBoolean(Keys.WAITING_FOR_USER, false);
                                noBnd.putBoolean(Keys.WAITING_FOR_USER_FLAG, true);

                                Intent yesService = new Intent(BluetoothService.this, BluetoothService.class);
                                yesService.putExtras(yesBnd);
                                PendingIntent yesPIntent = PendingIntent.getService(BluetoothService.this, Keys.TUNNEL_USER_RESPONSE_INTENT_YES, yesService, PendingIntent.FLAG_UPDATE_CURRENT);

                                Intent noService = new Intent(BluetoothService.this, BluetoothService.class);
                                noService.putExtras(noBnd);
                                PendingIntent noPIntent = PendingIntent.getService(BluetoothService.this, Keys.TUNNEL_USER_RESPONSE_INTENT_NO, noService, PendingIntent.FLAG_UPDATE_CURRENT);

                                publishUpdate(StateMachineThread.this);
                                try {
                                    Log.v("BT:", "Waiting for Fan to turn off");
                                    Thread.sleep(20000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                currState = "Calibration 2: ONW";


                                Notification.Builder builder = new Notification.Builder(BluetoothService.this)
                                        .setSmallIcon(R.drawable.ic_launcher)
                                        .setContentTitle(getString(R.string.notification_filled_tunnel_title))
                                        .setContentText(getString(R.string.notification_filled_tunnel_desc))
                                        .addAction(R.drawable.ic_yes, getString(android.R.string.yes), yesPIntent)
                                        .addAction(R.drawable.ic_no, getString(android.R.string.no), noPIntent);
                                //Sound & Lights
                                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                builder.setSound(alarmSound);
                                builder.setLights(Color.BLUE, 500, 500);
                                long[] pattern = {500, 500, 500, 500, 500, 500, 500, 500, 500};
                                builder.setVibrate(pattern);

                                Notification tunnelEmpty = builder.build();
                                tunnelEmpty.flags |= Notification.FLAG_NO_CLEAR;
                                NotificationManager notificationManager =
                                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                notificationManager.notify(Keys.TUNNEL_USER_RESPONSE_NOTIFICATION, tunnelEmpty);

                                //Waiting for User Response
                                waitingUserResponse = true;

                                return true;

                            }
                            return false;
                        }
                    })
            );
            //Get Drag Back
            queue.add(
                    //17. bt:lcb
                    new StateMachineContainer(SerialStrings.getLoadBack(), new StateMachineCallback() {
                        @Override
                        public boolean callback(String receivedString) {
                            String[] commands = BTResponseCodes.splitCommand(receivedString);
                            if (commands.length == 0) {
                                return false;
                            }
                            if (BTResponseCodes.validForce(commands[0])) {
                                try {
                                    currLift = Double.parseDouble(commands[1]);
                                    setInitialValues(commands, nonEmptyTunnelNoWind);
                                    return true;
                                } catch (NumberFormatException e) {
                                    return false;
                                }
                            }
                            return false;
                        }
                    })
            );
            //Get Load Up
            queue.add(
                    //18. bt:lcu
                    new StateMachineContainer(SerialStrings.getLoadUp(), new StateMachineCallback() {
                        @Override
                        public boolean callback(String receivedString) {
                            String[] commands = BTResponseCodes.splitCommand(receivedString);
                            if (commands.length == 0) {
                                return false;
                            }
                            if (BTResponseCodes.validForce(commands[0])) {
                                try {
                                    currDrag = Double.parseDouble(commands[1]);
                                    setInitialValues(commands, nonEmptyTunnelNoWind);
                                    return true;
                                } catch (NumberFormatException e) {
                                    return false;
                                }
                            }
                            return false;
                        }
                    })
            );
            //Get Load Down
            queue.add(
                    //19. bt:lcd
                    new StateMachineContainer(SerialStrings.getLoadDown(), new StateMachineCallback() {
                        @Override
                        public boolean callback(String receivedString) {
                            String[] commands = BTResponseCodes.splitCommand(receivedString);
                            if (commands.length == 0) {
                                return false;
                            }
                            if (BTResponseCodes.validForce(commands[0])) {
                                try {
                                    currDrag = currDrag - Double.parseDouble(commands[1]);
                                    setInitialValues(commands, nonEmptyTunnelNoWind);
                                    return true;
                                } catch (NumberFormatException e) {
                                    return false;
                                }
                            }
                            return false;
                        }
                    })
            );
            //Get Load Right
            queue.add(
                    //20. bt:lcr
                    new StateMachineContainer(SerialStrings.getLoadRight(), new StateMachineCallback() {
                        @Override
                        public boolean callback(String receivedString) {
                            String[] commands = BTResponseCodes.splitCommand(receivedString);
                            if (commands.length == 0) {
                                return false;
                            }
                            if (BTResponseCodes.validForce(commands[0])) {
                                try {
                                    currSide = Double.parseDouble(commands[1]);
                                    setInitialValues(commands, nonEmptyTunnelNoWind);
                                    return true;
                                } catch (NumberFormatException e) {
                                    return false;
                                }
                            }
                            return false;
                        }
                    })
            );
            //Get Load Left
            queue.add(
                    //21. bt:lcl
                    new StateMachineContainer(SerialStrings.getLoadLeft(), new StateMachineCallback() {
                        @Override
                        public boolean callback(String receivedString) {
                            String[] commands = BTResponseCodes.splitCommand(receivedString);
                            if (commands.length == 0) {
                                return false;
                            }
                            if (BTResponseCodes.validForce(commands[0])) {
                                try {
                                    currSide = currSide - Double.parseDouble(commands[1]);
                                    setInitialValues(commands, nonEmptyTunnelNoWind);
                                    return true;
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                    return false;
                                }
                            }
                            return false;
                        }
                    })
            );
            queue.add(
                    //22. bt:lcf = No Wind Values
                    new StateMachineContainer(SerialStrings.getLoadFront(), new StateMachineCallback() {
                        @Override
                        public boolean callback(String receivedString) {
                            String[] commands = BTResponseCodes.splitCommand(receivedString);
                            if (commands.length == 0) {
                                return false;
                            }
                            if (BTResponseCodes.validForce(commands[0])) {
                                try {
                                    currLift = Double.parseDouble(commands[1])-currLift;
                                    setInitialValues(commands, nonEmptyTunnelNoWind);
                                    return true;
                                } catch (NumberFormatException e) {
                                    return false;
                                }
                            }
                            return false;

                        }
                    })
            );

            queue.add(
                    //23. bt:fan=on
                    new StateMachineContainer(SerialStrings.enableFan(), new StateMachineCallback() {
                        @Override
                        public boolean callback(String receivedString) {

                            if (receivedString.equals(BTResponseCodes.fanOn)) {
                                ackNeeded = true;
                                return true;
                            } else if (receivedString.equals(BTResponseCodes.ack)) {
                                Log.v("BS:", "bt:fan=On acknowledge received");

                                publishUpdate(StateMachineThread.this);
                                try {
                                    Log.v("BS:", "Waiting for 20 Secs...");
                                    Log.v("BS:", "Waiting for 20 Secs...");
                                    Thread.sleep(20000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }


                                ackNeeded = false;
                                wait = true;
                                return true;
                            }
                            return false;
                        }
                    })
            );
            //Loop in Order to Create the Queue for Additional Commands
            for (int i = 0, runNum = 1; i < experiment.getAmountOfValues(); i++, runNum++) {
                queue.add(new StateMachineContainer("bt:ack\r\n", new StateMachineCallback() {
                    @Override
                    public boolean callback(String receivedString) {
                        //To Get Wait Time We Add Time.sleep here
                        Log.v("BS:", "Wait Time: " + experiment.getFrequency());
                        currState ="Run ";
                        wait = true;
                        return receivedString.equals(BTResponseCodes.ack);

                    }
                }));

                //Forces
                //Get Drag Back
                queue.add(
                        //17. bt:lcb
                        new StateMachineContainer(SerialStrings.getLoadBack(), new StateMachineCallback() {
                            @Override
                            public boolean callback(String receivedString) {
                                String[] commands = BTResponseCodes.splitCommand(receivedString);
                                if (commands.length == 0) {
                                    return false;
                                }
                                if (BTResponseCodes.validForce(commands[0])) {
                                    try {
                                        currLift = Double.parseDouble(commands[1]);
                                        setMeasurement(commands, GlobalConstants.Measurements.BackKey);
                                        return true;
                                    } catch (NumberFormatException e) {
                                        Log.v("BS:", "Number Format Error");
                                        return false;
                                    }
                                }
                                return false;
                            }
                        })
                );
                //Get Load Up
                queue.add(
                        //18. bt:lcu
                        new StateMachineContainer(SerialStrings.getLoadUp(), new StateMachineCallback() {
                            @Override
                            public boolean callback(String receivedString) {
                                String[] commands = BTResponseCodes.splitCommand(receivedString);
                                if (commands.length == 0) {
                                    Log.v("BS:", "Error Split");
                                    return false;
                                }
                                if (BTResponseCodes.validForce(commands[0])) {
                                    try {
                                        currDrag = Double.parseDouble(commands[1]);
                                        setMeasurement(commands, GlobalConstants.Measurements.UpKey);
                                        return true;
                                    } catch (NumberFormatException e) {
                                        Log.v("BS:", "Number Format Error");
                                        return false;
                                    }
                                }
                                return false;
                            }
                        })
                );
                //Get Load Down
                queue.add(
                        //19. bt:lcd
                        new StateMachineContainer(SerialStrings.getLoadDown(), new StateMachineCallback() {
                            @Override
                            public boolean callback(String receivedString) {
                                String[] commands = BTResponseCodes.splitCommand(receivedString);
                                if (commands.length == 0) {
                                    Log.v("BS:", "Split");
                                    return false;
                                }
                                if (BTResponseCodes.validForce(commands[0])) {
                                    try {
                                        currDrag = currDrag - Double.parseDouble(commands[1]);
                                        setMeasurement(commands, GlobalConstants.Measurements.DownKey);
                                        return true;
                                    } catch (NumberFormatException e) {
                                        Log.v("BS:", "Number Format Error");
                                        return false;
                                    }
                                }
                                return false;
                            }
                        })
                );
                //Get Load Right
                queue.add(
                        //20. bt:lcr
                        new StateMachineContainer(SerialStrings.getLoadRight(), new StateMachineCallback() {
                            @Override
                            public boolean callback(String receivedString) {
                                String[] commands = BTResponseCodes.splitCommand(receivedString);
                                if (commands.length == 0) {
                                    Log.v("BS:", "Split");
                                    return false;
                                }
                                if (BTResponseCodes.validForce(commands[0])) {
                                    try {
                                        currSide = Double.parseDouble(commands[1]);
                                        setMeasurement(commands, GlobalConstants.Measurements.RightKey);
                                        return true;
                                    } catch (NumberFormatException e) {
                                        Log.v("BS:", "Number Format Error");
                                        return false;
                                    }
                                }
                                return false;
                            }
                        })
                );
                //Get Load Left
                queue.add(
                        //21. bt:lcl
                        new StateMachineContainer(SerialStrings.getLoadLeft(), new StateMachineCallback() {
                            @Override
                            public boolean callback(String receivedString) {
                                String[] commands = BTResponseCodes.splitCommand(receivedString);
                                if (commands.length == 0) {
                                    Log.v("BS:", "Split");
                                    return false;
                                }
                                if (BTResponseCodes.validForce(commands[0])) {
                                    try {
                                        currSide = currSide - Double.parseDouble(commands[1]);
                                        setMeasurement(commands, GlobalConstants.Measurements.LeftKey);
                                        return true;
                                    } catch (NumberFormatException e) {
                                        Log.v("BS:", "Number Format Error");
                                        e.printStackTrace();
                                        return false;
                                    }
                                }
                                return false;
                            }
                        })
                );
                queue.add(
                        //22. bt:lcf = No Wind Values
                        new StateMachineContainer(SerialStrings.getLoadFront(), new StateMachineCallback() {
                            @Override
                            public boolean callback(String receivedString) {
                                String[] commands = BTResponseCodes.splitCommand(receivedString);
                                if (commands.length == 0) {
                                    Log.v("BS:", "Split");
                                    return false;
                                }
                                if (BTResponseCodes.validForce(commands[0])) {
                                    try {
                                        currLift = Double.parseDouble(commands[1])-currLift;
                                        setMeasurement(commands, GlobalConstants.Measurements.FrontKey);
                                        return true;
                                    } catch (NumberFormatException e) {
                                        Log.v("BS:", "Number Format Error");
                                        return false;
                                    }
                                }
                                return false;

                            }
                        })
                );

                queue.add(
                        //Humidity
                        new StateMachineContainer(SerialStrings.getHumidity(), new StateMachineCallback() {
                            @Override
                            public boolean callback(String receivedString) {
                                String[] commands = BTResponseCodes.splitCommand(receivedString);
                                if (commands.length == 0) {
                                    return false;
                                }
                                if (BTResponseCodes.validTimerCommand(commands[0])) {
                                    try {
                                        currHumidity = Double.parseDouble(commands[1]);
                                        setMeasurement(commands, GlobalConstants.Measurements.HumidityKey);
                                        return true;
                                    } catch (NumberFormatException e) {
                                        return false;
                                    }
                                }
                                return false;

                            }
                        })
                );
                queue.add(
                        //Temperature
                        new StateMachineContainer(SerialStrings.getTemperature(), new StateMachineCallback() {
                            @Override
                            public boolean callback(String receivedString) {
                                String[] commands = BTResponseCodes.splitCommand(receivedString);
                                if (commands.length == 0) {
                                    return false;
                                }
                                if (BTResponseCodes.validTimerCommand(commands[0])) {
                                    try {
                                        currTemp = Double.parseDouble(commands[1]);
                                        setMeasurement(commands, GlobalConstants.Measurements.TemperatureKey);
                                        return true;
                                    } catch (NumberFormatException e) {
                                        return false;
                                    }
                                }
                                return false;

                            }
                        })
                );
                queue.add(
                        //Wind Direction
                        new StateMachineContainer(SerialStrings.getWindDirection(), new StateMachineCallback() {
                            @Override
                            public boolean callback(String receivedString) {
                                String[] commands = BTResponseCodes.splitCommand(receivedString);
                                if (commands.length == 0) {
                                    return false;
                                }
                                if (BTResponseCodes.validTimerCommand(commands[0])) {
                                    try {
                                        currWindDir = Double.parseDouble(commands[1]);
                                        setMeasurement(commands, GlobalConstants.Measurements.WindDirectionKey);
                                        return true;
                                    } catch (NumberFormatException e) {
                                        return false;
                                    }
                                }
                                return false;

                            }
                        })
                );


                queue.add(
                        //Wind Speed
                        new StateMachineContainer("bt:spda=?\r\n", new StateMachineCallback() {
                            @Override
                            public boolean callback(String receivedString) {
                                String[] commands = BTResponseCodes.splitCommand(receivedString);
                                if (commands.length == 0) {
                                    return false;
                                }
                                if (BTResponseCodes.validTimerCommand(commands[0])) {
                                    try {
                                        currWindSpeed = Double.parseDouble(commands[1]);
                                        setMeasurement(commands, GlobalConstants.Measurements.WindSpeedKey);
                                        return true;
                                    } catch (NumberFormatException e) {
                                        return false;
                                    }
                                }
                                return false;

                            }
                        })
                );

                queue.add(
                        //Pressure Sensor
                        new StateMachineContainer(SerialStrings.getPressureSensorValue(0), new StateMachineCallback() {
                            @Override
                            public boolean callback(String receivedString) {
                                String[] commands = BTResponseCodes.splitCommand(receivedString);
                                if (commands.length == 0) {
                                    return false;
                                }
                                if (BTResponseCodes.validTimerCommand(commands[0])) {
                                    try {
                                        currPressure = Double.parseDouble(commands[1]);
                                        setMeasurement(commands, GlobalConstants.Measurements.PressureKey);
                                        publishUpdate(StateMachineThread.this);
                                        return true;
                                    } catch (NumberFormatException e) {
                                        return false;
                                    }
                                }
                                return false;

                            }
                        })
                );







            }

            //Final bt:fan=Off
            queue.add(new StateMachineContainer(SerialStrings.disableFan(), new StateMachineCallback() {
                @Override
                public boolean callback(String receivedString) {
                    return receivedString.equals(BTResponseCodes.fanOff);
                }
            }));

            return queue;
        }



    }
}