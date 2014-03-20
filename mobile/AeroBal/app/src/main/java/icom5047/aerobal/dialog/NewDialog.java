package icom5047.aerobal.dialog;

import icom5047.aerobal.activities.R;
import icom5047.aerobal.controllers.UnitController;
import icom5047.aerobal.interfaces.AeroCallback;
import icom5047.aerobal.resources.GlobalConstants;
import icom5047.aerobal.resources.Keys;
import icom5047.aerobal.resources.UnitFactory;

import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.aerobal.data.objects.Experiment;

public class NewDialog extends DialogFragment {
	
	
	private AeroCallback callback;
	private UnitController unitController;
	
	public static NewDialog getNewDialog(UnitController unitController, AeroCallback callback){
		NewDialog nd = new NewDialog();
		nd.setCallback(callback);
		nd.setUnitController(unitController);
		return nd;
	}
	
	private void setUnitController(UnitController unitController){
		this.unitController = unitController;
	}
	
	private void setCallback(AeroCallback callback){
		this.callback = callback;
	}
	
	
	public NewDialog(){
		super();
	}
	
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = this.getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.dialog_new_experiment, null);
		
		
		//Change TypeFace
		TextView name = (TextView) view.findViewById(R.id.dialogNewExpTvName);
		TextView interval = (TextView) view.findViewById(R.id.dialogNewExpTvTimeInterval);
		TextView sample = (TextView) view.findViewById(R.id.dialogNewExpTvSample);
		TextView speed = (TextView) view.findViewById(R.id.dialogNewExpTvWindSpeed);
		Typeface face = Typeface.createFromAsset(this.getActivity().getAssets(), "fonts/Roboto-Thin.ttf");
		name.setTypeface(face);
		interval.setTypeface(face);
		sample.setTypeface(face);
		speed.setTypeface(face);
		
		//Units
		TextView wSpeedUnit = (TextView) view.findViewById(R.id.dialogNewExpTvWindSpeedUnit);
		wSpeedUnit.setText(unitController.getCurrentSpeedUnit());
		
		//Get Edit Text
		final EditText etName = (EditText) view.findViewById(R.id.dialogNewExpEtName);
		final EditText etInterval = (EditText) view.findViewById(R.id.dialogNewExpEtTimeInterval);
		final EditText etSample = (EditText) view.findViewById(R.id.dialogNewExpEtSample);
		final EditText etSpeed = (EditText) view.findViewById(R.id.dialogNewExpEtWindSpeed);
		
		etInterval.setHint("[ "+GlobalConstants.TimeIntervalMinimum+" , "+GlobalConstants.TimeIntervalMaximum+" ]");
		etSample.setHint("[ "+GlobalConstants.SamplesMinimum+" , "+GlobalConstants.SampleMaximum+" ]");
		etSpeed.setHint("[ "+UnitFactory.Speed.convert(GlobalConstants.WindSpeedMinimumKMPH, UnitFactory.Speed.UNIT_KMPH, unitController.getCurrentType(UnitFactory.Type.SPEED))
				+" , "+
				UnitFactory.Speed.convert(GlobalConstants.WindSpeedMaximumKMPH, UnitFactory.Speed.UNIT_KMPH, unitController.getCurrentType(UnitFactory.Type.SPEED))
				+" ]");
		
		
		etInterval.setInputType(InputType.TYPE_CLASS_NUMBER);
		
		
		
		
		builder.setTitle(R.string.title_dialog_new_experiment);
		
		builder.setView(view);
		
		
		builder.setNegativeButton(R.string.cancel, null);
		
		final NewDialog nd = this;
		builder.setPositiveButton(R.string.create, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				if(validateName(etName) && validateWindSpeed(etSpeed) && validateTimeInterval(etInterval) && validateSample(etSample) ){
					//Get Data send Info
					
					//TODO: Change to new Constructor
					Experiment exp = new Experiment(-1, -1, 
							etName.getText().toString(), 
							Integer.parseInt(etSample.getText().toString()), 
							Integer.parseInt(etInterval.getText().toString()),
							Double.parseDouble(etSpeed.getText().toString()), 
							null);
					Map<String, Object> map = new HashMap<String, Object>();
					map.put(Keys.CallbackMap.NewExperimentDialog, nd);
					map.put(Keys.CallbackMap.NewExperimentObject, exp);
					
					callback.callback(map);
				}
			}
		} );
		
		return builder.create();
	}
	
	private boolean validateTimeInterval(EditText interval){
		String strInterval = interval.getText().toString();
		if(strInterval.isEmpty()){
			Toast.makeText(getActivity(), R.string.toast_invalid_exp_interval_empty, Toast.LENGTH_SHORT).show();
			return false;
		}
		
		int numericValue = Integer.parseInt(strInterval);
		
		if(numericValue < GlobalConstants.TimeIntervalMinimum  ){
			Toast.makeText(getActivity(), getString(R.string.toast_invalid_exp_interval_min)+" "+numericValue, Toast.LENGTH_SHORT).show();
			return false;
		}
		
		if(numericValue > GlobalConstants.TimeIntervalMaximum){
			
			Toast.makeText(getActivity(), getString(R.string.toast_invalid_exp_interval_max)+" "+numericValue, Toast.LENGTH_SHORT).show();
			return false;
		}
		
		return true;
	}
	
	private boolean validateSample(EditText sample){
		String strInterval = sample.getText().toString();
		if(strInterval.isEmpty()){
			Toast.makeText(getActivity(), R.string.toast_invalid_exp_samples_empty, Toast.LENGTH_SHORT).show();
			return false;
		}
		
		int numericValue = Integer.parseInt(strInterval);
		
		if(numericValue < GlobalConstants.SamplesMinimum  ){
			Toast.makeText(getActivity(), getString(R.string.toast_invalid_exp_samples_min)+" "+numericValue, Toast.LENGTH_SHORT).show();
			return false;
		}
		
		if(numericValue > GlobalConstants.SampleMaximum){
			
			Toast.makeText(getActivity(), getString(R.string.toast_invalid_exp_samples_max)+" "+numericValue, Toast.LENGTH_SHORT).show();
			return false;
		}
		
		return true;
	}
	
	private boolean validateWindSpeed(EditText speed){
		
		String strSpeed = speed.getText().toString();
		
		if(strSpeed.isEmpty()){
			Toast.makeText(getActivity(), R.string.toast_invalid_exp_wind_speed_empty, Toast.LENGTH_SHORT).show();
			return false;
		}
		
		double numericValue = Double.parseDouble(strSpeed);
		
		if(UnitFactory.Speed.convert(numericValue, unitController.getCurrentType(UnitFactory.Type.SPEED), UnitFactory.Speed.UNIT_KMPH) < GlobalConstants.WindSpeedMinimumKMPH){
			
			Toast.makeText(getActivity(), getString(R.string.toast_invalid_exp_wind_speed_min)+" "+numericValue+" "+unitController.getCurrentSpeedUnit(), Toast.LENGTH_SHORT).show();
			return false;
		}
		
		if(UnitFactory.Speed.convert(numericValue, unitController.getCurrentType(UnitFactory.Type.SPEED), UnitFactory.Speed.UNIT_KMPH) > GlobalConstants.WindSpeedMaximumKMPH){
			
			Toast.makeText(getActivity(), getString(R.string.toast_invalid_exp_wind_speed_max)+" "+numericValue+" "+unitController.getCurrentSpeedUnit(), Toast.LENGTH_SHORT).show();
			return false;
		}
		
		return true;
	}
	
	private boolean validateName(EditText name){
		
		String strName = name.getText().toString();
		
		if(strName.isEmpty())
		{
			Toast.makeText(getActivity(), R.string.toast_invalid_exp_name_empty, Toast.LENGTH_SHORT).show();
			return false;
		}
		
		if(strName.length() > GlobalConstants.CharacterLimit){
			Toast.makeText(getActivity(), R.string.toast_invalid_exp_name_empty, Toast.LENGTH_SHORT).show();
			return false;
		}
		
		return true;
	}

}
