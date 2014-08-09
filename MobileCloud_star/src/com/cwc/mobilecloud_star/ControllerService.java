package com.cwc.mobilecloud_star;

import java.util.Dictionary;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.simple.JSONValue;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.cwc.mobilecloud_star.ConfigData.AppConstant;
import com.cwc.mobilecloud_star.utilities.Constants;
import com.cwc.mobilecloud_star.utilities.Utilities;

public class ControllerService extends Service {

	private static final String TAG = "ControllerService";

	/************ <Controller Local Objects> ***********/
	private final IBinder mBinder = new MyBinder();
	private Timer CNINFOTimer;
	private Handler uiHandler = null;

	private int startCount = 0;
	private Thread rxThread;

	private Thread CLThread;

	private Context ctx;
	private Context appCtx;

	private Handler CTRXHandler;



	/***************************************************/

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			Bundle bundle = msg.getData();
			String mtype = bundle.getString("to");

			if (mtype == null)
				return;

			if (mtype.equals("UI")) { /* Forward Message to UI */
				if (uiHandler != null) {
					Message uiMsg = uiHandler.obtainMessage();
					uiMsg.setData(bundle);
					uiHandler.sendMessage(uiMsg);
				}
			}

			if (mtype.equals("CS")){

				Log.d(TAG, "data initializing trigger");

				sendingAsyncTask asynctask = new sendingAsyncTask(bundle);

				asynctask.execute();

			}
		}
	};

	public Handler getUIhandler(){

		return handler;
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		startCount += 1;
		Log.d(TAG, "Service started = " + String.valueOf(startCount) + " times");

		if (startCount == 1) {
			/* Do all initializations */
			Log.d(TAG, "Doing Service Initializations");
			ControllerInit();
			Log.d(TAG, "Service Initialization Succesful");

		} else {
			Log.d(TAG, "No Service Initialization this time");
		}

		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "Activity Binding to Service");
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(TAG, "Service Unbind called");
		uiHandler = null;
		return true;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "Destroying Service");
		ControllerClose();
		Log.d(TAG, "Controller Service Closed Succesfully");

	}

	public class MyBinder extends Binder {
		ControllerService getService() {
			return ControllerService.this;
		}
	}

	/***** Service Initialization/Destroy Methods ******/
	/**************************************************/
	private void ControllerInit() {

		// Initialize contexts
		ctx = this;
		appCtx = ctx.getApplicationContext();
		// Register battery Status receiver
		registerReceiver(Utilities.mBatInfoReceiver, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));
		// Get Wifi Service Handler
		ConfigData.Wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);


		// Initialize Broadcast Socket and Logger
		//		ConfigData.CtrlSock = new UDPConnection("", "",
		//				ConfigData.getCtrlPort(), ConfigData.getCtrlPort(), true);

		// for s4 ONLY
		//		Shell.setDefaultGw();
		// for s4 only


		ConfigData.CtrlSock = new MulticastConnection(ConfigData.getMcIP(), ConfigData.getMcPort());
		ConfigData.NLog = new NetLog();
		// Start Receiver Thread
		rxThread = new ControlTransceiver(ctx, handler);
		rxThread.start();

		// Start Timer
		CNINFOTimer = new Timer();
		CNINFOTimer.schedule(ts, 10000, (AppConstant.CN_INFO_TIMEOUT * 1000));

		// Start CL Selection Thread
		Log.d(TAG, "Starting CL selection thread");
		CLThread = new CLSelecThread(ctx, handler);
		CLThread.start();
		
//		Utilities.generateTestingResults("Test_Results1.txt", "abdul moiz");

	}

	private void ControllerClose() {

		unregisterReceiver(Utilities.mBatInfoReceiver);
		CNINFOTimer.cancel();
		rxThread.interrupt();
		CLThread.interrupt();
		ConfigData.CtrlSock.close();
		ConfigData.NLog.close();
	}

	/***** Interface Methods ******/
	/******************************/

	public String getData() {
		return "Hello World : " + String.valueOf(startCount);
	}

	public void setHandler(Handler h) {
		uiHandler = h;
	}

	/***** Internal Methods *******/
	/******************************/

	private void CNInfoTimerCallback() {
		String localIP = Utilities.getLocalIP(true);

		// Send CN_DISCOVERY_REQ to peers
		Map<String, Object> data = new LinkedHashMap();
		data.put("msgType", "CN_DISCOVERY_REQ");
		data.put("cnid", Utilities.getDeviceID());
		String jsondata = JSONValue.toJSONString(data);


		try {
			ConfigData.CtrlSock.send(jsondata, ConfigData.getMcIP(), ConfigData.getMcPort());
			Log.d(TAG, "CN_DISCOVERY_REQ Message sent");
		} catch (Exception e) {
			Log.v(TAG, "Error in sending CN_DISCOVERY_REQ Message");
		}

		ConfigData.updatePeersAge();

		// "Send CN_INFO message to BS" ---> Moved to Dump

		// Create BAT_INFO Message for UI
		Dictionary<?, ?> batProp = Utilities.getBatteryStatus();
		Bundle bundle = new Bundle();
		if (uiHandler != null) {
			Message uiMsg = uiHandler.obtainMessage();
			bundle.putString("mtype", "BAT_INFO");
			bundle.putString("BatLevel", Utilities.getBatVal());

			uiMsg.setData(bundle);
			uiHandler.sendMessage(uiMsg);
		}

		//		//for printing contents on all the peers
		//		for (String id : ConfigData.getPeerIds()){
		//			Log.d(TAG, "Neighbor ID: " + ConfigData.getPeer(id).ID);
		//			Log.d(TAG, "Content on this ID " + ConfigData.getPeer(id).contents);
		//		}
	}


	public TimerTask ts = new TimerTask() {

		@Override
		public void run() {
			CNInfoTimerCallback();
		}
	};


	private class sendingAsyncTask extends AsyncTask<Void, Void, Void>{

		Bundle asyncBundle;

		public sendingAsyncTask(Bundle asyncBundle){

			this.asyncBundle = asyncBundle;
		}

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			
			if(asyncBundle.get("requestType").equals(Constants.request_file)){
				
				//send message to cl or node that has the required file
				
				Map<String, Object> init_data_msg=new LinkedHashMap();

				Log.d(TAG, "data initializing trigger");

				String file_req = (String) asyncBundle.get("file");

				String file_on_IP = (String) asyncBundle.get("sourceIP");

				init_data_msg.put("msgType", "CN_DATA_INIT_TO_NODE_REQ");
				init_data_msg.put("cnid", Utilities.getDeviceID());
				init_data_msg.put("data_port", ConfigData.getDataPort());
				init_data_msg.put("file", file_req );

				String jsondata = JSONValue.toJSONString(init_data_msg);

				Log.d(TAG, "Sending file request to node: " + file_on_IP + " on Port: " + ConfigData.getMcPort());

				ConfigData.CtrlSock.send(jsondata, file_on_IP , ConfigData.getMcPort());
			}
			
			
			else if(asyncBundle.get("requestType").equals(Constants.request_url)){
				
				//send message to CL for URL request
				Log.d(TAG, "requesting CL to get file from URL ");
				
				String file_req = (String) asyncBundle.get("file");

				String file_on_IP = (String) asyncBundle.get("sourceIP");
				
				//send message to cl or node that has the required file
				Map<String, Object> url_req_msg=new LinkedHashMap();

				//file_req is the name of the required file For the time being and for testing, it is hard coded.

				//file_on_IP is the IP of the node that has the required file/content. For the time being and for testing, it is hard coded.

				url_req_msg.put("msgType", "URL_FILE_REQ");
				url_req_msg.put("cnid", Utilities.getDeviceID());
				url_req_msg.put("data_port", ConfigData.getDataPort());
				url_req_msg.put("url", file_req );

				String jsondata = JSONValue.toJSONString(url_req_msg);

				Log.d(TAG, "Sending file request to node: " + file_on_IP + " on Port: " + ConfigData.getMcPort());

				ConfigData.CtrlSock.send(jsondata, file_on_IP , ConfigData.getMcPort());
				
			}
			
			return null;
		}
	}

}
