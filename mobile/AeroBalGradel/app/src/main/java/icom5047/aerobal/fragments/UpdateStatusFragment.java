package icom5047.aerobal.fragments;

import android.graphics.Typeface;
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
public class UpdateStatusFragment extends Fragment {

    private String message;
    private ListView updateList;
    private Bundle bundle;
    private TextView noData;
    private volatile UnitController unitController;



    public static UpdateStatusFragment newInstance(String message, Bundle updateData, UnitController unitController) {
        UpdateStatusFragment fragment = new UpdateStatusFragment();
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
        View view = inflater.inflate(R.layout.fragment_update, container, false);
        ((TextView) view.findViewById(R.id.fragLoadingMessage)).setText(message);
        updateList = (ListView) view.findViewById(R.id.fragExpLoadingListView);
        noData = (TextView) view.findViewById(R.id.fragExpLoadingNoData);
        TextView summary = (TextView) view.findViewById(R.id.fragExpLoadingSummary);
        Typeface tf = Typeface.createFromAsset(this.getActivity().getAssets(), "fonts/Roboto-Thin.ttf");
        summary.setTypeface(tf);
        noData.setTypeface(tf);



        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(bundle != null) {
            ValueContainer[] currentContainers = new ValueContainer[]{
                    new ValueContainer(getString(R.string.cur_state), bundle.getString(Keys.UpdateKeys.State), true, false, ""),
                    new ValueContainer(getString(R.string.curr_wind_speed),
                            unitController.convertFromDefaultToCurrent(GlobalConstants.Measurements.WindSpeedKey, bundle.getDouble(Keys.UpdateKeys.WindSpeed)),
                            false, true, unitController.getCurrentSpeedUnit()),
                    new ValueContainer(getString(R.string.curr_wind_direction),
                            unitController.convertFromDefaultToCurrent(GlobalConstants.Measurements.WindDirectionKey, bundle.getDouble(Keys.UpdateKeys.WindDirection)),
                            false, true, unitController.getCurrentDirectionUnit()),
                    new ValueContainer(getString(R.string.curr_temperature),
                            unitController.convertFromDefaultToCurrent(GlobalConstants.Measurements.TemperatureKey, bundle.getDouble(Keys.UpdateKeys.Temperature)),
                            false, true, unitController.getCurrentTemperatureUnit()),
                    new ValueContainer(getString(R.string.curr_humidity),
                            unitController.convertFromDefaultToCurrent(GlobalConstants.Measurements.HumidityKey, bundle.getDouble(Keys.UpdateKeys.Humidity)),
                            false, true, unitController.getCurrentHumidityUnit()),
                    new ValueContainer(getString(R.string.curr_pressure),
                            unitController.convertFromDefaultToCurrent(GlobalConstants.Measurements.PressureKey, bundle.getDouble(Keys.UpdateKeys.Pressure)),
                            false, true, unitController.getCurrentPressureUnit()),
                    new ValueContainer(getString(R.string.curr_side),
                            unitController.convertFromDefaultToCurrent(GlobalConstants.Measurements.SideKey, bundle.getDouble(Keys.UpdateKeys.Side)),
                            false, true, unitController.getCurrentForceUnit()),
                    new ValueContainer(getString(R.string.curr_drag),
                            unitController.convertFromDefaultToCurrent(GlobalConstants.Measurements.DragKey, bundle.getDouble(Keys.UpdateKeys.Drag)),
                            false, true, unitController.getCurrentForceUnit()),
                    new ValueContainer(getString(R.string.curr_lift),
                            unitController.convertFromDefaultToCurrent(GlobalConstants.Measurements.LiftKey, bundle.getDouble(Keys.UpdateKeys.Lift)),
                            false, true, unitController.getCurrentForceUnit())
            };
            noData.setVisibility(View.GONE);
            updateList.setAdapter(new ValueAdapter(this.getActivity(), currentContainers));
            updateList.setVisibility(View.VISIBLE);
        } else {
            updateList.setVisibility(View.GONE);
            noData.setVisibility(View.VISIBLE);
        }

    }





}
