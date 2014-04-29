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

import com.aerobal.data.objects.Run;
import com.aerobal.data.objects.Stats;

import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;

import icom5047.aerobal.activities.R;
import icom5047.aerobal.containers.SpinnerContainer;
import icom5047.aerobal.containers.SummaryContainer;
import icom5047.aerobal.controllers.ExperimentController;
import icom5047.aerobal.controllers.UnitController;
import icom5047.aerobal.resources.GlobalConstants;
import scala.collection.JavaConversions;

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

        listView = (ListView) view.findViewById(R.id.fragDataSumList);
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        listView.setAdapter(new SummaryAdapter(this.getActivity(), getSummaryDetails()));
    }

    public void fullRefresh(SpinnerContainer run){
        Log.v("Refresh", "Changes Run");
        experimentController.setActiveRun(run);
        refresh();
    }

    public void refresh(){
        Log.v("Refresh", "Change Var");
        if(listView != null){
            listView.invalidateViews();
        }

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

            NumberFormat nf = NumberFormat.getInstance();
            nf.setMinimumFractionDigits(GlobalConstants.DecimalPrecision);
            nf.setMaximumFractionDigits(GlobalConstants.DecimalPrecision);



            title.setText(summaryContainer.type);
            value.setText(nf.format(summaryContainer.value));

            int unit_type = GlobalConstants.Measurements.measurementToUnitMapping().get(currContainer.index);

            unit.setText(unitController.getCurrentUnitForType(unit_type));

            return view;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }
    }


    public List<SummaryContainer> getSummaryDetails() {


        Stats stats;
        List<Run> runsList = JavaConversions.asJavaList(experimentController.getExperiment().runs());
        if(experimentController.getActiveRun().index == ExperimentController.ALL_RUNS){
            stats = new Stats(GlobalConstants.Measurements.getMessurmentTypeForSpinner(currContainer), experimentController.getExperiment());
        }else{
            stats = new Stats(GlobalConstants.Measurements.getMessurmentTypeForSpinner(currContainer), runsList.get(experimentController.getActiveRun().index));
        }


        List<SummaryContainer> list = new LinkedList<SummaryContainer>();
        String[] summary = GlobalConstants.allMeasurementType;
        list.add(0, new SummaryContainer(summary[0],
                unitController.convertFromDefaultToCurrent( currContainer.index ,stats.getMean() ))); //Ave
        list.add(1, new SummaryContainer(summary[1],
                unitController.convertFromDefaultToCurrent( currContainer.index ,stats.getMax() ))); //Max
        list.add(2, new SummaryContainer(summary[2],
                unitController.convertFromDefaultToCurrent( currContainer.index, stats.getMin()))); //Min
        list.add(3, new SummaryContainer(summary[3],
                unitController.convertFromDefaultToCurrent( currContainer.index, stats.getStandardDeviation()))); //Standard Deviation
        list.add(4, new SummaryContainer(summary[4],
                unitController.convertFromDefaultToCurrent( currContainer.index, stats.getMedian()))); //Median
        return list;
    }









}
