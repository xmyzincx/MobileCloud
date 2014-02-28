package com.cwc.mobilecloud;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;

import com.cwc.mobilecloud.utilities.Utilities;

import android.util.Log;

public class MulticastConnection {
	private static final String TAG = "MulticastConnection";
	private MulticastSocket dSocket = null;
	private MulticastSocket cSocket = null;

	private boolean init = false;

	private Node group = new Node();

	private String adapterName = "wlan0";

	MulticastConnection(){
		init = false;
	}
	MulticastConnection(Node grp)
	{		
		group = grp;
		try{
			if (group.getPort()!=0 && (!group.getIP().equals("0.0.0.0") && (!group.getIP().equals("")))){

				NetworkInterface Iface = NetworkInterface.getByName(adapterName);
				dSocket = new MulticastSocket(group.getPort());

				dSocket.joinGroup(new InetSocketAddress(group.getPort()), Iface);
			}
			else{
				throw new Exception("Invalid Multicast connection Parameters");
			}

		}
		catch (Exception e)
		{
			Log.e(TAG, "1- Error in binding Multicast Socket to group: " + group.getIP() + " and port: " + String.valueOf(group.getPort()) );
			e.printStackTrace();
			init = false;
			return;
		}
		init = true;
	}

	MulticastConnection(String group_add, int port)
	{

		group.setIP(group_add);
		group.setPort(port);

		String IfaceIPAddr = Utilities.getInterfaceIPAddress(adapterName);

		try{
			Log.d(TAG, "McastIP: " + group.getIP() + " and port: " + String.valueOf(group.getPort()) );

			if (port!=0 && (!group.getIP().equals("0.0.0.0") && (!group.getIP().equals("")))){


				NetworkInterface Iface = NetworkInterface.getByName(adapterName);

				//				InetAddress localAddr = InetAddress.getByName(IfaceIPAddr);
				//
				//				SocketAddress SockAddr = new InetSocketAddress(localAddr, port);
				//
				//				dSocket = new MulticastSocket(SockAddr);

				dSocket = new MulticastSocket(group.getPort());

				dSocket.setNetworkInterface(Iface);

				InetAddress groupAddr = InetAddress.getByName(group.getIP());

				SocketAddress groupSockAddr = new InetSocketAddress(groupAddr, port);

				dSocket.joinGroup(groupSockAddr, Iface);



				//				dSocket = new MulticastSocket(groupSockAddress);

				//				dSocket.joinGroup(groupSockAddress, Iface);

				//				dSocket = new MulticastSocket(group.getPort());

				//				dSocket.setNetworkInterface(Iface);

//				dSocket.joinGroup(InetAddress.getByName(group.getIP()));

				Log.d(TAG, "Socket done");
			}
			else{
				throw new Exception("Invalid Multicast connection Parameters");
			}
		}
		catch (Exception e)
		{
			Log.e(TAG, "2- Error in binding Multicast Socket to group: " + group.getIP() + " and port: " + String.valueOf(group.getPort()) );
			e.printStackTrace();
			init = false;
			return;
		}
		Log.e(TAG, "MulticastSocket Bound to = " + dSocket.getLocalAddress().getHostAddress() + ": " + String.valueOf(dSocket.getLocalPort()) );

		Log.e(TAG, "MulticastSocket Bound to = " + group.getIP() + ": " + String.valueOf(dSocket.getLocalPort()) );

		init = true;
	}

	public String getGroup(){
		return group.getIP();
	}
	public int getPort(){
		return group.getPort();
	}


	public void close(){
		if (dSocket.isClosed() == false)
			dSocket.close();
	}

	public MulticastSocket getSocket(){
		return dSocket;
	}

	public synchronized boolean send(String message, String dGroup, int dPort)
	{
		if (init == false){
			Log.e(TAG, "MulticastSocket not initialized for sending data" );
			return false;
		}

		if ((dGroup == "0.0.0.0") || (dGroup == ""))
		{
			Log.e(TAG, "Destination Group not Set. Aborting MulticastSocket Send Operation" );
			return false;
		}

		if (dPort == 0)
		{
			Log.e(TAG, "Destination Port not Set. Aborting MulticastSocket Send Operation" );
			return false;
		}

		byte[] buff = new byte[message.length()];
		buff = message.getBytes();
		try{
			InetAddress dstIP = InetAddress.getByName(dGroup);
			DatagramPacket sendPacket = new DatagramPacket(buff, buff.length, dstIP, dPort);
			int ttl = 1;

			dSocket.setTimeToLive(ttl);
			NetworkInterface Iface = NetworkInterface.getByName(adapterName);
			dSocket.setNetworkInterface(Iface);


			Log.d(TAG, "Sending from: " + dSocket.getLocalAddress().getHostAddress() + " to : "  + dGroup + " on port: "+ dPort);

			dSocket.send(sendPacket);	
			//			Log.d(TAG, "Message sent");

		}
		catch(Exception e){
			Log.e(TAG, "Error in Sending MulticastSocket data to " + dGroup);
			e.printStackTrace();
			return false;
		}		
		return true;
	}

	public synchronized boolean sendBytes(byte[] message, String dGroup, int dPort)
	{
		if (init == false){
			Log.e(TAG, "MulticastSocket not initialized for sending data" );
			return false;
		}

		if ((dGroup == "0.0.0.0") || (dGroup == ""))
		{
			Log.e(TAG, "Destination Group not Set. Aborting MulticastSocket Send Operation" );
			return false;
		}

		if (dPort == 0)
		{
			Log.e(TAG, "Destination Port not Set. Aborting MulticastSocket Send Operation" );
			return false;
		}

		byte[] buff = message;

		try{
			InetAddress dstIP = InetAddress.getByName(dGroup);
			DatagramPacket sendPacket = new DatagramPacket(buff, buff.length, dstIP, dPort);
			int ttl = 1;
			//Log.d(TAG, "Sending to " + src.getIP() + ":"+ src.getPort() + " " + dIP + ":"+ dPort);
			dSocket.setTimeToLive(ttl);
			NetworkInterface Iface = NetworkInterface.getByName(adapterName);
			dSocket.setNetworkInterface(Iface);
			dSocket.send(sendPacket);			
		}
		catch(Exception e){
			Log.e(TAG, "Error in Sending MulticastSocket data to " + dGroup);
			e.printStackTrace();
			return false;
		}		
		return true;
	}



	@Override
	protected void finalize() throws Throwable {
		Log.d(TAG, "Deleting MulticastConnection " + group.getIP() + ":"+ group.getPort());
		try{        	
			this.close();
		}catch(Throwable t){
			throw t;
		}finally{            
			super.finalize();
		}

	}
}
