package icom5047.aerobal.controllers;

import icom5047.aerobal.activities.R;
import icom5047.aerobal.dialog.BluetoothDialog;
import icom5047.aerobal.interfaces.AeroCallback;
import icom5047.aerobal.resources.Keys;

import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.view.MenuItem;
import android.widget.Toast;

public class BluetoothController {
	
	private Context context;
	private static final int REQUEST_ENABLE_BT = 12;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothDevice selectedBtDevices;
	private boolean isConnected;
	
	public BluetoothController(Context context){
		this.context = context;
		init();
	}
	
	private void init(){
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		selectedBtDevices = null;
		isConnected = false;
	}
	
	//Has Bluetooth Module
	public boolean hasBluetoothModule(){
		return mBluetoothAdapter != null;
	}
	
	public boolean isBluetoothActive(){
		if(mBluetoothAdapter == null)
			return false;
		return mBluetoothAdapter.isEnabled();
	}
	
	public boolean isControllerConnected(){
		return this.isConnected;
	}

	public Set<BluetoothDevice> getBluethoodDevices(){		
		//Fail Safe
		mBluetoothAdapter =BluetoothAdapter.getDefaultAdapter(); //Refresh for Test
		if (!mBluetoothAdapter.isEnabled()) { 
			Intent enableBtIntent = new Intent(android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE);
			((Activity)this.context).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		return  mBluetoothAdapter.getBondedDevices();
	}
	
	

	private void btErrorToast(){
		Toast.makeText(context, R.string.toast_no_bt_found, Toast.LENGTH_LONG).show();
	}
	

	public DialogFragment getCurrentDialog(final MenuItem item) {
		
		if(mBluetoothAdapter == null){
			this.btErrorToast();
			return null;
		}
		
		BluetoothDialog btDialog = BluetoothDialog.getBluetoothDialog(this, new AeroCallback() {
			@Override
			public void callback(Map<String, Object> payload) {
				
				if((Boolean)payload.get(Keys.CallbackMap.BluetoothConnectedStatus)){
					isConnected = true;
					selectedBtDevices = (BluetoothDevice) payload.get(Keys.CallbackMap.BluetoothDevice);
				}
				else{
					selectedBtDevices = null;
					isConnected = false;
				}
				changeMenuIcon(item);
				((BluetoothDialog) payload.get(Keys.CallbackMap.BluetoothDialog)).dismiss();
				
			}
		});
		return btDialog;
		
	}
	
	private void changeMenuIcon(MenuItem item){
		if(isConnected)
			item.setIcon(R.drawable.ic_bluetooth_connected);
		else
			item.setIcon(R.drawable.ic_bluetooth);
		
	}
	

}
