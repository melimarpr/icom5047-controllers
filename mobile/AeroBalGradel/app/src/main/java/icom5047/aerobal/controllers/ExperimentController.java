package icom5047.aerobal.controllers;

import android.content.Context;

import com.aerobal.data.objects.Experiment;

/**
 * Created by enrique on 3/20/14.
 */
public class ExperimentController {


    public Experiment experiment;
    public Context context;


    public ExperimentController(Context context) {
        this.context = context;
        experiment = null;
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


}
