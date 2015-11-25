package br.pucrio.acanhota.ubicomp;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.ObdResetCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;
import com.github.pires.obd.exceptions.NoDataException;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import lac.cnclib.net.NodeConnection;
import lac.cnclib.net.mrudp.MrUdpNodeConnection;
import lac.cnclib.sddl.message.ApplicationMessage;
import lac.contextnet.sddl_pingasynctasktest.MainActivity;
import lac.contextnet.sddl_pingasynctasktest.MessageHandler;
import lac.contextnet.sddl_pingasynctasktest.PingConnectionListener;

/**
 * @author acanhota@puc-rio.br
 *
 */
public class MonitoringService extends Service {
	private static final String TAG = "MonitoringService";

	private TimerTask mainProcess;
	private Timer timer;
	private BluetoothSocket socket;
	
	private NodeConnection myConnection;
	private PingConnectionListener myNodeConnectionListener;
	private Handler messageHandler;
	
	private ApplicationMessage packagedMessage;
	private Serializable rawInfo;
	private List<String> tags;
	private UUID clientUUID;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {				
		Log.d(TAG, "onCreate");
		
		//@todo Mudar
		String ipAddress = "10.1.1.31";
		int port = 5500;
		
		try {
			if(myConnection == null)
				myConnection = new MrUdpNodeConnection(UUID.fromString(MainActivity.GetUUID(this)));
			
			this.messageHandler = new MessageHandler(this);
			if(myNodeConnectionListener == null)
				myNodeConnectionListener = new PingConnectionListener(messageHandler);
			
			myConnection.addNodeConnectionListener(myNodeConnectionListener);
			SocketAddress sc = new InetSocketAddress(ipAddress, port);
			myConnection.connect(sc);
			
			Log.i(TAG, "Connection stablished");
			
		} catch (IOException e) {
			Log.e(TAG, "Error creating connection");
			
			e.printStackTrace();
		}
		
		timer = new Timer();
	}
	
	@Override
	public void onStart(Intent intent, int startid) {		
		Log.d(TAG, "onStart");
		
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		
		String deviceAddress = intent.getStringExtra("deviceAddress");
		BluetoothDevice device = btAdapter.getRemoteDevice(deviceAddress);
		
		UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
		
		try {
			socket = device.createRfcommSocketToServiceRecord(uuid);
			socket.connect();
			
			new ObdResetCommand().run(socket.getInputStream(), socket.getOutputStream());				
			new EchoOffCommand().run(socket.getInputStream(), socket.getOutputStream());				
			new LineFeedOffCommand().run(socket.getInputStream(), socket.getOutputStream());				
			new TimeoutCommand(62).run(socket.getInputStream(), socket.getOutputStream());				
			new SelectProtocolCommand(ObdProtocols.AUTO).run(socket.getInputStream(), socket.getOutputStream());				
		} catch (Exception e) {
			Log.e(TAG, "Pt3");
			e.printStackTrace();
		}
		
		mainProcess = new TimerTask() {
			@Override
		    public void run(){
				// OBD-2
				/*
				RPMCommand foo = new RPMCommand();				
				try {
					foo.run(socket.getInputStream(), socket.getOutputStream());
				} catch (NoDataException e) {
					Log.e(TAG, "No data from OBD-2");
					e.printStackTrace();
				} catch (IOException e) {
					Log.e(TAG, "Not connected to OBD-2");
					e.printStackTrace();
				} catch (InterruptedException e) {
					Log.e(TAG, "Pt2");
					e.printStackTrace();
				}
				Log.i(TAG, "RPM = " + foo.getFormattedResult());
				*/
				// SDDL
				packagedMessage = new ApplicationMessage();
				packagedMessage.setContentObject(rawInfo);
				tags = new LinkedList<String>();
				packagedMessage.setTagList(tags);
				tags.add("FOO");
				packagedMessage.setSenderID(clientUUID);
		    	
		    	try {
		    		myConnection.sendMessage(packagedMessage);
		    		
		        }
		        catch (Exception e) {
		        	Log.e(TAG, "Messaged could not be sent");
		        	e.printStackTrace();
		        }
				
				
		    }
		};
		
		timer.schedule(mainProcess, 0, 500);		
	}
	
	@Override
	public void onDestroy() {		
		Log.d(TAG, "onDestroy");
		
		timer.cancel();
		timer.purge();
	}
	
	

}
