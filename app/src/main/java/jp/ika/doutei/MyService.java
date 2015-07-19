package jp.ika.doutei;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by ikeda on 15/07/07.
 */
public class MyService extends Service implements GoogleApiClient
		.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener{

	public static final String ACTION = "MyService Action";
	static final String TAG="LocalService";
	private GoogleApiClient googleApiClient;
	private LocationRequest locationRequest;
	private MainData data;
	private NotificationManager nm;

	@Override
	public IBinder onBind(Intent intent) {
		Log.i(TAG, "onBind");
		Toast.makeText(this, "onBind", Toast.LENGTH_SHORT).show();
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "onCreate");
		Toast.makeText(this, "MyService#onCreate", Toast.LENGTH_SHORT).show();

		googleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API)
				.build();

		locationRequest = new LocationRequest()
				.setInterval(10000)
				.setFastestInterval(5000)
				.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		showNotification();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		Log.i(TAG, "onStartCommand Received start id " + startId + ": " + intent);
		Toast.makeText(this, "MyService#onStartCommand", Toast.LENGTH_SHORT).show();

		if(intent != null)
			data = (MainData)intent.getSerializableExtra("data");
		else if(data == null)
			data = MainData.newInstance(this);

		if (!googleApiClient.isConnected() || !googleApiClient.isConnecting())
			googleApiClient.connect();

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "onDestroy");
		Toast.makeText(this, "MyService#onDestroy", Toast.LENGTH_SHORT).show();
		data.save(this);
		stopLocationUpdates();
		googleApiClient.disconnect();
		nm.cancel(1);
	}

	@Override
	public void onConnected(Bundle bundle) {
		Log.i(TAG, "onConnected");
		Toast.makeText(this, "onConnected", Toast.LENGTH_SHORT).show();
		startLocationUpdates();
	}

	@Override
	public void onConnectionSuspended(int cause) {
		Toast.makeText(this, "onConnectionSuspended", Toast.LENGTH_SHORT).show();
		Log.i(TAG, "onConnectionSuspended");
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Toast.makeText(this, "onConnectionFailed", Toast.LENGTH_SHORT).show();
		Log.i(TAG, "onConnectionFailed");
	}
	@Override
	public void onLocationChanged(Location location) {
		Toast.makeText(this, "onLocationChanged", Toast.LENGTH_SHORT).show();
		Log.i(TAG, "onLocationChanged");
		PositionUpdate(location);
	}

	private void startLocationUpdates() {
		LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
	}

	private void stopLocationUpdates() {
		LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
	}

	private void PositionUpdate(Location location){
		data.addPosition(location);
		data.save(this);
		Intent intent = new Intent(ACTION);
		intent.putExtra("location", new double[]{location.getLatitude(), location.getLongitude()});
		sendBroadcast(intent);
	}

	private void showNotification() {
		Log.d(TAG, "showNotification");
		PendingIntent intent = PendingIntent.getActivity(this, 0, new Intent(this,
				MapsActivity.class), 0);

		Notification notification= new Notification.Builder(this)
				.setSmallIcon(R.mipmap.ic_launcher)
				.setContentTitle(getString(R.string.app_name))
				.setContentText("Now Tracing...")
				.setContentIntent(intent)
				.build();

		notification.flags = Notification.FLAG_ONGOING_EVENT;
		nm.notify(1, notification);
	}
}
