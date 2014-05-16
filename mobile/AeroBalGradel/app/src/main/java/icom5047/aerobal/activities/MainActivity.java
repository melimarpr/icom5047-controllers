package icom5047.aerobal.activities;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.aerobal.data.objects.Experiment;
import com.aerobal.data.objects.Run;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import icom5047.aerobal.adapters.DrawerAdapter;
import icom5047.aerobal.callbacks.AeroCallback;
import icom5047.aerobal.controllers.BluetoothController;
import icom5047.aerobal.controllers.ExperimentController;
import icom5047.aerobal.controllers.UnitController;
import icom5047.aerobal.controllers.UserController;
import icom5047.aerobal.dialog.NewDialog;
import icom5047.aerobal.dialog.OpenDialog;
import icom5047.aerobal.fragments.EmptyFragment;
import icom5047.aerobal.fragments.ExperimentFragment;
import icom5047.aerobal.fragments.UpdateStatusFragment;
import icom5047.aerobal.resources.GlobalConstants;
import icom5047.aerobal.resources.Keys;
import icom5047.aerobal.resources.UnitFactory;
import icom5047.aerobal.services.BluetoothService;
import scala.collection.JavaConversions;

public class MainActivity extends FragmentActivity {


    //Drawer Vars
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerAdapter mDrawerAdapter;

    //Menu Items
    private Menu abMenu;
    private boolean expMenuBoolean;


    //Controllers Instants Fields
    private volatile UserController userController;
    private volatile BluetoothController btController;
    private volatile UnitController unitController;
    private volatile ExperimentController experimentController;




    //Error, or Success Run Receiver
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            //Get Bundle
            Bundle bundle = intent.getExtras();

            if(bundle != null){
                //Edit Values
                if(bundle.getBoolean(BluetoothService.Keys.ERROR, false)){
                    experimentController.setRunning(false);
                    btController.reset();
                    expMenuBoolean = true;

                    //Invalidate View
                    invalidateOptionsMenu();
                    onResume();
                }
                else{

                    Experiment experimentWithRun = (Experiment) bundle.getSerializable(Keys.BundleKeys.Experiment);
                    experimentController.setExperiment(experimentWithRun);
                    experimentController.setRunning(false);
                    btController.reset();
                    expMenuBoolean = true;

                    invalidateOptionsMenu();
                    onResume();

                }
            } else{
                Toast.makeText(MainActivity.this, R.string.toast_error_run_fail, Toast.LENGTH_SHORT).show();
            }
        }
    };

    //Update Bundle

    private Bundle updateBundle;
    public static final String BROADCAST_CODE = "LoadingSupportFragment";
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if(bundle != null) {
                updateBundle = bundle;
                onResume();
            }
        }
    };
    private Intent btIntent;


    @SuppressLint("UseSparseArrays")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            // Activity was brought to front and not created,
            // Thus finishing this will get us to the last viewed activity
            Log.v("MA:", "Closed");
            finish();
            return;
        }
        setContentView(R.layout.activity_main);

		
		/* ------------------------------ Application Logic ------------------------- */
        userController = new UserController(this);
        btController = new BluetoothController(this);
        experimentController = new ExperimentController();


        HashMap<Integer, Integer> values = new HashMap<Integer, Integer>();
        values.put(UnitFactory.Type.PRESSURE, UnitFactory.Pressure.UNIT_PASCAL);
        values.put(UnitFactory.Type.FORCE, UnitFactory.Force.UNIT_POUNDS);
        values.put(UnitFactory.Type.HUMIDITY, UnitFactory.Humidity.UNIT_PERCENTAGE);
        values.put(UnitFactory.Type.TEMPERATURE, UnitFactory.Temperature.UNIT_CELSIUS);
        values.put(UnitFactory.Type.SPEED, UnitFactory.Speed.UNIT_MPH);
        values.put(UnitFactory.Type.DIRECTION, UnitFactory.Direction.UNIT_DEGREES);

        unitController = new UnitController(values);

        this.expMenuBoolean = false;
		
		
		/* ------------------------------ View Logic ------------------------- */

        //Set Action Bar Settings
        final ActionBar ab = this.getActionBar();
        ab.setTitle(R.string.title_actvity_main_menu);


        //==Set Up the List==
        mDrawerList = (ListView) findViewById(R.id.drawer_list);

        // set up the drawer's list view with items and click listener
        mDrawerAdapter = new DrawerAdapter(this, userController.getDrawerList());
        mDrawerList.setAdapter(mDrawerAdapter);

        //Set Drawer List to React to Click
        mDrawerList.setOnItemClickListener(new DrawerOnClickListener());

        //==Init Drawer Layout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View view) { //Callback for toggle
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);



    }

    @Override
    protected void onNewIntent(Intent intent) {

        super.onNewIntent(intent);
        Log.v("MA:", "onNewIntent");
        Bundle bundle = intent.getExtras();

        if(bundle != null){
            Log.v("MA:", bundle.toString());

            //Edit Values
            if(bundle.getBoolean(BluetoothService.Keys.ERROR, false)){
                experimentController.setRunning(false);
                btController.reset();
                expMenuBoolean = true;

                //Invalidate View
                invalidateOptionsMenu();
                onResume();
            }
            else{

                Experiment experimentWithRun = (Experiment) bundle.getSerializable(Keys.BundleKeys.Experiment);
                experimentController.setExperiment(experimentWithRun);
                experimentController.setRunning(false);
                btController.reset();
                expMenuBoolean = true;

                invalidateOptionsMenu();
                onResume();

            }
        }




    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(btIntent != null)
            stopService(btIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(receiver, new IntentFilter(BluetoothService.BROADCAST_CODE));
        registerReceiver(broadcastReceiver, new IntentFilter(BROADCAST_CODE));


        //Refresh Drawer
        mDrawerAdapter.clear();
        mDrawerAdapter.addAll(userController.getDrawerList());
        mDrawerAdapter.notifyDataSetChanged();

        //Check Data
        //Check if Opened
        Uri data = getIntent().getData();
        if(data != null) {
            getIntent().setData(null);
            try {
                importFileData(data);
            } catch (Exception e) {
                // warn user about bad data here
                finish();
                Toast.makeText(this, R.string.toast_error_bad_data, Toast.LENGTH_SHORT).show();
            }
        }

        //Set experiment
        if(experimentController.isRunning()){
            setWaitingFragment();
            return;
        }
        else if(experimentController.isExperimentSet()){
            resetExperimentFragment();
            return;
        }

        //Set Default Fragment
        FragmentManager fm = this.getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.content_frame, new EmptyFragment(), Keys.FragmentTag.EmptyTag).commit();

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        unregisterReceiver(broadcastReceiver);
    }

    private void importFileData(Uri data) {

        final String scheme = data.getScheme();
        Log.v("Data", "importFileData");
        if(ContentResolver.SCHEME_FILE.equals(scheme)) {
            ContentResolver cr = getContentResolver();
            InputStream is = null;
            try {
                is = cr.openInputStream(data);
            } catch (FileNotFoundException e) {
                Toast.makeText(this, R.string.toast_error_bad_data, Toast.LENGTH_SHORT).show();
            }
            if(is == null) return;

            try {
                ObjectInputStream input = new ObjectInputStream(is);
                Experiment experiment = (Experiment) input.readObject();
                experimentController.setExperiment(experiment);
                is.close();
                input.close();
            } catch (IOException e) {
                Toast.makeText(this, R.string.toast_error_bad_data, Toast.LENGTH_SHORT).show();
            } catch (ClassNotFoundException e) {
                Toast.makeText(this, R.string.toast_error_bad_data, Toast.LENGTH_SHORT).show();
            }
        }
     }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Activity Result for Open
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case Keys.ActivityOnResult.OpenKey:

                if(resultCode == RESULT_OK){
                    Experiment experiment = (Experiment) data.getExtras().getSerializable(Keys.BundleKeys.Experiment);
                    newExperimentFragmentLoader(experiment);

                }
                break;

            case BluetoothController.REQUEST_ENABLE_BT:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, R.string.toast_bt_activation_cancel, Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(this, R.string.toast_bt_enable, Toast.LENGTH_SHORT).show();
                }
                break;
            case UserController.REQUEST_LOGIN:
                if(resultCode == RESULT_OK ){
                    String token = data.getExtras().getString(Keys.BundleKeys.UserToken);
                    userController.setToken(token);
                    onResume();
                }
                break;
        }
    }

    /*======================== Action Bar Methods ========================*/

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        if(btController.isAeroBalConnected()){
            menu.findItem(R.id.ab_btn_bluetooth).setIcon(R.drawable.ic_bluetooth_connected);
        } else {
            menu.findItem(R.id.ab_btn_bluetooth).setIcon(R.drawable.ic_bluetooth);
        }

        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);

        getActionBar().setHomeButtonEnabled(!experimentController.isRunning());
        menu.findItem(R.id.ab_btn_bluetooth).setEnabled(!experimentController.isRunning());
        menu.findItem(R.id.ab_btn_units).setEnabled(!experimentController.isRunning());
        if(experimentController.isRunning())
             mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        else
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);


        if (expMenuBoolean) {
            menu.findItem(R.id.ab_btn_start_run).setVisible(!drawerOpen);
            menu.findItem(R.id.ab_btn_show_data).setVisible(!drawerOpen);
            menu.findItem(R.id.ab_btn_save_online).setVisible(!drawerOpen);
            menu.findItem(R.id.ab_btn_export).setVisible(!drawerOpen);
            menu.findItem(R.id.ab_btn_close).setVisible(!drawerOpen);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        this.abMenu = menu; //Get Action Bar Menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.ab_btn_bluetooth:
                DialogFragment df = btController.getCurrentDialog(abMenu.findItem(R.id.ab_btn_bluetooth));
                if (df != null)
                    df.show(getSupportFragmentManager(), "Bluetooth Dialog");
                break;
            case R.id.ab_btn_units:
                unitController.getDialog(new AeroCallback() {
                    @Override
                    public void callback(Map<String, Object> objectMap) {

                        Fragment frag = getSupportFragmentManager().findFragmentByTag(Keys.FragmentTag.ExperimentTag);
                        if(frag != null && frag.isVisible()){
                            FragmentManager fm = getSupportFragmentManager();
                            FragmentTransaction ft = fm.beginTransaction();
                            ft.replace(R.id.content_frame, ExperimentFragment.getExperimentFragment(experimentController, unitController), Keys.FragmentTag.ExperimentTag);
                            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                            ft.commit();
                        }

                    }
                }).show(getSupportFragmentManager(), "Units Dialog");
                break;
            default:
                //If More Check Other Item Selected
                onExperimentLoadedItemSelected(item.getItemId());
                break;
        }

        return true;
    }

    public void onExperimentLoadedItemSelected(int id) {


        switch (id) {
            case R.id.ab_btn_start_run:
                if(btController.isAeroBalConnected())
                    onStartRunListener();
                else
                    Toast.makeText(this, R.string.toast_bt_not_connected, Toast.LENGTH_SHORT ).show();
                break;
            case R.id.ab_btn_show_data:
                //Start New Activity
                Intent intent = new Intent(this, DataDetailActivity.class);
                //Send Extra
                Bundle bundle = new Bundle();
                bundle.putSerializable(Keys.BundleKeys.ExperimentController, experimentController);
                bundle.putSerializable(Keys.BundleKeys.UnitController, unitController);
                intent.putExtras(bundle);
                this.startActivity(intent);
                break;
            case R.id.ab_btn_save_online:
                if(userController.isUserLogIn()){
                    Intent saveIntent = new Intent(this, SaveActivity.class);
                    Bundle saveBnd = new Bundle();
                    saveBnd.putSerializable(Keys.BundleKeys.ExperimentController, experimentController);
                    saveIntent.putExtras(saveBnd);
                    startActivity(saveIntent);
                } else{
                    Toast.makeText(this, R.string.toast_invalid_open_user_not_login, Toast.LENGTH_SHORT).show();
                }

                break;

            case R.id.ab_btn_export:
                exportExperimentDialog();
                break;
            case R.id.ab_btn_close:
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.content_frame, new EmptyFragment(), Keys.FragmentTag.EmptyTag);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                setExperimentMenuVisibility(false);
                invalidateOptionsMenu();
                ft.commit();
                experimentController.closeExperiment();
                break;
        }

    }

    /*========================= File Related onClicks =========================*/
    private void exportExperimentDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_dialog_export);
        builder.setIcon(R.drawable.ic_export);

        //Get Views
        View view = this.getLayoutInflater().inflate(R.layout.dialog_export, null, false);
        final EditText et = (EditText) view.findViewById(R.id.dialogExportEt);
        final RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.dialogExportRadioFormat);


        builder.setView(view);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //NoOp
            }
        });
        builder.setNeutralButton(R.string.local, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                File path = new File(Environment.getExternalStorageDirectory(),GlobalConstants.File.pathName);
                if (!path.mkdirs()) {
                    Log.v("DIR_TAG", "Directory not created");
                }

                //Get Variables
                String fileName;
                if(validFileName(et.getText().toString().trim())){
                    fileName = et.getText().toString().trim();
                }
                else{
                    Toast.makeText(getBaseContext(), R.string.toast_invalid_filename, Toast.LENGTH_SHORT).show();
                    return;
                }
                int id = radioGroup.getCheckedRadioButtonId();
                switch(id){
                    case R.id.dialogExportRadioCSV:

                        List<Run> runs = JavaConversions.asJavaList(experimentController.getExperiment().runs());
                        if(runs.size() == 0){
                            Toast.makeText(getBaseContext(), R.string.toast_error_no_runs, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        for(int i=0; i<runs.size(); i++){
                            File file = new File(path, fileName+"Run"+i+GlobalConstants.File.csvExt);
                            if(file.exists()){
                                dialog.dismiss();
                                overrideFile(file, GlobalConstants.File.csvExt, runs.get(i));
                                return;
                            }
                            boolean success = false;
                            try {
                                success = file.createNewFile();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                            if(success) {
                                createCSVFiles(file, runs.get(i));
                                Toast.makeText(getBaseContext(), R.string.toast_file_create, Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(getBaseContext(), R.string.toast_file_not_create, Toast.LENGTH_SHORT).show();
                            }
                        }

                        break;
                    case R.id.dialogExportRadioAerobal:
                        //Check File Exits
                        File file = new File(path, fileName+GlobalConstants.File.aeroExt);
                        if(file.exists()){
                            dialog.dismiss();
                            overrideFile(file, GlobalConstants.File.aeroExt, null);
                            return;
                        }

                        boolean success = false;
                        try {
                            success = file.createNewFile();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        if(success) {
                            createAeroFiles(file);
                            Toast.makeText(getBaseContext(), R.string.toast_file_create, Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(getBaseContext(), R.string.toast_file_not_create, Toast.LENGTH_SHORT).show();
                        }
                        break;
                }

            }
        });

        builder.setPositiveButton(R.string.email, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));

                String fileName;
                if(validFileName(et.getText().toString().trim())){
                    fileName = et.getText().toString().trim();
                }
                else{
                    Toast.makeText(getBaseContext(), R.string.toast_invalid_filename, Toast.LENGTH_SHORT).show();
                    return;
                }

                File[] files= {};
                int id = radioGroup.getCheckedRadioButtonId();
                switch(id){
                    case R.id.dialogExportRadioCSV:
                        files = createEmailCSVFiles(fileName);
                        break;
                    case R.id.dialogExportRadioAerobal:
                        files = createEmailAeroFiles(fileName);
                        break;
                }

                for(File e: files){
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + e.getAbsolutePath()));
                }


                startActivity(Intent.createChooser(intent, getResources().getString(R.string.title_intent_email)));
            }
        });

        builder.create().show();


    }

    private void createAeroFiles(File file){

        try {
                FileOutputStream fout = new FileOutputStream(file);
                ObjectOutputStream oos = new ObjectOutputStream(fout);
                oos.writeObject(experimentController.getExperiment());
                fout.close();
                oos.close();

        } catch (IOException e) {
            Log.e("File", "Error");
            file.delete();
            Toast.makeText(getBaseContext(), R.string.toast_file_not_create, Toast.LENGTH_SHORT).show();
        }

    }

    private void createCSVFiles(File file, Run run){

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(run.toCSV());
            writer.close();


        }catch (IOException e) {
            Toast.makeText(getBaseContext(), R.string.toast_file_not_create, Toast.LENGTH_SHORT).show();
            // Error while creating file
        }

    }

    private File[] createEmailAeroFiles(String fileName){

        File tmpFile = null;
        try {
            tmpFile = File.createTempFile(fileName, GlobalConstants.File.aeroExt, getBaseContext().getCacheDir());
            FileOutputStream fout = new FileOutputStream(tmpFile);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(experimentController.getExperiment());
            fout.close();
            oos.close();
        }catch (IOException e) {
            Toast.makeText(getBaseContext(), R.string.toast_file_not_create, Toast.LENGTH_SHORT).show();
            // Error while creating file
        }

        return new File[]{tmpFile};

    }

    private File[] createEmailCSVFiles(String fileName){

        List<Run> runs = JavaConversions.asJavaList(experimentController.getExperiment().runs());
        File[] ret = new File[runs.size()];
        for(int i=0; i<runs.size(); i++){
            File tmpFile = null;
            try {
                tmpFile = File.createTempFile(fileName+i, GlobalConstants.File.csvExt, getBaseContext().getCacheDir());
                BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile));
                writer.write(runs.get(i).toCSV());
                writer.close();
            }catch (IOException e) {
                Toast.makeText(getBaseContext(), R.string.toast_file_not_create, Toast.LENGTH_SHORT).show();
                // Error while creating file
            }
            ret[i] = tmpFile;
        }
        return ret;

    }


    private void overrideFile(final File file, final String suffix, final Run run){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setIcon(R.drawable.ic_file);
        builder.setTitle(R.string.title_dialog_override);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(suffix.equals(GlobalConstants.File.aeroExt)){
                    createAeroFiles(file);
                }
                else if (suffix.equals(GlobalConstants.File.csvExt)){
                    createCSVFiles(file, run);
                }

            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //No-Op
            }
        });

        Dialog dialog = builder.create();
        dialog.show();

    }

    private boolean validFileName(String str){
        return !str.isEmpty() && str.length() >= 3;
    }



    public void setExperimentMenuVisibility(boolean bool) {
        this.expMenuBoolean = bool;
    }


    /*======================== Drawer OnClick Listeners ========================*/
    public class DrawerOnClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
                                long id) {

            switch (pos) {
                case 0:
                    //New
                    if (!btController.hasBluetoothRadio()) {
                        btController.btNotFoundErrorToast();
                    } else {
                        NewDialog.getNewDialog(unitController, new AeroCallback() {
                            @Override
                            public void callback(final Map<String, Object> objectMap) {

                               //Get Experiment
                               final Experiment experiment = (Experiment) objectMap.get(Keys.CallbackMap.NewExperimentObject);
                                ((NewDialog) objectMap.get(Keys.CallbackMap.NewExperimentDialog)).dismiss();
                                closeDrawer();
                                newExperimentFragmentLoader(experiment);


                            }
                        }).show(getSupportFragmentManager(), "New Experiment Dialog");
                    }
                    break;
                case 1:
                    //Open

                    OpenDialog.getOpenDialog(userController, new AeroCallback() {
                        @Override
                        public void callback(Map<String, Object> objectMap) {
                            String value = (String) objectMap.get(Keys.CallbackMap.OpenType);

                            if(value.equalsIgnoreCase(OpenDialog.LOCAL)){

                                Intent intentLocal = new Intent(getBaseContext(), OpenActivity.class);
                                Bundle bnd = new Bundle();
                                bnd.putSerializable(Keys.BundleKeys.OpenType, value);
                                intentLocal.putExtras(bnd);

                                startActivityForResult(intentLocal, Keys.ActivityOnResult.OpenKey);


                            }
                            else if(value.equalsIgnoreCase(OpenDialog.ONLINE)){

                                    Intent intentView = new Intent(getBaseContext(), OpenActivity.class);
                                    Bundle bnd = new Bundle();
                                    bnd.putString(Keys.BundleKeys.OpenType, value);
                                    bnd.putSerializable(Keys.BundleKeys.UnitController, unitController);
                                    intentView.putExtras(bnd);
                                    startActivityForResult(intentView, Keys.ActivityOnResult.OpenKey);
                            }
                            closeDrawer();
                        }
                    }).show(getSupportFragmentManager(), "Open Experiment Dialog");
                    break;
                case 2:
                    //Bluetooth Setup
                    DialogFragment df = btController.getCurrentDialog(abMenu.findItem(R.id.ab_btn_bluetooth));
                    if (df != null)
                        df.show(getSupportFragmentManager(), "Bluetooth Dialog");
                    break;
                case 3:
                    //Log-in //Log-out
                    if (userController.isUserLogIn()) {
                        userController.logout();
                        onResume();
                    } else {
                        userController.login();
                    }
                    break;
            }
        }


    }

    private void closeDrawer() {
        mDrawerLayout.closeDrawers();
    }



    /*======================== Fragment Loaders Methods ====================*/

    private void newExperimentFragmentLoader(final Experiment experiment){

        if(!experimentController.isExperimentSet()) {

            experimentController.setExperiment(experiment);

            //Open new fragment

            //Remove Old
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.content_frame, ExperimentFragment.getExperimentFragment(experimentController, unitController), Keys.FragmentTag.ExperimentTag);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();
            closeDrawer();
            return;

        }
        else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle(R.string.title_dialog_override);
            builder.setIcon(R.drawable.ic_file);

            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //NoOp
                }
            });

            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    experimentController.setExperiment(experiment);
                    //Open new fragment
                    //Remove Old
                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.content_frame, ExperimentFragment.getExperimentFragment(experimentController, unitController), Keys.FragmentTag.ExperimentTag);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    ft.commit();

                }
            });

            Dialog dialog = builder.create();
            dialog.show();



        }
    }

    public void resetExperimentFragment(){
        setExperimentMenuVisibility(true);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.content_frame, ExperimentFragment.getExperimentFragment(experimentController, unitController), Keys.FragmentTag.ExperimentTag);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
        invalidateOptionsMenu();
    }

    private void setWaitingFragment(){
        FragmentManager fm = getSupportFragmentManager();
        //Removing old Fragment

        FragmentTransaction ft = fm.beginTransaction();
        Fragment f = this.getSupportFragmentManager().findFragmentByTag(Keys.FragmentTag.UpdateTag);
        if( f != null){
            ft.remove(f);
            ft.commit();
            ft = fm.beginTransaction();
        }

        ft.replace(R.id.content_frame, UpdateStatusFragment.newInstance("Running Experiment...", updateBundle, unitController), Keys.FragmentTag.UpdateTag);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
        invalidateOptionsMenu();
    }


    /*========================= Services Related Methods ============================= */
    //Set Experiment Vars
    private void onStartRunListener(){
        experimentController.setRunning(true);
        setExperimentMenuVisibility(false);
        btController.disconnectSocket(); //

        btIntent = new Intent(this, BluetoothService.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(BluetoothService.Keys.BLUETOOTH_DEVICE, btController.getAerobalDevice()); //Send Device
        bundle.putSerializable(Keys.BundleKeys.Experiment, experimentController.getExperiment());
        btIntent.putExtras(bundle); //Set other Data
        startService(btIntent);

        //Reset Views
        invalidateOptionsMenu();
        onResume();
    }


















}
