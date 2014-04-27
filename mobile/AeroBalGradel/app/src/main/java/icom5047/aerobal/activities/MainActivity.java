package icom5047.aerobal.activities;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.aerobal.data.objects.Experiment;
import com.aerobal.data.objects.Session;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import icom5047.aerobal.adapters.DrawerAdapter;
import icom5047.aerobal.controllers.BluetoothController;
import icom5047.aerobal.controllers.ExperimentController;
import icom5047.aerobal.controllers.UnitController;
import icom5047.aerobal.controllers.UserController;
import icom5047.aerobal.dialog.NewDialog;
import icom5047.aerobal.dialog.OpenDialog;
import icom5047.aerobal.fragments.EmptyFragment;
import icom5047.aerobal.fragments.ExperimentFragment;
import icom5047.aerobal.interfaces.AeroCallback;
import icom5047.aerobal.mockers.Mocker;
import icom5047.aerobal.resources.Keys;
import icom5047.aerobal.resources.UnitFactory;

public class MainActivity extends FragmentActivity {

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerAdapter mDrawerAdapter;
    private Menu abMenu;
    private boolean expMenuBoolean;


    //Controllers Instants Fields
    private volatile UserController userController;
    private volatile BluetoothController btController;
    private volatile UnitController unitController;
    private volatile ExperimentController experimentController;


    @SuppressLint("UseSparseArrays")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		
		/* ------------------------------ Application Logic ------------------------- */
        userController = new UserController(this);
        btController = new BluetoothController(this);
        experimentController = new ExperimentController();


        HashMap<Integer, Integer> values = new HashMap<Integer, Integer>();
        values.put(UnitFactory.Type.PRESSURE, UnitFactory.Pressure.UNIT_PASCAL);
        values.put(UnitFactory.Type.FORCE, UnitFactory.Force.UNIT_NEWTON);
        values.put(UnitFactory.Type.HUMIDITY, UnitFactory.Humidity.UNIT_PERCENTAGE);
        values.put(UnitFactory.Type.TEMPERATURE, UnitFactory.Temperature.UNIT_CELSIUS);
        values.put(UnitFactory.Type.SPEED, UnitFactory.Speed.UNIT_MS);
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

        //Set Default Fragment
        FragmentManager fm = this.getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.content_frame, new EmptyFragment(), Keys.FragmentTag.EmptyTag).commit();


    }

    @Override
    protected void onResume() {
        super.onResume();
        //Refresh Data After Log-In
        mDrawerAdapter.clear();
        mDrawerAdapter.addAll(userController.getDrawerList());
        mDrawerAdapter.notifyDataSetChanged();

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


    //Action Bar Menu

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
        menu.findItem(R.id.ab_btn_bluetooth).setVisible(!drawerOpen);
        menu.findItem(R.id.ab_btn_units).setVisible(!drawerOpen);
        if(btController.isAeroBalConnected()){

        }

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
        this.abMenu = menu;
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
                onExperimentLoadedItemSelected(item.getItemId());
                break;
        }

        return true;
    }

    public void onExperimentLoadedItemSelected(int id) {


        switch (id) {
            case R.id.ab_btn_start_run:

                experimentController.generateExperimentRun(btController, this);


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
                saveExperimentOnlineDialog();
                break;

            case R.id.ab_btn_export:
                exportExperimentDialog();
                break;
            case R.id.ab_btn_close:
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                // ft.remove(fm.findFragmentByTag(Keys.FragmentTag.ExperimentTag));
                ft.replace(R.id.content_frame, new EmptyFragment(), Keys.FragmentTag.EmptyTag);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                setExperimentMenuVisibility(false);
                invalidateOptionsMenu();
                ft.commit();
                break;
        }

    }

    private void exportExperimentDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.title_dialog_export);
        builder.setIcon(R.drawable.ic_export);

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


                File path = new File(Environment.getExternalStorageDirectory(),"AeroBal");
                if (!path.mkdirs()) {
                    Log.e("DIR_TAG", "Directory not created");
                }

                String fileName = "experiment";
                if(validFileName(et.getText().toString().trim())){
                    fileName = et.getText().toString().trim();
                }
                else{
                    Toast.makeText(getBaseContext(), R.string.toast_invalid_filename, Toast.LENGTH_SHORT).show();
                    return;
                }
                String suffix = ".csv";
                int id = radioGroup.getCheckedRadioButtonId();
                switch(id){
                    case R.id.dialogExportRadioCSV:
                        suffix = ".csv";
                        break;
                    case R.id.dialogExportRadioAerobal:
                        suffix = ".aero";
                        break;
                }

                File file = new File(path, fileName+suffix);

                //Check File Exits
                if(file.exists()){
                    dialog.dismiss();
                    overrideFile(file);
                    return;
                }


                try {
                   boolean success =  file.createNewFile();

                   if(success){
                       FileOutputStream fout = new FileOutputStream(file);
                       ObjectOutputStream oos = new ObjectOutputStream(fout);
                       oos.writeObject(experimentController.getExperiment());
                       oos.close();
                       Toast.makeText(getBaseContext(), R.string.toast_file_create, Toast.LENGTH_SHORT).show();
                   }
                   else {
                       Toast.makeText(getBaseContext(), R.string.toast_file_not_create, Toast.LENGTH_SHORT).show();
                   }
                } catch (IOException e) {
                    Log.e("File", "Error");
                    file.delete();
                    Toast.makeText(getBaseContext(), R.string.toast_file_not_create, Toast.LENGTH_SHORT).show();
                }


            }
        });

        builder.setPositiveButton(R.string.email, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "AeroBal Experiment");

                File tmpFile = null;

                String fileName = "experiment";
                if(validFileName(et.getText().toString().trim())){
                    fileName = et.getText().toString().trim();
                }
                else{
                    Toast.makeText(getBaseContext(), R.string.toast_invalid_filename, Toast.LENGTH_SHORT).show();
                    return;
                }



                String suffix = ".csv";
                int id = radioGroup.getCheckedRadioButtonId();
                switch(id){
                    case R.id.dialogExportRadioCSV:
                        suffix = ".csv";
                        break;
                    case R.id.dialogExportRadioAerobal:
                        suffix = ".aero";
                        break;
                }

                try {
                    tmpFile = File.createTempFile(fileName, suffix, getBaseContext().getCacheDir());
                    FileOutputStream fout = new FileOutputStream(tmpFile);
                    ObjectOutputStream oos = new ObjectOutputStream(fout);
                    oos.writeObject(experimentController.getExperiment());
                    oos.close();
                }catch (IOException e) {
                    Toast.makeText(getBaseContext(), R.string.toast_file_not_create, Toast.LENGTH_SHORT).show();
                    // Error while creating file
                }

                File file = tmpFile;

                Uri uri = Uri.parse("file://" + file);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(intent, getResources().getString(R.string.title_intent_email)));
            }
        });

        builder.create().show();


    }

    private void overrideFile(final File file){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setIcon(R.drawable.ic_file);
        builder.setTitle(R.string.title_dialog_override);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {

                        FileOutputStream fout = new FileOutputStream(file);
                        ObjectOutputStream oos = new ObjectOutputStream(fout);
                        oos.flush();
                        oos.writeObject(experimentController.getExperiment());
                        oos.close();
                        Toast.makeText(getBaseContext(), R.string.toast_file_updated, Toast.LENGTH_SHORT).show();

                } catch (IOException e) {
                    Log.e("File", "Error");
                    Toast.makeText(getBaseContext(), R.string.toast_file_not_create, Toast.LENGTH_SHORT).show();
                }

            }
        });

        builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
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


    private void saveExperimentOnlineDialog() {

        //TODO: Get Session From Jesus
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_dialog_save_online);

        View view = this.getLayoutInflater().inflate(R.layout.dialog_save_experiment, null);



        builder.setView(view);

        doHttpLoadSessions(view);


        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //NoOp;
            }
        });

        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        Dialog dialog = builder.create();


        dialog.show();


    }


    private void doHttpLoadSessions(View view){

        //Get Vars
        TextView textView = (TextView) view.findViewById(R.id.dialogSaveExpOnlineNoSession);
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.dialogSaveExpOnlineProgressBar);
        Spinner spinner = (Spinner) view.findViewById(R.id.dialogSaveExpOnlineSpinner);

        List<Session> sessions = Mocker.generateFakeSessions(10);

        //TODO: Changed Mocker

        progressBar.setVisibility(View.GONE);

        if(sessions.size() == 0){
            textView.setVisibility(View.VISIBLE);
        }
        else{
            spinner.setAdapter(new ArrayAdapter<Session>(this, android.R.layout.simple_dropdown_item_1line, sessions));
            spinner.setVisibility(View.VISIBLE);
        }




    }

    private void closeDrawer() {
        mDrawerLayout.closeDrawers();
    }


    public void setExperimentMenuVisibility(boolean bool) {

        this.expMenuBoolean = bool;

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
                        newExperiment(experiment);

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
                                newExperiment(experiment);


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
                        login();
                    }
                    break;
            }
        }


    }

    public void login() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, UserController.REQUEST_LOGIN);
    }

    private void newExperiment(final Experiment experiment){

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



}
