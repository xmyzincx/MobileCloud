package com.cwc.mobilecloud;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.cwc.mobilecloud.utilities.Tree;
import com.cwc.mobilecloud.utilities.TreeNode;
import com.cwc.mobilecloud.utilities.Utilities;

public class dump {

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	//	public class SectionsPagerAdapter extends FragmentPagerAdapter {
	//
	//		public SectionsPagerAdapter(FragmentManager fm) {
	//			super(fm);
	//		}
	//
	//		@Override
	//		public Fragment getItem(int position) {
	//			// getItem is called to instantiate the fragment for the given page.
	//			// Return a DummySectionFragment (defined as a static inner class
	//			// below) with the page number as its lone argument.
	//			Fragment fragment = new DummySectionFragment();
	//			Bundle args = new Bundle();
	//			args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
	//			fragment.setArguments(args);
	//			return fragment;
	//		}

	//		@Override
	//		public int getCount() {
	//			// Show 3 total pages.
	//			return 3;
	//		}
	//
	//		@Override
	//		public CharSequence getPageTitle(int position) {
	//			Locale l = Locale.getDefault();
	//			switch (position) {
	//			case 0:
	//				return getString(R.string.title_section1).toUpperCase(l);
	//			case 1:
	//				return getString(R.string.title_section2).toUpperCase(l);
	//			case 2:
	//				return getString(R.string.title_section3).toUpperCase(l);
	//			}
	//			return null;
	//		}
	//	}
	//
	//	/**
	//	 * A dummy fragment representing a section of the app, but that simply
	//	 * displays dummy text.
	//	 */
	//	public static class DummySectionFragment extends Fragment {
	//		/**
	//		 * The fragment argument representing the section number for this
	//		 * fragment.
	//		 */
	//		public static final String ARG_SECTION_NUMBER = "section_number";
	//
	//		public DummySectionFragment() {
	//		}
	//
	//		@Override
	//		public View onCreateView(LayoutInflater inflater, ViewGroup container,
	//				Bundle savedInstanceState) {
	//			View rootView = inflater.inflate(R.layout.fragment_main_dummy,
	//					container, false);
	//			TextView dummyTextView = (TextView) rootView
	//					.findViewById(R.id.section_label);
	//			dummyTextView.setText(Integer.toString(getArguments().getInt(
	//					ARG_SECTION_NUMBER)));
	//			return rootView;
	//		}
	//	}


	//	private void send_data_init_to_node_req(String file_req, String file_on_IP){
	//
	//		Log.d(TAG, "data initializing trigger thrasnrevier");
	//
	//		//send message to cl or node that has the required file
	//		Map<String, Object> init_data_msg=new LinkedHashMap();
	//
	//		//file_req is the name of the required file For the time being and for testing, it is hard coded.
	//
	//		//file_on_IP is the IP of the node that has the required file/content. For the time being and for testing, it is hard coded.
	//
	//		init_data_msg.put("msgType", "CN_DATA_INIT_TO_NODE_REQ");
	//		init_data_msg.put("cnid", Utilities.getDeviceID());
	//		init_data_msg.put("data_port", ConfigData.getDataPort());
	//		init_data_msg.put("file", file_req );
	//
	//		String jsondata = JSONValue.toJSONString(init_data_msg);
	//
	//		Log.d(TAG, "Sending file request to node: " + file_on_IP + " on Port: " + ConfigData.getMcPort());
	//
	//		CtrlChannel.send(jsondata, file_on_IP , ConfigData.getMcPort());
	//
	//}



	//	//TODO have to move this function to ControllerService
	//	private void send_url_data_req_to_cl(String file_url, String cl_IP){
	//
	//		if(send_counter==0){
	//
	//			Log.d(TAG, "requesting CL to get file from URL ");
	//
	//			//send message to cl or node that has the required file
	//			Map<String, Object> url_req_msg=new LinkedHashMap();
	//
	//			//file_req is the name of the required file For the time being and for testing, it is hard coded.
	//
	//			//file_on_IP is the IP of the node that has the required file/content. For the time being and for testing, it is hard coded.
	//
	//			url_req_msg.put("msgType", "URL_FILE_REQ");
	//			url_req_msg.put("cnid", Utilities.getDeviceID());
	//			url_req_msg.put("data_port", ConfigData.getDataPort());
	//			url_req_msg.put("url", file_url );
	//
	//			String jsondata = JSONValue.toJSONString(url_req_msg);
	//
	//			Log.d(TAG, "Sending file request to node: " + cl_IP + " on Port: " + ConfigData.getMcPort());
	//
	//			CtrlChannel.send(jsondata, cl_IP , ConfigData.getMcPort());
	//
	//			send_counter = 1;
	//		}



	//private void get_file_from_url(String... f_url){
	//		
	//		Log.d(TAG, "get file from url");
	//
	//		int nBytes = 0;
	//		long tBytes = 0;
	//
	//		File sdPath = null;
	//		String fullPath = null;
	//
	//
	//		try {
	//			URL url = new URL(f_url[0]);
	//			URLConnection conection = url.openConnection();
	//			conection.connect();
	//			
	//			Log.d(TAG, "try condition");
	//
	//			String file_name = "downloaded_file.jpg";
	//
	//			fullPath = ConfigData.getAppPath() +  "/" + file_name;
	//			sdPath = new File(fullPath);
	//
	//			// getting file length
	//			int totalBytes = conection.getContentLength();
	//			Log.d(TAG, "total size in bytes = " + totalBytes);
	//
	//
	//
	//			// input stream to read file - with 8k buffer
	//			InputStream input = new BufferedInputStream(url.openStream(), 8192);
	//
	//			// Output stream to write file
	//			OutputStream output = new FileOutputStream(sdPath);
	//
	//			byte[] chunk_size = new byte[1024];
	//
	//			// Send message to UI
	//			//			long totalBytes = Integer.valueOf((String) metadata.get("file_size"));
	//			Message msg = handler.obtainMessage();
	//			Bundle bundle = new Bundle();
	//			bundle.putString("to", "UI");
	//			bundle.putString("mtype", "FILE_START");
	//			bundle.putLong("TOTAL_BYTES", totalBytes);
	//			msg.setData(bundle);
	//			handler.sendMessage(msg);
	//
	//
	//			int prev_perc = 0;
	//			int curr_perc = 0;
	//
	//			while ((nBytes = input.read(chunk_size)) != -1) {
	//				tBytes += nBytes;
	//				// publishing the progress....
	//				// After this onProgressUpdate will be called
	//
	//				//Log.d(TAG, "Bytes Received = " + String.valueOf(tBytes));
	//
	//				curr_perc = (int)((tBytes * 100) / totalBytes);
	//				//Log.d(TAG, "Download % = " + String.valueOf(curr_perc));
	//				if (curr_perc != prev_perc){
	//					Log.d(TAG, "Download % = " + String.valueOf(curr_perc));
	//
	//					msg = handler.obtainMessage();
	//					bundle = new Bundle();
	//					bundle.putString("to", "UI");
	//					bundle.putString("mtype", "FILE_PROGRESS");
	//					bundle.putInt("CURRENT_BYTES", curr_perc);
	//					msg.setData(bundle);
	//					handler.sendMessage(msg);
	//
	//					prev_perc = curr_perc;
	//				}
	//
	//
	//				// writing data to file
	//				output.write(chunk_size, 0, nBytes);
	//			}
	//
	//			// flushing output
	//			output.flush();
	//
	//			// closing streams
	//			output.close();
	//			input.close();
	//
	//			Log.d(TAG, "Total Bytes Received = " + String.valueOf(tBytes));
	//
	//			msg = handler.obtainMessage();
	//			bundle = new Bundle();
	//			bundle.putString("to", "UI");
	//			bundle.putString("mtype", "FILE_END");
	//			bundle.putString("path", file_name);
	//
	//			msg.setData(bundle);
	//			handler.sendMessage(msg);
	//			
	//			
	//			//Now broadcast message to CN's
	//			
	//			Map<String, Object> init_data_msg=new LinkedHashMap();
	//			JSONObject metadata = new JSONObject();
	//			metadata.put("name", file_name);
	//			metadata.put("mime", "file");
	//			metadata.put("file_size", totalBytes);
	//			
	//			init_data_msg.put("msgType", "CL_DATA_INIT_REQ");
	//			init_data_msg.put("clid", Utilities.getDeviceID());
	//			init_data_msg.put("chunk_size", String.valueOf(chunk_size));
	//			init_data_msg.put("metadata", metadata);
	//			String jsondata = JSONValue.toJSONString(init_data_msg);
	//					
	//			String localIP = UDPConnection.getLocalIP(true);
	//			CtrlChannel.send(jsondata, UDPConnection.getBroadcastAddr(localIP, UDPConnection.getNetMask()) , ConfigData.getCtrlPort());		
	//			
	//
	//		} catch (Exception e) {
	//			Log.e("Download Error: ", e.getMessage());
	//		}
	//
	//	}
	
	
//	private void handle_cl_data_init_req(JSONObject rxdata, String ip, int port){
//		JSONObject metadata = (JSONObject) rxdata.get("metadata");
//		int chunk_size = Integer.valueOf((String) rxdata.get("chunk_size"));
//		String mime = (String)metadata.get("mime");
//		String file_name = (String)metadata.get("name");
//				
//		if ("file".equals(mime)){
//			// Send message to UI
//			long totalBytes = Integer.valueOf((String) metadata.get("file_size"));
//			Message msg = handler.obtainMessage();
//			Bundle bundle = new Bundle();
//			bundle.putString("to", "UI");
//			bundle.putString("mtype", "FILE_START");
//			bundle.putLong("TOTAL_BYTES", totalBytes);
//            msg.setData(bundle);
//            handler.sendMessage(msg);
//			
//			
//			FileOutputStream out;
//			ServerSocket serverSocket = null;
//			Socket connectionSocket = null;
//			InputStream is = null;
//			OutputStream os = null;
//			byte[] chunk = new byte[chunk_size];
//			int nBytes = 0;
//			long tBytes = 0;
//			File sdPath = null;
//			String fullPath = null;
//			try {
//				fullPath = ConfigData.getAppPath() +  "/" + file_name;
//				sdPath = new File(fullPath);
//				//out = context.openFileOutput((String)metadata.get("name"), Context.MODE_WORLD_READABLE);
//				out = new FileOutputStream(sdPath);
//			} catch (FileNotFoundException e1) {
//				Log.d(TAG, "Error Opening file for writing at CN");
//				e1.printStackTrace();
//				return;
//			}  
//			
//			try {
//				
//				
//				//Now Send message to CL to send file
//				Map<String, Object> init_data_msg=new LinkedHashMap();
//				
//				init_data_msg.put("msgType", "CL_DATA_INIT_RESP");
//				init_data_msg.put("cnid", Utilities.getDeviceID());
//				init_data_msg.put("chunk_size", String.valueOf(chunk_size));
//				init_data_msg.put("filename", file_name);
//				init_data_msg.put("port", String.valueOf(ConfigData.getDataPort()));
//				String jsondata = JSONValue.toJSONString(init_data_msg);
//						
//				String localIP = UDPConnection.getLocalIP(true);
//				CtrlChannel.send(jsondata, ip , port);		
//
//				
//				// Accept Connection
//				serverSocket = new ServerSocket(ConfigData.getDataPort());
//				connectionSocket = serverSocket.accept();
//				is = connectionSocket.getInputStream();
//				os = connectionSocket.getOutputStream();
//				
//				
//				int prev_perc = 0;
//				int curr_perc = 0;
//				// Receive data
//				Log.d(TAG, "Starting Transfer : " + connectionSocket.toString());
//				while ((nBytes = is.read(chunk)) != -1){
//					
//					out.write(chunk, 0, nBytes);
//					tBytes += nBytes;
//					//Log.d(TAG, "Bytes Received = " + String.valueOf(tBytes));
//					
//					curr_perc = (int)((tBytes * 100) / totalBytes);
//					//Log.d(TAG, "Download % = " + String.valueOf(curr_perc));
//					if (curr_perc != prev_perc){
//						Log.d(TAG, "Download % = " + String.valueOf(curr_perc));
//						
//						msg = handler.obtainMessage();
//						bundle = new Bundle();
//						bundle.putString("to", "UI");
//						bundle.putString("mtype", "FILE_PROGRESS");
//						bundle.putInt("CURRENT_BYTES", curr_perc);
//						msg.setData(bundle);
//				        handler.sendMessage(msg);
//				        
//				        prev_perc = curr_perc;
//					}
//				}				
//				
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			finally{
//				try {
//					is.close();
//					out.close();
//					connectionSocket.close();
//					serverSocket.close();
//					
//				} catch (IOException e) {
//					
//					e.printStackTrace();
//				}
//				
//			}
//			
//			Log.d(TAG, "Total Bytes Received = " + String.valueOf(tBytes));
//			
//			msg = handler.obtainMessage();
//			bundle = new Bundle();
//			bundle.putString("to", "UI");
//			bundle.putString("mtype", "FILE_END");
//			bundle.putString("path", file_name);
//			
//			msg.setData(bundle);
//	        handler.sendMessage(msg);
//					
//		}
//		
//	}
	
	
//	private void handle_cl_data_init_resp(JSONObject rxdata, String ip, int port){
//		Log.d(TAG, "CL_DATA_INIT_RESP msg received");
//		try{
//			String fname = (String) rxdata.get("filename");
//
//			int r_port = Integer.valueOf((String) rxdata.get("port"));
//			//			String r_McastIP = ((String) rxdata.get("DataMcastIP"));
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
//	}

	
	
	
//	// UnSorted battery values with IDs
//	Map<String, Integer> IDandBatVal = new HashMap<String, Integer>();
//
//	IDandBatVal.put("1", 15);
//	IDandBatVal.put("2", 14);
//	IDandBatVal.put("3", 13);
//	IDandBatVal.put("4", 12);
//	IDandBatVal.put("5", 11);
//	IDandBatVal.put("6", 10);
//	IDandBatVal.put("7", 9);
//	IDandBatVal.put("8", 8);
//	IDandBatVal.put("9", 7);
//	IDandBatVal.put("10", 6);
//	IDandBatVal.put("11", 5);
//	IDandBatVal.put("12", 4);
//	IDandBatVal.put("13", 3);
//	IDandBatVal.put("14", 2);
//	IDandBatVal.put("15", 1);
//
//	// Sorted battery values with IDs
//	Map<String, Integer> SortedIDBatVal = new HashMap<String, Integer>();
//
//	SortedIDBatVal = Utilities.sortBatvals(IDandBatVal);
//
//	String[] idList = SortedIDBatVal.keySet().toArray(new String[0]);
//
//	int nodes_limit = idList.length;
//
//	boolean limit_reached =  false;
//
//	TreeNode<String> root_node = new TreeNode<String>(Utilities.getDeviceID());
//
//	Tree<String> nodes_tree = new Tree<String>();
//
//	int main_index = 0;
//
//	int multiplier = 1;
//
//	int nodesMultiple = -(ConfigData.relayNodes);
//
//	int previous_index = 0;
//
//	int parent_limit = 0;
//
//	int nodes_counter = 0;
//
//	List<TreeNode<String>> previous_nodes_list = new ArrayList<TreeNode<String>>();
//
//	List<TreeNode<String>> temp_list = new ArrayList<TreeNode<String>>();
//	
//	previous_nodes_list.add(root_node);
//
//	TreeNode<String> parent_node;
//	
//	Map<String, String[]> dist_table = new HashMap<String, String[]>();
//	
//	String pNodeID = null;
//	
//	String[] cNodesArray = null;
//
//	while(limit_reached == false){
//
//		int child_limit = ConfigData.relayNodes;
//
//		if(multiplier == 1){
//
//			parent_limit = 1;
//
//			previous_index = 0;
//		}
//
//		else{
//
//			parent_limit = ConfigData.relayNodes * (previous_index + 1);
//
//			previous_index = parent_limit;
//
//		}
//
////		Log.d(DTAG, "parent_limit: " + parent_limit);
////		Log.d(DTAG, "previous_index: " + previous_index);
//
//		for(int i = main_index; i < parent_limit; i++){
//
//			if(limit_reached == false){
//
////				Log.d(DTAG, "parent_node index: " + (i - main_index));
////
////				Log.d(DTAG, "previous nodes list size: " + previous_nodes_list.size());
//
//				parent_node = previous_nodes_list.get(i - main_index);
//				
//				pNodeID = parent_node.getData();
//				
//				cNodesArray = new String[child_limit];
//
//
//				for(int j = 0; j < child_limit; j++){
//
//					if(nodes_counter < nodes_limit){
//
////						Log.d(DTAG, "ids from list: " + idList[j + nodesMultiple + ConfigData.relayNodes]);
//
//						TreeNode<String> child_node = new TreeNode<String>(idList[j + nodesMultiple + ConfigData.relayNodes]);
//
//						parent_node.addChild(child_node);
//
//						temp_list.add(child_node);
//						
//						cNodesArray[j] = child_node.getData();
//						
//						nodes_counter++;
//
////						Log.d(DTAG, "Counter: " + nodes_counter);
//
//					}
//
//					else{
//
//						limit_reached = true;
//
//						Log.d(DTAG, "nodes limit has reached");
//
//						break;
//					}
//				}
//
//				nodesMultiple = nodesMultiple + ConfigData.relayNodes;
//
////				Log.d(DTAG, "nodesMultiple: " + nodesMultiple);
//				
//				dist_table.put(pNodeID, cNodesArray);
//				
////				Log.d(DTAG, "Parent Node " + pNodeID + ": " + "Children nodes: " + cNodesArray.toString());
//
//			}
//
//			else break;
//
//		}
//		
//		previous_nodes_list = null;
//
//		previous_nodes_list = new ArrayList<TreeNode<String>>();
//
//		previous_nodes_list = temp_list;
//
//		temp_list = null;
//
//		temp_list = new ArrayList<TreeNode<String>>();
//
//		main_index = previous_index;
//
//		multiplier = multiplier * ConfigData.relayNodes;
//
////		Log.d(DTAG, "main_index: " + main_index);
////
////		Log.d(DTAG, "multiplier: " + multiplier);
//	}
//
//	nodes_tree.setRoot(root_node);
//
//	Log.d(DTAG, "number of nodes in the tree: " + nodes_tree.getNumberOfNodes());
//
//	
////	for(String key : dist_table.keySet()){
////		
////		Log.d(DTAG, "Parent Node " + (String) key + ": " + "Children nodes: " + dist_table.get(key).toString());
////		
////	}


}
