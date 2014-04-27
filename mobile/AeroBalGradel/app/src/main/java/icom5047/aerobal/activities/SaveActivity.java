package icom5047.aerobal.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import icom5047.aerobal.controllers.ExperimentController;
import icom5047.aerobal.controllers.UserController;
import icom5047.aerobal.fragments.LoadingFragment;
import icom5047.aerobal.fragments.SaveOnlineFragment;
import icom5047.aerobal.http.HttpRequest;
import icom5047.aerobal.http.Server;
import icom5047.aerobal.resources.Keys;


public class SaveActivity extends Activity {

    private ProgressDialog progressDialog;
    private ExperimentController experimentController;
    private UserController userController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open);

        final ActionBar ab = this.getActionBar();

        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);

        //Instance Field


        Bundle bnd = getIntent().getExtras();
        experimentController = (ExperimentController) bnd.getSerializable(Keys.BundleKeys.ExperimentController);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.main_container, new SaveOnlineFragment()).commit();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.progress_load_experiment));

        userController = new UserController(this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.ab_btn_new_session:
                createNewSessionDialog();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void createNewSessionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.title_dialog_new_session);

        View view = View.inflate(this, R.layout.dialog_new_session, null);
        final EditText title = (EditText) view.findViewById(R.id.dialogNesSessionTitle);
        final EditText desc = (EditText) view.findViewById(R.id.dialogNewSessionDesc);
        final RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.dialogNewSessionRadioPublic);
        builder.setView(view);


        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //No-Op
            }
        });

        builder.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(!title.getText().toString().isEmpty() && !desc.getText().toString().isEmpty()){
                    boolean publicSession = true;
                    int id = radioGroup.getCheckedRadioButtonId();
                    switch(id){
                        case R.id.dialogNewSessionPrivateOpt:
                            publicSession = false;
                            break;

                    }
                    doHttpNewSession(title.getText().toString(), desc.getText().toString(), publicSession);
                }
                else{
                    Toast.makeText(getBaseContext(), R.string.toast_error_new_session_params, Toast.LENGTH_SHORT).show();
                }

            }
        });



        Dialog dialog = builder.create();
        dialog.show();

    }

    public void doHttpNewSession(String title, String desc, boolean publicSession) {
        Bundle params = new Bundle();
        params.putString("method", "POST");
        params.putString("url", Server.Session.POST_NEW_SESSION);

        String payload = "name="+title.trim()+"&"+"desc="+desc.trim()+"&"+"isPublic="+publicSession;

        Map<String, String> headers = new HashMap<String, String>();
        headers.put(Server.Headers.TOKEN, userController.getToken());

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.main_container, LoadingFragment.newInstance("Loading...")).commit();
        HttpRequest request = new HttpRequest(params, payload, headers ,HttpRequest.CONTENT_TYPE_X_FORM_URL_ENCODED, new HttpRequest.HttpCallback() {
            @Override
            public void onSucess(JSONObject json) {
                //Works
                Toast.makeText(getBaseContext(), R.string.toast_success_new_session_create, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(JSONObject json) {
                Toast.makeText(getBaseContext(), R.string.toast_net_error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDone() {

                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.main_container, new SaveOnlineFragment()).commit();
            }
        });
        request.execute();
    }

    public void saveExperiment(long sessionId){
        SharedPreferences pref = getSharedPreferences(Keys.SharedPref.UserSharedPreferences, Context.MODE_PRIVATE);
        long experimentId = pref.getLong(Keys.SharedPref.ExperimentId, -1);
        long sessionIdStored = pref.getLong(Keys.SharedPref.SessionId, -1);

        if(experimentId == -1){
            //New Experiment
            doHttpAppendExperiment(sessionId);
        }
        else if (sessionId != sessionIdStored){
            doHttpAppendExperiment(sessionId);
        }
        else{
            doHttpUpdateExperiment(sessionId, experimentId);
        }

    }

    private void doHttpUpdateExperiment(long sessionId, long experimentId) {
        Log.v("update", "update");
    }

    private void doHttpAppendExperiment(long sessionId) {

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.main_container, LoadingFragment.newInstance("Loading...")).commit();
        Bundle params = new Bundle();
        params.putString("method", "POST");
        params.putString("url", Server.Experiments.POST_COMPLETE_EXPERIMENT);

        Map<String, String> headers = new HashMap<String, String>();
        headers.put(Server.Headers.TOKEN, userController.getToken());
        headers.put(Server.Headers.SESSION_ID, sessionId+"");

        JSONObject payload;
        try {
            payload = new JSONObject(experimentController.getExperiment().toString());
        } catch (JSONException e) {
            Toast.makeText(this, R.string.toast_error_json, Toast.LENGTH_SHORT).show();
            return;
        }

        Log.v("Body", payload.toString());

        HttpRequest request = new HttpRequest(params,payload,headers,HttpRequest.CONTENT_TYPE_JSON, new HttpRequest.HttpCallback() {


            @Override
            public void onSucess(JSONObject json) {
                Toast.makeText(getBaseContext(), R.string.toast_success_save_experiment, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailed(JSONObject json) {
                Log.e("error", json.toString());
                Toast.makeText(getBaseContext(), R.string.toast_net_error, Toast.LENGTH_SHORT).show();
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.main_container, new SaveOnlineFragment()).commit();

            }


        });
        request.execute();


    }

}
