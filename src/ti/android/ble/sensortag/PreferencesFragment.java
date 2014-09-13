///////////////////////////////
//    OLD TI CODE. UNUSED    //
///////////////////////////////

package ti.android.ble.sensortag;

import java.util.Locale;

import ti.android.ble.sensortag.R;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class PreferencesFragment extends PreferenceFragment  {
  
  @SuppressWarnings("unused")
  private static final String TAG = "PreferencesFragment";
  private PreferencesListener preferencesListener;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
    preferencesListener = new PreferencesListener(getActivity(), prefs, this);
    prefs.registerOnSharedPreferenceChangeListener(preferencesListener);
  }

  
  public boolean isEnabledByPrefs(final Sensor sensor) {
    String preferenceKeyString = "pref_" + sensor.name().toLowerCase(Locale.ENGLISH) + "_on";

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

    if (!prefs.contains(preferenceKeyString)) {
      throw new RuntimeException("Programmer error, could not find preference with key " + preferenceKeyString);
    }

    return prefs.getBoolean(preferenceKeyString, true);
  }
}
