package lac.contextnet.sddl_pingasynctasktest;

import java.io.Serializable;
import java.net.SocketAddress;
import java.util.List;

import lac.cnclib.net.NodeConnection;
import lac.cnclib.net.NodeConnectionListener;
import lac.cnclib.sddl.serialization.Serialization;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.infopae.model.PingObject;

/**
 * Listener para mensagens do Controlador. 
 * Separa as mensagens e envia para tratamento no handler registrado.
 * 
 * @author andremd
 *
 */
public class PingConnectionListener implements NodeConnectionListener {

	private Handler handler;
	
	public PingConnectionListener(Handler handler) 
	{
		this.handler = handler;
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	/* Called upon a new status information */
	private void handleNewStatus(String status) {
		Bundle bund = new Bundle();
		bund.putString("status", status);
		Message msg = new Message();
		msg.setData(bund);
		handler.sendMessage(msg);
	}

	/* Called upon a non-status package received */
	private void handleNewPackage(Serializable incoming) {
		Bundle bund = new Bundle();
		bund.putString("status", "package");
		bund.putSerializable("package", incoming);
		Message msg = new Message();
		msg.setData(bund);
		handler.sendMessage(msg);
	}
	
	public void connected(NodeConnection remoteCon) {
		handleNewStatus("connected");
	}

	public void reconnected(NodeConnection remoteCon, SocketAddress endPoint,
			boolean wasHandover, boolean wasMandatory) {
		handleNewStatus("reconnected - newend=" + endPoint + " handover="
				+ wasHandover + " mandatory=" + wasMandatory);
	}

	public void disconnected(NodeConnection remoteCon) {
		handleNewStatus("disconnected");
	}

	public void newMessageReceived(NodeConnection remoteCon, lac.cnclib.sddl.message.Message msg) 
	{
		String className = msg.getContentObject().getClass().getCanonicalName();
		Serializable s = Serialization.fromJavaByteStream(msg.getContent());
		
		if (className != null) 
		{
			if (className.equals(PingObject.class.getCanonicalName()))
				handleNewPackage(s);
			/* Here you can add different treatments to different types of 
			 * received data, or repass this logic to the handler. */
		}
		else 
		{
			handleNewStatus("new object received: " + msg.getContentObject().toString());
		}
	}

	public void unsentMessages(NodeConnection remoteCon, List<lac.cnclib.sddl.message.Message> unsentMessages) 
	{
		handleNewStatus("objects not sent: " + unsentMessages.size());
	}

	public void internalException(NodeConnection remoteCon, Exception e) 
	{
		handleNewStatus("connection internal exception " + e);
	}

}