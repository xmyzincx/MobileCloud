package com.cwc.mobilecloud;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;



import com.cwc.mobilecloud.ConfigData.AppConstant;
import com.cwc.mobilecloud.ConfigData.Facility;
import com.cwc.mobilecloud.ConfigData.Severity;
import com.cwc.mobilecloud.utilities.Constants;
import com.cwc.mobilecloud.utilities.Utilities;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.webkit.MimeTypeMap;

public class ControlTransceiver extends Thread {

	private static final String TAG = "CtrlTxRxThread";
	private static final int maxCtrlMsgLen = 1024;

	private MulticastConnection CtrlChannel;
	private NetLog NLog;
	private Context context;
	private Handler handler;
	private static Thread dataThread;

	public Handler ctrhandler;

	private String adapterName = "wlan0";

	private volatile int acceptCounter = 0;



	ControlTransceiver(Context c, Handler h){
		context = c;

		handler = h;
	}


	private void setupChannel()
	{
		// Get Connections //

		CtrlChannel = ConfigData.CtrlSock;
		//String localIP = Utils.getLocalIP(true);
		//		CtrlChannel.setDstIP(UDPConnection.getBroadcastAddr(localIP, UDPConnection.getNetMask()));	// Dst IP set to broadcast address for now. It may change afterwards.

		NLog = ConfigData.NLog;
		//CtrlChannel.send("Ping");
		///////////////////////////////////	

	}


	public void run() {

		// Create Rx Control Channel //

		setupChannel();

		// Allocate Resources // 
		byte[] receiveData = new byte[maxCtrlMsgLen];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);


		//try { Thread.sleep(5000); } catch (Exception e){};
		Log.d(TAG, "Rx Control Thread Started");

		// Start Listening Loop //


		//		String file_url = "http://api.androidhive.info/progressdialog/hive.jpg";
		//
		//		for (int i=1; i<2; i++){
		//
		//			get_file_from_url(file_url);
		//
		//		}		

		while (true){
			String strData = null;
			// receive packet //

			try
			{
				CtrlChannel.getSocket().receive(receivePacket);
				strData = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
				Log.d(TAG, "Host IP address: " + receivePacket.getAddress().getHostAddress());
				Log.d(TAG, "Local address: " + Utilities.getInterfaceIPAddress("wlan0"));
			}
			catch(Exception e)
			{
				//CtrlChannel.close();
				Log.e(TAG, "Error Receiving data in RxThread" );
				return;
			}

			/* Discard messages from local IP */
			if (receivePacket.getAddress().getHostAddress().equals(Utilities.getInterfaceIPAddress(adapterName))){
				Log.d(TAG, "Discarding Packets from local address" );
				continue;
			}

			Log.d(TAG, "Rx raw data : " + strData);

			// Parse JSON //

			Object obj = null;
			try
			{
				obj=JSONValue.parse(strData);
			}
			catch(Exception e)
			{
				Log.d(TAG, "Invalid Control Packet Received" );
				continue;
			}		

			// Process Received Message //

			msgHandler(obj, receivePacket.getAddress().getHostAddress(), receivePacket.getPort());

		}
	}


	private void msgHandler(Object cPkt, String Address, int port)
	{
		JSONObject rxdata = null;
		try
		{
			//			Log.d(TAG, "Packet received from " + Address + " Port: " + String.valueOf(port));

			rxdata = (JSONObject) cPkt;

			//Log.d(TAG, "value " + rxdata.get("msgType"));

		}
		catch(Exception e){Log.d(TAG, "Error in JSON handling"); return;}

		String msgType = (String)rxdata.get("msgType");

		Map<String, Method> fMap = new LinkedHashMap();
		Class[] params = new Class[3];
		params[0] = JSONObject.class;
		params[1] = String.class;
		params[2] = Integer.TYPE;	

		try{
			// Discovery messages

			fMap.put("CN_DISCOVERY_REQ", this.getClass().getDeclaredMethod("handle_disc_req", params));
			fMap.put("CN_DISCOVERY_RESP", this.getClass().getDeclaredMethod("handle_disc_resp", params));


			// CL selection messages

			fMap.put("CL_SELECTION_REQ", this.getClass().getDeclaredMethod("handle_cl_select_req", params));
			fMap.put("CN_SET_CL", this.getClass().getDeclaredMethod("handle_cn_set_cl", params));


			// URL file request messages

			fMap.put("URL_FILE_REQ", this.getClass().getDeclaredMethod("handle_url_file_req", params));
			fMap.put("URL_FILE_RESP", this.getClass().getDeclaredMethod("handle_url_file_resp", params));
			fMap.put("CL_SEND_URL_FILE", this.getClass().getDeclaredMethod("handle_cl_send_url_file", params));


			// In-cloud data/content transfer/exchange messages

			fMap.put("CN_DATA_INIT_TO_NODE_REQ", this.getClass().getDeclaredMethod("handle_cn_data_init_to_node_req", params));
			fMap.put("CN_DATA_INIT_TO_NODE_RESP", this.getClass().getDeclaredMethod("handle_cn_data_init_to_node_resp", params));
			fMap.put("CL_DATA_INIT_RESP", this.getClass().getDeclaredMethod("handle_cl_data_init_resp", params));


			// Video Streaming messages

			fMap.put("START_STREAM_REQ", this.getClass().getDeclaredMethod("handle_start_stream_req", params));

		}

		catch(Exception e){Log.d(TAG, "No such methods found"); return;}


		Method m = fMap.get(msgType);
		if (m == null){
			Log.d(TAG,  "Invalid Message Type Received");
			return ;
		}
		else{
			try{
				m.invoke(this, rxdata, Address, port);
			}
			catch(Exception e){
				Log.d(TAG, "Error invoking method");
				e.printStackTrace();
				return;
			}
		}

	}

	////////////////////////////////////////////////////////////////////////
	//////////--------- Cloud formation messages--------------//////////////
	////////////////////////////////////////////////////////////////////////


	private void handle_disc_req(JSONObject rxdata, String ip, int port){

		Log.d(TAG, "CN_DISCOVERY_REQ msg received");

		//JSONArray FilesOnPeer = (JSONArray) rxdata.get("FilesOnNode");	//moved to response message


		Map<String, Object> data=new LinkedHashMap();
		Dictionary<?, ?> batProp = Utilities.getBatteryStatus();

		/////////////////////////////////////////////////////////////////
		// This is the algorithm or method to calculate parameter metric.
		// On this basis cloud leader will be selected.
		// Currently it is the battery level PLUS charging condition.
		// This can be changed in future if needed.

		String param_metric = Utilities.getBatVal();

		//		//		int param_metric = (Integer) batProp.get("charge_percent");
		//
		//		boolean charging_state = (Boolean) batProp.get("charging");
		//		if (charging_state == true){
		//
		//			//			Log.d(TAG, "battery level: " + param_metric);
		//			param_metric = (Integer) batProp.get("charge_percent") + 10;
		//			//			Log.d(TAG, "battery level if charging condition is true " + param_metric);
		//		} 
		//
		//		else{param_metric = (Integer) batProp.get("charge_percent");}

		//		Log.d(TAG, "check 2 battery level: " + param_metric);

		////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////

		JSONArray FilesOnNode = new JSONArray();
		FilesOnNode = Utilities.getFileNames(); 

		data.put("msgType", "CN_DISCOVERY_RESP");
		data.put("cnid", Utilities.getDeviceID());
		data.put("param_metric", param_metric);
		data.put("FilesOnNode", FilesOnNode);

		String jsondata = JSONValue.toJSONString(data);
		//		Log.d(TAG, "json data before sending: " + jsondata);
		CtrlChannel.send(jsondata, ip, port);
		//		Log.d(TAG, "sent to IP: " + ip + " on port: " + port);

	}


	private void handle_disc_resp(JSONObject rxdata, String ip, int port){

		Log.d(TAG, "CN_DISCOVERY_RESP mesg received");

		String peer_cnid = (String)rxdata.get("cnid");
		String peer_param_metric = (String)rxdata.get("param_metric");
		JSONArray peer_FilesOnPeer = (JSONArray) rxdata.get("FilesOnNode");


		//		Log.d(TAG, "My CNID: " + Utils.getDeviceID());
		//		Log.d(TAG, "CN_DISCOVERY_RESPONSE from: " + (String)rxdata.get("cnid"));
		//		//		Log.d(TAG, "Files/Contents on : " + (String)rxdata.get("cnid") + " are ");
		//
		//				// printing files on peer
		//				for (int i=0; i < FilesOnPeer.size(); i++)
		//				{
		//					Log.d(TAG, "FileName: " + FilesOnPeer.get(i));
		//				}

		if (peer_cnid == null){
			NLog.Log("Discovery response with no CNID", ConfigData.getFacility(), Severity.INFO, Utilities.getDeviceID());
			return;
		}
		try{
			Node peer = ConfigData.getPeer(peer_cnid);


			if (peer == null){
				peer = new Node();
				peer.setIP(ip);
				peer.setPort(port);
				peer.ID = peer_cnid;
				peer.age = AppConstant.PEER_EXPIRE_AGE;
				peer.param_metric = Integer.valueOf(peer_param_metric);
				peer.setContents(peer_FilesOnPeer);
				ConfigData.putPeer(peer_cnid, peer);


			}else{
				peer.setIP(ip);
				peer.setPort(port);
				peer.ID = peer_cnid;
				peer.age = AppConstant.PEER_EXPIRE_AGE;
				peer.param_metric = Integer.valueOf(peer_param_metric);
				peer.setContents(peer_FilesOnPeer);
				ConfigData.putPeer(peer_cnid, peer);
			}

			///////////////////for testing only --- remove/comment after testing//////////////////////////////
			//			
			//			Node temppeer = ConfigData.getPeer(cnid);
			//			int sizeofcontent = temppeer.contents.size(); 			
			//			
			//			for (int i=0; i < sizeofcontent; i++)
			//			{
			//				Log.d(TAG, "Fetch from neighbor database: " + temppeer.contents.get(i) );
			//			}
			//			
			/////////////////////////////////////// testing only//////////////////////////////////////////////


		}
		catch(Exception e){
			Log.d(TAG, "Exception in Handling peer resp");
		}

		//		Manually triggering file trasnfer. For testing purpose only
		//		send_data_init_to_node_req("12.jpg", "192.168.1.147");

		return;
	}


	private void handle_cl_select_req(JSONObject rxdata, String ip, int port){

		Log.d(TAG, "CL_SELECTION_REQ mesg received");

		//TODO Have to perform some checks to agree to form Cloud Leader
		//TODO For the time being we are assuming that the node agrees to become the cloud leader

		Map<String, Object> cl_sel_resp=new LinkedHashMap();

		cl_sel_resp.put("msgType", "CN_SET_CL");
		cl_sel_resp.put("clid", Utilities.getDeviceID());
		try {
			cl_sel_resp.put("cluster_id", Utilities.generateRandomString(6, Utilities.Mode.ALPHANUMERIC));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cl_sel_resp.put("status", "ACCEPT");


		String jsondata = JSONValue.toJSONString(cl_sel_resp);
		CtrlChannel.send(jsondata, ip, port);

		// Change Role to CL
		boolean roleChanged = false;
		if (ConfigData.getFacility().equals(Facility.CL)){
			roleChanged = false;
		}
		else{
			roleChanged = true;
		}

		ConfigData.setFacility(Facility.CL);

		// Update UI
		if (roleChanged == true){
			Message msg = handler.obtainMessage();
			Bundle bundle = new Bundle();
			bundle.putString("to", "UI");
			bundle.putString("mtype", "ROLE_CHANGED");
			bundle.putString("role", "CL");
			msg.setData(bundle);
			handler.sendMessage(msg);
		}

	}


	private void handle_cn_set_cl(JSONObject rxdata, String ip, int port){

		Log.d(TAG, "CN_SET_CL mesg received");

		Log.d(TAG, "Node Selected as CL");

		String clid = (String)rxdata.get("clid");
		String clusterid = (String)rxdata.get("cluster_id");

		//Log.d(TAG, "New clid = " + clid);
		//Log.d(TAG, "New cluster ID = " + clusterid);
		boolean roleChanged = false;
		if (ConfigData.getFacility().equals(Facility.CN)){
			roleChanged = false;

			// Update UI
			Message msg = handler.obtainMessage();
			Bundle bundle = new Bundle();
			bundle.putString("to", "UI");
			bundle.putString("mtype", "ROLE_CHANGED");
			bundle.putString("role", "CN");
			msg.setData(bundle);
			handler.sendMessage(msg);
		}
		else{
			roleChanged = true;
		}

		ConfigData.setFacility(Facility.CN);	// Set Current role a CN

		Node new_cl = new Node();
		new_cl.setIP(ip);
		new_cl.setPort(port);
		new_cl.ID = clid;

		ConfigData.setCL(new_cl);	

		// Update UI
		if (roleChanged == true){
			Message msg = handler.obtainMessage();
			Bundle bundle = new Bundle();
			bundle.putString("to", "UI");
			bundle.putString("mtype", "ROLE_CHANGED");
			bundle.putString("role", "CN");
			msg.setData(bundle);
			handler.sendMessage(msg);
		}

		//		String file_url = "http://api.androidhive.info/progressdialog/hive.jpg";
		//		send_url_data_req_to_cl(file_url, ip );

	}


	////////////////////////////////////////////////////////////////////////
	//////////--------- Cloud formation messages--------------//////////////
	////////////////////////////////////////////////////////////////////////


	// Old handle_cl_select_req moved to dump
	// Old handle_cn_set_cl moved to dump
	// send_data_init_to_node_req has been moved to dump
	// old handler for "cl_data_init_req" has been moved to Dump



	////////////////////////////////////////////////////////////////////////////////////
	/////////------- Content transfer/exchange from CL to all nodes --------////////////
	////////////////////////////////////////////////////////////////////////////////////


	private void handle_url_file_req(JSONObject rxdata, String ip, int port){

		Log.d(TAG, "URL_FILE_REQ msg received from " + ip);

		Log.d(TAG, "get file from url");

		downloadingThread d_thread = new downloadingThread(rxdata, ip, port, true);

		d_thread.start();

	}


	private void handle_url_file_resp(JSONObject rxdata, String ip, int port){

		Log.d(TAG, "URL_FILE_RESP mesg received");

		downloadingThread d_thread = new downloadingThread(rxdata, ip, port, false);

		d_thread.start();

	}


	private void handle_cl_send_url_file(JSONObject rxdata, String ip, int port){

		Log.d(TAG, "CL_SEND_URL_FILE msg received");

		acceptCounter = acceptCounter + 1;

		Log.d(TAG, "accept_counter: " + acceptCounter);

		//		try{
		//			String fname = (String) rxdata.get("filename");
		//			int r_port = Integer.valueOf((String) rxdata.get("port"));
		//			int chunk_size = Integer.valueOf((String) rxdata.get("chunk_size"));
		//			File sdPath;
		//
		//			sdPath = new File(ConfigData.getAppPath() +  "/" + fname);
		//
		//			Thread t = new FileSenderTCP(context, handler, ip, r_port, sdPath, chunk_size);
		//			t.start();
		//		}
		//		catch(Exception e){
		//			Log.d(TAG, "Error in handling CL_DATA_INIT_RESP");
		//			e.printStackTrace();
		//		}

	}


	////////////////////////////////////////////////////////////////////////////////////
	/////////------- Content transfer/exchange from CL to all nodes --------////////////
	////////////////////////////////////////////////////////////////////////////////////



	////////////////////////////////////////////////////////////////////////////////////
	//////////------- Content transfer/exchange from node to node ----------////////////
	////////////////////////////////////////////////////////////////////////////////////


	private void handle_cn_data_init_to_node_req(JSONObject rxdata, String ip, int port){

		Log.d(TAG, "CN_DATA_INIT_TO_NODE_REQ msg received from " + ip + " on Port: " + port);

		// send response message to the node that requested the file

		//TODO insert check whether the file is actually in the node's storage or not

		//TODO this chunk size is temporary only for testing purpose. later put it in constant
		String dataChunkSize = String.valueOf(1024);

		// creating metadata of the file
		JSONObject metadata = new JSONObject();
		File req_file = new File(Constants.path + "/" + rxdata.get("file"));
		String req_file_size = String.valueOf(req_file.length());

		metadata.put("name", rxdata.get("file"));
		metadata.put("mime", "file");
		metadata.put("file_size", req_file_size);

		Map<String, Object> init_data_msg=new LinkedHashMap();

		init_data_msg.put("msgType", "CN_DATA_INIT_TO_NODE_RESP");
		init_data_msg.put("clid", Utilities.getDeviceID());
		init_data_msg.put("chunk_size", dataChunkSize);
		init_data_msg.put("metadata", metadata);

		String jsondata = JSONValue.toJSONString(init_data_msg);

		CtrlChannel.send(jsondata, ip , port);

	}


	private void handle_cn_data_init_to_node_resp(JSONObject rxdata, String ip, int port){

		Log.d(TAG, "CN_DATA_INIT_TO_NODE_RESP msg received");

		JSONObject metadata = (JSONObject) rxdata.get("metadata");

		int chunk_size = Integer.valueOf((String) rxdata.get("chunk_size"));
		String mime = (String)metadata.get("mime");
		String file_name = (String)metadata.get("name");
		//		String tempMcastIP = (String) rxdata.get("DataMcastIP");
		//		String tempMcastPort = (String) rxdata.get("DataMcastPort");


		if ("file".equals(mime)){


			// Send message to UI
			long totalBytes = Integer.valueOf((String) metadata.get("file_size"));
			Message msg = handler.obtainMessage();
			Bundle bundle = new Bundle();
			bundle.putString("to", "UI");
			bundle.putString("mtype", "FILE_START");
			bundle.putLong("TOTAL_BYTES", totalBytes);
			msg.setData(bundle);
			handler.sendMessage(msg);


			FileOutputStream out;
			ServerSocket serverSocket = null;
			Socket connectionSocket = null;
			InputStream is = null;
			OutputStream os = null;
			byte[] chunk = new byte[chunk_size];
			int nBytes = 0;
			long tBytes = 0;
			File sdPath = null;
			String fullPath = null;
			try {

				fullPath = ConfigData.getAppPath() +  "/" + file_name;
				sdPath = new File(fullPath);
				//out = context.openFileOutput((String)metadata.get("name"), Context.MODE_WORLD_READABLE);
				out = new FileOutputStream(sdPath);
			} catch (FileNotFoundException e1) {
				Log.d(TAG, "Error Opening file for writing at CN");
				e1.printStackTrace();
				return;
			}  

			try {

				//TODO one important thing to do is to remove and stop sending multicast ip and port in the JSON message again and again. just do it one time only

				//Now Send message to CL to send file
				Map<String, Object> init_data_msg=new LinkedHashMap();

				init_data_msg.put("msgType", "CL_DATA_INIT_RESP");
				init_data_msg.put("cnid", Utilities.getDeviceID());
				init_data_msg.put("chunk_size", String.valueOf(chunk_size));
				init_data_msg.put("filename", file_name);
				init_data_msg.put("port", String.valueOf(ConfigData.getDataPort()));
				String jsondata = JSONValue.toJSONString(init_data_msg);

				// might cause a problem, check later
				// This should NOT be a multicast ip. it should be only meant for cloud leader or file sender
				Log.d(TAG, "handler 2, " + "IP: " + ip + " Port: " + port);
				CtrlChannel.send(jsondata, ip , port);

				// Accept Connection
				serverSocket = new ServerSocket(ConfigData.getDataPort());

				connectionSocket = serverSocket.accept();

				is = connectionSocket.getInputStream();

				os = connectionSocket.getOutputStream();

				int prev_perc = 0;
				int curr_perc = 0;

				// Receive data
				Log.d(TAG, "Starting Transfer : " + connectionSocket.toString());
				while ((nBytes = is.read(chunk)) != -1){

					out.write(chunk, 0, nBytes);
					tBytes += nBytes;
					//Log.d(TAG, "Bytes Received = " + String.valueOf(tBytes));

					curr_perc = (int)((tBytes * 100) / totalBytes);
					//Log.d(TAG, "Download % = " + String.valueOf(curr_perc));
					if (curr_perc != prev_perc){

						Log.d(TAG, "Download % = " + String.valueOf(curr_perc));

						msg = handler.obtainMessage();
						bundle = new Bundle();
						bundle.putString("to", "UI");
						bundle.putString("mtype", "FILE_PROGRESS");
						bundle.putInt("CURRENT_BYTES", curr_perc);
						msg.setData(bundle);
						handler.sendMessage(msg);

						prev_perc = curr_perc;
					}
				}				

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally{
				try {
					is.close();
					out.close();
					connectionSocket.close();
					serverSocket.close();

				} catch (IOException e) {

					e.printStackTrace();
				}

			}

			Log.d(TAG, "Total Bytes Received = " + String.valueOf(tBytes));

			msg = handler.obtainMessage();
			bundle = new Bundle();
			bundle.putString("to", "UI");
			bundle.putString("mtype", "FILE_END");
			bundle.putString("path", file_name);

			msg.setData(bundle);
			handler.sendMessage(msg);

		}

	}


	private void handle_cl_data_init_resp(JSONObject rxdata, String ip, int port){
		Log.d(TAG, "CL_DATA_INIT_RESP msg received");
		try{
			String fname = (String) rxdata.get("filename");

			int r_port = Integer.valueOf((String) rxdata.get("port"));
			//			String r_McastIP = ((String) rxdata.get("DataMcastIP"));
			int chunk_size = Integer.valueOf((String) rxdata.get("chunk_size"));
			File sdPath;

			sdPath = new File(ConfigData.getAppPath() +  "/" + fname);

			Thread t = new FileSenderTCP(context, handler, ip, r_port, sdPath, chunk_size);
			t.start();
		}
		catch(Exception e){
			Log.d(TAG, "Error in handling CL_DATA_INIT_RESP");
			e.printStackTrace();
		}
	}


	////////////////////////////////////////////////////////////////////////////////////
	//////////------- Content transfer/exchange from node to node ----------////////////
	////////////////////////////////////////////////////////////////////////////////////


	////////////////////////////////////////////////////////////////////////////////////
	//////////------------------ Video Streaming messages ------------------////////////
	////////////////////////////////////////////////////////////////////////////////////


	private void handle_start_stream_req(JSONObject rxdata, String ip, int port){


		String cnid = (String)rxdata.get("cnid");

		Log.d(TAG, "START_STREAM_REQ msg received");

		// Sending message to UI to get ready for streaming and start media player
		Message msg = handler.obtainMessage();
		Bundle bundle = new Bundle();
		bundle.putString("to", "UI");
		bundle.putString("mtype", "START_STREAM");
		msg.setData(bundle);
		handler.sendMessage(msg);

	}


	////////////////////////////////////////////////////////////////////////////////////
	//////////------------------ Video Streaming messages ------------------////////////
	////////////////////////////////////////////////////////////////////////////////////



	////////////////////////////////////////////////////////////////////////////////////
	///////---------- Thread for downloading content from internet and CL -------///////
	////////////////////////////////////////////////////////////////////////////////////


	private class downloadingThread extends Thread{

		JSONObject rxdata_dThread;
		String ip_dThread;
		int port_dThread;
		boolean nodeIsCL;


		public downloadingThread(JSONObject rxdata_dThread, String ip_dThread, int port_dThread, boolean nodeIsCL){

			this.rxdata_dThread = rxdata_dThread;
			this.ip_dThread = ip_dThread;
			this.port_dThread = port_dThread;
			this.nodeIsCL = nodeIsCL;

		}

		@Override
		public void run() {
			// TODO Auto-generated method stub

			// If cloud leader has initiated this task,
			// then this task will be used to download data from internet

			if(nodeIsCL == true){

				downloadFromURL(rxdata_dThread, ip_dThread, port_dThread);

			}

			// If cloud node has initiated this task,
			// then this task will be used to download data from cloud leader

			else if(nodeIsCL == false){

				downloadFromCL(rxdata_dThread, ip_dThread, port_dThread);

			}
		}


		private void downloadFromURL(JSONObject rxdata, String ip, int port){

			int nBytes = 0;
			long tBytes = 0;

			File sdPath = null;
			String fullPath = null;
			//
			//			Socket clientSocket = null;

			String file_url = (String) rxdata.get("url");

			//TODO this may generate errors when a node is remove from the peer's map
			Set<String> neighborsIDset = ConfigData.getPeerIds();
			int totalNeighbors = neighborsIDset.size();


			OutputStream os = null;
			ArrayList<Socket> clientConnections = new ArrayList<Socket>();
			//			ArrayList<OutputStream> clientOutputStreams = new ArrayList<OutputStream>();

			try {

				URL url = new URL(file_url);
				URLConnection conection = url.openConnection();
				conection.connect();

				String file_name = file_url.substring(file_url.lastIndexOf('/') + 1);

				fullPath = ConfigData.getAppPath() +  "/" + file_name;
				sdPath = new File(fullPath);

				// getting file length
				int totalBytes = conection.getContentLength();
				Log.d(TAG, "total size in bytes = " + totalBytes);


				// SEND MESSAGE TO ALL CN TO GET READY FOR DATA
				//Now broadcast message to CN's

				Map<String, Object> init_data_msg=new LinkedHashMap();
				JSONObject metadata = new JSONObject();
				metadata.put("name", file_name);
				metadata.put("mime", "file");
				metadata.put("file_size", String.valueOf(totalBytes));

				init_data_msg.put("msgType", "URL_FILE_RESP");
				init_data_msg.put("clid", Utilities.getDeviceID());

				//TODO check this one
				init_data_msg.put("chunk_size", String.valueOf("1024"));
				init_data_msg.put("metadata", metadata);
				String jsondata = JSONValue.toJSONString(init_data_msg);

				String localIP = Utilities.getLocalIP(true);
				CtrlChannel.send(jsondata, ConfigData.getMcIP() , ConfigData.getMcPort());	


				// Initialize new client sockets

				while(acceptCounter <= totalNeighbors){

					if(acceptCounter == totalNeighbors){

						for(String id : neighborsIDset){

							Socket clientSocket = new Socket();

							// Connect to the server ip and port

							String client_ip = ConfigData.getPeer(id).getIP();

							clientSocket.connect(new InetSocketAddress(client_ip, 5252), 5000);
							//							os = clientSocket.getOutputStream();

							clientConnections.add(clientSocket);
							//							clientOutputStreams.add(os);

							acceptCounter = totalNeighbors + 1;
						}
					}
				}

				// Send message to UI
				//			long totalBytes = Integer.valueOf((String) metadata.get("file_size"));
				Message msg = handler.obtainMessage();
				Bundle bundle = new Bundle();
				bundle.putString("to", "UI");
				bundle.putString("mtype", "FILE_START");
				bundle.putLong("TOTAL_BYTES", totalBytes);
				msg.setData(bundle);
				handler.sendMessage(msg);

				// input stream to read file - with 8k buffer
				InputStream input = new BufferedInputStream(url.openStream(), 8192);

				// Output stream to write file
				OutputStream output = new FileOutputStream(sdPath);

				byte[] chunk_size = new byte[1024];

				int prev_perc = 0;
				int curr_perc = 0;


				//				os = clientSocket.getOutputStream();

				while ((nBytes = input.read(chunk_size)) != -1) {

					tBytes += nBytes;

					//Log.d(TAG, "Bytes Received = " + String.valueOf(tBytes));

					curr_perc = (int)((tBytes * 100) / totalBytes);

					//Log.d(TAG, "Download % = " + String.valueOf(curr_perc));

					for(int index = 0; index<clientConnections.size(); index++){

						os = clientConnections.get(index).getOutputStream();
						os.write(chunk_size, 0, nBytes);
						//						clientOutputStreams.get(index).write(chunk_size, 0, nBytes);

					}

					if (curr_perc != prev_perc){
						Log.d(TAG, "Download % = " + String.valueOf(curr_perc));

						msg = handler.obtainMessage();
						bundle = new Bundle();
						bundle.putString("to", "UI");
						bundle.putString("mtype", "FILE_PROGRESS");
						bundle.putInt("CURRENT_BYTES", curr_perc);
						msg.setData(bundle);
						handler.sendMessage(msg);

						prev_perc = curr_perc;
					}

					// writing data to file
					output.write(chunk_size, 0, nBytes);
				}

				// Clearing accept counter
				acceptCounter = 0;

				// flushing output
				output.flush();
				os.flush();

				// closing streams
				output.close();
				input.close();
				os.close();

				Log.d(TAG, "Total Bytes Received = " + String.valueOf(tBytes));

				msg = handler.obtainMessage();
				bundle = new Bundle();
				bundle.putString("to", "UI");
				bundle.putString("mtype", "FILE_END");
				bundle.putString("path", file_name);

				msg.setData(bundle);
				handler.sendMessage(msg);

			} 

			catch (SocketException  e) {
				e.printStackTrace();
				Log.e("Download Error: ", e.getMessage());
			}
			catch (IOException  e) {
				e.printStackTrace();
				Log.e("Download Error: ", e.getMessage());
			}
			catch (Exception e) {
				e.printStackTrace();
				Log.e("Download Error: ", e.getMessage());
			}
			finally{
				for(int index=0; index < clientConnections.size(); index++)
					try {
						clientConnections.get(index).close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}

		}


		private void downloadFromCL(JSONObject rxdata, String ip, int port){

			Log.d(TAG, "download from CL thread");

			JSONObject metadata = (JSONObject) rxdata.get("metadata");
			int chunk_size = Integer.valueOf((String) rxdata.get("chunk_size"));
			String mime = (String)metadata.get("mime");
			String file_name = (String)metadata.get("name");

			int temp_totalBytes = Integer.valueOf((String) metadata.get("file_size"));

			Log.d(TAG, "File_size: " + temp_totalBytes);

			if ("file".equals(mime)){

				// Send message to UI
				long totalBytes = Integer.valueOf((String) metadata.get("file_size"));
				Message msg = handler.obtainMessage();
				Bundle bundle = new Bundle();
				bundle.putString("to", "UI");
				bundle.putString("mtype", "FILE_START");
				bundle.putLong("TOTAL_BYTES", totalBytes);
				msg.setData(bundle);
				handler.sendMessage(msg);


				FileOutputStream out = null;
				ServerSocket serverSocket = null;
				Socket connectionSocket = null;
				InputStream is = null;
				OutputStream os = null;
				byte[] chunk = new byte[chunk_size];
				int nBytes = 0;
				long tBytes = 0;
				File sdPath = null;
				String fullPath = null;
				try {
					fullPath = ConfigData.getAppPath() +  "/" + file_name;
					sdPath = new File(fullPath);
					//out = context.openFileOutput((String)metadata.get("name"), Context.MODE_WORLD_READABLE);
					out = new FileOutputStream(sdPath);
				} catch (FileNotFoundException e1) {
					Log.d(TAG, "Error Opening file for writing at CN");
					e1.printStackTrace();
				}  

				try {

					//Now Send message to CL to send file

					Map<String, Object> init_data_msg=new LinkedHashMap();

					init_data_msg.put("msgType", "CL_SEND_URL_FILE");
					init_data_msg.put("cnid", Utilities.getDeviceID());
					init_data_msg.put("chunk_size", String.valueOf(chunk_size));
					init_data_msg.put("filename", file_name);
					init_data_msg.put("port", String.valueOf(ConfigData.getDataPort()));
					String jsondata = JSONValue.toJSONString(init_data_msg);


					CtrlChannel.send(jsondata, ip , port);		

					// Make new server socket
					serverSocket = new ServerSocket(5252);

					// Accept the incomming clnnections from client socket
					connectionSocket = serverSocket.accept();

					// Initialize input and output streams
					is = connectionSocket.getInputStream();
					os = connectionSocket.getOutputStream();

					int prev_perc = 0;
					int curr_perc = 0;
					// Receive data
					Log.d(TAG, "Starting Transfer : " + connectionSocket.toString());

					while ((nBytes = is.read(chunk)) != -1){

						out.write(chunk, 0, nBytes);
						tBytes += nBytes;
						//Log.d(TAG, "Bytes Received = " + String.valueOf(tBytes));

						curr_perc = (int)((tBytes * 100) / totalBytes);

						//Log.d(TAG, "Download % = " + String.valueOf(curr_perc));
						if (curr_perc != prev_perc){


							Log.d(TAG, "Download % = " + String.valueOf(curr_perc));

							msg = handler.obtainMessage();
							bundle = new Bundle();
							bundle.putString("to", "UI");
							bundle.putString("mtype", "FILE_PROGRESS");
							bundle.putInt("CURRENT_BYTES", curr_perc);
							msg.setData(bundle);
							handler.sendMessage(msg);

							prev_perc = curr_perc;
						}
					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				finally{
					try {
						is.close();
						out.close();
						connectionSocket.close();
						serverSocket.close();

					} catch (IOException e) {

						e.printStackTrace();
					}

				}

				Log.d(TAG, "Total Bytes Received = " + String.valueOf(tBytes));

				msg = handler.obtainMessage();
				bundle = new Bundle();
				bundle.putString("to", "UI");
				bundle.putString("mtype", "FILE_END");
				bundle.putString("path", file_name);

				msg.setData(bundle);
				handler.sendMessage(msg);

			}

		}

	}



	////////////////////////////////////////////////////////////////////////////////////
	///////--------- Async task for downloading content from internet ---------/////////
	////////////////////////////////////////////////////////////////////////////////////

}	// End Class
