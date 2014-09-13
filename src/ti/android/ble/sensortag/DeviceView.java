///////////////////////////////////////////
//        NOT USED. OLD TI CODE.         //
///////////////////////////////////////////
package ti.android.ble.sensortag;

import static ti.android.ble.sensortag.R.drawable.buttonsoffoff;
import static ti.android.ble.sensortag.R.drawable.buttonsoffon;
import static ti.android.ble.sensortag.R.drawable.buttonsonoff;
import static ti.android.ble.sensortag.R.drawable.buttonsonon;
import static ti.android.ble.sensortag.SensorTag.UUID_ACC_DATA;
import static ti.android.ble.sensortag.SensorTag.UUID_BAR_DATA;
import static ti.android.ble.sensortag.SensorTag.UUID_GYR_DATA;
import static ti.android.ble.sensortag.SensorTag.UUID_HUM_DATA;
import static ti.android.ble.sensortag.SensorTag.UUID_IRT_DATA;
import static ti.android.ble.sensortag.SensorTag.UUID_KEY_DATA;
import static ti.android.ble.sensortag.SensorTag.UUID_MAG_DATA;

import java.text.DecimalFormat;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import ti.android.util.Point3D;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.FloatMath;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.util.PlotStatistics;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;

// Fragment for Device View
public class DeviceView extends Fragment {
	
	private static final String TAG = "DeviceView";
	
  // Sensor table; the iD corresponds to row number
	private static final int ID_OFFSET = 0;
  private static final int ID_KEY = 0;
  private static final int ID_ACC = 1;
  private static final int ID_MAG = 2;
  private static final int ID_GYR = 3;
  private static final int ID_OBJ = 4;
  private static final int ID_AMB = 5;
  private static final int ID_HUM = 6;
  private static final int ID_BAR = 7;
  
  // Number of data points to keep in history
  private static final int HISTORY_SIZE = 30;

	public static DeviceView mInstance = null;

	// GUI
  private TableLayout table;
	private TextView mAccValue;
	private TextView mMagValue;
	private TextView mGyrValue;
	private TextView mObjValue;
	private TextView mAmbValue;
	private TextView mHumValue;
	private TextView mBarValue;
	private ImageView mButton;
	private TextView mStatus;
	private TableRow mMagPanel;
	private TableRow mBarPanel;
	
  // House-keeping
  private DecimalFormat decimal = new DecimalFormat("+0.00;-0.00");
  private DeviceActivity mActivity;
  private static final double PA_PER_METER = 12.0;
  
  // Plot Stuff
  private XYPlot SensorPlot;
  private SimpleXYSeries xHistorySeries = null;
  private SimpleXYSeries yHistorySeries = null;
  private SimpleXYSeries zHistorySeries = null;
  private SimpleXYSeries totHistorySeries = null;
  private SimpleXYSeries accLevelsSeries = null;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    Log.i(TAG, "onCreateView");
    mInstance = this;
    // mActivity = (DeviceActivity) getActivity();
    
    // The last two arguments ensure LayoutParams are inflated properly.
    // View view = inflater.inflate(R.layout.plot, container, false);

    /*
    // Hide all Sensors initially (but show the last line for status)
    table = (TableLayout) view.findViewById(R.id.services_browser_layout);

    // UI widgets
    mAccValue = (TextView) view.findViewById(R.id.accelerometerTxt);
    mMagValue = (TextView) view.findViewById(R.id.magnetometerTxt);
  	mGyrValue = (TextView) view.findViewById(R.id.gyroscopeTxt);
  	mObjValue = (TextView) view.findViewById(R.id.objTemperatureText);
  	mAmbValue = (TextView) view.findViewById(R.id.ambientTemperatureTxt);
  	mHumValue = (TextView) view.findViewById(R.id.humidityTxt);
  	mBarValue = (TextView) view.findViewById(R.id.barometerTxt);
  	mButton = (ImageView) view.findViewById(R.id.buttons);
  	mStatus = (TextView) view.findViewById(R.id.status);
  	
  	// Support for calibration
  	mMagPanel = (TableRow) view.findViewById(R.id.magPanel);
  	mBarPanel = (TableRow) view.findViewById(R.id.barPanel);
  	OnClickListener cl = new OnClickListener() {
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.magPanel:
					mActivity.calibrateMagnetometer();
					break;
				case R.id.barPanel:
					mActivity.calibrateHeight();
					break;
				default:
				}
			}
		};
		
		mMagPanel.setOnClickListener(cl);
		mBarPanel.setOnClickListener(cl);
		*/
  	
    
	// Initialize Plot
	
	// initialize our XYPlot reference:
    // SensorPlot = (XYPlot) view.findViewById(R.id.SensorPlot);
    
    // plotUpdater = new MyPlotUpdater(SensorPlot);                           // Set the display title of the series
    
    xHistorySeries = new SimpleXYSeries("X Axis");
    xHistorySeries.useImplicitXVals();
    yHistorySeries = new SimpleXYSeries("Y Axis");
    yHistorySeries.useImplicitXVals();
    zHistorySeries = new SimpleXYSeries("Z Axis");
    zHistorySeries.useImplicitXVals();
    totHistorySeries = new SimpleXYSeries("Total Acc.");
    totHistorySeries.useImplicitXVals();
    accLevelsSeries = new SimpleXYSeries("Acc. Levels");
    accLevelsSeries.useImplicitXVals();
    
 // freeze the range boundaries:
    SensorPlot.setRangeBoundaries(-50, 50, BoundaryMode.FIXED);
    SensorPlot.setDomainBoundaries(0, 30, BoundaryMode.FIXED);
    SensorPlot.addSeries(xHistorySeries, new LineAndPointFormatter(Color.rgb(100,100,200),Color.BLACK, null, null));
    SensorPlot.addSeries(yHistorySeries, new LineAndPointFormatter(Color.rgb(100,200,100),Color.BLACK, null, null));
    SensorPlot.addSeries(zHistorySeries, new LineAndPointFormatter(Color.rgb(200,100,100),Color.BLACK, null, null));
    SensorPlot.addSeries(totHistorySeries, new LineAndPointFormatter(Color.rgb(200,100,200),Color.BLACK, null, null));
    
    // thin out domain/range tick labels so they dont overlap each other:
    SensorPlot.setTicksPerDomainLabel(5);
    SensorPlot.setTicksPerRangeLabel(5);
    
    final PlotStatistics histStats = new PlotStatistics(1000, false);
    
    SensorPlot.addListener(histStats);
    SensorPlot.setLayerType(View.LAYER_TYPE_NONE, null);
    
    
		
    // Notify activity that UI has been inflated
    // mActivity.onViewInflated(view);

    return null;
  }


  @Override
  public void onResume() {
    super.onResume();
    updateVisibility();
  }


	@Override
  public void onPause() {
    super.onPause();
  }

  /**
   * Handle changes in sensor values
   * */
  public void onCharacteristicChanged(String uuidStr, byte[] rawValue) {
		Point3D v;
		String msg;

  	if (uuidStr.equals(UUID_ACC_DATA.toString())) {
  		v = Sensor.ACCELEROMETER.convert(rawValue);
  		msg = decimal.format(v.x) + "\n" + decimal.format(v.y) + "\n" + decimal.format(v.z) + "\n";
  		mAccValue.setText(msg);
  		updatePlot(v);
  	} 
  
  	if (uuidStr.equals(UUID_MAG_DATA.toString())) {
  		v = Sensor.MAGNETOMETER.convert(rawValue);
  		msg = decimal.format(v.x) + "\n" + decimal.format(v.y) + "\n" + decimal.format(v.z) + "\n";
  		mMagValue.setText(msg);
  	} 

  	if (uuidStr.equals(UUID_GYR_DATA.toString())) {
  		v = Sensor.GYROSCOPE.convert(rawValue);
  		msg = decimal.format(v.x) + "\n" + decimal.format(v.y) + "\n" + decimal.format(v.z) + "\n";
  		mGyrValue.setText(msg);
  		//updatePlot(v);
  	} 

  	if (uuidStr.equals(UUID_IRT_DATA.toString())) {
  		v = Sensor.IR_TEMPERATURE.convert(rawValue);
  		msg = decimal.format(v.x) + "\n";
  		mAmbValue.setText(msg);
  		msg = decimal.format(v.y) + "\n";
  		mObjValue.setText(msg);
  	}
  	
  	if (uuidStr.equals(UUID_HUM_DATA.toString())) {
  		v = Sensor.HUMIDITY.convert(rawValue);
  		msg = decimal.format(v.x) + "\n";
  		mHumValue.setText(msg);
  	}


  	if (uuidStr.equals(UUID_KEY_DATA.toString())) {
  		SimpleKeysStatus s;
  		final int img;
  		s = Sensor.SIMPLE_KEYS.convertKeys(rawValue);
  		
  		switch (s) {
  		case OFF_OFF:
  			img = buttonsoffoff;
  			break;
  		case OFF_ON:
  			img = buttonsoffon;
  			break;
  		case ON_OFF:
  			img = buttonsonoff;
  			break;
  		case ON_ON:
  			img = buttonsonon;
  			break;
  		default:
  			throw new UnsupportedOperationException();
  		}

  		mButton.setImageResource(img);
  	}
  }
  
  void updatePlot(Point3D v) {
 	  Number[] series1Numbers = {v.x, v.y, v.z};
  	 
  	
  	// get rid the oldest sample in history:
      if (xHistorySeries.size() > HISTORY_SIZE) {
          xHistorySeries.removeFirst();
          yHistorySeries.removeFirst();
          zHistorySeries.removeFirst();
          totHistorySeries.removeFirst();
      }

      // add the latest history sample:
      xHistorySeries.addLast(null, v.x);
      yHistorySeries.addLast(null, v.y);
      zHistorySeries.addLast(null, v.z);
      totHistorySeries.addLast(null, FloatMath.sqrt(FloatMath.pow((float)v.x,2.0f)+FloatMath.pow((float)v.y,2.0f)+FloatMath.pow((float)v.z,2.0f)));

      // redraw the Plots:
      SensorPlot.redraw();
  }
  
  void updateVisibility() {
  	showItem(ID_KEY,mActivity.isEnabledByPrefs(Sensor.SIMPLE_KEYS));
  	showItem(ID_ACC,mActivity.isEnabledByPrefs(Sensor.ACCELEROMETER));
  	showItem(ID_MAG,mActivity.isEnabledByPrefs(Sensor.MAGNETOMETER));
  	showItem(ID_GYR,mActivity.isEnabledByPrefs(Sensor.GYROSCOPE));
  	showItem(ID_OBJ,mActivity.isEnabledByPrefs(Sensor.IR_TEMPERATURE));
  	showItem(ID_AMB,mActivity.isEnabledByPrefs(Sensor.IR_TEMPERATURE));
  	showItem(ID_HUM,mActivity.isEnabledByPrefs(Sensor.HUMIDITY));
  	showItem(ID_BAR,mActivity.isEnabledByPrefs(Sensor.BAROMETER));
  }


  private void showItem(int id, boolean visible) {
  	View hdr = table.getChildAt(id*2 + ID_OFFSET);
  	View txt = table.getChildAt(id*2 + ID_OFFSET + 1);
  	int vc = visible ? View.VISIBLE : View.GONE;
  	hdr.setVisibility(vc);    
  	txt.setVisibility(vc);    
  }

  void setStatus(String txt) {
  	mStatus.setText(txt);
  	mStatus.setTextAppearance(mActivity, R.style.statusStyle_Success);
  }

  void setError(String txt) {
  	mStatus.setText(txt);
  	mStatus.setTextAppearance(mActivity, R.style.statusStyle_Failure);
  }

  void setBusy(boolean f) {
  	if (f)
  		mStatus.setTextAppearance(mActivity, R.style.statusStyle_Busy);
  	else
  		mStatus.setTextAppearance(mActivity, R.style.statusStyle);  		
  }

}
