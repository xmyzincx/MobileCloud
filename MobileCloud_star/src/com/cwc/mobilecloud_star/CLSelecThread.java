package com.cwc.mobilecloud_star;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.simple.JSONValue;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.cwc.mobilecloud_star.ConfigData.AppConstant;
import com.cwc.mobilecloud_star.utilities.Constants;
import com.cwc.mobilecloud_star.utilities.Utilities;

public class CLSelecThread extends Thread {

	private static final String TAG = "CLSelecThread";

	private Context context;
	private Handler handler;
	private Timer CLSelecTimer;
	private static Node MaxCN = new Node();
	public static String CurrentCLID;
	private static MulticastConnection CtrlChannel;
	private NetLog NLog;


	CLSelecThread(Context temp_context, Handler temp_handler){

		context = temp_context;

		handler = temp_handler;

	}

	private void setupChannel()
	{
		// Get Connections //

		CtrlChannel = ConfigData.CtrlSock;

		NLog = ConfigData.NLog;

	}

	public void run(){

		// Setup channel
		setupChannel();

		// Start Timer
		CLSelecTimer = new Timer();

		CLSelecTimer.schedule(task, 10000, (AppConstant.CL_SELEC_TIMEOUT * 1000));

	}

	private void CLSelecTimerCallback() {

		Log.d(TAG, "In CL selection callback");

		Node req_peer = new Node();

		//		String highest_battery = null;
		//		String my_battery = (String) Utils.getBatteryStatus().get("charge_percent");

		//for printing contents on all the peers
		//		for (String id : ConfigData.getPeerIds()){
		//
		//			Log.d(TAG, "My CNID: " + Utils.getDeviceID());		
		//			Log.d(TAG, "Neighbor ID: " + ConfigData.getPeer(id).ID);
		//			Log.d(TAG, "Content on this ID " + ConfigData.getPeer(id).contents);
		//			Log.d(TAG, "Parameter Weight value: " + ConfigData.getPeer(id).param_metric);		
		//
		//		}



		MaxCN = getBestPeer();

		Log.d(TAG, "MaxCN ID: " + MaxCN.getID());

		if (MaxCN.getID() == ""){
			// for this, cloud node will not change the role
			Log.d(TAG, "No neighbor discovred: TimerThread");
			
			ConfigData.setFacility(Constants.CN);
		}
		
		else if(MaxCN.getParamValue() <= Integer.parseInt(Utilities.getBatVal())){
			Log.d(TAG, "No qualified neighbor for Cloud Leader");
		}
		
		else if(MaxCN.getParamValue() > Integer.parseInt(Utilities.getBatVal())) {
			// for this cloud node will request the qualified neighbor to become cloud leader 
			Log.d(TAG, "Neighbor qualified for Cloud Leader, ID: " + MaxCN.getID());
			CurrentCLID = MaxCN.getID();

			// now send pass this message to controller service to send request to the neighboring node


			//			Message msg = handler.obtainMessage();
			//			Bundle bundle = new Bundle();
			//			bundle.putString("to", "Controller Service");
			//			bundle.putString("mtype", "Req_for_CL");
			//			bundle.putString("CNID", MaxCN.getID());
			//			msg.setData(bundle);
			//			handler.sendMessage(msg);


			Map<String, Object> CLReq = new LinkedHashMap();
			CLReq.put("msgType", "CL_SELECTION_REQ");
			CLReq.put("cnid", Utilities.getDeviceID());
			String jsondata = JSONValue.toJSONString(CLReq);

			try {

				CtrlChannel.send(jsondata, ConfigData.getPeer(MaxCN.getID()).getIP(), ConfigData.getMcPort());

				Log.d(TAG, "CL_SELECTION_REQ Message sent");
			} catch (Exception e) {
				Log.v(TAG, "Error in sending CL_SELECTION_REQ Message");
			}

		}

		//		Log.d(TAG, "Max Peer ID: " + req_peer.ID);		
		//		Log.d(TAG, "Max Peer Parameter Weight: " + req_peer.param_metric);


	}

	private static Node getBestPeer(){

		Node BestPeer = new Node();

		//		MaxPeer = null;

		for (String id : ConfigData.getPeerIds()){

			if (BestPeer == null || ConfigData.getPeer(id).getParamValue() > BestPeer.getParamValue() ){

				BestPeer = ConfigData.getPeer(id);
			}

		}

		//		Log.d(TAG, "Max Neighbor ID: " + BestPeer.getID());		
		//		Log.d(TAG, "Max Neighbor Parameter Weight: " + BestPeer.getParamValue());

		return BestPeer;
	}


	public static String getCurrentCLID(){
		return CurrentCLID;
	}

	public static void ForcedCLSelecTrigger(){

		Log.d(TAG, "Triggering Forced CL Selection");

		Node req_peer = new Node();


		MaxCN = getBestPeer();

		if (MaxCN.getID() == ""){
			// for this, cloud node will not change the role
			Log.d(TAG, "No neighbor discovred: ForcedThread");

		}
		else if(MaxCN.getParamValue() > Integer.parseInt(Utilities.getBatVal())) {
			// for this cloud node will request the qualified neighbor to become cloud leader 
			Log.d(TAG, "Neighbor qualified for Cloud Leader, ID: " + MaxCN.getID());
			CurrentCLID = MaxCN.getID();

			Map<String, Object> CLReq = new LinkedHashMap();
			CLReq.put("msgType", "CL_SELECTION_REQ");
			CLReq.put("cnid", Utilities.getDeviceID());
			String jsondata = JSONValue.toJSONString(CLReq);

			try {

				CtrlChannel.send(jsondata, ConfigData.getPeer(MaxCN.getID()).getIP(), ConfigData.getMcPort());

				Log.d(TAG, "CL_SELECTION_REQ Message sent");
			} catch (Exception e) {
				Log.v(TAG, "Error in sending CL_SELECTION_REQ Message");
			}

		}
		else{Log.d(TAG, "No qualified neighbor for Cloud Leader");}
		// for this, cloud node will not change its role until it is requested

		//		Log.d(TAG, "Max Peer ID: " + req_peer.ID);		
		//		Log.d(TAG, "Max Peer Parameter Weight: " + req_peer.param_metric);


		//CLSelecTimerCallback();
	}
	

	public TimerTask task = new TimerTask() {

		@Override
		public void run() {
			CLSelecTimerCallback();
		}
	};
}
