// Update receiver that manages the network connection. THIS IS NOT USED. //
////////////////////////////////////////////////////////////////////////////

package ti.android.ble.sensortag;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkUpdateReceiver extends BroadcastReceiver {
	private static String NET = "NetworkConnectivity";

    @Override
    public void onReceive(Context context, Intent intent) {
  
          ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE );
          NetworkInfo activeNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
          NetworkInfo activeWifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
          boolean isConnected = (activeNetInfo != null && activeNetInfo.isConnectedOrConnecting()) || 
        		  (activeWifiInfo != null && activeWifiInfo.isConnectedOrConnecting());
          if (isConnected)
              Log.i(NET, "Connected" + isConnected);   
          else {
        	  Log.i(NET, "Not Connected" + isConnected);
          }
        }
    }
