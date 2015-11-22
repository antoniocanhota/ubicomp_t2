package br.pucrio.acanhota.ubicomp;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * @author acanhota@puc-rio.br
 *
 */
public class MonitoringService extends Service {
	private static final String TAG = "MonitoringService";

	private TimerTask mainProcess;
	private Timer timer;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {				
		Log.d(TAG, "onCreate");
		
		mainProcess = new TimerTask() {
			@Override
		    public void run(){
		       Log.i(TAG, "Hello!");
		    }
		};
		
		timer = new Timer();
	}
	
	@Override
	public void onStart(Intent intent, int startid) {		
		Log.d(TAG, "onStart");
		
		timer.schedule(mainProcess, 0, 500);		
	}
	
	@Override
	public void onDestroy() {		
		Log.d(TAG, "onDestroy");
		
		timer.cancel();
		timer.purge();
	}
	
	

}
