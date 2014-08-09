package com.cwc.mobilecloud_star;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONValue;

import android.util.Log;

public class NetLog {
	
	private static final String TAG = "NetLog";
	
	private static UDPConnection LogServer = new UDPConnection();
	
	private boolean init = false;
	
	
	public synchronized void close(){
		LogServer.close();
	}
	public synchronized boolean setServer(String address, int port)
	{
		LogServer.setDstIP(address);
		LogServer.setDstPort(port);
		if (LogServer.bind() == false){
			Log.e(TAG, "Error in Connection with Logging server" );
			init = false;
			return false;
		}
		
		init = true;
		return true;
	}
	
	public synchronized boolean Log(String message, String facility, String severity, String id){
		if (init == false)
		{
			return false;
		}
		else
		{
			Map<String, String> logMap=new LinkedHashMap();
			
			logMap.put("id", id);
		    logMap.put("facility",facility);
		    logMap.put("severity", severity);
		    logMap.put("message", message);
		    
		    String logStr = JSONValue.toJSONString(logMap);
		    Log.d(TAG, logStr );
		    if (LogServer.send(logStr) == true)
		    {		    	
		    	return true;
		    }
		    else
		    {	
		    	Log.e(TAG, "Error in Sending Log Message" );
		    	return false;
		    }
		    
		}
	}
	
}
