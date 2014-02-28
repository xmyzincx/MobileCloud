package com.cwc.mobilecloud;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import android.net.wifi.WifiManager;
import android.util.Log;



public final class ConfigData {
	private static final String TAG = "ConfigData";
	private static String APP_PATH = null;
	private static String tmpDelFileName = null;
	//private static ReadWriteLock ConfigLock = new ReentrantReadWriteLock();
	
	private static int controlPort = 36963;			// UDP Port for all control communication 
	private static int dataPort    = 36964;			// UDP port for data transfer
	
	//private static String bsIP     = "192.168.1.90";	// IP for Base Station
	//private static int bsPort      = 36965;	            // Port for Base Station
	private static String McIP     = "224.2.3.4";	// IP for control Multicasting
	private static int McPort      = 1234;	 
	
	private static String DataMcIP     = "224.2.3.6";	// IP for data Multicasting
	private static int DataMcPort      = 4321;	
	
	//private static int McPort      = 36965;	            // Port for Base Station
	
	private static String facility = Facility.CN;	// Current facility of this node
	
	private static Node CL = null;					// Current CL of this node

	private static Map<String, Node> peerMap = new LinkedHashMap();
	private static ReadWriteLock peerMapLock = new ReentrantReadWriteLock();
	
	//public static UDPConnection CtrlSock;			// COntrol Socket 
	public static MulticastConnection CtrlSock;			// COntrol Socket 
	public static WifiManager Wifi;
	
	public static File FilesDir;
	public static NetLog NLog;						// Logging Server
	
	public class AppConstant{
		public static final int CN_INFO_TIMEOUT = 10;		// seconds
		public static final int PEER_EXPIRE_AGE = 5;		// 5 * CN_INFO_TIMEOUT
		public static final int CL_SELEC_TIMEOUT = 30;		//seconds
		
	}
	
	public class Severity{
		public static final String VERBOSE = "verbose";
		public static final String DEBUG = "debug";
		public static final String INFO = "info";
		public static final String WARN = "warning";
		public static final String CRITICAL = "error";
		public static final String UNDEFINED = "undefined";
	}
	
	public class Facility{
		public static final String CL = "CL";
		public static final String CN = "CN";
		public static final String UNDEFINED = "Undef";
		
	}
	
	
	
	
	// Getter functions
	public synchronized static int getCtrlPort() { return controlPort;}
	public synchronized static int getDataPort() { return dataPort;}
	
	public synchronized static String getFacility() { return facility;}
	public synchronized static Node getCL() { return CL.clone();}
	public synchronized static String getMcIP() { return McIP;}
	public synchronized static int getMcPort() { return McPort;}
	
	public synchronized static String getDataMcIP() { return DataMcIP;}
	public synchronized static int getDataMcPort() { return DataMcPort;}
	//public synchronized static String getBSAddress() { return bsIP;}
	//public synchronized static int getBSPort() { return bsPort;}
	public synchronized static String getAppPath() { return APP_PATH;}
	public synchronized static String getdelFile() {return tmpDelFileName;}
	
	// Setter Functions
	public synchronized static void setCtrlPort(int port) {controlPort = port;}
	public synchronized static void setDataPort(int port) {dataPort = port;}
	public synchronized static void setFacility(String s) { facility = s;}
	public synchronized static void setCL(Node n) { CL = n;}
	public synchronized static void setMcAddress(String s) { McIP = s;}
	public synchronized static void setMcPort(int port) { McPort = port;}
	//public synchronized static void setBSAddress(String s) { bsIP = s;}
	//public synchronized static void setBSPort(int port) { bsPort = port;}
	public synchronized static void setAppPath(String path) { APP_PATH = path;}
	public synchronized static void setdelFile(String file) { tmpDelFileName = file;}
	
	
	public synchronized static Node getPeer(String id){
		
		Node n = new Node();
		Lock l = peerMapLock.readLock();
    	l.lock();
    	try {
    		n = peerMap.get(id);
    		if (n != null)
    			n = n.clone();
    	}
    	catch(Exception e){
    		Log.v (TAG, "Error reading value : " + id );
    	}
    	finally {
  		   l.unlock();
  		}
		return n;		
	}
	
	public synchronized static void putPeer(String id, Node n){
		Lock l = peerMapLock.writeLock();
    	l.lock();
    	try {
    		peerMap.put(id,  n.clone() );
    	}
    	finally {
  		   l.unlock();
  		}
	}
	
	public synchronized static void updatePeersAge(){
		Lock l = peerMapLock.writeLock();
    	l.lock();
    	try {
    		List<String> delList = new ArrayList<String>();
    		for (Map.Entry<String, Node> peer : peerMap.entrySet()) {
    			int current_age = peer.getValue().age;
    			if (current_age == 0){
    				delList.add(peer.getKey());
    				  				
    				if (peer.getValue().ID == (String) CLSelecThread.CurrentCLID){
    					
    					CLSelecThread.ForcedCLSelecTrigger();
    				}
    				
    			}
    			else{
    				//Log.v (TAG, "Peer Age decreased of ID " +  peer.getKey());
    				peerMap.get(peer.getKey()).age =  current_age - 1;
    			}
    		}
    		
    		for (String id : delList) {
    			peerMap.remove(id);
    			Log.v (TAG, "Removing " + id + " from peerMap" );
    		}
    	}
    	finally {
  		   l.unlock();
  		}		
	}
	
	
	public synchronized static  Set<String> getPeerIds(){
		
		Set<String> keys;
		Lock l = peerMapLock.readLock();
    	l.lock();
    	try {
    		keys = peerMap.keySet();    		
    	}
    	finally {
  		   l.unlock();
  		}
		return keys;		
	}	
	
}
