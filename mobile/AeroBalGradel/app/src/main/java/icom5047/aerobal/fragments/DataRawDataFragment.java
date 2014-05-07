package icom5047.aerobal.fragments;


import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
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
import android.widget.TextView;

import com.aerobal.data.objects.Run;
import com.aerobal.data.objects.Stats;

import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;

import icom5047.aerobal.activities.R;
import icom5047.aerobal.containers.RawContainer;
import icom5047.aerobal.containers.SpinnerContainer;
import icom5047.aerobal.controllers.ExperimentController;
import icom5047.aerobal.controllers.UnitController;
import icom5047.aerobal.resources.GlobalConstants;
import scala.collection.JavaConversions;

/**
 * Created by enrique on 3/26/14.
 */
public class DataRawDataFragment extends Fragment {

    private volatile ExperimentController experimentController;
    private volatile UnitController unitController;
    private SpinnerContainer currContainer;
    private LinearLayout linearLayout;
    private RawDataAdapter rawDataAdapter;
    private TextView noDataText;
    private int counter = 0;
    private ListView listView;
    private List<RawContainer> rawData;

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
        View view = inflater.inflate(R.layout.fragment_data_raw, container, false);

        currContainer = new SpinnerContainer(0, GlobalConstants.Measurements.PressureKey, GlobalConstants.Measurements.PressureString);

        //Measurement Spinner
        Spinner typeSpinner = (Spinner) view.findViewById(R.id.fragDataRawSpinner);
        typeSpinner.setAdapter(new ArrayAdapter<SpinnerContainer>(this.getActivity(), android.R.layout.simple_dropdown_item_1line, GlobalConstants.Measurements.getMeasurementListSpinner()));
        typeSpinner.setSelection(currContainer.spinnerIndex);

        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                SpinnerContainer container = (SpinnerContainer) adapterView.getItemAtPosition(i);
                currContainer = container;
                if(listView != null){
                    refresh();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //NoOp
            }
        });

        listView = (ListView) view.findViewById(R.id.fragDataRawList);
        linearLayout = (LinearLayout) view.findViewById(R.id.fragDataRawContainer);
        noDataText = (TextView) view.findViewById(R.id.fragDataRawNoData);
        //Set Adapter Not Null
        rawDataAdapter = new RawDataAdapter(this.getActivity(), getRawData());

        //Change Typeface
        Typeface face = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Thin.ttf");
        noDataText.setTypeface(face);
        counter++;
        Log.v("DataSummary","OnView Called "+counter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //Set List On Resume

        Log.v("DataRaw", "Called On Resume");
        Log.v("DataSummary", "Run "+experimentController.getActiveRun());

        //Error Message for Experiment
        if (experimentController.getActiveRun().index == ExperimentController.ALL_RUNS ){
            linearLayout.setVisibility(View.GONE);
            noDataText.setText(R.string.frag_data_raw_full_exp_no_display);
            noDataText.setVisibility(View.VISIBLE);
            return;
        }

        //Set For Run
        List<RawContainer> data = getRawData();
        Log.v("DataRaw", "Data Obtained:" +data.toString());

        if(data.size() == 0){
            linearLayout.setVisibility(View.GONE);
            noDataText.setText(R.string.frag_data_raw_no_data);
            noDataText.setVisibility(View.VISIBLE);
        }
        else{
            noDataText.setVisibility(View.GONE);
            linearLayout.setVisibility(View.VISIBLE);
            //Refresh Vars
            rawData = data;
            rawDataAdapter = new RawDataAdapter(this.getActivity(), rawData);
            listView.setAdapter(rawDataAdapter);
        }
    }

    public void fullRefresh(SpinnerContainer run){
        Log.v("DataRaw", "Full Refresh for"+run);
        experimentController.setActiveRun(run);
        Log.v("DataRaw", "Set:"+experimentController.getActiveRun());
        refresh();

    }

    public void refresh(){
        Log.v("DataRaw", "Refresh Called Summary Details List Obtain for:" + experimentController.getActiveRun());

        if(listView != null && rawDataAdapter != null){
            Log.v("DataRaw", "Not Null");
            onResume();
            listView.invalidate();
            rawDataAdapter.notifyDataSetChanged();
        }
        Log.v("DataRaw", "Null");
    }


    public class RawDataAdapter extends ArrayAdapter<RawContainer>{


        public RawDataAdapter(Context context, List<RawContainer> e){
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

            NumberFormat nf = NumberFormat.getInstance();
            nf.setMinimumFractionDigits(GlobalConstants.DecimalPrecision);
            nf.setMaximumFractionDigits(GlobalConstants.DecimalPrecision);
            messValue.setText(nf.format(unitController.convertFromDefaultToCurrent( currContainer.index ,pair.value)));

            //Values
            timeValue.setText(pair.nanoseconds+"");


            return view;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;

        }

    }

    private List<RawContainer> getRawData(){

        //Object Stats Contain Values Need
        Stats stats;
        List<Run> runsList = JavaConversions.asJavaList(experimentController.getCloneExperiment().runs());
        Log.v("Raw LIST:", experimentController.getActiveRun().name);
        if(experimentController.getActiveRun().index == ExperimentController.ALL_RUNS){
            stats = new Stats(GlobalConstants.Measurements.getMessurmentTypeForSpinner(currContainer), experimentController.getCloneExperiment());
        }else{
            stats = new Stats(GlobalConstants.Measurements.getMessurmentTypeForSpinner(currContainer), ExperimentController.getCloneRun(runsList.get(experimentController.getActiveRun().index)));
        }

        List<Object> values = JavaConversions.asJavaList(stats.values());

        List<RawContainer> ret = new LinkedList<RawContainer>();

        //Return Empty
        if(values.size() == 0){
            return ret;
        }

        //Run Case Case
        Double firstMeasure = (Double) values.get(0);
        ret.add(new RawContainer(0, firstMeasure));
        int timeCounter = 0;


        for(int i=1; i<values.size(); i++){
            timeCounter += experimentController.getExperiment().getFrequency();
            Double e = (Double) values.get(i);
            ret.add(i,new RawContainer(timeCounter, (Double) e));
        }



        return ret;
    }



}
