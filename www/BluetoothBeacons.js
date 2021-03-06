var bluetoothBeacons = {
	init: function(beaconUuid, packageLaunchActivity, successCallback, errorCallback) {
		cordova.exec(successCallback, errorCallback, "Beacons", "init", [beaconUuid, packageLaunchActivity]);
	},
	
	didDetermineStateForRegion: function(successCallback, errorCallback) {
		cordova.exec(function(state) {
			bluetoothBeacons.didDetermineStateForRegion(successCallback, errorCallback);
			successCallback(state);
		}, errorCallback, "Beacons", "didDetermineStateForRegion", []);
	},
	
	didRangeBeaconsInRegion: function(successCallback, errorCallback) {
		cordova.exec(function(beacons) {
			bluetoothBeacons.didRangeBeaconsInRegion(successCallback, errorCallback);
			successCallback(JSON.parse(beacons));
		}, errorCallback, "Beacons", "didRangeBeaconsInRegion", []);
	},
	
	stopRangingBeaconsInRegion: function(successCallback, errorCallback) {
		cordova.exec(successCallback, errorCallback, "Beacons", "stopRangingBeaconsInRegion", []);
	}
};

module.exports = bluetoothBeacons;
