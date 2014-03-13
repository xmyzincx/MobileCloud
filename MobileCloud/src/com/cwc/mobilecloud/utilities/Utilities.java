package com.cwc.mobilecloud.utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.http.conn.util.InetAddressUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.cwc.mobilecloud.ConfigData;
import com.cwc.mobilecloud.utilities.Constants;
import com.cwc.mobilecloud.utilities.Shell;
import com.cwc.mobilecloud.utilities.Utilities;
import com.cwc.mobilecloud.utilities.Shell.ShellException;
import com.cwc.mobilecloud.utilities.Utilities.Mode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Environment;
import android.util.Log;

public final class Utilities {

	private static final String TAG = "Utilities";

	public static boolean batteryDataOk = false;
	private static boolean charging = false;
	private static boolean usbCharge = false;
	private static boolean acCharge = false;
	private static int level_percent = 0;
	private static String BatVal;

	private static Map<String, String> childParentTable = new HashMap<String, String>();

	private static ReadWriteLock batteryLock = new ReentrantReadWriteLock();


	public static BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context arg0, Intent intent) {
			// Extract battery stats from Intent Extras
			Lock l = batteryLock.writeLock();
			l.lock();
			try {
				// access the resource protected by this lock

				int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
				int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
				int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
				int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

				level_percent = (level*100)/scale;
				charging = (status == BatteryManager.BATTERY_STATUS_CHARGING) || (status == BatteryManager.BATTERY_STATUS_FULL);
				usbCharge = (chargePlug == BatteryManager.BATTERY_PLUGGED_USB);
				acCharge = (chargePlug == BatteryManager.BATTERY_PLUGGED_AC);

				batteryDataOk = true;			    
			} 
			finally {
				l.unlock();
			}        	
			batteryDataOk = true;          
		}
	};      

	public static Dictionary getBatteryStatus()
	{
		Dictionary ret = new Hashtable();
		Lock l = batteryLock.readLock();
		l.lock();
		try {      			
			ret.put(Constants.CHARGE_PERCENT, level_percent);
			ret.put(Constants.charging, charging);
			ret.put(Constants.acCharge, acCharge);
			ret.put(Constants.usbCharge, usbCharge);      		     			
		} 
		finally {
			l.unlock();
		}

		return ret;

	};

	public static boolean isBatteryDataValid()
	{
		return batteryDataOk;    	  
	}


	public static String getDeviceID(){
		return android.os.Build.MANUFACTURER + android.os.Build.HARDWARE + android.os.Build.DEVICE + android.os.Build.ID + android.os.Build.SERIAL;

	}



	/**
	 * Get IP address from first non-localhost interface
	 * @param ipv4  true=return ipv4, false=return ipv6
	 * @return  address or empty string
	 */
	public static String getLocalIP(boolean useIPv4) {
		try {
			List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces) {
				List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
				for (InetAddress addr : addrs) {
					if (!addr.isLoopbackAddress()) {
						String sAddr = addr.getHostAddress().toUpperCase();
						//boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
						boolean isIPv4 = !(addr instanceof Inet6Address);
						if (useIPv4) {
							if (isIPv4) 
								return sAddr;
						} else {
							if (!isIPv4) {
								int delim = sAddr.indexOf('%'); // drop ip6 port suffix
								return delim<0 ? sAddr : sAddr.substring(0, delim);
							}
						}
					}
				}
			}
		} catch (Exception ex) { } // for now eat exceptions
		Log.e(TAG, "Unable to find local address");
		return "127.0.0.1";
	}


	public static String getBatVal(){
		Dictionary<?, ?> batProp = Utilities.getBatteryStatus();

		if((Boolean) batProp.get("charging") == true){

			int tempbatval = Integer.parseInt(String.valueOf(batProp.get("charge_percent")));
			BatVal = String.valueOf(tempbatval + 10);
		} 
		else{BatVal = String.valueOf(batProp.get("charge_percent"));};

		return BatVal;
	}


	public static JSONArray getFileNames(){
		JSONArray FilesList = new JSONArray();
		//		Log.d(TAG, "Files  " + "Path: " + path);
		File f = new File(Constants.path);        
		File file[] = f.listFiles();
		//		Log.d(TAG, "Files  " + "Size: "+ file.length);
		for (int i=0; i < file.length; i++)
		{
			FilesList.add(file[i].getName());
			//			Log.d(TAG, "Files" + "FileName:" + FilesList.get(i));
		}
		return FilesList;
	}


	public static enum Mode {
		ALPHA, ALPHANUMERIC, NUMERIC 
	}



	public static String generateRandomString(int length, Mode mode) throws Exception {

		StringBuffer buffer = new StringBuffer();
		String characters = "";

		switch(mode){

		case ALPHA:
			characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
			break;

		case ALPHANUMERIC:
			characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
			break;

		case NUMERIC:
			characters = "1234567890";
			break;
		}

		int charactersLength = characters.length();

		for (int i = 0; i < length; i++) {
			double index = Math.random() * charactersLength;
			buffer.append(characters.charAt((int) index));
		}
		return buffer.toString();
	}


	public static JSONArray getRoutingTable() {
		String output = null;
		String[] route = null;

		JSONArray kernel_table = new JSONArray();
		JSONObject table_values = new JSONObject();

		try {
			output = Shell.sudo("busybox route");
			if(output != null) {
				route = output.split("\\s+");

				// Parse out adapter names.
				for(int j = 12; j < route.length; j+=8) {

					table_values.put("Iface", route[j+7]);
					table_values.put("NetMask", route[j+2]);
					table_values.put("Gateway", route[j+1]);
					table_values.put("DestIP", route[j]);

					kernel_table.add(table_values);

				}

				Log.d(TAG, kernel_table.toString());

			}                        
		} catch (ShellException e) {
			Log.e(TAG, e.getMessage());
		}

		// Return null if there is no output returned.
		if(kernel_table != null) {
			return kernel_table;
		} else {
			return null;
		}
	}


	public static String getInterfaceIPAddress(String Iface){

		try {
			List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces) {
				List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
				for (InetAddress addr : addrs) {
					if (!addr.isLoopbackAddress()) {
						String sAddr = addr.getHostAddress().toUpperCase();
						boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
						if (isIPv4 && intf.getDisplayName().contains(Iface)) {
							return sAddr;
						}
					}
				}
			}
		} catch (Exception ex) {
			return null;
		}
		return null;
	}


	public static <K, V extends Comparable<V>> Map<K, V> sortBatvals(Map<K, V> unsortedMap) {

		SortedMap<K, V> sortedMap = new TreeMap<K, V>(new ValueComparer<K, V>(unsortedMap) );

		sortedMap.putAll(unsortedMap);

		return sortedMap;
	}


	public static Map<String, JSONArray> getTreeDistTable(Map<String, Integer> SortedIDBatVal){

		String[] idList = SortedIDBatVal.keySet().toArray(new String[0]);
		String pNodeID = null;
		JSONArray cNodesArray = null;
		childParentTable.clear();

		boolean limit_reached =  false;

		int nodes_limit = idList.length;
		int main_index = 0;
		int multiplier = 1;
		int nodesMultiple = -(ConfigData.relayNodes);
		int previous_index = 0;
		int parent_limit = 0;
		int nodes_counter = 0;

		TreeNode<String> root_node = new TreeNode<String>(Utilities.getDeviceID());
		Tree<String> nodes_tree = new Tree<String>();
		TreeNode<String> parent_node;

		List<TreeNode<String>> previous_nodes_list = new ArrayList<TreeNode<String>>();
		List<TreeNode<String>> temp_list = new ArrayList<TreeNode<String>>();

		previous_nodes_list.add(root_node);

		Map<String, JSONArray> dist_table = new HashMap<String, JSONArray>();

		while(limit_reached == false){

			int child_limit = ConfigData.relayNodes;

			if(multiplier == 1){
				parent_limit = 1;
				previous_index = 0;
			}

			else{
				parent_limit = ConfigData.relayNodes * (previous_index + 1);
				previous_index = parent_limit;
			}

			//			Log.d(DTAG, "parent_limit: " + parent_limit);
			//			Log.d(DTAG, "previous_index: " + previous_index);

			for(int i = main_index; i < parent_limit; i++){

				if(limit_reached == false){

					//					Log.d(DTAG, "parent_node index: " + (i - main_index));
					//					Log.d(DTAG, "previous nodes list size: " + previous_nodes_list.size());

					parent_node = previous_nodes_list.get(i - main_index);
					pNodeID = parent_node.getData();
					cNodesArray = new JSONArray();

					for(int j = 0; j < child_limit; j++){

						if(nodes_counter < nodes_limit){

							//							Log.d(DTAG, "ids from list: " + idList[j + nodesMultiple + ConfigData.relayNodes]);

							TreeNode<String> child_node = new TreeNode<String>(idList[j + nodesMultiple + ConfigData.relayNodes]);
							parent_node.addChild(child_node);
							temp_list.add(child_node);
							String cNodeID = child_node.getData();
							cNodesArray.add(cNodeID);
							childParentTable.put(cNodeID, pNodeID);
							nodes_counter++;

							//							Log.d(DTAG, "Counter: " + nodes_counter);
						}

						else{
							limit_reached = true;
							Log.d(TAG, "nodes limit has reached");
							break;
						}
					}

					nodesMultiple = nodesMultiple + ConfigData.relayNodes;
					//					Log.d(DTAG, "nodesMultiple: " + nodesMultiple);



					if(!cNodesArray.isEmpty() && cNodesArray!=null){
						dist_table.put(pNodeID, cNodesArray);
						Log.d(TAG, "pNodeID: " + pNodeID + " cNodesArray" + cNodesArray.toString());
					}
					//					Log.d(DTAG, "Parent Node " + pNodeID + ": " + "Children nodes: " + cNodesArray.toString());
				}

				else break;
			}

			previous_nodes_list = null;
			previous_nodes_list = new ArrayList<TreeNode<String>>();
			previous_nodes_list = temp_list;
			temp_list = null;
			temp_list = new ArrayList<TreeNode<String>>();
			main_index = previous_index;
			multiplier = multiplier * ConfigData.relayNodes;
			//			Log.d(DTAG, "main_index: " + main_index);
			//			Log.d(DTAG, "multiplier: " + multiplier);
		}

		nodes_tree.setRoot(root_node);
		Log.d(TAG, "number of nodes in the tree: " + nodes_tree.getNumberOfNodes());

		//		for(String key : dist_table.keySet()){
		//			
		//			Log.d(DTAG, "Parent Node " + (String) key + ": " + "Children nodes: " + dist_table.get(key).toString());
		//			
		//		}

		return dist_table;
	}


	public static String getParentNode(String node){

		String parent = null;
		
		parent = childParentTable.get(node);

		return parent;
	}


	private static class ValueComparer<K, V extends Comparable<V>> implements Comparator<K> {

		private final Map<K, V> map;

		public ValueComparer(Map<K, V> map) {
			super();
			this.map = map;
		}

		public int compare(K key1, K key2) {
			V value1 = this.map.get(key1);
			V value2 = this.map.get(key2);
			int c = value2.compareTo(value1);
			if (c != 0) {
				return c;
			}
			Integer hashCode1 = key1.hashCode();
			Integer hashCode2 = key2.hashCode();
			return hashCode2.compareTo(hashCode1);
		}
	}


	public static void generateTestingResults (String fileName, String data){

		File filePath = new File(Constants.results_path);
		if (!filePath.exists()) {
			filePath.mkdirs();
		}
		File dataFile = new File(Constants.results_path, fileName);

		// If file does not exist, then create a new file
		if(!dataFile.exists()){
			try {
				dataFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {

			FileWriter fileWriter = new FileWriter(dataFile,true);
			BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
			bufferWriter.write(data);
			bufferWriter.write("\r\n");
			bufferWriter.close();

		} 
		catch (FileNotFoundException e) {
			Log.e(TAG, "File not found: " + e.toString());
			e.printStackTrace();
		}

		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, "Failed to write: " + e.toString());
		}
	}


} // End Class
