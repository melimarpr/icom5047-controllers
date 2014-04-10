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

import icom5047.aerobal.activities.R;
import icom5047.aerobal.containers.RawContainer;
import icom5047.aerobal.containers.SpinnerContainer;
import icom5047.aerobal.containers.SummaryContainer;
import icom5047.aerobal.controllers.ExperimentController;
import icom5047.aerobal.controllers.UnitController;
import icom5047.aerobal.mockers.Mocker;
import icom5047.aerobal.resources.GlobalConstants;

/**
 * Created by enrique on 3/26/14.
 */
public class DataRawDataFragment extends Fragment {

    private volatile ExperimentController experimentController;
    private volatile UnitController unitController;
    private SpinnerContainer currContainer;

    private ListView listView;

    public static DataRawDataFragment getInstance(ExperimentController experimentController, UnitController unitController){

        DataRawDataFragment dataSummaryFragment = new DataRawDataFragment();
        dataSummaryFragment.setExperimentController(experimentController);
        dataSummaryFragment.setUnitController(unitController);
        return dataSummaryFragment;

    }

    private void setExperimentController(ExperimentController experimentController){
        this.experimentController = experimentController;
    }
    private void setUnitController(UnitController unitController){
        this.unitController = unitController;
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

        listView.setAdapter(new RawDataAdapter(this.getActivity(), Mocker.generateRawContainer(10)));










        return view;
    }

    public void refresh(SpinnerContainer container){

        Log.v("Refresh", "Refresh");
        currContainer = container;
        listView.invalidateViews();

    }

    public class RawDataAdapter extends ArrayAdapter<RawContainer>{


        public RawDataAdapter(Context context, RawContainer[] e){
            super(context, android.R.layout.simple_list_item_1, e);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            RawContainer pair = this.getItem(position);


            LayoutInflater inflater = ((Activity) this.getContext()).getLayoutInflater();
            View view = inflater.inflate(R.layout.row_raw_data, parent, false);


            TextView timeValue = (TextView) view.findViewById(R.id.rowRawDataTimeValue);
            TextView messValue = (TextView) view.findViewById(R.id.rowRawDataMessValue);
            TextView messUnit = (TextView) view.findViewById(R.id.rowRawDataMessUnit);


            int unit_type = GlobalConstants.Measurements.measurementToUnitMapping().get(currContainer.index);

            messUnit.setText(unitController.getCurrentUnitForType(unit_type));

            //Values
            timeValue.setText(pair.seconds+"");
            messValue.setText(pair.value+"");






            return view;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;

        }

    }



}
