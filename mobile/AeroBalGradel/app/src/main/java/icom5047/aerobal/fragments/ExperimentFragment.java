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

import java.text.NumberFormat;

import icom5047.aerobal.activities.MainActivity;
import icom5047.aerobal.activities.R;
import icom5047.aerobal.controllers.ExperimentController;
import icom5047.aerobal.controllers.UnitController;
import icom5047.aerobal.resources.GlobalConstants;

/**
 * Created by enrique on 3/24/14.
 */
public class ExperimentFragment extends Fragment {

    private ExperimentController experimentController;
    private MainActivity mainActivity;
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
        mainActivity = (MainActivity) this.getActivity();
        mainActivity.setExperimentMenuVisibility(true);
        mainActivity.invalidateOptionsMenu();

        //Typeface
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

                        if(validateName(et)){
                            //TODO: Change to Setter
                            String newName = et.getText().toString();
                            experimentController.getExperiment().setName(newName);
                            tvTitle.invalidate();
                            tvTitle.setText(newName);
                            tvTitle.invalidate();

                        }

                    }
                });
                builder.create().show();
            }
        });




        TextView tvParameter = (TextView) view.findViewById(R.id.fragExpSumParams);
        tvParameter.setTypeface(tf);

        TextView tvSummary = (TextView) view.findViewById(R.id.fragExpSumSummary);
        tvSummary.setTypeface(tf);

        //Set Units
        //TODO Fix when default unit specified
        TextView tvWindSpeedUnits = (TextView) view.findViewById(R.id.fragExpSumWindSpeedUnits);
        tvWindSpeedUnits.setText(unitController.getCurrentSpeedUnit());

       NumberFormat nf = NumberFormat.getInstance();
       nf.setMaximumFractionDigits(GlobalConstants.DecimalPrecision);
       nf.setMinimumFractionDigits(GlobalConstants.DecimalPrecision);

       TextView tvSampleValue = (TextView) view.findViewById(R.id.fragExpSumSampleValue);
       tvSampleValue.setText(""+experimentController.getExperiment().amountOfValues());
       TextView tvWindSpeedValue = (TextView) view.findViewById(R.id.fragExpSumWindSpeedValue);
       tvWindSpeedValue.setText(""+nf.format(experimentController.getExperiment().windSpeed()));
       TextView tvTimeIntervalValue = (TextView) view.findViewById(R.id.fragExpSumTimeIntervalValue);
       tvTimeIntervalValue.setText(""+experimentController.getExperiment().frequency());

        ListView summaryLv = (ListView) view.findViewById(R.id.fragExpSumListView);

        //TODO: Fill With Jesus Data
        AverageContainer[] averageContainers = new AverageContainer[]{
            new AverageContainer("Number of Runs", experimentController.getExperiment().runs().size() , true, false, ""),

            //TODO: Fix when implemented
            new AverageContainer("Average Wind Speed", 0.0, false, true, unitController.getCurrentSpeedUnit()),
            new AverageContainer("Average Wind Direction", 0.0, false, true, unitController.getCurrentDirectionUnit()),
            new AverageContainer("Average Temperature", 0.0, false, true, unitController.getCurrentTemperatureUnit()),
            new AverageContainer("Average Humidity", 0.0, false, true, unitController.getCurrentHumidityUnit()),
            new AverageContainer("Average Pressure", 0.0, false, true, unitController.getCurrentPressureUnit()),
            new AverageContainer("Average Tilt", 0.0, false, true, unitController.getCurrentForceUnit()),
            new AverageContainer("Average Drag", 0.0, false, true, unitController.getCurrentForceUnit()),
            new AverageContainer("Average Lift", 0.0, false, true, unitController.getCurrentForceUnit())
        };

        summaryLv.setAdapter(new AverageAdapter(this.getActivity(), averageContainers));




       return view;
    }

    private boolean validateName(EditText name) {

        String strName = name.getText().toString();

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
