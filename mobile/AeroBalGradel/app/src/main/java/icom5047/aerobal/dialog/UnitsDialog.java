package icom5047.aerobal.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import icom5047.aerobal.activities.R;
import icom5047.aerobal.controllers.UnitController;
import icom5047.aerobal.callback.AeroCallback;
import icom5047.aerobal.resources.Keys;
import icom5047.aerobal.resources.UnitFactory;

public class UnitsDialog extends DialogFragment {

    private AeroCallback callback;
    private UnitController unitController;

    public static UnitsDialog getUnitsDialog(UnitController unitController, AeroCallback callback) {
        UnitsDialog ud = new UnitsDialog();
        ud.setCallback(callback);
        ud.setUnitController(unitController);
        return ud;
    }

    private void setUnitController(UnitController unitController) {
        this.unitController = unitController;
    }

    private void setCallback(AeroCallback callback) {
        this.callback = callback;
    }

    public UnitsDialog() {
        super();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.title_dialog_units);
        LayoutInflater inflater = this.getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_units, null);

        TextView tvPressure = (TextView) view.findViewById(R.id.dialogUnitTvPressure);
        TextView tvForce = (TextView) view.findViewById(R.id.dialogUnitTvForce);
        TextView tvHumidity = (TextView) view.findViewById(R.id.dialogUnitTvHumidity);
        TextView tvTemp = (TextView) view.findViewById(R.id.dialogUnitTvTemp);
        TextView tvSpeed = (TextView) view.findViewById(R.id.dialogUnitTvSpeed);
        TextView tvDirection = (TextView) view.findViewById(R.id.dialogUnitTvDirection);
        Typeface face = Typeface.createFromAsset(this.getActivity().getAssets(), "fonts/Roboto-Thin.ttf");
        tvPressure.setTypeface(face);
        tvForce.setTypeface(face);
        tvHumidity.setTypeface(face);
        tvTemp.setTypeface(face);
        tvSpeed.setTypeface(face);
        tvDirection.setTypeface(face);

        final Spinner spnrPressure = (Spinner) view.findViewById(R.id.dialogUnitSpinnerPressure);
        String[] pressureArray = getArrayFromUnitMap(UnitFactory.Pressure.getAllUnits());
        spnrPressure.setAdapter(new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_dropdown_item, pressureArray));
        spnrPressure.setSelection(positionFromString(unitController.getCurrentPressureUnit(), pressureArray));


        final Spinner spnrForce = (Spinner) view.findViewById(R.id.dialogUnitSpinnerForce);
        String[] ForceArray = getArrayFromUnitMap(UnitFactory.Force.getAllUnits());
        spnrForce.setAdapter(new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_dropdown_item, ForceArray));
        spnrForce.setSelection(positionFromString(unitController.getCurrentForceUnit(), ForceArray));


        final Spinner spnrHumidity = (Spinner) view.findViewById(R.id.dialogUnitSpinnerHumidity);
        String[] humidityArray = getArrayFromUnitMap(UnitFactory.Humidity.getAllUnits());
        spnrHumidity.setAdapter(new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_dropdown_item, humidityArray));
        spnrHumidity.setSelection(positionFromString(unitController.getCurrentHumidityUnit(), humidityArray));


        final Spinner spnrTemperature = (Spinner) view.findViewById(R.id.dialogUnitSpinnerTemp);
        String[] temperatureArray = getArrayFromUnitMap(UnitFactory.Temperature.getAllUnits());
        spnrTemperature.setAdapter(new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_dropdown_item, temperatureArray));
        spnrTemperature.setSelection(positionFromString(unitController.getCurrentTemperatureUnit(), temperatureArray));


        final Spinner spnrSpeed = (Spinner) view.findViewById(R.id.dialogUnitSpinnerSpeed);
        String[] speedArray = getArrayFromUnitMap(UnitFactory.Speed.getAllUnits());
        spnrSpeed.setAdapter(new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_dropdown_item, speedArray));
        spnrSpeed.setSelection(positionFromString(unitController.getCurrentSpeedUnit(), speedArray));

        final Spinner spnrDirection = (Spinner) view.findViewById(R.id.dialogUnitSpinnerDirection);
        String[] directionArray = getArrayFromUnitMap(UnitFactory.Direction.getAllUnits());
        spnrDirection.setAdapter(new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_dropdown_item, directionArray));
        spnrDirection.setSelection(positionFromString(unitController.getCurrentDirectionUnit(), directionArray));


        final UnitsDialog ud = this;

        builder.setNegativeButton(R.string.cancel, null);
        builder.setNeutralButton(R.string.dialog_units_default, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put(Keys.CallbackMap.UnitsDialog, ud);
                map.put(Keys.CallbackMap.UnitsBtnType, Keys.CallbackMap.UnitsDefault);
                callback.callback(map);
            }
        });
        builder.setPositiveButton(R.string.save, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put(Keys.CallbackMap.UnitsDialog, ud);
                map.put(Keys.CallbackMap.UnitsBtnType, Keys.CallbackMap.UnitsSaveNew);
                map.put(Keys.CallbackMap.UnitsPressure, getTypeFromPositionInArray((String) spnrPressure.getSelectedItem()));
                map.put(Keys.CallbackMap.UnitsForce, getTypeFromPositionInArray((String) spnrForce.getSelectedItem()));
                map.put(Keys.CallbackMap.UnitsHumidity, getTypeFromPositionInArray((String) spnrHumidity.getSelectedItem()));
                map.put(Keys.CallbackMap.UnitsTemperature, getTypeFromPositionInArray((String) spnrTemperature.getSelectedItem()));
                map.put(Keys.CallbackMap.UnitsSpeed, getTypeFromPositionInArray((String) spnrSpeed.getSelectedItem()));
                map.put(Keys.CallbackMap.UnitsDirection, getTypeFromPositionInArray((String) spnrDirection.getSelectedItem()));
                callback.callback(map);
            }

        });
        builder.setView(view);

        return builder.create();
    }

    private int getTypeFromPositionInArray(String type) {

        Map<Integer, String> map = UnitFactory.getAllUnits();
        Set<Entry<Integer, String>> set = map.entrySet();
        for (Entry<Integer, String> e : set) {
            if (type.equals(e.getValue()))
                return e.getKey();
        }
        return -1;

    }

    private int positionFromString(String unit, String[] arrayValue) {

        for (int i = 0; i < arrayValue.length; i++) {
            if (arrayValue[i].equals(unit)) {
                return i;
            }
        }

        return -1;
    }


    private String[] getArrayFromUnitMap(Map<Integer, String> map) {
        ArrayList<String> list = new ArrayList<String>();

        Set<Entry<Integer, String>> set = map.entrySet();
        for (Entry<Integer, String> e : set) {
            list.add(e.getValue());
        }

        return list.toArray(new String[list.size()]);
    }


}
