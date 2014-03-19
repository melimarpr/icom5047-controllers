package icom5047.aerobal.activities;

import icom5047.aerobal.adapters.DrawerAdapter;
import icom5047.aerobal.controllers.BluetoothController;
import icom5047.aerobal.controllers.UnitController;
import icom5047.aerobal.controllers.UserController;
import icom5047.aerobal.dialog.NewDialog;
import icom5047.aerobal.dialog.OpenDialog;
import icom5047.aerobal.fragments.ExperimentFragment;
import icom5047.aerobal.resources.UnitFactory;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MainActivity extends FragmentActivity {

	 private DrawerLayout mDrawerLayout;
     private ListView mDrawerList;
     private ActionBarDrawerToggle mDrawerToggle;
     private Menu abMenu;

	
	 //Controllers Instants Fields
     private UserController userController;
     private BluetoothController btController;
     private UnitController unitController;
     
     
	@SuppressLint("UseSparseArrays")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		/* ------------------------------ Application Logic ------------------------- */
		userController = new UserController(this, null);
		btController = new BluetoothController(this);
		SparseArray<Integer> values = new SparseArray<Integer>();
		values.append(UnitFactory.Type.PRESSURE, UnitFactory.Pressure.UNIT_PASCAL);
		values.append(UnitFactory.Type.FORCE, UnitFactory.Force.UNIT_NEWTON);
		values.append(UnitFactory.Type.HUMIDITY, UnitFactory.Humidity.UNIT_PERCENTAGE);
		values.append(UnitFactory.Type.TEMPERATURE, UnitFactory.Temperature.UNIT_CELSIUS);
		values.append(UnitFactory.Type.SPEED, UnitFactory.Speed.UNIT_MS);
		values.append(UnitFactory.Type.DIRECTION, UnitFactory.Direction.UNIT_DEGREES);
	
		unitController = new UnitController(values);
		
		/* ------------------------------ View Logic ------------------------- */
		
		//Set Action Bar Settings
		final ActionBar ab = this.getActionBar();
		ab.setTitle(R.string.title_actvity_main_menu);
		
		
		//==Set Up the List==
        mDrawerList = (ListView) findViewById(R.id.drawer_list);

        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new DrawerAdapter(this, userController.getDrawerList()));
      
        //Set Drawer List to React to Click
        mDrawerList.setOnItemClickListener( new DrawerOnClickListener());
        
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
            public void onDrawerClosed(View view) { //Callback for toggle
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        
        //Set Default Fragment
        FragmentManager fm = this.getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.content_frame, new ExperimentFragment()).commit();
      
        
		
		
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
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.ab_btn_bluetooth).setVisible(!drawerOpen);
        menu.findItem(R.id.ab_btn_units).setVisible(!drawerOpen);
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
			if(df != null)
				df.show(getSupportFragmentManager(), "Bluetooth Dialog");
			break;
		case R.id.ab_btn_units:
			unitController.getDialog().show(getSupportFragmentManager(), "Units Dialog");
			break;
		}
       
		
		return true;
	}
	
	
	
	public class DrawerOnClickListener implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
				long id) {
			
			switch (pos) {
			case 0:
				//New
				new NewDialog().show(getSupportFragmentManager(), "New Experiment Dialog");
				break;
			case 1:
				//Open
				new OpenDialog().show(getSupportFragmentManager(), "Open Experiment Dialog");
				break;
			case 2:
				//Bluetooth Setup
				DialogFragment df = btController.getCurrentDialog(abMenu.findItem(R.id.ab_btn_bluetooth));
				if(df != null)
					df.show(getSupportFragmentManager(), "Bluetooth Dialog");
				break;
			case 3:
				//Log-in //Log-out
				if(userController.isUserLogIn()){
					userController.logout();
				}
				else{
					userController.login();
				}
				break;
			}
		}
	}
	
	
	

}
