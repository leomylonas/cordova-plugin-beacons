package co.mylonas.cordova.beacons;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;
import android.util.Log;
import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.Environment;

import org.altbeacon.beacon.AltBeacon;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.powersave.*;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.Identifier;
import android.content.SharedPreferences;

import org.apache.cordova.PluginResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.util.*;

public class BeaconService extends Service implements BeaconConsumer {

	protected static final String TAG = "BeaconService";
	private BeaconManager beaconManager;
	private BackgroundPowerSaver backgroundPowerSaver;

	private Intent currentIntent = null;
	private String beaconUuid = null;
	private String packageLaunchActivity = null;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onCreate() {
		
		//Toast.makeText(this, "Service CREATED", Toast.LENGTH_LONG).show();
		beaconManager = BeaconManager.getInstanceForApplication(this);
		BeaconParser estimote = new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24");
		try {
			beaconManager.getBeaconParsers().add(estimote);
		} catch (Exception ex) {
			Log.e(TAG, "Could not add beacon parser");
			ex.printStackTrace();
		}
		beaconManager.bind(this);
		
		//backgroundPowerSaver = new BackgroundPowerSaver(this);
		//beaconManager.setBackgroundScanPeriod(5100l);
		
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		currentIntent = intent;
		File file = new File(getFilesDir().toString() + "/beaconSettings.txt");
		if (intent != null) {
			beaconUuid = intent.getExtras().get("beaconUuid").toString();
			packageLaunchActivity = intent.getExtras().get("packageLaunchActivity").toString();
			try {
				FileOutputStream stream = new FileOutputStream(file);
				stream.write((beaconUuid + "\n" + packageLaunchActivity).getBytes());
				stream.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			try {
				int length = (int) file.length();
				byte[] bytes = new byte[length];
				FileInputStream stream = new FileInputStream(file);
				stream.read(bytes);
				stream.close();
				String[] beaconSettings = new String(bytes).split("\n");
				beaconUuid = beaconSettings[0];
				packageLaunchActivity = beaconSettings[1];
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		//Toast.makeText(this, "Service Started: " + beaconUuid, Toast.LENGTH_LONG).show();
		return START_STICKY;
	}
	
	@Override
	public void onBeaconServiceConnect() {
		beaconManager.setMonitorNotifier(new MonitorNotifier() {

		@Override
			public void didEnterRegion(Region region) {
				//Log.i(TAG, "I just saw an beacon for the first time!");        
			}

			@Override
			public void didExitRegion(Region region) {
				//Log.i(TAG, "I no longer see an beacon");
			}

			@Override
			public void didDetermineStateForRegion(int state, Region region) {
				Log.i(TAG, "didDetermineStateForRegion: "+state);
				if (Beacons.didDetermineStateForRegionCallbackContext != null) {
					Beacons.didDetermineStateForRegionCallbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, state));
				}
				if (currentIntent == null) {
					//Log.i(TAG, "*** INTENT: NULL");
					startMainActivity();
				}/* else {
					Log.i(TAG, "*** INTENT: " + currentIntent.toString());
				}*/
			}
		});

		try {
			Log.e(TAG, "MONITORING: " + beaconUuid);
			beaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", Identifier.parse(beaconUuid), null, null));
		} catch (RemoteException e) {
		
		}
		
		beaconManager.setRangeNotifier(new RangeNotifier() {
			@Override 
			public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
				String json = "[";
				for (int i=0; i<beacons.size(); i++) {
					Beacon b = beacons.iterator().next();
					json += "{\"major\": "+b.getId2().toString()+", \"minor\": "+b.getId3().toString()+", \"rssi\": "+String.valueOf(b.getRssi())+"}";
				}
				json += "]";
				//Log.i(TAG, json);
				if (Beacons.didRangeBeaconsInRegionCallbackContext != null) {
					Beacons.didRangeBeaconsInRegionCallbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, json));
				}
			}
		});

		try {
			beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", Identifier.parse(beaconUuid), null, null));
		} catch (RemoteException e) {
		
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Toast.makeText(this, "Service Stopped", Toast.LENGTH_LONG).show();
	}
	
	public void startMainActivity() {
		try {
			Intent dialogIntent = new Intent(this, Class.forName(packageLaunchActivity));
			dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			dialogIntent.putExtra("startedFromBackground", true);
			startActivity(dialogIntent);
		} catch (Exception ex) {
			Log.e(TAG, "Could not start activity");
			ex.printStackTrace();
		}
	}
}