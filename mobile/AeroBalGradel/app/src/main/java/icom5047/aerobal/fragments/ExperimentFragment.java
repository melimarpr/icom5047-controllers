package icom5047.aerobal.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aerobal.data.objects.Stats;

import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import icom5047.aerobal.activities.MainActivity;
import icom5047.aerobal.activities.R;
import icom5047.aerobal.controllers.ExperimentController;
import icom5047.aerobal.controllers.UnitController;
import icom5047.aerobal.controllers.UserController;
import icom5047.aerobal.http.HttpRequest;
import icom5047.aerobal.http.Server;
import icom5047.aerobal.resources.GlobalConstants;
import icom5047.aerobal.resources.UnitFactory;

/**
 * Created by enrique on 3/24/14.
 */
public class ExperimentFragment extends Fragment {

    private ExperimentController experimentController;
    private UnitController unitController;

    public static ExperimentFragment getExperimentFragment(ExperimentController experimentController, UnitController unitController) {
        ExperimentFragment ef = new ExperimentFragment();
        ef.setExperimentController(experimentController);
        ef.setUnitController(unitController);
        return ef;
    }

    private void setExperimentController(ExperimentController experimentController) {
        this.experimentController = experimentController;
    }

    private void setUnitController(UnitController unitController) {
        this.unitController = unitController;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_experiment_summary, container, false);

        //Creates A New Menu
        MainActivity mainActivity = (MainActivity) this.getActivity();
        mainActivity.setExperimentMenuVisibility(true);
        mainActivity.invalidateOptionsMenu();

        //Get Typeface
        Typeface tf = Typeface.createFromAsset(this.getActivity().getAssets(), "fonts/Roboto-Thin.ttf");

        //Get Textview and set
        final TextView tvTitle = (TextView) view.findViewById(R.id.fragExpSumTittle);
        tvTitle.setText(experimentController.getExperiment().name());
        tvTitle.setTypeface(tf);
        tvTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View layout = inflater.inflate(R.layout.dialog_edit_text, null);

                final EditText et = (EditText) layout.findViewById(R.id.dialogEditText);
                et.setText(experimentController.getExperiment().name());

                builder.setIcon(R.drawable.ic_edit);
                builder.setView(layout);
                builder.setTitle(R.string.title_dialog_save_exp_name);
                builder.setNegativeButton(R.string.cancel, null);
                builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        updateExperimentName(et, tvTitle);
                    }
                });
                builder.create().show();
            }
        });




        //Set Summary Stats
        TextView tvParameter = (TextView) view.findViewById(R.id.fragExpSumParams);
        tvParameter.setTypeface(tf);

        TextView tvSummary = (TextView) view.findViewById(R.id.fragExpSumSummary);
        tvSummary.setTypeface(tf);

        //Set Units
        TextView tvWindSpeedUnits = (TextView) view.findViewById(R.id.fragExpSumWindSpeedUnits);
        tvWindSpeedUnits.setText(unitController.getCurrentSpeedUnit());

       NumberFormat nf = NumberFormat.getInstance();
       nf.setMaximumFractionDigits(GlobalConstants.DecimalPrecision);
       nf.setMinimumFractionDigits(GlobalConstants.DecimalPrecision);

       TextView tvSampleValue = (TextView) view.findViewById(R.id.fragExpSumSampleValue);
       tvSampleValue.setText(""+experimentController.getExperiment().amountOfValues());
       TextView tvWindSpeedValue = (TextView) view.findViewById(R.id.fragExpSumWindSpeedValue);
       double newVal = UnitFactory.Speed.convert(experimentController.getExperiment().windSpeed(), UnitFactory.Speed.DEFAULT, unitController.getCurrentType(UnitFactory.Type.SPEED));
       tvWindSpeedValue.setText(""+nf.format(newVal));
       TextView tvTimeIntervalValue = (TextView) view.findViewById(R.id.fragExpSumTimeIntervalValue);
       tvTimeIntervalValue.setText(""+experimentController.getExperiment().frequency());

        ListView summaryLv = (ListView) view.findViewById(R.id.fragExpSumListView);

        //Set Stats Summary
        Map<Integer, Stats> statsMap = ExperimentController.getStatsForExperiment(experimentController.getExperiment());
        AverageContainer[] averageContainers = new AverageContainer[]{
            new AverageContainer(getString(R.string.frag_exp_sum_num_of_runs), experimentController.getExperiment().runs().size() , true, false, ""),
            new AverageContainer(getString(R.string.ave_wind_speed),
                    unitController.convertFromDefaultToCurrent( GlobalConstants.Measurements.WindSpeedKey ,statsMap.get(GlobalConstants.Measurements.WindSpeedKey).getMean()),
                    false, true, unitController.getCurrentSpeedUnit()),
            new AverageContainer(getString(R.string.ave_wind_direction),
                    unitController.convertFromDefaultToCurrent( GlobalConstants.Measurements.WindDirectionKey ,statsMap.get(GlobalConstants.Measurements.WindDirectionKey).getMean()),
                    false, true, unitController.getCurrentDirectionUnit()),
            new AverageContainer(getString(R.string.ave_temperature),
                    unitController.convertFromDefaultToCurrent(GlobalConstants.Measurements.TemperatureKey, statsMap.get(GlobalConstants.Measurements.TemperatureKey).getMean()),
                    false, true, unitController.getCurrentTemperatureUnit()),
            new AverageContainer(getString(R.string.ave_humidity),
                    unitController.convertFromDefaultToCurrent(GlobalConstants.Measurements.HumidityKey, statsMap.get(GlobalConstants.Measurements.HumidityKey).getMean()),
                    false, true, unitController.getCurrentHumidityUnit()),
            new AverageContainer(getString(R.string.ave_pressure),
                    unitController.convertFromDefaultToCurrent( GlobalConstants.Measurements.PressureKey, statsMap.get(GlobalConstants.Measurements.PressureKey).getMean()),
                    false, true, unitController.getCurrentPressureUnit()),
            new AverageContainer(getString(R.string.ave_side),
                    unitController.convertFromDefaultToCurrent( GlobalConstants.Measurements.SideKey, statsMap.get(GlobalConstants.Measurements.SideKey).getMean()),
                    false, true, unitController.getCurrentForceUnit()),
            new AverageContainer(getString(R.string.ave_drag),
                    unitController.convertFromDefaultToCurrent( GlobalConstants.Measurements.DragKey, statsMap.get(GlobalConstants.Measurements.DragKey).getMean()),
                    false, true, unitController.getCurrentForceUnit()),
            new AverageContainer(getString(R.string.ave_lift),
                    unitController.convertFromDefaultToCurrent( GlobalConstants.Measurements.LiftKey, statsMap.get(GlobalConstants.Measurements.LiftKey).getMean()),
                    false, true, unitController.getCurrentForceUnit())
        };

        summaryLv.setAdapter(new AverageAdapter(this.getActivity(), averageContainers));




       return view;
    }

    private void updateExperimentName(EditText et, TextView tvTitle) {
        if(validateName(et)){
            String newName = et.getText().toString();

            //Update Online
            if(experimentController.getExperiment().id() > 0){
                doHttpExperimentName(newName);
            }
            experimentController.getExperiment().setName(newName);
            tvTitle.invalidate();
            tvTitle.setText(newName);
            tvTitle.invalidate();

        }
    }

    private boolean validateName(EditText name) {

        String strName = name.getText().toString();

        if(strName == null){
            Toast.makeText(getActivity(), R.string.toast_invalid_exp_name_empty, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (strName.isEmpty()) {
            Toast.makeText(getActivity(), R.string.toast_invalid_exp_name_empty, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (strName.length() > GlobalConstants.CharacterLimit) {
            Toast.makeText(getActivity(), R.string.toast_invalid_exp_name_empty, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void doHttpExperimentName(String newName){

        Bundle params = new Bundle();
        params.putString("method", "PUT");
        params.putString("url", Server.Experiments.PUT_UPDATE_EXPERIMENT_NAME);

        String payload = "name="+newName.trim()+"&"+"id="+experimentController.getExperiment().id();

        UserController userController = new UserController(this.getActivity());
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(Server.Headers.TOKEN, userController.getToken());

        HttpRequest request = new HttpRequest(params, payload, headers, HttpRequest.CONTENT_TYPE_X_FORM_URL_ENCODED, new HttpRequest.HttpCallback() {
            @Override
            public void onSucess(JSONObject json) {
                //Context for Updating Doesn't Matter
                Toast.makeText(getActivity(), R.string.toast_success_name_update, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(JSONObject json) {
                Toast.makeText(getActivity(), R.string.toast_success_name_update, Toast.LENGTH_SHORT).show();
            }
        });
        request.execute();
    }


    public class AverageAdapter extends ArrayAdapter<AverageContainer>{

        public AverageAdapter(Context context, AverageContainer[] values){
           super(context, R.layout.row_average_2_unit_lv, values);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            AverageContainer container = this.getItem(position);

            LayoutInflater inflater = ((Activity)this.getContext()).getLayoutInflater();
            //Inflate 3 Value Layout
            View view = inflater.inflate(R.layout.row_average_3_unit_lv, parent, false);

            if(!container.hasUnit){
                view = inflater.inflate(R.layout.row_average_2_unit_lv, parent, false);
            }

            ((TextView) view.findViewById(R.id.rowAveUnitTitle)).setText(container.title);

            TextView tvValue = (TextView) view.findViewById(R.id.rowAveUnitValue);

            if(container.isInteger){

                tvValue.setText( ( (int) container.value )+"" );

            }
            else{
                NumberFormat nf = NumberFormat.getInstance();
                nf.setMinimumFractionDigits(GlobalConstants.DecimalPrecision);
                nf.setMaximumFractionDigits(GlobalConstants.DecimalPrecision);
                tvValue.setText(nf.format(container.value));

            }


            if(container.hasUnit){
                ((TextView) view.findViewById(R.id.rowAve3UnitUnit)).setText(container.unit);
            }



            return view;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }
    }

    public class AverageContainer{

        public String title;
        public double value;
        public String unit;
        public boolean hasUnit;
        public boolean isInteger;


        public AverageContainer(String title, double value, boolean isInteger, boolean hasUnit, String unit ){
            this.title = title;
            this.value = value;
            this.isInteger = isInteger;
            this.hasUnit = hasUnit;
            this.unit = unit;
        }

    }

}
