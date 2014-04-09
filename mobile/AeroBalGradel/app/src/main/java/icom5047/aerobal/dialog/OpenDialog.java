package icom5047.aerobal.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import icom5047.aerobal.activities.R;
import icom5047.aerobal.controllers.UserController;
import icom5047.aerobal.interfaces.AeroCallback;
import icom5047.aerobal.resources.Keys;

public class OpenDialog extends DialogFragment {


    private AeroCallback callback;
    private UserController userController;


    public static final String LOCAL = "local";
    public static final String ONLINE = "online";

    public static OpenDialog getOpenDialog(UserController controller, AeroCallback callback) {
        OpenDialog od = new OpenDialog();
        od.setCallback(callback);
        od.setUserController(controller);
        return od;
    }

    private void setCallback(AeroCallback callback) {
        this.callback = callback;
    }

    private void setUserController(UserController userController) {
        this.userController = userController;
    }


    public OpenDialog() {
        super();
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_dialog_open_experiment);
        builder.setIcon(R.drawable.ic_folder);

        //Set Buttons
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                callback.callback(null);
            }
        });

        builder.setNeutralButton(R.string.local, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Map<String, Object> map = new HashMap<String, Object>();

                map.put( Keys.CallbackMap.OpenType, LOCAL);

                callback.callback(map);
            }
        });

        builder.setPositiveButton(R.string.online, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {

                Map<String, Object> map = new HashMap<String, Object>();

                if(userController.isUserLogIn() || true){
                    //TODO: Change For User
                    map.put(Keys.CallbackMap.OpenType, ONLINE);

                }
                else
                    Toast.makeText(getActivity(), R.string.toast_invalid_open_user_not_login, Toast.LENGTH_SHORT).show();

                callback.callback(map);

            }
        });


        return builder.create();
    }
}
