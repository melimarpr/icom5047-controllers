package icom5047.aerobal.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.aerobal.data.dto.ExperimentDto;
import com.aerobal.data.objects.Experiment;
import com.aerobal.data.serializers.GlobalGson;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import icom5047.aerobal.controllers.UserController;
import icom5047.aerobal.dialog.OpenDialog;
import icom5047.aerobal.fragments.OpenLocalFragment;
import icom5047.aerobal.fragments.OpenOnlineSessionsFragment;
import icom5047.aerobal.http.HttpRequest;
import icom5047.aerobal.http.Server;
import icom5047.aerobal.resources.Keys;


public class OpenActivity extends Activity {

    private Stack<Fragment> fragmentStack;
    private String type;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open);

        final ActionBar ab = this.getActionBar();

        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);

        fragmentStack = new Stack<Fragment>();
        //Instance Field


        Bundle bnd = getIntent().getExtras();

        if(bnd != null){
            type = bnd.getString(Keys.BundleKeys.OpenType);
        }

        FragmentManager fragmentManager = getFragmentManager();
        if(type.equalsIgnoreCase(OpenDialog.LOCAL)){
            setTitle(R.string.title_activity_open_local);
            OpenLocalFragment openLocalFragment = new OpenLocalFragment();
            Bundle bundle = new Bundle();

            //Load Base
            String extState = Environment.getExternalStorageState();
            if(!extState.equals(Environment.MEDIA_MOUNTED)){
                Toast.makeText(this, R.string.toast_file_not_create, Toast.LENGTH_SHORT).show();
                finish();
            }

            bundle.putSerializable(Keys.BundleKeys.FileRoot, Environment.getExternalStorageDirectory());
            openLocalFragment.setArguments(bundle);
            fragmentStack.push(openLocalFragment);
            fragmentManager.beginTransaction().replace(R.id.main_container, fragmentStack.peek()).commit();

        }
        else{

            setTitle(R.string.title_activity_open_online);
            OpenOnlineSessionsFragment openOnlineFragment = new OpenOnlineSessionsFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable(Keys.BundleKeys.UnitController, bnd.getSerializable(Keys.BundleKeys.UnitController));
            openOnlineFragment.setArguments(bundle);
            fragmentStack.push(openOnlineFragment);
            fragmentManager.beginTransaction().replace(R.id.main_container, fragmentStack.peek()).commit();
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.progress_load_experiment));







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
            case R.id.ab_btn_back:
                folderBack();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void folderBack(){
        if(this.fragmentStack.size() <= 1){
            Toast.makeText(this, R.string.toast_file_parent_error, Toast.LENGTH_SHORT).show();
        }
        else{
            this.fragmentStack.pop();
            //Set Main Fragment
            FragmentManager fm = this.getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.main_container, fragmentStack.peek()).commit();
        }
    }

    public void setActivityResult(Experiment experiment){
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Keys.BundleKeys.Experiment, experiment);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }
    public void setActivityResultDTO(com.aerobal.data.dto.ExperimentDto experiment){

        progressDialog.show();
        doHttpFullExperiment(experiment);


    }

    private void doHttpFullExperiment(final ExperimentDto experimentDto) {

        //URL
        Uri.Builder urlB = Uri.parse(Server.Experiments.GET_EXPERIMENT_COMPLETE).buildUpon();
        urlB.appendQueryParameter(Server.Params.ID, experimentDto.id()+"");
        Log.v("id",experimentDto.id()+"");

        //Params
        final Bundle params = new Bundle();
        params.putString("method", "GET");
        params.putString("url", urlB.toString().trim());

        //Headers
        Map<String, String> headers = new HashMap<String, String>();
        UserController userController = new UserController(this);
        headers.put(Server.Headers.TOKEN, userController.getToken());

        HttpRequest request = new HttpRequest(params, headers, new HttpRequest.HttpCallback() {
            @Override
            public void onSucess(JSONObject json) {

                progressDialog.dismiss();
                SharedPreferences.Editor editor = getSharedPreferences(Keys.SharedPref.UserSharedPreferences, Context.MODE_PRIVATE).edit();
                editor.putLong(Keys.SharedPref.ExperimentId, experimentDto.id());
                editor.putLong(Keys.SharedPref.SessionId, experimentDto.sessionId());
                editor.commit();
                Gson gson = GlobalGson.gson();
                Experiment experiment = gson.fromJson(json.toString(), Experiment.class);
                setActivityResult(experiment);
            }

            @Override
            public void onFailed(JSONObject jsonObject) {
                Log.e("Error", jsonObject.toString());
                Toast.makeText(getBaseContext(), R.string.toast_net_error, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onDone() {
                progressDialog.dismiss();
            }
        });
        request.execute();
    }

    public void loadFragmentIntoActivity(Fragment fragment){
        //Set Main Fragment
        FragmentManager fm = this.getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        fragmentStack.push(fragment);
        ft.replace(R.id.main_container, fragmentStack.peek()).commit();
    }

    @Override
    public void onBackPressed() {
        //Normal Back if no other Fragment is Use
        if(this.fragmentStack.size() <= 1){
            super.onBackPressed();
        }
        else{
            this.fragmentStack.pop();
            //Set Main Fragment
            FragmentManager fm = this.getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.main_container, fragmentStack.peek()).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.open, menu);
        return true;
    }

}
