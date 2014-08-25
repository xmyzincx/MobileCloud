package com.example.androidhive;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import com.example.androidhive.Constants;
import com.example.androidhive.Utilities;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class AndroidDownloadFileByProgressBarActivity extends Activity {

	// button to show progress dialog
	Button btnShowProgress;
	
	private Context ctx;
	
	// Progress Dialog
	private ProgressDialog pDialog;
	ImageView my_image;
	// Progress dialog type (0 - for Horizontal progress bar)
	public static final int progress_bar_type = 0; 
	
	// File url to download
	private static String file_url = "http://www.ee.oulu.fi/~amoiz/downloads/5mb_video.mp4";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		ctx = this;

		// show progress bar button
		btnShowProgress = (Button) findViewById(R.id.btnProgressBar);
		// Image view to show image after downloading
		my_image = (ImageView) findViewById(R.id.my_image);
		/**
		 * Show Progress bar click event
		 * */
		btnShowProgress.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// starting new Async Task
				new DownloadFileFromURL().execute(file_url);
			}
		});
		
		Utilities.phoneStateSetup(ctx);
	}

	/**
	 * Showing Dialog
	 * */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case progress_bar_type:
			pDialog = new ProgressDialog(this);
			pDialog.setMessage("Downloading file. Please wait...");
			pDialog.setIndeterminate(false);
			pDialog.setMax(100);
			pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			pDialog.setCancelable(true);
			pDialog.show();
			return pDialog;
		default:
			return null;
		}
	}

	/**
	 * Background Async Task to download file
	 * */
	class DownloadFileFromURL extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread
		 * Show Progress Bar Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showDialog(progress_bar_type);
		}

		/**
		 * Downloading file in background thread
		 * */
		@Override
		protected String doInBackground(String... f_url) {

			int count;
	        try {
	            URL url = new URL(f_url[0]);
	            URLConnection conection = url.openConnection();
	            conection.connect();
	            // getting file length
	            int lenghtOfFile = conection.getContentLength();

	            // input stream to read file - with 8k buffer
	            InputStream input = new BufferedInputStream(url.openStream(), 8192);
	            
	            // Output stream to write file
	            OutputStream output = new FileOutputStream("/sdcard/5mb_video.mp4");

	            byte data[] = new byte[1024];

	            long total = 0;
	            
	         // Node type for test results
				Utilities.generateTestingResults(Constants.test_result_file, "Node type                     : Cloud leader");

				// Battery level of the node
				Utilities.generateTestingResults(Constants.test_result_file, "Battery level                 : " + Utilities.getBatVal().toString());
				
				// getting Wifi RSSI for test results
				Utilities.generateTestingResults(Constants.test_result_file, "Wifi RSSI                     : none");
				
				// getting Mobile Network RSSi for test results
				Utilities.generateTestingResults(Constants.test_result_file, "Cellular RSSI                 : " + Utilities.getCellularRSSI() + " dBm");
				
				// file size for test results
				Utilities.generateTestingResults(Constants.test_result_file, "File size                     : " + lenghtOfFile/1048576 + " MB");
				
				// content distribution topology
				Utilities.generateTestingResults(Constants.test_result_file, "Content distribution topology : none ");
				
				// timestamping for starting main content downloading
				Utilities.timeStamp("Starting to download content ");
				

	            while ((count = input.read(data)) != -1) {
	                total += count;
	                // publishing the progress....
	                // After this onProgressUpdate will be called
	                publishProgress(""+(int)((total*100)/lenghtOfFile));
	                
	                // writing data to file
	                output.write(data, 0, count);
	            }
	            
	         // timestamping for ending main content downloading
				Utilities.timeStamp("Content downloading ended    ");


				Utilities.generateTestingResults(Constants.test_result_file, "-----------END-----------");

	            // flushing output
	            output.flush();
	            
	            // closing streams
	            output.close();
	            input.close();
	            
	        } catch (Exception e) {
	        	Log.e("Error: ", e.getMessage());
	        }
	        
	        return null;
		}
		
		/**
		 * Updating progress bar
		 * */
		protected void onProgressUpdate(String... progress) {
			// setting progress percentage
            pDialog.setProgress(Integer.parseInt(progress[0]));
       }

		/**
		 * After completing background task
		 * Dismiss the progress dialog
		 * **/
		@Override
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after the file was downloaded
			dismissDialog(progress_bar_type);
			
			// Displaying downloaded image into image view
			// Reading image path from sdcard
			String imagePath = Environment.getExternalStorageDirectory().toString() + "/5mb_video.mp4";
			// setting downloaded into image view
			my_image.setImageDrawable(Drawable.createFromPath(imagePath));
		}

	}
	
	@Override
	protected void onStop(){
		super.onStop();
		Utilities.stopPhoneStateListener();
	}

	@Override
	protected void onDestroy(){
		super.onDestroy();
		Utilities.stopPhoneStateListener();
	}
}