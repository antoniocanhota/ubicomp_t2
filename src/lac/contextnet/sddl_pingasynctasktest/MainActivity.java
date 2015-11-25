package lac.contextnet.sddl_pingasynctasktest;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import br.pucrio.acanhota.ubicomp.MonitoringService;

import com.infopae.model.PingObject;

/**
 * MainActivity: This is our application's MainActivity. It consists in 
 * 				 a UUID randomly generated and shown in txt_uuid, a text 
 * 				 field for the IP:PORT in et_ip and a "Ping!" button 
 * 				 (btn_ping) that is used to make a connection to the 
 * 				 SDDL server and send a Ping object message.
 * 
 * @author andremd
 * 
 */
public class MainActivity extends Activity {

	/* Shared Preferences */
	private static String uniqueID = null;
	private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";
	private static final String PING_TAG = "PING";
	
	/* Static Elements */
	private TextView txt_uuid;
	private EditText et_ip;
	private Button btn_ping;
	
	/* Others */	
	private String deviceAddress;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		/* GUI Elements */
		txt_uuid = (TextView) findViewById(R.id.txt_uuid);
		et_ip = (EditText) findViewById(R.id.et_ip);
		btn_ping = (Button) findViewById(R.id.btn_ping);
		txt_uuid.setText(" " + GetUUID(getBaseContext()));
		
		/* Bluetooh */		
		ArrayList<String> deviceStrs = new ArrayList<String>();
	    final ArrayList<String> devices = new ArrayList<String>();

	    BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
	        for (BluetoothDevice device : pairedDevices) {
		        deviceStrs.add(device.getName() + "\n" + device.getAddress());
		        devices.add(device.getAddress());
	        }
        }

        // show list
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.select_dialog_singlechoice, deviceStrs.toArray(new String[deviceStrs.size()]));

        alertDialog.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
		        dialog.dismiss();
		        int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
		        deviceAddress = (String) devices.get(position);		        
		        startMonitoringService(deviceAddress);
	        }
        });

        alertDialog.setTitle("Escolha o dispositivo OBD-2");
        alertDialog.show();
		/* BLUETOOH END */
		
		/* Ping Button Listener*/
		btn_ping.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String ipPort = et_ip.getText().toString();
				
				if(!IPPort.IPRegexChecker(ipPort))
				{
					Toast.makeText(getBaseContext(), getResources().getText(R.string.msg_e_invalid_ip), Toast.LENGTH_LONG).show();
					return;
				}

				/* Connection and Data send */
				IPPort ipPortObj = new IPPort(ipPort);
				PingObject ping = new PingObject("foo");
				CommunicationTask commTask = new CommunicationTask(getBaseContext(), ipPortObj, UUID.fromString(uniqueID), PING_TAG, ping);
				commTask.execute();
			}
		});
	}
	
	protected void startMonitoringService(String deviceAddress) {		
		Intent mIntent = new Intent(this, MonitoringService.class);
		mIntent.putExtra("deviceAddress", deviceAddress);    
		startService(mIntent);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopService(new Intent(this, MonitoringService.class));
	}

	//See http://androidsnippets.com/generate-random-uuid-and-store-it
	public synchronized static String GetUUID(Context context) {
	    if (uniqueID == null) {
	        SharedPreferences sharedPrefs = context.getSharedPreferences(
	                PREF_UNIQUE_ID, Context.MODE_PRIVATE);
	        uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
	        if (uniqueID == null) {
	            uniqueID = UUID.randomUUID().toString();
	            Editor editor = sharedPrefs.edit();
	            editor.putString(PREF_UNIQUE_ID, uniqueID);
	            editor.commit();
	        }
	    }
	    return uniqueID;
	}
}