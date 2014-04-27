package icom5047.aerobal.controllers;

import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.aerobal.data.objects.Experiment;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import icom5047.aerobal.comm.BluetoothDataManager;
import icom5047.aerobal.comm.BluetoothTimer;
import icom5047.aerobal.containers.SpinnerContainer;
import icom5047.aerobal.interfaces.AeroCallback;

/**
 * Created by enrique on 3/20/14.
 */
public class ExperimentController extends TimerTask implements Serializable, AeroCallback {


    //Constants
    public static int ALL_RUNS = -1;
    public static String ALL_RUNS_STRING = "All";

    private Experiment experiment;
    private BluetoothDataManager ioBtManager;
    private BluetoothTimer timer;

    private long dataSamplesCounter;

    private SpinnerContainer activeRun;


    public ExperimentController() {
        experiment = null;
        this.activeRun = new SpinnerContainer(0, ALL_RUNS, ALL_RUNS_STRING);
    }

    /*============= Setter, Getter, Haser ======*/

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public boolean isExperimentSet() {
        return experiment != null;
    }

    public void closeExperiment(){
        this.experiment = null;
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
        this.timer.runTask(this, experiment.frequency());


    }

    @Override
    public void run() {
        //Reset if Run Multiple Times
        if (dataSamplesCounter == experiment.amountOfValues()){
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
