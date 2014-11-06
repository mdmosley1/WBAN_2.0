// This activity handles the device communication while also plotting data from the sensor.
// It also handles data forwarding to the database and saving data to a file if there is 
// no network connection.

package ti.android.ble.sensortag;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import ti.android.ble.common.BluetoothLeService;
import ti.android.ble.common.GattInfo;
import ti.android.util.Point3D;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.androidplot.util.PlotStatistics;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;

public class DeviceActivity extends Activity {
  // Log
  private static String TAG = "DeviceActivity";
  private static String NET = "NetworkConnectivity";

	// Activity
	public static final String EXTRA_DEVICE = "EXTRA_DEVICE";
	private static final int HIST_ACT_REQ = 0;

	public static final byte ENABLE_SENSOR_CODE = 7;
	public static final byte ACC_PERIOD = 10;		// [ACC_PERIOD]*10ms = Accelerometer's period
//	private UUID servUuid = SensorTag.UUID_ACC_SERV;
//	private UUID dataUuid = SensorTag.UUID_ACC_DATA;
//	private UUID confUuid = SensorTag.UUID_ACC_CONF;
//	private UUID perUUID = SensorTag.UUID_ACC_PERI; 
	
	private UUID servUuid = SensorTag.UUID_GYR_SERV;
	private UUID dataUuid = SensorTag.UUID_GYR_DATA;
	private UUID confUuid = SensorTag.UUID_GYR_CONF;
	private UUID perUUID = SensorTag.UUID_GYR_PERI; 

  // BLE
  private BluetoothLeService mBtLeService = null;
  private BluetoothDevice mBluetoothDevice = null;
  private BluetoothGatt mBtGatt = null;
  private List<BluetoothGattService> mServiceList = null;
  private static final int GATT_TIMEOUT = 100; // milliseconds
  private boolean mServicesRdy = false;
  private boolean mIsReceiving = false;

  // SensorTag
  private List<Sensor> mEnabledSensors = new ArrayList<Sensor>();
  private BluetoothGattService mConnControlService = null;
  
  //Plot Stuff
  private XYPlot aSensorPlot;
  private XYPlot gSensorPlot;
  private XYPlot hPlot;
  
  private SimpleXYSeries[] historySeries = null;
  
  //private SimpleXYSeries totHistorySeries = null;
  
  private final int SERIES_SIZE = 50;
  
  private boolean[] toggle_plot = {true, true, true, true};
  
      
  // Number of data points to keep in history
  private static final int HISTORY_SIZE = 50;
  
  // Data storage vars
  private File curr_file = null;
  private final String FILENAME = "wbandata";
  List<Integer> file_vec = new ArrayList<Integer>();
  private long len;
  private int buff_count;
  private boolean isBeginning = false;
 
  // DM Hansen
  private final File PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
  
  Button apButton, astbutton, aetbutton, aButton;
  Button gpButton, gstbutton, getbutton, gButton;
  Button backbutton;
  
  // Time Picker Dialog and display
	TimePicker time_picker;
	static final int dialog_id = 0;
	
	// Accel Variables
	int ashour, asminute;
	int ashour2;
	int aehour, aeminute;
	int aehour2;
	
	TextView astlabel;
	TextView aetlabel;
	
	//Gyro Variables
	int gshour, gsminute;
	int gshour2;
	int gehour, geminute;
	int gehour2;	
	
	TextView gstlabel;
	TextView getlabel;
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    super.onCreate(savedInstanceState);
    Intent intent = getIntent();
    setContentView(R.layout.plot);
    
    apButton = (Button) findViewById(R.id.apButton);
    astbutton = (Button) findViewById(R.id.astbutton);
    aetbutton = (Button) findViewById(R.id.aetbutton);
    aButton = (Button) findViewById(R.id.aButton);
    gpButton = (Button) findViewById(R.id.gpButton);
    gstbutton = (Button) findViewById(R.id.gstbutton);
    getbutton = (Button) findViewById(R.id.getbutton);
    gButton = (Button) findViewById(R.id.gButton);
    backbutton = (Button) findViewById(R.id.backbutton);
    
    apButton();
	astbutton();
	aetbutton();
	aButton();
    gpButton();
	gstbutton();
	getbutton();
	gButton();
    backbutton();
    
	showDialog(dialog_id);
	astlabel=(TextView)findViewById(R.id.asttextView);
	aetlabel=(TextView)findViewById(R.id.aettextView);
	gstlabel=(TextView)findViewById(R.id.gsttextView);
	getlabel=(TextView)findViewById(R.id.gettextView);	

    
    // Used only for debugging purposes. On app start, this boolean will be used to clear the data save file.
    // isBeginning = true;
        
    PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

    // BLE variables
    mBtLeService = BluetoothLeService.getInstance();
    mBluetoothDevice = intent.getParcelableExtra(EXTRA_DEVICE);
    mServiceList = new ArrayList<BluetoothGattService>();
  	registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

    // Plot variables. Creates a plot and instantiates the series for each axis of acceleration
    aSensorPlot = (XYPlot) findViewById(R.id.aSensorPlot);
    gSensorPlot = (XYPlot) findViewById(R.id.gSensorPlot);
    hPlot = (XYPlot) findViewById(R.id.hPlot);
    
    // instantiate an some new historySeries and then add them to the sensor plots
    for(int i=0; i<2;i++ )
	{
		historySeries[i] = new SimpleXYSeries("axis");
		historySeries[i].useImplicitXVals();
		aSensorPlot.addSeries(historySeries[i], new LineAndPointFormatter(Color.rgb(100,100,200),Color.BLACK, null, null));
	}

    // freeze the range boundaries:
    aSensorPlot.setRangeBoundaries(-4, 4, BoundaryMode.FIXED);
    aSensorPlot.setDomainBoundaries(0, SERIES_SIZE, BoundaryMode.FIXED);
    aSensorPlot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 1);
    aSensorPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 5);
    aSensorPlot.setRangeValueFormat( new DecimalFormat("#"));
    
    // Reformats the axis tick labels
    String[] graph_labels = {"0","","","","","-0.5","","","","","-1",
    						"-1.5","","","","","-2","","","","","-2.5",
    						"-3","","","","","-3.5","","","","","-4",
    						"-4.5","","","","","-5"};
    
    // Uses a custom index format to change the tick labels
    MyIndexFormat mif = new MyIndexFormat ();
    mif.Labels = graph_labels;

    // Attach index->string formatter to the plot instance
    aSensorPlot.getGraphWidget().setDomainValueFormat(mif); 
        
    final PlotStatistics histStats = new PlotStatistics(1000, false);
    
    aSensorPlot.addListener(histStats);
    aSensorPlot.setLayerType(View.LAYER_TYPE_NONE, null);
    
    // GATT database
    Resources res = getResources();
    XmlResourceParser xpp = res.getXml(R.xml.gatt_uuid);
    new GattInfo(xpp);
    
    mBtGatt = BluetoothLeService.getBtGatt();    
    
    // Initialize save file for use if there is a network outage.
    PATH.mkdirs();
    // Now create a file in that location
    curr_file = new File(PATH, FILENAME);

    curr_file.setWritable(true);
    buff_count = 0;
    
    // Start service discovery
    if (!mServicesRdy && mBtGatt != null) {
      if (mBtLeService.getNumServices() == 0)
        discoverServices();
      else
        displayServices();
    }
  }
  
  
  //------------------------------------------------------------------------------------------  
  // Dialog Implementation and Storing User Input  
    
    
    // Display user input accelerometer start time
	public void updateAccelStartTime()
	{
		if (ashour == 0)
		{
			ashour2 = ashour+12;
			if (asminute < 10)
			{
				astlabel.setText(ashour2+":0"+asminute+" AM");
			}
			else
			{
				astlabel.setText(ashour2+":"+asminute+" AM");
			}
		}
		else if (ashour-12 < 0)
		{
			if (asminute < 10)
			{
				astlabel.setText(ashour+":0"+asminute+" AM");
			}
			else
			{
				astlabel.setText(ashour+":"+asminute+" AM");
			}
		}
		else if (ashour-12 == 0)
		{
			if (asminute < 10)
			{
				astlabel.setText(ashour+":0"+asminute+" PM");
			}
			else
			{
				astlabel.setText(ashour+":"+asminute+" PM");
			}
		}
		else if (ashour > 12)
		{
			ashour2 = ashour-12;
			if (asminute < 10)
			{
				astlabel.setText(ashour2+":0"+asminute+" PM");
			}
			else
			{
				astlabel.setText(ashour2+":"+asminute+" PM");
			}
		}
	}
  
	// Display user input accelerometer end time
	public void updateAccelEndTime()
	{
		if (aehour == 0)
		{
			aehour2 = aehour+12;
			if (aeminute < 10)
			{
				aetlabel.setText(aehour2+":0"+aeminute+" AM");
			}
			else
			{
				aetlabel.setText(aehour2+":"+aeminute+" AM");
			}
		}
		else if (aehour-12 < 0)
		{
			if (aeminute < 10)
			{
				aetlabel.setText(aehour+":0"+aeminute+" AM");
			}
			else
			{
				aetlabel.setText(aehour+":"+aeminute+" AM");
			}
		}
		else if (aehour-12 == 0)
		{
			if (aeminute < 10)
			{
				aetlabel.setText(aehour+":0"+aeminute+" PM");
			}
			else
			{
				aetlabel.setText(aehour+":"+aeminute+" PM");
			}
		}
		else if (aehour > 12)
		{
			aehour2 = aehour-12;
			if (aeminute < 10)
			{
				aetlabel.setText(aehour2+":0"+aeminute+" PM");
			}
			else
			{
				aetlabel.setText(aehour2+":"+aeminute+" PM");
			}
		}

    }	
	
	// Display user input gyroscope start time
	public void updateGyroStartTime()
	{
		if (gshour == 0)
		{
			gshour2 = gshour+12;
			if (gsminute < 10)
			{
				gstlabel.setText(gshour2+":0"+gsminute+" AM");
			}
			else
			{
				gstlabel.setText(gshour2+":"+gsminute+" AM");
			}
		}
		else if (gshour-12 < 0)
		{
			if (gsminute < 10)
			{
				gstlabel.setText(gshour+":0"+gsminute+" AM");
			}
			else
			{
				gstlabel.setText(gshour+":"+gsminute+" AM");
			}
		}
		else if (gshour-12 == 0)
		{
			if (gsminute < 10)
			{
				gstlabel.setText(gshour+":0"+gsminute+" PM");
			}
			else
			{
				gstlabel.setText(gshour+":"+gsminute+" PM");
			}
		}
		else if (gshour > 12)
		{
			gshour2 = gshour-12;
			if (gsminute < 10)
			{
				gstlabel.setText(gshour2+":0"+gsminute+" PM");
			}
			else
			{
				gstlabel.setText(gshour2+":"+gsminute+" PM");
			}
		}
	}
	
	// Display user input gyroscope end time
	public void updateGyroEndTime()
	{
		if (gehour == 0)
		{
			gehour2 = gehour+12;
			if (geminute < 10)
			{
				getlabel.setText(gehour2+":0"+geminute+" AM");
			}
			else
			{
				getlabel.setText(gehour2+":"+geminute+" AM");
			}
		}
		else if (gehour-12 < 0)
		{
			if (geminute < 10)
			{
				getlabel.setText(gehour+":0"+geminute+" AM");
			}
			else
			{
				getlabel.setText(gehour+":"+geminute+" AM");
			}
		}
		else if (gehour-12 == 0)
		{
			if (geminute < 10)
			{
				getlabel.setText(gehour+":0"+geminute+" PM");
			}
			else
			{
				getlabel.setText(gehour+":"+geminute+" PM");
			}
		}
		else if (gehour > 12)
		{
			gehour2 = gehour-12;
			if (geminute < 10)
			{
				getlabel.setText(gehour2+":0"+geminute+" PM");
			}
			else
			{
				getlabel.setText(gehour2+":"+geminute+" PM");
			}
		}
	}
	
	// Accelerometer Start Time Dialog
	private TimePickerDialog.OnTimeSetListener asTimeSetListener =
			new TimePickerDialog.OnTimeSetListener() {
		
				@Override
				public void onTimeSet(TimePicker view, int hourOfDay, int hour_minute) {
					// TODO Auto-generated method stub
					
						ashour = hourOfDay;
						asminute = hour_minute;
						updateAccelStartTime();
				}
					
			};
			
	// Accelerometer End Time Dialog	
	private TimePickerDialog.OnTimeSetListener aeTimeSetListener =
			new TimePickerDialog.OnTimeSetListener() {
						
				@Override
				public void onTimeSet(TimePicker view, int hourOfDay, int hour_minute) {
					// TODO Auto-generated method stub		

						aehour = hourOfDay;
						aeminute = hour_minute;
						updateAccelEndTime();
				        
				}
					
			};	
			
	// Gyroscope Start Time Dialog
	private TimePickerDialog.OnTimeSetListener gsTimeSetListener =
			new TimePickerDialog.OnTimeSetListener() {
					
		
				@Override
				public void onTimeSet(TimePicker view, int hourOfDay, int hour_minute) {
					// TODO Auto-generated method stub
					
						gshour = hourOfDay;
						gsminute = hour_minute;
						updateGyroStartTime();
				}			
			};			
	
	// Gyroscope End Time Dialog
	private TimePickerDialog.OnTimeSetListener geTimeSetListener =
			new TimePickerDialog.OnTimeSetListener() {
					
		
				@Override
				public void onTimeSet(TimePicker view, int hourOfDay, int hour_minute) {
					// TODO Auto-generated method stub
					
						gehour = hourOfDay;
						geminute = hour_minute;
						updateGyroEndTime();
				}	
					
			};					

	// Acceleromter Start Time Button		
	private void astbutton() {
		// TODO Auto-generated method stub
		
		// 1. Get a reference to the button.
		Button astbutton = (Button) findViewById(R.id.astbutton);
		
		// 2. Set the click listener to run my code
		astbutton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setAccelStartTime();

			}
		});
	}		
	
	// Acceleromter End Time Button	
	private void aetbutton() {
		// TODO Auto-generated method stub
		
		// 1. Get a reference to the button.
		Button aetbutton = (Button) findViewById(R.id.aetbutton);
		
		// 2. Set the click listener to run my code
		aetbutton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setAccelEndTime();

			}
		});
	}		
	

	
	
	// Gyroscope Start Time Button		
	private void gstbutton() {
		// TODO Auto-generated method stub
		
		// 1. Get a reference to the button.
		Button gstbutton = (Button) findViewById(R.id.gstbutton);
		
		// 2. Set the click listener to run my code
		gstbutton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setGyroStartTime();

			}
		});
	}		
	
	// Gyroscope Start Time Button		
	private void getbutton() {
		// TODO Auto-generated method stub
		
		// 1. Get a reference to the button.
		Button getbutton = (Button) findViewById(R.id.getbutton);
		
		// 2. Set the click listener to run my code
		getbutton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setGyroEndTime();

			}
		});
	}				
	
	public void setAccelStartTime()
	{
		new TimePickerDialog(this, asTimeSetListener, ashour, asminute, false).show();
	}	
	
	public void setAccelEndTime()
	{
		new TimePickerDialog(this, aeTimeSetListener, aehour, aeminute, false).show();
	}	
    
	public void setGyroStartTime()
	{
		new TimePickerDialog(this, gsTimeSetListener, gshour, gsminute, false).show();
	}	

	public void setGyroEndTime()
	{
		new TimePickerDialog(this, geTimeSetListener, gehour, geminute, false).show();
	}		
	
	
	// toggle real time plot for acclerometer
		private void apButton() {
			apButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
						aSensorPlot.setVisibility(View.VISIBLE);
						backbutton.setVisibility(View.VISIBLE);
						
						apButton.setVisibility(View.INVISIBLE);
						astbutton.setVisibility(View.INVISIBLE);
						aetbutton.setVisibility(View.INVISIBLE);
						aButton.setVisibility(View.INVISIBLE);
						gpButton.setVisibility(View.INVISIBLE);
						gstbutton.setVisibility(View.INVISIBLE);
						getbutton.setVisibility(View.INVISIBLE);
						gButton.setVisibility(View.INVISIBLE);
				}
			});
		}
    // This button toggles the history plot for acceleration
		private void aButton() {
			Button aButton = (Button) findViewById(R.id.aButton);
			aButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					hPlot.setVisibility(View.VISIBLE);
					backbutton.setVisibility(View.VISIBLE);
				}
			});
		}	
		
    // toggle real time plot for gyroscope
		private void gpButton() {
			gpButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
						gSensorPlot.setVisibility(View.VISIBLE);
						backbutton.setVisibility(View.VISIBLE);
						
						apButton.setVisibility(View.INVISIBLE);
						astbutton.setVisibility(View.INVISIBLE);
						aetbutton.setVisibility(View.INVISIBLE);
						aButton.setVisibility(View.INVISIBLE);
						gpButton.setVisibility(View.INVISIBLE);
						gstbutton.setVisibility(View.INVISIBLE);
						getbutton.setVisibility(View.INVISIBLE);
						gButton.setVisibility(View.INVISIBLE);

				}
			});
		}
    // This button toggles the history plot for gyroscope
		private void gButton() {
			Button gButton = (Button) findViewById(R.id.gButton);
			gButton.setOnClickListener(new View.OnClickListener() {
	
				@Override
				public void onClick(View v) {
					hPlot.setVisibility(View.VISIBLE);
					backbutton.setVisibility(View.VISIBLE);
				}
			});
		}	
		
		private void backbutton() {
			backbutton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
						aSensorPlot.setVisibility(View.INVISIBLE);
                        gSensorPlot.setVisibility(View.INVISIBLE);
						backbutton.setVisibility(View.INVISIBLE);
						hPlot.setVisibility(View.INVISIBLE);
						
						apButton.setVisibility(View.VISIBLE);
						astbutton.setVisibility(View.VISIBLE);
						aetbutton.setVisibility(View.VISIBLE);
						aButton.setVisibility(View.VISIBLE);
						gpButton.setVisibility(View.VISIBLE);
						gstbutton.setVisibility(View.VISIBLE);
						getbutton.setVisibility(View.VISIBLE);
						gButton.setVisibility(View.VISIBLE);

				}
			});
		}

	//------------------------------------------------------------------------------------------------
	
	
//------------------------------------------------------------------------------------------ 

	@Override
  public void onDestroy() {
    super.onDestroy();
    finishActivity(HIST_ACT_REQ);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu items for use in the action bar
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.device_activity_actions, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle presses on the action bar items
    switch (item.getItemId()) {
    case R.id.opt_x:
    	toggle_plot[0] = !toggle_plot[0];
    	break;
    case R.id.opt_y:
    	toggle_plot[1] = !toggle_plot[1];
    	break;
    case R.id.opt_z:
    	toggle_plot[2] = !toggle_plot[2];
    	break;
    case R.id.opt_tot:
    	toggle_plot[3] = !toggle_plot[3];
    	break;
    case R.id.opt_filesize:
    	// Display size of file
    	long file_size = len;
    	String units = " Bytes";
    	if(len>1000&&len<1000000) {
    		file_size=len/1000;
    		units=" KB";
    	}
    	else if (len>1000000) {
    		file_size=len/1000000;
    		units=" MB";
    	}
    	Toast.makeText(this, Long.toString(file_size)+units, Toast.LENGTH_LONG).show();
    	break;
    default:
      return super.onOptionsItemSelected(item);
    }
    return true;
  }
  
  @Override 
  protected void onResume()
  {
  	Log.d(TAG,"onResume");
    super.onResume();
    if (!mIsReceiving) {
    	registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    	mIsReceiving = true;
    }
  }

  @Override
  protected void onPause() {
  	Log.d(TAG,"onPause");
  	super.onPause();
  	if (mIsReceiving) {
  		unregisterReceiver(mGattUpdateReceiver);
  		mIsReceiving = false;
  	}
  }

  // Creates an intent filter which notifies the activity when the device is trying to do something
  private static IntentFilter makeGattUpdateIntentFilter() {
  	final IntentFilter fi = new IntentFilter();
  	fi.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
  	fi.addAction(BluetoothLeService.ACTION_DATA_NOTIFY);
  	fi.addAction(BluetoothLeService.ACTION_DATA_WRITE);
  	fi.addAction(BluetoothLeService.ACTION_DATA_READ);
  	return fi;
  }

  void onViewInflated(View view) {
    Log.d(TAG, "Gatt view ready");

    // Set title bar to device name
    setTitle(mBluetoothDevice.getName());
    // Create GATT object
    mBtGatt = BluetoothLeService.getBtGatt();

    // Start service discovery
    if (!mServicesRdy && mBtGatt != null) {
      if (mBtLeService.getNumServices() == 0)
        discoverServices();
      else
        displayServices();
    }
  }

  // Application implementation
  // If any sensors are listed, clear them. Then go through all enabled sensors and update list.
  private void updateSensorList() {
  	mEnabledSensors.clear();

  	for (int i=0; i<Sensor.SENSOR_LIST.length; i++) {
  		Sensor sensor = Sensor.SENSOR_LIST[i];
  		if (isEnabledByPrefs(sensor)) {
  			mEnabledSensors.add(sensor);
  		}
  	}
  }
  
  // Used to reformat plot tick labels
  public class MyIndexFormat extends Format {

	    public String[] Labels = null;

	        @Override
	        public StringBuffer format(Object obj, 
	                StringBuffer toAppendTo, 
	                FieldPosition pos) {

	            // try turning value to index because it comes from indexes
	            // but if is too far from index, ignore it - it is a tick between indexes
	            float fl = ((Number)obj).floatValue();
	            int index = Math.round(fl);
	            if(Labels == null || Labels.length <= index ||
	                    Math.abs(fl - index) > 0.1)
	                return new StringBuffer("");    

	            return new StringBuffer(Labels[index]); 
	        }

			@Override
			public Object parseObject(String arg0, ParsePosition arg1) {
				return null;
			}
  }

  // Check to see if user wants the given sensors displaying data. If app receives sensor data from a sensor that is not preffed,
  // it will not be displayed.
  boolean isEnabledByPrefs(final Sensor sensor) {
    String preferenceKeyString = "pref_" + sensor.name().toLowerCase(Locale.ENGLISH) + "_on";

    // Keep data on preferences even when app isn't running
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

    Boolean defaultValue = true;
    return prefs.getBoolean(preferenceKeyString, defaultValue);
  }

  BluetoothGattService getConnControlService() {
    return mConnControlService;
  }
  
  // Starts the history plot activity, which should make a static plot of historical data
  private void startHistoryActivity() {
	  final Intent i = new Intent(this, HistoryPlot.class);
	  startActivityForResult(i,HIST_ACT_REQ);
  }
  
  private void discoverServices() {
    if (mBtGatt.discoverServices()) {
      Log.i(TAG, "START SERVICE DISCOVERY");
      mServiceList.clear();
    }
  }

  private void displayServices() {
    mServicesRdy = true;

    try {
      mServiceList = mBtLeService.getSupportedGattServices();
    } catch (Exception e) {
      e.printStackTrace();
      mServicesRdy = false;
    }

    // Characteristics descriptor readout done
    if (mServicesRdy) {
      enableSensors(true);
      enableNotifications(true);
    }
  }

  // Enables data reception from sensors on the board.
  private void enableSensors(boolean enable) {
  		BluetoothGattService serv = mBtGatt.getService(servUuid);
  		BluetoothGattCharacteristic charac = serv.getCharacteristic(confUuid);

  		// Assigns value as either enabled or disabled. Use getEnableSensorCode() because of gyroscope funkiness (described in Sensor class)***
  		byte value =  ENABLE_SENSOR_CODE;
  		mBtLeService.writeCharacteristic(charac, value);
		mBtLeService.waitIdle(GATT_TIMEOUT);
  	// }
  	
  }

  // Allows the app to receive notifications from the device. Notifications are used to tell
  // the app that there is data available to be received.
  private void enableNotifications(boolean enable) {
  		BluetoothGattService serv = mBtGatt.getService(servUuid);
  		BluetoothGattCharacteristic charac = serv.getCharacteristic(dataUuid);
  		BluetoothGattCharacteristic period = serv.getCharacteristic(perUUID);
  		
  		mBtLeService.setCharacteristicNotification(charac,enable);
		mBtLeService.waitIdle(GATT_TIMEOUT);
		mBtLeService.writeCharacteristic(period,ACC_PERIOD);
  }

  // Activity result handling
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    switch (requestCode) {
    case HIST_ACT_REQ:
    	Toast.makeText(this, "Returning to live plot", Toast.LENGTH_SHORT).show();
      if (!mIsReceiving) {
      	mIsReceiving = true;
      	registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
      }

      updateSensorList();
      enableSensors(true);
      enableNotifications(true);
      break;
    default:
      Log.e(TAG, "Unknown request code");
      break;
    }
  }

  // Listens for updates from the BLE service. If the update is a data notification, the
  // updatereceiver takes the data and calls the plotting functions
  private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
  	@Override
  	public void onReceive(Context context, Intent intent) {
  		final String action = intent.getAction();
  		int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS, BluetoothGatt.GATT_SUCCESS);

  		if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
  			if (status == BluetoothGatt.GATT_SUCCESS) {
  				displayServices();
  			  	// Enable Sensors and Start Plotting 
  			    enableSensors(true);
  			    enableNotifications(true);
  			} else {
  				Toast.makeText(getApplication(), "Service discovery failed", Toast.LENGTH_LONG).show();
  				return;
  			}
  		} else if (BluetoothLeService.ACTION_DATA_NOTIFY.equals(action)) {
  			// Notification - get the time that the data was received and update the plot

  			String uuidStr = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);
  			Log.i(TAG,"Data discovered.");
  			
  			byte[] value = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
  			String[] time = getTimeStamp();
  			dataPoint myPoint = new dataPoint(value, time);

  			updatePlots(uuidStr, myPoint);
  			saveData(myPoint);
  			
  		} else if (BluetoothLeService.ACTION_DATA_WRITE.equals(action)) {
  			// Data written to device
  			String uuidStr = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);
  			onCharacteristicWrite(uuidStr,status);
  		} else if (BluetoothLeService.ACTION_DATA_READ.equals(action)) {
  			// Data read
  			String uuidStr = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);
  			byte  [] value = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
  			// onCharacteristicsRead(uuidStr,value,status);
  		}

  		if (status != BluetoothGatt.GATT_SUCCESS) {
  			Log.e(TAG,"GATT error code: " + status);
  		}
  	}
  };

	private void onCharacteristicWrite(String uuidStr, int status) {
	  Log.d(TAG,"onCharacteristicWrite: " + uuidStr);
  }
	

	
	// Make sure the file size is accurate
	private void updateFileSize() {
		len = curr_file.length();
	}
	
	// Reads a file to a byte array. NOT USED
	public static byte[] readFileToByteArray(File file) throws IOException {
        InputStream in = null;
        try {
            in = openInputStream(file);
            return toByteArray(in, file.length());
        } finally {
            closeQuietly((Closeable)in);
        }
    }
	
	// Closes an inputstream. NOT USED
	public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }
	
	// Opens an input stream for reading a file. NOT USED.
	public static FileInputStream openInputStream(File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (file.canRead() == false) {
                throw new IOException("File '" + file + "' cannot be read");
            }
        } else {
            throw new FileNotFoundException("File '" + file + "' does not exist");
        }
        return new FileInputStream(file);
    }
	
	// Returns an input stream as a byte array. NOT USED.
	public static byte[] toByteArray(InputStream input, long size) throws IOException {
      if(size > Integer.MAX_VALUE) {
          throw new IllegalArgumentException("Size cannot be greater than Integer max value: " + size);
      }

      return toByteArray(input, (int) size);
    }
	
	// Logs the data written to the file in logcat for debugging purposes. Only used to verify that
	// data was saved properly.
	void logData(File f) {
		InputStream in = null;

		try {
			byte[] bytes = new byte[4];
			in = new BufferedInputStream(new FileInputStream(f));
			file_vec.add(in.read(bytes,0,4));
			String temp = Integer.toString(file_vec.get(buff_count));
			float coord = ByteArray2float(bytes);
			Log.i(TAG,"READ:" + Float.toString(coord));
			buff_count++;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if (in!=null) {
				in.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Returns an array with the year, month, day, hours, minutes, seconds and millisecond count
	private String[] getTimeStamp() {
		Calendar cal = Calendar.getInstance();
		
		int hrs = cal.get(Calendar.HOUR_OF_DAY);
		int mins = cal.get(Calendar.MINUTE);
		int secs = cal.get(Calendar.SECOND);
		
		float f_hrs = (float)hrs;
		float f_mins = (float)mins;
		float f_secs = (float)secs;
		float f_mils = (float)(cal.get(Calendar.MILLISECOND));
		
		String[] t = {Integer.toString(cal.get(Calendar.YEAR)), Integer.toString(cal.get(Calendar.MONTH)), 
				Integer.toString(cal.get(Calendar.DATE)), Integer.toString(hrs), Integer.toString(mins), Integer.toString(secs), ""};
		
		String temp = Float.toString(3600000*f_hrs + 60000*f_mins + 1000*f_secs + f_mils);
		
		t[6] = temp;
				
		return t;
	}
	
	// Converts a float to a byte array
	public static byte [] float2ByteArray (float value)
	{  
		 return ByteBuffer.allocate(4).putFloat(value).array();
	}
	
	// Converts a byte array to a float
	public static float ByteArray2float (byte[] array) {
		return ByteBuffer.wrap(array).getFloat();
	}
	
	
	// Saves data to a file in internal memory when data connection is not present
	void saveData(dataPoint point) {
		int k = point.data.length;
		byte[][] coords = new byte[3][4];
		byte[][] toWrite;
		for (int i=0;i<point.data.length;i++){
			float x = (float) point.data[i];
			coords[i] = float2ByteArray(x);
			toWrite[i] = coords[i]; 
		}
		
		if(!curr_file.exists()) {
			// curr_file = new File(getFilesDir(), FILENAME);
			PATH.mkdirs();
			// Now create a file in that location
			curr_file = new File(PATH, FILENAME);
			Log.i(TAG, "New File Created");
		}
		// Define the file size limit (11.52 MB corresponds approximately to 8 hours of data)
		final long FILE_SIZE = 11520000;
		updateFileSize();
		Log.i(TAG, Long.toString(len));
		// If the file gets too big (ie exceeds the amount of data recorded over 8 hours) delete it and start over again.
		if(len>FILE_SIZE) {
			Log.i(TAG,"File Size Exceeded");
			curr_file.delete();
			isBeginning = true;
			updateFileSize();
		}
		else
			isBeginning=false;
		
		for(int i=0; i<point.tStamp.length; i++) {
			toWrite[i+k] = float2ByteArray(Float.parseFloat(point.tStamp[i])); // t.getBytes("UTF-8");
		}
		
		// Write all three axes to the file using an outputstream
		FileOutputStream outputStream;
		try {
			  outputStream = new FileOutputStream(curr_file, !isBeginning);
			  for (int i = 0; i < toWrite.length; i++) {
				  outputStream.write(toWrite[i],0,toWrite[i].length);
				  float justwrote = ByteArray2float(toWrite[i]);
				  Log.i(TAG, "WRITE:" + Float.toString(justwrote));
			  }
			  outputStream.close();
			  
			} catch (Exception e) {
			  e.printStackTrace();
			}
	}
	
	// Updates the plot with new data obtained from the service notification
	void updatePlots(String uuidStr, dataPoint point) {
		int k = point.data.length;

		
		// use accelerometer conversion (1/64) and add data to history series
		for (int i=0; i<k; i++) {
			point.data[i] /= 64;
			historySeries[i].addFirst(null, point.data[i]);
			
		}
	  	  // get rid the oldest sample in history:
	      if (historySeries[1].size() > HISTORY_SIZE) {
	    	  for(int i=0;i<k;i++) historySeries[i].removeLast();
          }
	      
	      // redraw the Plots:
 	      aSensorPlot.redraw();
 	      Log.i(TAG, "Plot updated.");
	  }
	
	public class dataPoint{
		public byte[] data;
		public String[] tStamp;

		public dataPoint(byte[] data, String[] tStamp) {
			super();
			this.data = data;
			this.tStamp = tStamp;
		}
		public byte[] getData() {
			return data;
		}
		public void setData(byte[] data) {
			this.data = data;
		}
		public String gettStamp() {
			return tStamp;
		}
		public void settStamp(String[] tStamp) {
			this.tStamp = tStamp;
		}
	}
}
