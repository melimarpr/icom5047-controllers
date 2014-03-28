package icom5047.aerobal.controllers;

import com.aerobal.data.objects.Experiment;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import icom5047.aerobal.containers.SpinnerContainer;
import icom5047.aerobal.resources.GlobalConstants;

/**
 * Created by enrique on 3/20/14.
 */
public class ExperimentController implements Serializable{


    //Constants
    public static int ALL_RUNS = -1;
    public static String ALL_RUNS_STRING = "All";

    private Experiment experiment;



    private SpinnerContainer activeRun;
    private SpinnerContainer activeMeasurement;


    public ExperimentController() {
        experiment = null;
        this.activeRun = new SpinnerContainer(0, ALL_RUNS, ALL_RUNS_STRING);
        this.activeMeasurement = new SpinnerContainer(0, GlobalConstants.Measurements.PressureKey, GlobalConstants.Measurements.PressureString);
    }


    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }

    public boolean isExperimentSet() {
        return experiment != null;
    }


    public Experiment getExperiment() {
        return experiment;
    }

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

    public SpinnerContainer getActiveMeasurementValue(){
        return this.activeMeasurement;
    }


    public void setActiveRun(SpinnerContainer activeRun) {
        this.activeRun = activeRun;
    }

    public void setActiveMeasurement(SpinnerContainer activeMeasurement) {
        this.activeMeasurement = activeMeasurement;
    }







}
