// This activity takes two dates and times and draws a static plot of the data between those times.
// IT IS NOT COMPLETED
// There should be a php function (unwritten as of yet) that returns the data between the two dates
// and times as a JSON object. This function will take the JSON data, convert it and plot it.
package ti.android.ble.sensortag;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.androidplot.util.PlotStatistics;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;

public class HistoryPlot extends Activity {
	
	private static String TAG = "HistoryActivity";
	
	// URL to get contacts JSON
    private static String url = "http://24.162.106.233/";
 
    // JSON Node names
    private static final String R_TIME = "RTime";
    private static final String P_TIME = "PTime";
    private static final String X_ACC = "accelx";
    private static final String Y_ACC = "accely";
    private static final String Z_ACC = "accelz";

	// Plotting variables
	private XYPlot HistoryPlot;
	private SimpleXYSeries xHistorySeries;
	private SimpleXYSeries yHistorySeries;
	private SimpleXYSeries zHistorySeries;
	private SimpleXYSeries totHistorySeries;
	
	private Button submit_button;
	private Spinner StartYear;
	private Spinner StartMonth;
	private Spinner EndYear;
	private Spinner EndMonth;
	private EditText EditStartDate;
    private EditText EditStartTime;
    private EditText EditEndDate;
    private EditText EditEndTime;
	
	// Text inputs
    String start_year = null;
    String start_month = null;
	String start_day = null;
	String start_time = null;
	String end_year = null;
	String end_month = null;
	String end_day = null;
	String end_time = null;
	
	public HistoryPlot() {
	  }
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    // Intent intent = getIntent();
	    setContentView(R.layout.history);
	    
	    // Instantiate buttons
	    StartYear = (Spinner) findViewById(R.id.start_years_spinner);
	    ArrayAdapter<CharSequence> sy_adapter = ArrayAdapter.createFromResource(this, R.array.years_array, android.R.layout.simple_spinner_item);
	    sy_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    StartYear.setAdapter(sy_adapter);
	    
	    StartMonth = (Spinner) findViewById(R.id.start_months_spinner);
	    ArrayAdapter<CharSequence> sm_adapter = ArrayAdapter.createFromResource(this, R.array.months_array, android.R.layout.simple_spinner_item);
	    sm_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    StartMonth.setAdapter(sm_adapter);
	    
	    EditStartDate = (EditText) findViewById(R.id.startDay);
	    EditStartTime = (EditText) findViewById(R.id.startTime);
	    
	    EndYear = (Spinner) findViewById(R.id.end_years_spinner);
	    ArrayAdapter<CharSequence> ey_adapter = ArrayAdapter.createFromResource(this, R.array.years_array, android.R.layout.simple_spinner_item);
	    ey_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    EndYear.setAdapter(ey_adapter);
	    
	    EndMonth = (Spinner) findViewById(R.id.end_months_spinner);
	    ArrayAdapter<CharSequence> em_adapter = ArrayAdapter.createFromResource(this, R.array.months_array, android.R.layout.simple_spinner_item);
	    em_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    StartYear.setAdapter(em_adapter);
	    
	    EditEndDate = (EditText) findViewById(R.id.endDay);
	    EditEndTime = (EditText) findViewById(R.id.endTime);
	    
	    Button submitBtn = (Button) findViewById(R.id.submitTime);
	    
	    // Create button listeners
	    submitBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	
            }
        });
	    
	    StartYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
	        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
	            Object item = parent.getItemAtPosition(pos);
	            start_year = item.toString();
	        }
	        public void onNothingSelected(AdapterView<?> parent) {
	        }
	    });
	    StartMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
	        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
	            Object item = parent.getItemAtPosition(pos);
	            start_month = item.toString();
	        }
	        public void onNothingSelected(AdapterView<?> parent) {
	        }
	    });
	    EndYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
	        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
	            Object item = parent.getItemAtPosition(pos);
	            end_year = item.toString();
	        }
	        public void onNothingSelected(AdapterView<?> parent) {
	        }
	    });
	    EndMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
	        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
	            Object item = parent.getItemAtPosition(pos);
	            end_month = item.toString();
	        }
	        public void onNothingSelected(AdapterView<?> parent) {
	        }
	    });
	    
	    
	  }
	
	// Will check to see if the dates entered are valid
	private boolean canSubmit() {
		boolean isOk = false;
		
		
		return isOk;
	}
	
	private void submitTimes() {
		Log.i(TAG,"Retrieving Data");
		
	}
	
	// Will create series from the received data and plot it
	private void plotSelection() {
	    setContentView(R.layout.plot);
		
		HistoryPlot = (XYPlot) findViewById(R.id.SensorPlot);
	    
	    xHistorySeries = new SimpleXYSeries("X Axis");
	    xHistorySeries.useImplicitXVals();
	    yHistorySeries = new SimpleXYSeries("Y Axis");
	    yHistorySeries.useImplicitXVals();
	    zHistorySeries = new SimpleXYSeries("Z Axis");
	    zHistorySeries.useImplicitXVals();
	    totHistorySeries = new SimpleXYSeries("Total Acc.");
	    totHistorySeries.useImplicitXVals();
	    
	    // freeze the range boundaries:
	    HistoryPlot.setRangeBoundaries(-4, 4, BoundaryMode.FIXED);
	    HistoryPlot.setDomainBoundaries(0, 50, BoundaryMode.FIXED);
	    HistoryPlot.addSeries(xHistorySeries, new LineAndPointFormatter(Color.rgb(100,100,200),Color.BLACK, null, null));
	    HistoryPlot.addSeries(yHistorySeries, new LineAndPointFormatter(Color.rgb(100,200,100),Color.BLACK, null, null));
	    HistoryPlot.addSeries(zHistorySeries, new LineAndPointFormatter(Color.rgb(200,100,100),Color.BLACK, null, null));
	    HistoryPlot.addSeries(totHistorySeries, new LineAndPointFormatter(Color.rgb(200,100,200),Color.BLACK, null, null));
	    
	    // thin out domain/range tick labels so they dont overlap each other:
	    HistoryPlot.setTicksPerDomainLabel(5);
	    HistoryPlot.setTicksPerRangeLabel(5);
	    
	    final PlotStatistics histStats = new PlotStatistics(1000, false);
	    
	    HistoryPlot.addListener(histStats);
	    HistoryPlot.setLayerType(View.LAYER_TYPE_NONE, null);
	}
}
