package icom5047.aerobal.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import icom5047.aerobal.activities.R;
import icom5047.aerobal.containers.SpinnerContainer;
import icom5047.aerobal.controllers.ExperimentController;
import icom5047.aerobal.resources.GlobalConstants;

/**
 * Created by enrique on 3/26/14.
 */
public class DataRawDataFragment extends Fragment {

    private volatile ExperimentController experimentController;
    private LinearLayout linearLayout;

    public static DataRawDataFragment getInstance(ExperimentController experimentController){

        DataRawDataFragment dataSummaryFragment = new DataRawDataFragment();
        dataSummaryFragment.setExperimentController(experimentController);
        return dataSummaryFragment;

    }

    private void setExperimentController(ExperimentController experimentController){
        this.experimentController = experimentController;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_data_summary, container, false);

        //Measurement Spinner
        Spinner typeSpinner = (Spinner) view.findViewById(R.id.fragDataSumSpinner);
        typeSpinner.setAdapter(new ArrayAdapter<SpinnerContainer>(this.getActivity(), android.R.layout.simple_dropdown_item_1line, GlobalConstants.Measurements.getMeasurementListSpinner()));
        typeSpinner.setSelection(experimentController.getActiveMeasurementValue().spinnerIndex);

        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                SpinnerContainer container = (SpinnerContainer) adapterView.getItemAtPosition(i);
                refresh(container);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //NoOp
            }
        });

        linearLayout = (LinearLayout) view.findViewById(R.id.fragDataSumContainer);

        ListView listView = (ListView) linearLayout.findViewById(R.id.fragDataSumList);

        //listView.setAdapter(new RawDataAdapter(this, null));










        return view;
    }

    public void refresh(SpinnerContainer container){

        Log.v("Refresh", "Refresh");

        linearLayout.invalidate();

    }

    public class RawDataAdapter extends ArrayAdapter<String>{


        public RawDataAdapter(Context context, String[] e){
            super(context, android.R.layout.simple_list_item_1, e);
        }

    }


}
