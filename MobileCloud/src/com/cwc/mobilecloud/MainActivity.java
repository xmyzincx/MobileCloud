package com.cwc.mobilecloud;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;


import com.cwc.mobilecloud.ConfigData;
import com.cwc.mobilecloud.ControllerService;
import com.cwc.mobilecloud.MainActivity.AsyncAppInit;
import com.cwc.mobilecloud.adapter.NetworkFiles;
import com.cwc.mobilecloud.adapter.TabsPagerAdapter;
import com.cwc.mobilecloud.utilities.Constants;
import com.cwc.mobilecloud.R;
import com.cwc.mobilecloud.adapter.NetworkFiles;
//import com.SelfCloud.LazyAdapter;
//import com.SelfCloud.MainActivity;
//import com.SelfCloud.TextProgressBar;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener{

	private static final String TAG = "MainActivity";

	static final String KEY_FILENAME = "name";
	static final String KEY_SIZE = "size";
	static final String KEY_DATE = "downloaded_on";
	static final String KEY_TS = "ts";

	int count = 0;


	private static Thread rxThread;
	private static Timer CNINFOTimer;
	private Context ctx;
	private ControllerService cs;

	private String TagOfNetworkFiles;

	private Handler CSHandler;

	//	public FragmentCommunicator fragmentCommunicator;

	FragmentManager fragmentManager = getSupportFragmentManager();

	// Drawable items


	//	private Button roleButton;
	//private TextView onlineStatusText;
	//private TextView roleText;
	//private LinearLayout onlineStatusTextContainer;
	//private LinearLayout roleTextContainer;

	//	private TextProgressBar progressBar;

	//	private Button strmButton;

	private String bat_prcnt = null;

	// Data Items

	private ArrayList<String> logsdataarray = new ArrayList<String>();


	// Adapters
	//StableArrayAdapter logAdapter;

	// Handlers for messages from worker threads

	Handler handler = new Handler() {
		@Override

		public void handleMessage(Message msg) {

			NetworkFiles networkFileFrag = (NetworkFiles) getSupportFragmentManager().findFragmentByTag(TagOfNetworkFiles);

			Bundle bundle = msg.getData();
			String mtype = bundle.getString("mtype");
			//Log.d(TAG, "mType: "  + mtype);
			if (mtype == null) return;	// Discard message if "mtype" tag not found in message


			if (mtype.equals(Constants.bat_info)){

				bat_prcnt = bundle.getString("BatLevel");
				invalidateOptionsMenu();
				mSectionsPagerAdapter.notifyDataSetChanged();

			}

			if (mtype.equals(Constants.mtype_fileStart)){
				long totalBytes = bundle.getLong("TOTAL_BYTES");

				networkFileFrag.setProgressBar(100, 0);


			}else if (mtype.equals(Constants.mtype_fileEnd)){

				networkFileFrag.setProgressBar(100, 0);
				mSectionsPagerAdapter.notifyDataSetChanged();

				// Open Image file
				String fileName = bundle.getString("path");

				Toast.makeText(MainActivity.this, fileName + " Downloaded Successfully", Toast.LENGTH_SHORT).show();


			}else if (mtype.equals(Constants.mtype_fileProgres)){
				int currBytes = bundle.getInt("CURRENT_BYTES");
				Log.v(TAG, "Current Progress = " + String.valueOf(currBytes));

				networkFileFrag.setProgressBar(100, 0);
				networkFileFrag.setProgressBar(100, currBytes);


			}else if (mtype.equals(Constants.mtype_toastMsg)){
				String tmsg = bundle.getString("msg");
				Toast.makeText(MainActivity.this, tmsg, Toast.LENGTH_SHORT).show();

			}else if (mtype.equals(Constants.mtype_roleChanged)){
				String role = bundle.getString("role");

				if (role.equals(Constants.CN)){
					invalidateOptionsMenu();
					networkFileFrag.setButton(true);

					//					String tmsg = "Role Changed To Cloud Node";
					//					Toast.makeText(MainActivity.this, tmsg, Toast.LENGTH_SHORT).show();

				}else if (role.equals(Constants.CL)){
					invalidateOptionsMenu();
					//					networkFileFrag.setButton(false);

					String tmsg = "Role Changed To Cloud Leader";
					Toast.makeText(MainActivity.this, tmsg, Toast.LENGTH_SHORT).show();

				}

			}else if (mtype.equals(Constants.mtype_startStream)){
				Intent i= new Intent(Intent.ACTION_VIEW);
				i.setDataAndType(Uri.parse("udp://@224.1.2.3:1234"), getMimeType("Sample.mp4"));
				//i.setData(Uri.parse("http://192.168.137.1:8080"));
				startActivity(i);

			}

		}
	};

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	TabsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	// Tab titles
	private String[] tabs = {"Home Files", "Network Files", "Streaming" };



	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			// TODO Auto-generated method stub
			cs = ((ControllerService.MyBinder) binder).getService();
			Log.d(TAG, "Service Connected" );
			cs.setHandler(handler);
			CSHandler = cs.getUIhandler();
			//Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();				
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			// TODO Auto-generated method stub
			Log.d(TAG, "Service disConnected" );
			cs = null;
		}
	};



	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// Very initial setup
		ConfigData.setAppPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/COIN");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());

		mViewPager.setAdapter(mSectionsPagerAdapter);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		//		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setTitle("CWC");
		//		actionBar.setDisplayOptions(0);


		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				actionBar.setSelectedNavigationItem(position);
			}
		});

		// For each of the sections in the app, add a tab to the action bar.
		for (String tab_name : tabs) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.

			actionBar.addTab(actionBar.newTab().setText(tab_name).setTabListener(this));
		}


		//		android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		//		NetworkFiles NetworkFilesFrag = new NetworkFiles();
		//
		//		fragmentTransaction.add(R.id.pager, NetworkFilesFrag, "frag_tag");
		//		fragmentTransaction.commit();

		ctx = this;

		new AsyncAppInit().execute();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.main, menu);
		return true;
	}


	public boolean onPrepareOptionsMenu(Menu menu){

		menu.findItem(R.id.menu_battery).setTitle("Battery: " + bat_prcnt);

		if (ConfigData.getFacility().equals(Constants.CN)){	//TODO modify this constant in BTS side script in python
			menu.findItem(R.id.menu_facility).setIcon(R.drawable.cloud_node);

		}else if (ConfigData.getFacility().equals(Constants.CL)){
			menu.findItem(R.id.menu_facility).setIcon(R.drawable.cloud_leader);

			mSectionsPagerAdapter.notifyDataSetChanged();
		}	

		return true;
	}


	class AsyncAppInit extends AsyncTask<Void, Void, Void> {

		private Exception exception;

		protected Void doInBackground(Void... v) {
			Void ret = null;

			return ret;
		}

		protected void onPostExecute(Void v) {
			// TODO: check this.exception 
			// TODO: do something with the feed
		}
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}


	@Override
	protected void onStart(){
		super.onStart();
		Log.d(TAG, "Activity Started");
	}

	@Override
	protected void onRestart(){
		super.onRestart();
		Log.d(TAG, "Activity Restarted");
	}

	@Override
	protected void onResume(){
		super.onResume();
		try{
			Log.d(TAG, "Activity Resumed");
			Intent service = new Intent(ctx, ControllerService.class);
			ctx.bindService(service, mConnection, Context.BIND_AUTO_CREATE);
			ctx.startService(service);

		}
		catch(Exception e){
			Log.e(TAG, "Error Binding to service on activity resume");
			e.printStackTrace();
		}

	}

	@Override
	protected void onPause(){
		super.onPause();
		unbindService(mConnection);
		Log.d(TAG, "Activity Paused");
	}

	@Override
	protected void onStop(){
		super.onStop();
		Log.d(TAG, "Activity Stopped");
	}

	@Override
	protected void onDestroy(){
		super.onDestroy();
		Log.d(TAG, "Activity Destroyed");
	}


	public static String getMimeType(String url)
	{
		String type = null;
		String extension = MimeTypeMap.getFileExtensionFromUrl(url);
		if (extension != null) {
			MimeTypeMap mime = MimeTypeMap.getSingleton();
			type = mime.getMimeTypeFromExtension(extension);
		}
		return type;
	}

	public void setTagNetworkFilesFrag (String tag){
		TagOfNetworkFiles  = tag;
	}

	public String getTagNetworkFilesFrag(){
		return TagOfNetworkFiles;
	}


	public void passMesgToService(String file, String ip, String request){

		Log.d(TAG, "pass message to control service");

		Message msg = CSHandler.obtainMessage();
		Bundle bundle = new Bundle();
		bundle.putString("to", "CS");
		bundle.putString("requestType", request);
		bundle.putString("file", file);
		bundle.putString("sourceIP", ip);
		msg.setData(bundle);
		CSHandler.sendMessage(msg);

	}

}
