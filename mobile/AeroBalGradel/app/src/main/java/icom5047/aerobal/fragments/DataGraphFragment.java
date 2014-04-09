package icom5047.aerobal.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import icom5047.aerobal.activities.R;
import icom5047.aerobal.controllers.ExperimentController;

/**
 * Created by enrique on 3/26/14.
 */
public class DataGraphFragment extends Fragment{

    private volatile ExperimentController experimentController;

    public static DataGraphFragment getInstance(ExperimentController experimentController){

        DataGraphFragment dataSummaryFragment = new DataGraphFragment();
        dataSummaryFragment.setExperimentController(experimentController);
        return dataSummaryFragment;

    }

    private void setExperimentController(ExperimentController experimentController){
        this.experimentController = experimentController;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_data_graph, container, false);

        return view;
    }
}
