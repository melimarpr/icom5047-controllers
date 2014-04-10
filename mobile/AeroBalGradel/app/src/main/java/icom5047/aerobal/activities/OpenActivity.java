package icom5047.aerobal.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import icom5047.aerobal.controllers.ExperimentController;
import icom5047.aerobal.dialog.OpenDialog;
import icom5047.aerobal.resources.Keys;


public class OpenActivity extends Activity {

    private ExperimentController experimentController;
    private String type;
    private ProgressBar progressBar;
    private TextView textViewNotFound;
    private ListView listViewOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open);

        final ActionBar ab = this.getActionBar();

        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);

        //Instance Field
        progressBar = (ProgressBar) findViewById(R.id.activityOpenProgress);
        textViewNotFound = (TextView) findViewById(R.id.activityOpenNoSession);
        listViewOptions = (ListView) findViewById(R.id.activityOpenListView);


        Bundle bnd = getIntent().getExtras();

        if(bnd != null){
            type = bnd.getString(Keys.BundleKeys.OpenType);
            experimentController = (ExperimentController) bnd.getSerializable(Keys.BundleKeys.ExperimentController);
        }


        if(type.equalsIgnoreCase(OpenDialog.LOCAL)){
            doHttpOnline();
            setTitle(R.string.title_activity_open_local);
        }
        else{
            setTitle(R.string.title_activity_open_online);
            doHttpOnline();
        }







    }

    private void doHttpOnline() {

        progressBar.setVisibility(View.GONE);
        textViewNotFound.setVisibility(View.VISIBLE);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
