package icom5047.aerobal.controllers;

import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.aerobal.data.objects.Experiment;
import com.aerobal.data.objects.Run;
import com.aerobal.data.objects.Stats;
import com.aerobal.data.objects.measurementTypes.Drag;
import com.aerobal.data.objects.measurementTypes.Humidity;
import com.aerobal.data.objects.measurementTypes.Lift;
import com.aerobal.data.objects.measurementTypes.MeasurementTypes;
import com.aerobal.data.objects.measurementTypes.Pressure;
import com.aerobal.data.objects.measurementTypes.Temperature;
import com.aerobal.data.objects.measurementTypes.Tilt;
import com.aerobal.data.objects.measurementTypes.WindDirection;
import com.aerobal.data.objects.measurementTypes.WindSpeed;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import icom5047.aerobal.comm.BluetoothDataManager;
import icom5047.aerobal.comm.BluetoothTimer;
import icom5047.aerobal.containers.SpinnerContainer;
import icom5047.aerobal.interfaces.AeroCallback;
import icom5047.aerobal.resources.GlobalConstants;

/**
 * Created by enrique on 3/20/14.
 */
public class ExperimentController extends TimerTask implements Serializable, AeroCallback {


    //Constants
    public static int ALL_RUNS = -1;
    public static String ALL_RUNS_STRING = "All";

    private volatile Experiment runs;
    private BluetoothDataManager ioBtManager;
    private BluetoothTimer timer;

    private volatile boolean running;

    private long dataSamplesCounter;

    private SpinnerContainer activeRun;


    public ExperimentController() {
        runs = null;
        this.activeRun = new SpinnerContainer(0, ALL_RUNS, ALL_RUNS_STRING);
        this.running = false;
    }

    /*============= Setter, Getter, Haser ======*/

    public void setExperiment(Experiment experiment) {
        this.runs = experiment;
    }

    public Experiment getExperiment() {
        return runs;
    }

    public boolean isExperimentSet() {
        return runs != null;
    }

    public void closeExperiment(){
        this.runs = null;
    }

    public boolean isRunning(){
        return running;
    }


    //Run a Run
    public void generateExperimentRun(BluetoothController btController, FragmentActivity context){


        ioBtManager = btController.getIOManager();
        if(ioBtManager == null){
            return;
        }
        //Set Sample Counter
        dataSamplesCounter = 0;

        //Set Callback When Data Received
        ioBtManager.setCallback(this);

        //Start Listening
        ioBtManager.execute();


        //Run Schedule Task
        this.timer = new BluetoothTimer();
        this.timer.runTask(this, runs.frequency());


    }

    @Override
    public void run() {
        running = true;
        //Reset if Run Multiple Times
        if (dataSamplesCounter == runs.amountOfValues()){
            timer.reset();
            return;
        }

        Log.v("Test", "Test");
        //Sent Data
        ioBtManager.send("bt:ack", true);



        //Add Process Send Counter
        dataSamplesCounter++;
    }


    @Override
    public void callback(Map<String, Object> objectMap) {


        Log.v("BTRun", "Called");


        //Status Check

    }

    /* Data Stats */

    public static Map<Integer,Stats> getStatsForExperiment(Experiment experiment){

        Map<Integer,Stats> map = new HashMap<Integer, Stats>();

        Stats preasure = new Stats(MeasurementTypes.getType(Pressure.toString()), experiment);
        Stats windSpeed = new Stats(MeasurementTypes.getType(WindSpeed.toString()), experiment);
        Stats windDir = new Stats(MeasurementTypes.getType(WindDirection.toString()), experiment);
        Stats temp = new Stats(MeasurementTypes.getType(Temperature.toString()), experiment);
        Stats humidity = new Stats(MeasurementTypes.getType(Humidity.toString()), experiment);
        Stats tilt = new Stats(MeasurementTypes.getType(Tilt.toString()), experiment);
        Stats drag = new Stats(MeasurementTypes.getType(Drag.toString()), experiment);
        Stats lift = new Stats(MeasurementTypes.getType(Lift.toString()), experiment);

        map.put(GlobalConstants.Measurements.WindSpeedKey, windSpeed);
        map.put(GlobalConstants.Measurements.WindDirectionKey, windDir);
        map.put(GlobalConstants.Measurements.PressureKey, preasure);
        map.put(GlobalConstants.Measurements.TemperatureKey, temp);
        map.put(GlobalConstants.Measurements.HumidityKey, humidity);
        map.put(GlobalConstants.Measurements.TiltKey, tilt);
        map.put(GlobalConstants.Measurements.DragKey, drag);
        map.put(GlobalConstants.Measurements.LiftKey, lift);

        return map;
    }

    public static Map<Integer,Stats> getStatsForRuns(Run runs){

        Map<Integer,Stats> map = new HashMap<Integer, Stats>();

        Stats preasure = new Stats(MeasurementTypes.getType(Pressure.toString()), runs);
        Stats windSpeed = new Stats(MeasurementTypes.getType(WindSpeed.toString()), runs);
        Stats windDir = new Stats(MeasurementTypes.getType(WindDirection.toString()), runs);
        Stats temp = new Stats(MeasurementTypes.getType(Temperature.toString()), runs);
        Stats humidity = new Stats(MeasurementTypes.getType(Humidity.toString()),runs);
        Stats tilt = new Stats(MeasurementTypes.getType(Tilt.toString()), runs);
        Stats drag = new Stats(MeasurementTypes.getType(Drag.toString()), runs);
        Stats lift = new Stats(MeasurementTypes.getType(Lift.toString()), runs);

        map.put(GlobalConstants.Measurements.WindSpeedKey, windSpeed);
        map.put(GlobalConstants.Measurements.WindDirectionKey, windDir);
        map.put(GlobalConstants.Measurements.PressureKey, preasure);
        map.put(GlobalConstants.Measurements.TemperatureKey, temp);
        map.put(GlobalConstants.Measurements.HumidityKey, humidity);
        map.put(GlobalConstants.Measurements.TiltKey, tilt);
        map.put(GlobalConstants.Measurements.DragKey, drag);
        map.put(GlobalConstants.Measurements.LiftKey, lift);

        return map;
    }


     /*Data Controller */

    public List<SpinnerContainer> getRunsListSpinner(){
        LinkedList<SpinnerContainer> list = new LinkedList<SpinnerContainer>();

        //Add All Value
        list.add(0, new SpinnerContainer(0, ALL_RUNS, ALL_RUNS_STRING ));

        //Add Other Values
        for(int i=0; i< getExperiment().runs().size(); i++){

            list.add(i+1, new SpinnerContainer(i+1, i, "Runs"+(i+1)));

        }
        return list;
    }

    //Getter & Setter for Active Values
    public SpinnerContainer getActiveRun(){
        return this.activeRun;
    }

    public void setActiveRun(SpinnerContainer activeRun) {
        this.activeRun = activeRun;
    }
}
