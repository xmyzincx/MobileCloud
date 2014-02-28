package com.cwc.mobilecloud;

import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;   

import android.net.DhcpInfo;
import android.util.Log;

public class UDPConnection {
	private static final String TAG = "UDPConnection";
	private DatagramSocket dSocket = null;

	private boolean init = false;
	private Node src = new Node();
	private Node dst = new Node();
	private boolean broadcast = false;
	private String multicastIP = ConfigData.getMcIP();
	private int multicastPort = ConfigData.getMcPort();

	UDPConnection(){
		init = false;
	}
	UDPConnection(Node iSrc, Node iDst, boolean broadcast)
	{
		src = iSrc;
		dst = iDst;
		this.broadcast = broadcast;		
		try{
			if (iSrc.getPort()==0)
				dSocket = new DatagramSocket(iSrc.getPort());
			else
				dSocket = new DatagramSocket();

			dSocket.setBroadcast(broadcast);
		}
		catch (Exception e)
		{
			Log.e(TAG, "Error in binding UDP Socket to port " + String.valueOf(iSrc.getPort()) );
			init = false;
			return;
		}
		init = true;
	}

	UDPConnection(String srcIP, String dstIP, int srcPort, int dstPort, boolean broadcast)
	{
		src.setIP(srcIP);
		src.setPort(srcPort);

		dst.setIP(dstIP);
		dst.setPort(dstPort);
		this.broadcast = broadcast;
		try{
			if (srcPort!=0)
				dSocket = new DatagramSocket(srcPort);
			else
				dSocket = new DatagramSocket();

			dSocket.setBroadcast(broadcast);
		}
		catch (Exception e)
		{
			Log.e(TAG, "Error in binding UDP Socket to port " + String.valueOf(srcPort) );
			init = false;
			return;
		}
		Log.e(TAG, "Socket Bound to src port = " + String.valueOf(dSocket.getLocalPort()) );

		init = true;
	}

	public void setSrcIP(String ip){
		src.setIP(ip);
	}
	public void setDstIP(String ip){
		dst.setIP(ip);
	}
	public void setSrcPort(int port){
		src.setPort(port);
	}
	public void setDstPort(int port){
		dst.setPort(port);
	}
	public void setBroadcast(boolean bc){
		broadcast = bc;
	}
	public String getSrcIP(){
		return src.getIP();
	}
	public String getDstIP(){
		return dst.getIP();
	}
	public int getSrcPort(){
		return src.getPort();
	}
	public int getDstPort(){
		return dst.getPort();
	}
	public boolean getBroadcast(){
		return broadcast;
	}
	//	public String getMulticast(){
	//		return multicastIP;
	//	}

	public boolean bind(int port){
		try{
			if (port!=0)
				dSocket = new DatagramSocket(port);
			else
				dSocket = new DatagramSocket();
		}
		catch (Exception e)
		{
			Log.e(TAG, "Error in binding UDP Socket to port " + String.valueOf(port) );
			init = false;
			return false;
		}

		init = true;
		return true;
	}

	public boolean bind(){
		return bind(0);
	}

	public void close(){
		if (dSocket.isClosed() == false)
			dSocket.close();
	}

	public DatagramSocket getSocket(){
		return dSocket;
	}


	static String getLocalIP_old(){
		InetAddress[] localaddr;

		try {
			InetAddress ret = InetAddress.getByName("127.1.0.1");

			localaddr = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());
			for(int i = 0;i<localaddr.length;i++){
				Log.d(TAG, "IP = " + localaddr[i].getHostAddress());
				if (!(localaddr[i] instanceof Inet6Address)){
					if (!localaddr[i].isLoopbackAddress()) {
						ret = localaddr[i];
					}
				}
			}
			return ret.getHostAddress();
		} catch (UnknownHostException e) {
			Log.e(TAG, "Unable to find local address");
			return "0.0.0.0";

		}
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


	private static int packIP(byte[] bytes) {
		int val2 = ByteBuffer.wrap(bytes).getInt();
		/*
		int val = 0;
	    for (int i = 0; i < bytes.length; i++) {
	        val <<= 8;
	        Log.d(TAG, Integer.toBinaryString(val));
	        Byte b = new Byte(bytes[i]);
	        val |= b.intValue() ;
	        Log.d(TAG, Integer.toBinaryString(b.intValue()));
	    }*/
		//Log.d(TAG, "--" + Integer.toHexString(val2));
		return val2;
	}

	private static byte[] unpackIP(int bytes) {
		return new byte[] {
				(byte)((bytes >>> 24) & 0xff),
				(byte)((bytes >>> 16) & 0xff),
				(byte)((bytes >>>  8) & 0xff),
				(byte)((bytes       ) & 0xff)
		};
	}

	public static String getBroadcastAddr(String strIP, int prefix){
		try{
			String bcAddr = strIP;
			Log.d(TAG, "Unable to get broadcast address" + strIP);
			int intIP = packIP(InetAddress.getByName(bcAddr).getAddress());
			int mask = 0x7fffffff;
			int intBcAddr = intIP | (mask >>> (prefix-1));
			bcAddr = InetAddress.getByAddress(unpackIP((int)intBcAddr)).getHostAddress();
			return bcAddr;
		}
		catch(Exception e)
		{
			Log.e(TAG, "Unable to get broadcast address");
			return strIP;

		}
	}

	public static int getNetMask(){
		try{
			DhcpInfo d;
			d=ConfigData.Wifi.getDhcpInfo();
			return Integer.bitCount(d.netmask);
		}
		catch(Exception e){
			return 32;			
		}

	}

	public synchronized boolean send(String message)
	{
		if (init == false){
			Log.e(TAG, "Socket not initialized for sending data" );
			return false;
		}
		if ((dst.getIP() == "0.0.0.0") || (dst.getIP() == ""))
		{
			Log.e(TAG, "Destination IP not Set. Aborting Socket Send Operation" );
			return false;
		}


		if (dst.getPort() == 0)
		{
			Log.e(TAG, "Destination Port not Set. Aborting Socket Send Operation" );
			return false;
		}

		byte[] buff = new byte[message.length()];
		buff = message.getBytes();
		try{
			InetAddress dstIP = InetAddress.getByName(dst.getIP());
			DatagramPacket sendPacket = new DatagramPacket(buff, buff.length, dstIP, dst.getPort());
			dSocket.send(sendPacket);			
		}
		catch(Exception e){
			Log.e(TAG, "Error in Sending data to " + dst.getIP());
			return false;
		}

		return true;
	}


	public synchronized boolean send(String message, String dIP, int dPort)
	{
		if (init == false){
			Log.e(TAG, "Socket not initialized for sending data" );
			return false;
		}

		if ((dIP == "0.0.0.0") || (dIP == ""))
		{
			Log.e(TAG, "Destination IP not Set. Aborting Socket Send Operation" );
			return false;
		}

		if (dPort == 0)
		{
			Log.e(TAG, "Destination Port not Set. Aborting Socket Send Operation" );
			return false;
		}

		byte[] buff = new byte[message.length()];
		buff = message.getBytes();
		try{
			InetAddress dstIP = InetAddress.getByName(dIP);
			DatagramPacket sendPacket = new DatagramPacket(buff, buff.length, dstIP, dPort);
			//Log.d(TAG, "Sending to " + src.getIP() + ":"+ src.getPort() + " " + dIP + ":"+ dPort);
			dSocket.send(sendPacket);			
		}
		catch(Exception e){
			Log.e(TAG, "Error in Sending data to " + dst.getIP());
			return false;
		}

		return true;
	}

	public synchronized boolean sendBytes(byte[] message, String dIP, int dPort)
	{
		if (init == false){
			Log.e(TAG, "Socket not initialized for sending data" );
			return false;
		}

		if ((dIP == "0.0.0.0") || (dIP == ""))
		{
			Log.e(TAG, "Destination IP not Set. Aborting Socket Send Operation" );
			return false;
		}

		if (dPort == 0)
		{
			Log.e(TAG, "Destination Port not Set. Aborting Socket Send Operation" );
			return false;
		}

		byte[] buff = message;

		try{
			InetAddress dstIP = InetAddress.getByName(dIP);
			DatagramPacket sendPacket = new DatagramPacket(buff, buff.length, dstIP, dPort);
			//Log.d(TAG, "Sending to " + src.getIP() + ":"+ src.getPort() + " " + dIP + ":"+ dPort);
			dSocket.send(sendPacket);			
		}
		catch(Exception e){
			Log.e(TAG, "Error in Sending data to " + dst.getIP());
			return false;
		}

		return true;
	}

	@Override
	protected void finalize() throws Throwable {
		Log.d(TAG, "Deleting UDPConnection " + src.getIP() + ":"+ src.getPort() + " " + dst.getIP() + ":"+ dst.getPort());
		try{

			this.close();
		}catch(Throwable t){
			throw t;
		}finally{

			super.finalize();
		}

	}

} // End Class
