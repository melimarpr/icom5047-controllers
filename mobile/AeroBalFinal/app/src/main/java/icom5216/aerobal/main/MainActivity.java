package icom5216.aerobal.main;

import icom5216.aerobal.resources.ConstantClass;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;



@SuppressLint("NewApi")
public class MainActivity extends Activity {
	
	private static final int REQUEST_ENABLE_BT = 12;
	
					/* Unit Vars */
	private boolean unitLbs = true;
	private static final String activeUnitLbs = "lbs";
	private static final String activeUnitSi = "N";
	private String activeUnit = activeUnitLbs;
	
	
	
					/* Bluetooth Vars */
	private TextView tvBluetooth;
	private BluetoothSocket mSocket = null;
	private android.bluetooth.BluetoothAdapter mBluetoothAdapter;
	private AlertDialog dialog;
	private Thread readThread;
	private InputStream is;
	private InputStreamReader isr;
	private BufferedReader br;
	private LinkedList<String> queue;
	private static final String REGEX = "bt:[a-zA-Z]+=[0-9]+(.[0-9]+)?\r?\n?";
	private boolean btConnected = false;
	private MenuItem btBtnObj;
	
	
	
					/* Measurement Values */ 
	private List<Float> dragsValues = new ArrayList<Float>();
	private List<Float> liftValues = new ArrayList<Float>();
	private List<Float> tiltValues = new ArrayList<Float>();
	
	private float pressure = (float) -1.0;
	private float direction = (float) -1.0;
	private float temperature= (float) -1.0;
	private float windspeed= (float) -1.0;
	private float humidity= (float) -1.0;
	
	private float initDrag = (float) -1.0;
	private float initLift = (float) -1.0;
	private float initTilt = (float) -1.0;
	
	//TextViews
	private TextView tvHumidity;
	private TextView tvDirection;
	private TextView tvWindSpeed;
	private TextView tvTemperature;
	private TextView tvPressure;
	
	private TextView tvCurrDrag;
	private TextView tvCurrLift; 
	private TextView tvCurrTilt;
	
	private TextView tvInitDrag;
	private TextView tvInitLift;
	private TextView tvInitTilt;
	
	
	//Progress Dialog
	ProgressDialog pd;
	private boolean threadBool = true;
	
	//Init Method
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.queue = new LinkedList<String>();
		this.tvBluetooth = (TextView) findViewById(R.id.btStatus);
		
		tvHumidity = (TextView) findViewById(R.id.tv_humidity);
		tvDirection = (TextView) findViewById(R.id.tv_direction);
		tvWindSpeed = (TextView) findViewById(R.id.tv_windspeed);
		tvTemperature = (TextView) findViewById(R.id.tv_temperature);
		tvPressure = (TextView) findViewById(R.id.tv_pressure);
		

		tvCurrDrag = (TextView) findViewById(R.id.tv_currdrag);
		tvCurrLift = (TextView) findViewById(R.id.tv_currentlift);
		tvCurrTilt = (TextView) findViewById(R.id.tv_currenttilt);

		tvInitDrag = (TextView) findViewById(R.id.tv_initdrag);
		tvInitDrag.setVisibility(View.GONE);
		
		tvInitLift = (TextView) findViewById(R.id.tv_initlift);
		tvInitLift.setVisibility(View.GONE);
		
		tvInitTilt = (TextView) findViewById(R.id.tv_inittilt);
		tvInitTilt.setVisibility(View.GONE);
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		MenuItem unit = menu.findItem(R.id.btn_change_units);
		unit.setTitle(getResources().getString(R.string.btn_changeunits)+": "+this.activeUnit);
		return true;
	}
	
	//Value Loader
	private void loadValues(String key, float value){
		
		if(key.equals(ConstantClass.PRESSURE_KEY)){
			
            this.pressure = value; 
		}
		else if(key.equals(ConstantClass.TEMP_KEY)){
			this.temperature = value;
		}
		else if(key.equals(ConstantClass.WINDSPEED_KEY))
			this.windspeed = value;
		else if(key.equals(ConstantClass.HUMIDITY_KEY))
			this.humidity = value;
		else if(key.equals(ConstantClass.DIRECTION_KEY))
			this.direction = value;
		else if(key.equals(ConstantClass.DRAG_KEY)){
			dragsValues.add(value);
		}
		else if(key.equals(ConstantClass.LIFT_KEY)){
			liftValues.add(value);
		}
		else if(key.equals(ConstantClass.TITL_KEY)){
			tiltValues.add(value);
		}
		else if(key.equals(ConstantClass.INIT_DRAG_KEY)){
			this.initDrag = value;
			this.tvInitDrag.setVisibility(View.VISIBLE);
		}
		else if(key.equals(ConstantClass.INIT_LIFT_KEY)){
			this.initLift = value;
			this.tvInitLift.setVisibility(View.VISIBLE);
		}
		else if(key.equals(ConstantClass.INIT_TILT_KEY)){
			this.initTilt = value;
			this.tvInitTilt.setVisibility(View.VISIBLE);
		}
		
		if(key.equals(ConstantClass.INIT_DRAG_KEY) || key.equals(ConstantClass.INIT_LIFT_KEY) ||key.equals(ConstantClass.INIT_TILT_KEY)  )
			pd.dismiss();		
		
		
	}
	//View Reload
	private void reloadView() {
		NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(2);
        

		if(this.pressure != -1.0){
			this.tvPressure.setText(nf.format(getInCorrectUnits(pressure))+" "+this.activeUnit);
		}
		if(this.temperature != -1.0){
			this.tvTemperature.setText(nf.format(getInCorrectUnits(temperature))+" "+this.activeUnit);
		}
		if(this.windspeed != -1.0)
			this.tvWindSpeed.setText(nf.format(getInCorrectUnits(windspeed))+" "+this.activeUnit);
		if(this.humidity != -1.0)
			this.tvHumidity.setText(nf.format(getInCorrectUnits(humidity))+" "+this.activeUnit);
		if(this.direction != -1.0)
			this.tvDirection.setText(nf.format(getInCorrectUnits(this.direction))+" "+this.activeUnit);
		
		
		if(dragsValues.size() > 0){
			tvCurrDrag.setText(nf.format(getInCorrectUnits(dragsValues.get(dragsValues.size()-1)))+" "+this.activeUnit);
		}
		if(liftValues.size() > 0){
			tvCurrLift.setText(nf.format(getInCorrectUnits(liftValues.get(liftValues.size()-1)))+" "+this.activeUnit);
		}
		if(tiltValues.size() > 0){
			tvCurrTilt.setText(nf.format(getInCorrectUnits(tiltValues.get(tiltValues.size()-1)))+" "+this.activeUnit);
		}
		
		if(this.initDrag != -1.0){
			this.tvInitDrag.setText(nf.format(getInCorrectUnits(this.initDrag))+" "+this.activeUnit);
		}
		if(this.initLift != -1.0){
			this.tvInitLift.setText(nf.format(getInCorrectUnits(this.initLift))+" "+this.activeUnit);
		}
		if(this.initTilt != -1.0){
			this.tvInitTilt.setText(nf.format(getInCorrectUnits(this.initTilt))+" "+this.activeUnit);
		}
		
	}
	
	//Unit Formater
	private float getInCorrectUnits(float value){
		
		//TODO
		if(this.unitLbs){
			//Formula A
			return value;
		}else
			//Formula B
			return value;
	}

	
	
	
									/*Action Bar Listener */
	
	//Method For Activating the Bluetooth
	public void onBtnConfigurationListener(MenuItem item) {
		this.btBtnObj = item;
		if(!btConnected){
			this.connectBluetooth();
		}
		else{
			this.disconnectBluetooth();
		}
		
	}
	
		private void connectBluetooth(){
			 LayoutInflater inflater = this.getLayoutInflater();
	         View sfView = inflater.inflate(R.layout.dialog_bt, null);
	         AlertDialog.Builder builder = new AlertDialog.Builder(this);
	         builder.setTitle(R.string.title_btdialog);
	         builder.setView(sfView);
	         
	         //Fill And Listener of Bluetooth
	         ListView btListView = (ListView) sfView.findViewById(R.id.lvBtDialog);
	         btListView.setAdapter(new BluetoothAdapterList(this, this.getBluethoodDevices()));
	         
	         btListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> listView, View arg1, int position,
						long arg3) {
					BluetoothDevice bd = (BluetoothDevice) listView.getAdapter().getItem(position);
					BluetoothDevice actual = mBluetoothAdapter.getRemoteDevice(bd.getAddress());
					//Get Guid
					for(ParcelUuid p : actual.getUuids()) {
						try {
						mSocket = actual.createInsecureRfcommSocketToServiceRecord(p.getUuid());
						mSocket.connect();
						break;
						} catch(IOException e) {
							continue;
						}
					}
					tvBluetooth.setTextColor(getResources().getColor(android.R.color.holo_green_light));
					tvBluetooth.setText(R.string.bt_connected);
					//Start Reading
					mBluetoothAdapter.cancelDiscovery();
					if(!mSocket.isConnected())
						try {
							mSocket.connect();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					try {
						is = mSocket.getInputStream();
						isr = new InputStreamReader(is);
						br = new BufferedReader(isr);
						
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					threadBool = true;
					readThread = new Thread(new Runnable() {
						@Override
						public void run() {
							while (threadBool) {
									try {
										queue.addLast(br.readLine());
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									if(!queue.isEmpty())
										parseBtString(queue.removeFirst());									
							}
							
						}
					});
					btBtnObj.setTitle(R.string.btn_disconnect);
					btConnected = true;
					readThread.start();
					dialog.dismiss();				
					
				}
	         });
	        this.dialog = builder.create();
	        this.dialog.show();
		}
		
		private void disconnectBluetooth(){
			this.threadBool = false;
			
			try {
				mSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.btConnected = false;
			this.btBtnObj.setTitle(R.string.btn_connect);
			tvBluetooth.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
			tvBluetooth.setText(R.string.bt_disconnected);
		}
	
		private class BluetoothAdapterList extends ArrayAdapter<BluetoothDevice>{

			public BluetoothAdapterList(Context context, Set<BluetoothDevice> objects) {
				super(context, R.layout.btrow);
				this.addAll(objects);
				
			}
			
	        @Override
	        public View getView(int position, View convertView, ViewGroup parent) {
	                View row = convertView;

	                BluetoothDevice cat = this.getItem(position);

	                LayoutInflater inflater = ((Activity) this.getContext()).getLayoutInflater();
	                
	                row = inflater.inflate(R.layout.btrow, parent, false);
	                
	                TextView tvName = (TextView) row.findViewById(R.id.tvname);
	                TextView tvIp = (TextView) row.findViewById(R.id.tvip);
	                
	                tvName.setText(cat.getName());
	                tvIp.setText(cat.getAddress());
	                
	                row.setTag(cat);

	                return row;
	        }

		}
		
		public Set<BluetoothDevice> getBluethoodDevices(){
			final BluetoothManager bluetoothManager =
			        (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			android.bluetooth.BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
			if (!mBluetoothAdapter.isEnabled()) { 
				Intent enableBtIntent = new Intent(android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
			this.mBluetoothAdapter = mBluetoothAdapter;
			return  mBluetoothAdapter.getBondedDevices();
		}

		public void sentStringOverBt(String str){
			this.mBluetoothAdapter.cancelDiscovery();
				try{
					if(!mSocket.isConnected())
						mSocket.connect();
					OutputStream os = mSocket.getOutputStream();
							os.write(str.getBytes());
							os.flush();
				}catch(Exception e){
					
				}
				
		}
	
		private void parseBtString(String line){
			
			if(line.trim().matches(REGEX)) {
				final String[] str = line.split(":")[1].split("=");
				Log.v("btTag", str[0] + ": " + str[1]);
				
				if(str[0].equals(ConstantClass.INIT_DRAG_KEY) || str[0].equals(ConstantClass.INIT_LIFT_KEY) ||str[0].equals(ConstantClass.INIT_TILT_KEY)  )
				{
				   pd = new ProgressDialog(this);
		           pd.setMessage(getResources().getString(R.string.init));
				}
				loadValues(str[0], Float.valueOf(str[1]));
				
				this.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						reloadView();
					}
				});
			}
			else{
				Log.v("btInvalid", line);
			}
			
		}
		
	public void onChangeUnitsListener(MenuItem item){
		if(this.unitLbs){
			this.activeUnit = activeUnitSi;
			item.setTitle(getResources().getString(R.string.btn_changeunits)+": "+this.activeUnit);
		}
		else{
			this.activeUnit = activeUnitLbs;
			item.setTitle(getResources().getString(R.string.btn_changeunits)+": "+this.activeUnit);
		}
		this.unitLbs = !this.unitLbs;
	}
	
	public void onBtnOnListener(MenuItem item){
		if(btConnected)
			this.sentStringOverBt(ConstantClass.BTCommands.ON_CMD);
	}
	
	public void onBtnOffListener(MenuItem item){
		if(btConnected)
			this.sentStringOverBt(ConstantClass.BTCommands.OFF_CMD);
	}
	
	//BT Code
	
	
	
	
	
	
	
	//Button Listeners
	public void btnMessureListener(View v){
		this.sentStringOverBt(ConstantClass.BTCommands.MEASURE_CMD);
	}
	
	public void btnInitListener(View v){
		
	}
	
	public void btnFinishListener(View v){
		this.disconnectBluetooth();
		
	}
	

}
