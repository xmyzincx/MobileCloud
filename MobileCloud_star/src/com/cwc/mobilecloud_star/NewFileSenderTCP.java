package com.cwc.mobilecloud_star;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class NewFileSenderTCP extends Thread {
	private static final String TAG = "FileSenderTCP";
	private Context context;
	private Handler handler;
	private String ip = "";
	private int port;
	private int chunkSize;
	private File filePath;
	
	
	
	public NewFileSenderTCP(Context ctx, Handler h, String mIp, int mPort, File fPath, int chunk_size){
		ip = mIp;
		port = mPort;
		filePath = fPath;
		context = ctx;
		handler = h;
		chunkSize = chunk_size;
	}
	
	public void run(){
		Socket clientSocket = null;
		FileInputStream in = null;
		OutputStream os = null;
		InputStream is = null;
		byte[] chunk = new byte[chunkSize];
		try {
			try{
				in = new FileInputStream(filePath);
			}catch (FileNotFoundException e1) {
				Log.d(TAG, "Error Opening file for writing at CL");
				e1.printStackTrace();
				return;
			}  
			
			clientSocket = new Socket();
			clientSocket.connect(new InetSocketAddress(ip, port), 5000);
			os = clientSocket.getOutputStream();
			
			int nBytes = 0;
			int tBytes = 0;
			Log.d(TAG, "Starting Transfer : " + ip + ":" + String.valueOf(port));
			while ((nBytes = in.read(chunk)) != -1){
				os.write(chunk, 0, nBytes);
				tBytes += nBytes;
			}
			Log.d(TAG, "Total Bytes Transferred = " + String.valueOf(tBytes));
			
		} catch (UnknownHostException e) {
			Log.e(TAG, "Error in Sending file to CN : " + ip + ":" + String.valueOf(port));
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG, "Error in Sending file to CN : " + ip + ":" + String.valueOf(port));
			e.printStackTrace();
		}
		finally{
			try {
				in.close();
				os.close();
				clientSocket.close();
				
			} catch (IOException e) {
				Log.e(TAG, "Error in closing while Sending file to CN : " + ip + ":" + String.valueOf(port));
				e.printStackTrace();
			}
			
		}
		
	}	
}
