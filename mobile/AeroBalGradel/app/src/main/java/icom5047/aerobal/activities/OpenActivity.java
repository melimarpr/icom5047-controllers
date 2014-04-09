package icom5047.aerobal.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

import icom5047.aerobal.controllers.ExperimentController;
import icom5047.aerobal.dialog.OpenDialog;
import icom5047.aerobal.resources.Keys;


public class OpenActivity extends Activity {

    private ExperimentController experimentController;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open);


        Bundle bnd = getIntent().getExtras();

        if(bnd != null){
            type = bnd.getString(Keys.BundleKeys.OpenType);
            experimentController = (ExperimentController) bnd.getSerializable(Keys.BundleKeys.ExperimentController);
        }


        if(type.equalsIgnoreCase(OpenDialog.LOCAL)){
            setTitle(R.string.title_activity_open_local);
        }

        setTitle(R.string.title_activity_open_online);





    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
