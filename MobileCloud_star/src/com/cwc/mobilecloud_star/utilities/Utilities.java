package com.cwc.mobilecloud_star.utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.http.conn.util.InetAddressUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.cwc.mobilecloud_star.utilities.Shell.ShellException;

public final class Utilities {

	private static final String TAG = "Utilities";

	public static boolean batteryDataOk = false;
	private static boolean charging = false;
	private static boolean usbCharge = false;
	private static boolean acCharge = false;
	private static int level_percent = 0;
	private static String BatVal;

	private static ReadWriteLock batteryLock = new ReentrantReadWriteLock();

	private static ReadWriteLock rssiLock = new ReentrantReadWriteLock();

	private static PhoneStateListener myPhoneStateListener;

	private static TelephonyManager tm;

	private static String data2Write;

	private static Calendar calendar;

	private static int RSSI;

	private static SimpleDateFormat sdf = new SimpleDateFormat("dd:MM:yyyy HH:mm:ss");

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


	public static void timeStamp(String ref){

		calendar = Calendar.getInstance();
		int milisec = calendar.get(Calendar.MILLISECOND);
		String timestamp = ref + " : " + sdf.format(calendar.getTime()) + "." + milisec;
		//		Log.d(TAG, "Time Stamp: " + timestamp);
		Utilities.generateTestingResults("COIN_RESULT.txt", timestamp);

	}

	public static void phoneStateSetup(Context context){

		try {
			Log.d(TAG, "1");

			tm  = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

			Log.d(TAG, "2");

			myPhoneStateListener = new PhoneStateListener() {

				public void onSignalStrengthsChanged(SignalStrength signalStrength){
					Log.d(TAG, "3");
					Lock l = rssiLock.writeLock();
					Log.d(TAG, "4");
					l.lock();
					Log.d(TAG, "5");
					try {
						Log.d(TAG, "6");
						int rssi_gsm = -113 + (2 * signalStrength.getGsmSignalStrength());
						Log.d(TAG, "RSSI GSM: " + rssi_gsm);
						RSSI = rssi_gsm;
					}
					finally{
						l.unlock();
					}

					//				data2Write = "RSSI: " + rssi_gsm;
					//				Utilities.generateTestingResults("COIN_RESULT.txt", data2Write);
					//				int rssi_cdma = signalStrength.getCdmaDbm();
					//				Log.d(TAG, "RSSI CDMA: " + rssi_cdma);
					//				int rssi_evdo = signalStrength.getEvdoDbm();
				}
			};

			try{
				tm.listen(myPhoneStateListener, PhoneStateListener.LISTEN_CELL_LOCATION
						| PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
						);
			}catch(Exception e){

			}

		} catch (Exception e) {
			// TODO: handle exception
			Log.d(TAG, "exception: " + e);
		}
	}


	public static int getCellularRSSI(){
		Lock l = rssiLock.readLock();
		l.lock();
		try {
			return RSSI;
		} finally {
			l.unlock();
		}
	}

	public static void stopPhoneStateListener(){

		try{
			if(myPhoneStateListener != null){tm.listen(myPhoneStateListener, PhoneStateListener.LISTEN_NONE);}
		}catch(Exception e){
			e.printStackTrace();
		}	
	}


	public static int getWifiRSSI(Context context){
		int wifi_rssi = 0;
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		wifi_rssi = wifi.getConnectionInfo().getRssi();

		return wifi_rssi;
	}


} // End Class
