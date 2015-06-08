package co.mylonas.cordova.beacons;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import android.content.Context;
import android.content.Intent;
import android.app.ActivityManager;
import android.content.ComponentName;
import org.json.JSONException;
import org.json.JSONArray;
import android.util.Log;
import android.app.Activity;
import android.content.Intent;
import java.util.*;

public class Beacons extends CordovaPlugin {
	protected static final String TAG = "BluetoothBeacons";
	public static CallbackContext didDetermineStateForRegionCallbackContext = null;
	public static CallbackContext didRangeBeaconsInRegionCallbackContext = null;
	public static Context context = null;
	
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		Beacons.context = this.cordova.getActivity().getApplicationContext();
		
		Log.i(TAG, "Executing action: " + action);
		
		if (action.equals("init")) {
			Intent beaconService = new Intent(this.cordova.getActivity().getBaseContext(), BeaconService.class);
			beaconService.putExtra("beaconUuid", args.get(0).toString());
			beaconService.putExtra("packageLaunchActivity", args.get(1).toString());
			
			this.cordova.getActivity().startService(beaconService);
			
			callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, ""));
			return true;
		} else if (action.equals("didDetermineStateForRegion")) {
			Beacons.didDetermineStateForRegionCallbackContext = callbackContext;
			return true;
		} else if (action.equals("didRangeBeaconsInRegion")) {
			Beacons.didRangeBeaconsInRegionCallbackContext = callbackContext;
			return true;
		} else if (action.equals("stopRangingBeaconsInRegion")) {
			Beacons.didRangeBeaconsInRegionCallbackContext = null;
			callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, ""));
			return true;
		}
		
		callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Not a valid action"));
		return true;
	}
	
	
}