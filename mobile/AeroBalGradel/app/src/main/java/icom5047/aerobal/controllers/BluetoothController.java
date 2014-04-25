package icom5047.aerobal.controllers;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import icom5047.aerobal.activities.R;
import icom5047.aerobal.comm.BluetoothDataManager;
import icom5047.aerobal.dialog.BluetoothDialog;
import icom5047.aerobal.interfaces.AeroCallback;
import icom5047.aerobal.resources.Keys;

public class BluetoothController {

    private Context context;
    private static final UUID AERO_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Unique UUID for SSP
    public static final int REQUEST_ENABLE_BT = 12;
    private boolean isAerobalConnected;
    private BluetoothAdapter mBluetoothAdapter;
    private volatile BluetoothSocket mBtSocket;

    public BluetoothController(Context context) {
        //Set Context
        this.context = context;
        //Connected Bool
        this.isAerobalConnected = false;
        //Obtain Bluetooth Adapter (Radio
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    }


    /* ===================== if Type Methods ================= */

    public boolean hasBluetoothRadio() {
        return mBluetoothAdapter != null;
    }

    public boolean isBluetoothRadioActive() {
        //Adapter Doesn't Exist Does Inactive
        if (mBluetoothAdapter == null)
            return false;
        return mBluetoothAdapter.isEnabled();
    }


    public boolean isAeroBalConnected() {
        return this.isAerobalConnected;
    }

    /* ===================== Wrapper Methods ================*/
    public Set<BluetoothDevice> getBluetoothBondedDevices(){
        return mBluetoothAdapter.getBondedDevices();
    }



    //Return
    public DialogFragment getCurrentDialog(final MenuItem item) {

        //Error Checking Before  Creating Dialog

        //Adapter Exists
        if (mBluetoothAdapter == null) {
            this.btNotFoundErrorToast();
            return null;
        }
        //Make Sure Radio is Active
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((Activity) this.context).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return null;
        }

        BluetoothDialog btDialog = BluetoothDialog.getBluetoothDialog(this, new AeroCallback() {
            @Override
            public void callback(Map<String, Object> payload) {

                if ((Boolean) payload.get(Keys.CallbackMap.BluetoothConnectedStatus)) {
                    isAerobalConnected = true;
                    connectToManager((BluetoothDevice) payload.get(Keys.CallbackMap.BluetoothDevice));
                } else {
                    isAerobalConnected = false;
                    try{
                        mBtSocket.close();
                    } catch (IOException e) {
                        Log.w("BtError:","No need to Close");
                    } //Doesn't Mater is Just A Fail Safe


                }
                changeMenuIcon(item, isAerobalConnected);
                ((BluetoothDialog) payload.get(Keys.CallbackMap.BluetoothDialog)).dismiss();

            }
        });
        return btDialog;

    }

    private void connectToManager(BluetoothDevice selectedBtDevices) {
        BluetoothDevice actual = mBluetoothAdapter.getRemoteDevice(selectedBtDevices.getAddress());

        //Create Socket

        try {
            mBtSocket = actual.createInsecureRfcommSocketToServiceRecord(AERO_UUID);
        } catch (IOException e) {
            Log.w("BTError", "Unable to Create Connection");
            Toast.makeText(context, R.string.toast_bt_unable_to_connect, Toast.LENGTH_SHORT).show();
            isAerobalConnected = false;
            return;
        }

        if (!mBtSocket.isConnected()) {
            try {
                mBtSocket.connect();
            } catch (IOException e) {
                Log.w("BTError", "Unable to .connect()");
                Toast.makeText(context, R.string.toast_bt_unable_to_connect, Toast.LENGTH_SHORT).show();
                isAerobalConnected = false;
            }
        }
    }


    public BluetoothDataManager getIOManager(){

        if(isAerobalConnected){
            return new BluetoothDataManager(mBtSocket);
        }

        Toast.makeText(context, R.string.toast_bt_not_connected, Toast.LENGTH_SHORT).show();
        return null;
    }




/*================= Graphic Related Methods ===================*/


    private void changeMenuIcon(MenuItem item, boolean isConnected) {
        if (!isConnected)
            item.setIcon(R.drawable.ic_bluetooth_connected);
        else
            item.setIcon(R.drawable.ic_bluetooth);

    }


    //Error Just For Context Switch
    public void btNotFoundErrorToast() {
        Toast.makeText(context, R.string.toast_no_bt_found, Toast.LENGTH_LONG).show();
    }




}
