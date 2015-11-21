package lac.contextnet.sddl_pingasynctasktest;

import java.io.Serializable;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.infopae.model.PingObject;

public class MessageHandler extends Handler {
	
	private Context context;
	
	public MessageHandler(Context context)
	{
		this.context = context;
	}
	
	@Override
	public void handleMessage(Message msg) 
	{
		super.handleMessage(msg);
		
		if (msg.getData().getString("status") != null) 
		{
			String status = msg.getData().getString("status");
			
			if (status.equals("connected")) 
				Log.d("SDDL", (String) context.getResources().getText(R.string.msg_d_connected));
			else if (status.equals("disconnected")) 
				Log.d("SDDL", (String) context.getResources().getText(R.string.msg_d_disconnected));
			else if (status.equals("package")) 
			{
				Serializable s = msg.getData().getSerializable("package");
				
				if(s instanceof PingObject)
				{
					Toast.makeText(context, ((PingObject) s).toString(), Toast.LENGTH_LONG).show();
				}
				/* Here you can add different treatments to different types of 
				 * received data if you decide not to do that on the 
				 * NodeConnectionListener */
			}
			else
				Log.d("SDDL", status);
		}
	}
}
