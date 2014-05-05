package icom5047.aerobal.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import icom5047.aerobal.activities.R;
import icom5047.aerobal.controllers.BluetoothController;
import icom5047.aerobal.callbacks.AeroCallback;
import icom5047.aerobal.resources.Keys;


public class BluetoothDialog extends DialogFragment {


    private BluetoothController btController;
    private AeroCallback callback;
    //Singleton Instance

    public static BluetoothDialog getBluetoothDialog(BluetoothController btController, AeroCallback callback) {
        BluetoothDialog bd = new BluetoothDialog();
        bd.setBluetoothController(btController);
        bd.setCallback(callback);
        return bd;
    }

    private void setCallback(AeroCallback callback) {
        this.callback = callback;
    }

    private void setBluetoothController(BluetoothController btController) {
        this.btController = btController;
    }

    //Do Not Use
    public BluetoothDialog() {
        super();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflator = getActivity().getLayoutInflater();
        View view = inflator.inflate(R.layout.dialog_bluetooth_list, null);


        final ListView lv = (ListView) view.findViewById(R.id.dialog_lv_bt_selector);

        if (!btController.isAeroBalConnected()) {
            builder.setTitle(R.string.title_dialog_bt_selector);
            Set<BluetoothDevice> btDevices = btController.getBluetoothBondedDevices();
            if (btDevices.size() == 0) {
                lv.setVisibility(View.GONE);
                view.findViewById(R.id.dialog_lv_bt_empty).setVisibility(View.VISIBLE);

            } else {
                //List View
                lv.setAdapter(new BluetoothListAdapter(getActivity(), btDevices));
            }
            final BluetoothDialog bd = this;
            lv.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                        long arg3) {

                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put(Keys.CallbackMap.BluetoothDialog, bd);
                    map.put(Keys.CallbackMap.BluetoothDevice, lv.getItemAtPosition(position));
                    map.put(Keys.CallbackMap.BluetoothConnectedStatus, true);
                    callback.callback(map);
                }
            });
        } else {
            builder.setTitle(R.string.title_dialog_bt_disconenct);
            lv.setVisibility(View.GONE);
            TextView tv = (TextView) view.findViewById(R.id.dialog_lv_bt_empty);
            tv.setVisibility(View.VISIBLE);
            tv.setText(R.string.dialog_bt_disconnect);
            final BluetoothDialog bd = this;
            builder.setNegativeButton(R.string.dialog_bt_no, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //NoOp
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton(R.string.dialog_bt_yes, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put(Keys.CallbackMap.BluetoothDialog, bd);
                    map.put(Keys.CallbackMap.BluetoothDevice, null);
                    map.put(Keys.CallbackMap.BluetoothConnectedStatus, false);
                    callback.callback(map);
                }
            });

        }


        //Builder
        builder.setView(view);

        return builder.create();

    }


    class BluetoothListAdapter extends ArrayAdapter<BluetoothDevice> {
        public BluetoothListAdapter(Context context, Set<BluetoothDevice> objects) {
            super(context, R.layout.row_dialog_bt_lv);
            this.addAll(objects);

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;

            BluetoothDevice btDevice = this.getItem(position);

            LayoutInflater inflater = ((Activity) this.getContext()).getLayoutInflater();
            row = inflater.inflate(R.layout.row_dialog_bt_lv, parent, false);

            TextView tvName = (TextView) row.findViewById(R.id.row_dialog_bt_name);
            TextView tvIp = (TextView) row.findViewById(R.id.row_dialog_bt_ip);

            tvName.setText(btDevice.getName());
            tvIp.setText(btDevice.getAddress());

            row.setTag(btDevice);

            return row;
        }
    }


}
