///////////////////////////////
//    OLD TI CODE. UNUSED    //
///////////////////////////////

package ti.android.ble.sensortag;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.widget.ImageView;

public class PreferencesActivity extends PreferenceActivity {
  @SuppressWarnings("unused")
  private static final String TAG = "PreferencesActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Icon padding
    ImageView view = (ImageView) findViewById(android.R.id.home);
    view.setPadding(10, 0, 20, 10);
    
  }
  
}
