package icom5047.aerobal.controllers;

import icom5047.aerobal.dialog.UnitsDialog;
import icom5047.aerobal.exceptions.InvalidUnitException;
import icom5047.aerobal.interfaces.AeroCallback;
import icom5047.aerobal.resources.Keys;
import icom5047.aerobal.resources.UnitFactory;

import java.util.Map;

import android.util.SparseArray;

public class UnitController {
	
	private SparseArray<Integer> currValues;
	private SparseArray<Integer> defaults;
	

	public UnitController(SparseArray<Integer> defaults) {
		currValues = defaults.clone();
		this.defaults = defaults;
		
	}
	
	
	public String getCurrentPressureUnit(){
		try {
			return UnitFactory.Pressure.getUnitString(currValues.get(UnitFactory.Type.PRESSURE));
		} catch (InvalidUnitException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public String getCurrentForceUnit(){
		try {
			return UnitFactory.Force.getUnitString(currValues.get(UnitFactory.Type.FORCE));
		} catch (InvalidUnitException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String getCurrentHumidityUnit(){
		try {
			return UnitFactory.Humidity.getUnitString(currValues.get(UnitFactory.Type.HUMIDITY));
		} catch (InvalidUnitException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String getCurrentSpeedUnit(){
		try {
			return UnitFactory.Speed.getUnitString(currValues.get(UnitFactory.Type.SPEED));
		} catch (InvalidUnitException e) {
			e.printStackTrace();
		}
		return null;
	}
	public String getCurrentDirectionUnit(){
		try {
			return UnitFactory.Direction.getUnitString(currValues.get(UnitFactory.Type.DIRECTION));
		} catch (InvalidUnitException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String getCurrentTemperatureUnit(){
		try {
			return UnitFactory.Temperature.getUnitString(currValues.get(UnitFactory.Type.TEMPERATURE));
		} catch (InvalidUnitException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public void setDefaults(){
		this.currValues = defaults.clone();
	}
	
	public void setCurrentType(int type, int value){
		this.currValues.setValueAt(type, value);
	}
	
	public Integer getCurrentType(int type){
		return currValues.get(type);
	}
	

	public UnitsDialog getDialog() {
		UnitsDialog ud = UnitsDialog.getUnitsDialog( this ,new AeroCallback() {
			
			@Override
			public void callback(Map<String, Object> objectMap) {
				
				String type = (String) objectMap.get(Keys.CallbackMap.UnitsBtnType);
				if(type.equals(Keys.CallbackMap.UnitsDefault)){
					setDefaults();
				}
				else{
					setCurrentType(UnitFactory.Type.PRESSURE, (Integer) objectMap.get(Keys.CallbackMap.UnitsPressure));
					setCurrentType(UnitFactory.Type.FORCE, (Integer) objectMap.get(Keys.CallbackMap.UnitsForce));
					setCurrentType(UnitFactory.Type.HUMIDITY, (Integer) objectMap.get(Keys.CallbackMap.UnitsHumidity));
					setCurrentType(UnitFactory.Type.TEMPERATURE, (Integer) objectMap.get(Keys.CallbackMap.UnitsTemperature));
					setCurrentType(UnitFactory.Type.SPEED, (Integer) objectMap.get(Keys.CallbackMap.UnitsSpeed));
					setCurrentType(UnitFactory.Type.DIRECTION, (Integer) objectMap.get(Keys.CallbackMap.UnitsDirection));
				}
				
				
				((UnitsDialog) objectMap.get(Keys.CallbackMap.UnitsDialog)).dismiss();
			}
		});
		return ud;
	}
	
	
	
	
	

}
