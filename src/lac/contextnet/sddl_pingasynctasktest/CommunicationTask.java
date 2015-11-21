package lac.contextnet.sddl_pingasynctasktest;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import lac.cnclib.net.NodeConnection;
import lac.cnclib.net.mrudp.MrUdpNodeConnection;
import lac.cnclib.sddl.message.ApplicationMessage;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Toast;

/**
 * CommunicationTask: This is an asynchronous task created in order to 
 * 					  deal with the socket connection and package sending.
 * 
 * @author andremd
 * 
 */
public class CommunicationTask extends AsyncTask<Void, byte[], Boolean> {

	private NodeConnection myConnection;
	private PingConnectionListener myNodeConnectionListener;
	private Handler messageHandler;
	private Context context;
	
	private ApplicationMessage packagedMessage;
	private Serializable rawInfo;
	private List<String> tags;
	private UUID clientUUID;
	private String ipAddress;
	private int port;
	
	public CommunicationTask(Context context, IPPort ipPort, UUID clientUUID, String tag, Serializable rawInfo)
	{
		this.messageHandler = new MessageHandler(context);
		this.context = context;
		
		this.packagedMessage = new ApplicationMessage();
		this.rawInfo = rawInfo;
		this.tags = new LinkedList<String>();
		this.tags.add(tag);
		this.clientUUID = clientUUID;
		this.ipAddress = ipPort.getIP();
		this.port = Integer.valueOf(ipPort.getPort());
	}

	@Override
	protected Boolean doInBackground(Void... arg0) 
	{
		boolean result = true;
	
	    try 
	    {
	    	if(myConnection == null)
	    		myConnection = new MrUdpNodeConnection(clientUUID);
	    	
	    	if(myNodeConnectionListener == null)
	    		myNodeConnectionListener = new PingConnectionListener(messageHandler);
	    	
	    	myConnection.addNodeConnectionListener(myNodeConnectionListener);
	    	SocketAddress sc = new InetSocketAddress(ipAddress, port);
    		myConnection.connect(sc);
	    }
    	catch (IOException e) 
    	{
    		result = false;
    		e.printStackTrace();
    	}
	
	    return result;
	}
	
	@Override
	protected void onPostExecute(Boolean result) 
	{
		if(result)
		{			
			packagedMessage.setContentObject(rawInfo);
			packagedMessage.setTagList(tags);
			packagedMessage.setSenderID(clientUUID);
	    	
	    	try {
	    		myConnection.sendMessage(packagedMessage);
	    		result = true;
	        }
	        catch (Exception e) {
	        	result = false;
	        	e.printStackTrace();
	        }
		}
		else
			Toast.makeText(context, "Impossible to Send Info!", Toast.LENGTH_LONG).show();
	}
}