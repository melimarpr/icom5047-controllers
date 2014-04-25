package icom5047.aerobal.controllers;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import icom5047.aerobal.activities.R;
import icom5047.aerobal.resources.Keys;

public class UserController implements Serializable{


    private Context context;
    private String[] baseStringArray = new String[]{"New", "Open", "Bluetooth Setup"};
    private String token = "";
    private ProgressDialog progressDialog;
    public static final int REQUEST_LOGIN = 15;


    public UserController(Context context) {
        this.context = context;
        this.progressDialog = new ProgressDialog(context);
        this.progressDialog.setMessage(context.getResources().getString(R.string.progress_token_verification));
        init();
    }

    private void init() {
        SharedPreferences pref = this.context.getSharedPreferences(Keys.SharedPref.UserSharedPreferences, Context.MODE_PRIVATE);
        token = pref.getString(Keys.SharedPref.LoginStatus, "");
    }



    public boolean isUserLogIn() {
        return !token.equals("");
    }


    public void setToken(String token){
        this.token = token;
        SharedPreferences.Editor editor = this.context.getSharedPreferences(Keys.SharedPref.UserSharedPreferences, Context.MODE_PRIVATE).edit();
        editor.putString(Keys.SharedPref.LoginStatus, token);
        editor.commit();
    }


    public List<String> getDrawerList() {
        List<String> returnList = new LinkedList<String>(Arrays.asList(baseStringArray));
        if (isUserLogIn()) {
            returnList.add("Log Out");
        } else {
            returnList.add("Log In");
        }
        return returnList;
    }




    public void logout() {

        token = "";
        SharedPreferences.Editor editor = this.context.getSharedPreferences(Keys.SharedPref.UserSharedPreferences, Context.MODE_PRIVATE).edit();
        editor.putString(Keys.SharedPref.LoginStatus, "");
        editor.commit();
        Toast.makeText(context, R.string.toast_user_logout, Toast.LENGTH_SHORT).show();
    }

    public void invalidateToken(){
        token = "";
    }



}
