package icom5047.aerobal.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import icom5047.aerobal.activities.R;
import icom5047.aerobal.adapters.ValueAdapter;
import icom5047.aerobal.containers.ValueContainer;
import icom5047.aerobal.controllers.UnitController;
import icom5047.aerobal.resources.GlobalConstants;
import icom5047.aerobal.resources.Keys;

/**
 * Created by enrique on 5/2/14.
 */
public class LoadingSupportFragment extends Fragment {

    private String message;
    private ListView updateList;
    private Bundle bundle;
    private volatile UnitController unitController;



    public static LoadingSupportFragment newInstance(String message, Bundle updateData, UnitController unitController) {
        LoadingSupportFragment fragment = new LoadingSupportFragment();
        fragment.setMessage(message);
        fragment.setUpdateBundle(updateData);
        fragment.setUnitController(unitController);
        return fragment;
    }

    private void setMessage(String message){
        this.message = message;
    }
    private void setUpdateBundle(Bundle bundle){this.bundle = bundle; }
    private void setUnitController(UnitController unitController){ this.unitController = unitController; }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_loading_update, container, false);
        ((TextView) view.findViewById(R.id.fragLoadingMessage)).setText(message);
        updateList = (ListView) view.findViewById(R.id.fragExpLoadingListView);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(bundle != null) {
            ValueContainer[] averageContainers = new ValueContainer[]{
                    new ValueContainer(getString(R.string.frag_exp_sum_num_of_runs), bundle.getString(Keys.UpdateKeys.State), true, false, ""),
                    new ValueContainer(getString(R.string.ave_wind_speed),
                            unitController.convertFromDefaultToCurrent(GlobalConstants.Measurements.WindSpeedKey, bundle.getDouble(Keys.UpdateKeys.WindSpeed)),
                            false, true, unitController.getCurrentSpeedUnit()),
                    new ValueContainer(getString(R.string.ave_wind_direction),
                            unitController.convertFromDefaultToCurrent(GlobalConstants.Measurements.WindDirectionKey, bundle.getDouble(Keys.UpdateKeys.WindDirection)),
                            false, true, unitController.getCurrentDirectionUnit()),
                    new ValueContainer(getString(R.string.ave_temperature),
                            unitController.convertFromDefaultToCurrent(GlobalConstants.Measurements.TemperatureKey, bundle.getDouble(Keys.UpdateKeys.Temperature)),
                            false, true, unitController.getCurrentTemperatureUnit()),
                    new ValueContainer(getString(R.string.ave_humidity),
                            unitController.convertFromDefaultToCurrent(GlobalConstants.Measurements.HumidityKey, bundle.getDouble(Keys.UpdateKeys.Humidity)),
                            false, true, unitController.getCurrentHumidityUnit()),
                    new ValueContainer(getString(R.string.ave_pressure),
                            unitController.convertFromDefaultToCurrent(GlobalConstants.Measurements.PressureKey, bundle.getDouble(Keys.UpdateKeys.Pressure)),
                            false, true, unitController.getCurrentPressureUnit()),
                    new ValueContainer(getString(R.string.ave_side),
                            unitController.convertFromDefaultToCurrent(GlobalConstants.Measurements.SideKey, bundle.getDouble(Keys.UpdateKeys.Side)),
                            false, true, unitController.getCurrentForceUnit()),
                    new ValueContainer(getString(R.string.ave_drag),
                            unitController.convertFromDefaultToCurrent(GlobalConstants.Measurements.DragKey, bundle.getDouble(Keys.UpdateKeys.Drag)),
                            false, true, unitController.getCurrentForceUnit()),
                    new ValueContainer(getString(R.string.ave_lift),
                            unitController.convertFromDefaultToCurrent(GlobalConstants.Measurements.LiftKey, bundle.getDouble(Keys.UpdateKeys.Lift)),
                            false, true, unitController.getCurrentForceUnit())
            };

            updateList.setAdapter(new ValueAdapter(this.getActivity(), averageContainers));
        }

    }





}
