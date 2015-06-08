package co.mylonas.cordova.beacons;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BeaconBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, BeaconService.class));
    }
}