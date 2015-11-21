package lac.contextnet.sddl_pingasynctasktest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
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
	private static boolean bluetoothDefaultIsEnable = false;
	private String deviceAddress;
	private BluetoothAdapter btAdapter;
	
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

        btAdapter = BluetoothAdapter.getDefaultAdapter();
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
		        
		        // TODO save deviceAddress
		        
		        BluetoothDevice device = btAdapter.getRemoteDevice(deviceAddress);
		        
		        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

		        BluetoothSocket socket;
				try {
					socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
					socket.connect();
					Toast.makeText(getBaseContext(), "We have a connection", Toast.LENGTH_LONG).show();
				} catch (IOException e) {
					Toast.makeText(getBaseContext(), "We have a problem :(", Toast.LENGTH_LONG).show();
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

		        
	        }
        });

        alertDialog.setTitle("Choose Bluetooth device");
        alertDialog.show();
		/* BLUETOOH END /
		
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
				PingObject ping = new PingObject();
				CommunicationTask commTask = new CommunicationTask(getBaseContext(), ipPortObj, UUID.fromString(uniqueID), PING_TAG, ping);
				commTask.execute();
			}
		});
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