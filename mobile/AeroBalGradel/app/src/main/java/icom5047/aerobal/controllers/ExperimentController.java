package icom5047.aerobal.controllers;

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
import com.aerobal.data.serializers.GlobalGson;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import icom5047.aerobal.containers.SpinnerContainer;
import icom5047.aerobal.resources.GlobalConstants;

/**
 * Created by enrique on 3/20/14.
 */
public class ExperimentController implements Serializable{


    //Constants
    public static int ALL_RUNS = -1;
    public static String ALL_RUNS_STRING = "All";

    //
    private volatile Experiment experimentObj;

    //Running SubRouting Boolean
    private volatile boolean running;

    private SpinnerContainer activeRun;


    public ExperimentController() {
        experimentObj = null;
        this.activeRun = new SpinnerContainer(0, ALL_RUNS, ALL_RUNS_STRING);
        this.running = false;
    }

    /*============= Setter, Getter, Hazer=====*/

    public void setExperiment(Experiment experiment) {
        this.experimentObj = experiment;
    }

    public Experiment getExperiment() {
        return experimentObj;
    }

    public Experiment getCloneExperiment(){ return GlobalGson.gson().fromJson(experimentObj.toString(), Experiment.class);}

    public static Run getCloneRun(Run run){return GlobalGson.gson().fromJson(run.toString(), Run.class);}

    public boolean isExperimentSet() {
        return experimentObj != null;
    }

    public void setRunning(boolean running){ this.running = running; };

    public boolean isRunning(){
        return running;
    }

    public void closeExperiment(){
        this.experimentObj = null;
    }



/* ========================== Stats Maps ==================== */

    public static Map<Integer,Stats> getStatsForExperiment(Experiment experiment){

        Map<Integer,Stats> map = new HashMap<Integer, Stats>();

        Stats preasure = new Stats(MeasurementTypes.getType(Pressure.id()), experiment);
        Stats windSpeed = new Stats(MeasurementTypes.getType(WindSpeed.id()), experiment);
        Stats windDir = new Stats(MeasurementTypes.getType(WindDirection.id()), experiment);
        Stats temp = new Stats(MeasurementTypes.getType(Temperature.id()), experiment);
        Stats humidity = new Stats(MeasurementTypes.getType(Humidity.id()), experiment);
        Stats tilt = new Stats(MeasurementTypes.getType(Tilt.id()), experiment);
        Stats drag = new Stats(MeasurementTypes.getType(Drag.id()), experiment);
        Stats lift = new Stats(MeasurementTypes.getType(Lift.id()), experiment);

        map.put(GlobalConstants.Measurements.WindSpeedKey, windSpeed);
        map.put(GlobalConstants.Measurements.WindDirectionKey, windDir);
        map.put(GlobalConstants.Measurements.PressureKey, preasure);
        map.put(GlobalConstants.Measurements.TemperatureKey, temp);
        map.put(GlobalConstants.Measurements.HumidityKey, humidity);
        map.put(GlobalConstants.Measurements.SideKey, tilt);
        map.put(GlobalConstants.Measurements.DragKey, drag);
        map.put(GlobalConstants.Measurements.LiftKey, lift);

        return map;
    }

    public static Map<Integer,Stats> getStatsForRuns(Run runs){

        Map<Integer,Stats> map = new HashMap<Integer, Stats>();

        Stats preasure = new Stats(MeasurementTypes.getType(Pressure.id()), runs);
        Stats windSpeed = new Stats(MeasurementTypes.getType(WindSpeed.id()), runs);
        Stats windDir = new Stats(MeasurementTypes.getType(WindDirection.id()), runs);
        Stats temp = new Stats(MeasurementTypes.getType(Temperature.id()), runs);
        Stats humidity = new Stats(MeasurementTypes.getType(Humidity.id()),runs);
        Stats tilt = new Stats(MeasurementTypes.getType(Tilt.id()), runs);
        Stats drag = new Stats(MeasurementTypes.getType(Drag.id()), runs);
        Stats lift = new Stats(MeasurementTypes.getType(Lift.id()), runs);

        map.put(GlobalConstants.Measurements.WindSpeedKey, windSpeed);
        map.put(GlobalConstants.Measurements.WindDirectionKey, windDir);
        map.put(GlobalConstants.Measurements.PressureKey, preasure);
        map.put(GlobalConstants.Measurements.TemperatureKey, temp);
        map.put(GlobalConstants.Measurements.HumidityKey, humidity);
        map.put(GlobalConstants.Measurements.SideKey, tilt);
        map.put(GlobalConstants.Measurements.DragKey, drag);
        map.put(GlobalConstants.Measurements.LiftKey, lift);

        return map;
    }


/*=========Data Activity Data Managment =============== */

    public List<SpinnerContainer> getRunsListSpinner(){
        LinkedList<SpinnerContainer> list = new LinkedList<SpinnerContainer>();

        //Add All Valzue
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
