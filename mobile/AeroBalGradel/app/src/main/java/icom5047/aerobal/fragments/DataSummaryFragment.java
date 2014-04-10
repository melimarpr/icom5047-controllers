package icom5047.aerobal.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import icom5047.aerobal.activities.R;
import icom5047.aerobal.containers.SpinnerContainer;
import icom5047.aerobal.containers.SummaryContainer;
import icom5047.aerobal.controllers.ExperimentController;
import icom5047.aerobal.controllers.UnitController;
import icom5047.aerobal.resources.GlobalConstants;

/**
 * Created by enrique on 3/26/14.
 */
public class DataSummaryFragment extends Fragment {


    private volatile ExperimentController experimentController;
    private ListView listView;
    private SpinnerContainer currContainer;
    private volatile UnitController unitController;

    public static DataSummaryFragment getInstance(ExperimentController experimentController, UnitController unitController){

        DataSummaryFragment dataSummaryFragment = new DataSummaryFragment();
        dataSummaryFragment.setExperimentController(experimentController);
        dataSummaryFragment.setUnitController(unitController);
        return dataSummaryFragment;

    }

    private void setUnitController(UnitController unitController){
        this.unitController = unitController;
    }

    private void setExperimentController(ExperimentController experimentController){
        this.experimentController = experimentController;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_data_summary, container, false);

        currContainer = new SpinnerContainer(0, GlobalConstants.Measurements.PressureKey, GlobalConstants.Measurements.PressureString);
        //Measurement Spinner
        Spinner typeSpinner = (Spinner) view.findViewById(R.id.fragDataSumSpinner);
        typeSpinner.setAdapter(new ArrayAdapter<SpinnerContainer>(this.getActivity(), android.R.layout.simple_dropdown_item_1line, GlobalConstants.Measurements.getMeasurementListSpinner()));
        typeSpinner.setSelection(currContainer.spinnerIndex);

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


        listView = (ListView) view.findViewById(R.id.fragDataSumList);
        listView.setAdapter(new SummaryAdapter(this.getActivity(), getSummaryDetails()));



        return view;
    }

    public void refresh(SpinnerContainer container){

        Log.v("Refresh", "Refresh");
        currContainer = container;
        listView.invalidateViews();

    }

    public class SummaryAdapter extends ArrayAdapter<SummaryContainer>{

        public SummaryAdapter(Context context, List<SummaryContainer>e ){
            super(context, android.R.layout.simple_list_item_1, e);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {


            SummaryContainer summaryContainer = this.getItem(position);
            View view = ((Activity) this.getContext()).getLayoutInflater().inflate(R.layout.row_average_3_unit_lv, parent, false);


            TextView title = (TextView) view.findViewById(R.id.rowAveUnitTitle);
            TextView value = (TextView) view.findViewById(R.id.rowAveUnitValue);
            TextView unit = (TextView) view.findViewById(R.id.rowAve3UnitUnit);




            title.setText(summaryContainer.type);
            value.setText(summaryContainer.value+"");


            int unit_type = GlobalConstants.Measurements.measurementToUnitMapping().get(currContainer.index);

            unit.setText(unitController.getCurrentUnitForType(unit_type));

            return view;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }
    }


    public static List<SummaryContainer> getSummaryDetails(){

        ArrayList<SummaryContainer> list = new ArrayList<SummaryContainer>();

        for(String e : GlobalConstants.allMeasurementType){
            list.add(new SummaryContainer(e, 0.0));
        }

        return list;

    }






}
