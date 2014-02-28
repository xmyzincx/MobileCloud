package com.cwc.mobilecloud.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.cwc.mobilecloud.ControlTransceiver;
import com.cwc.mobilecloud.ControllerService;
import com.cwc.mobilecloud.TextProgressBar;
import com.cwc.mobilecloud.ConfigData;
import com.cwc.mobilecloud.Node;
import com.cwc.mobilecloud.R;
import com.cwc.mobilecloud.MainActivity;
import com.cwc.mobilecloud.utilities.Constants;
//import com.cwc.mobilecloud.MainActivity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnChildClickListener;


public class NetworkFiles extends Fragment {

	ExpandableListAdapter listAdapter;
	ExpandableListView expListView;
	List<String> listDataHeader;
	HashMap<String, List<String>> listDataChild;

	private static final String TAG = "Network files adapter";
	private TextProgressBar progressBar;
	private ActivityCommunicator activityCommunicator;
	private Button DLButton;
	private EditText URLTextBox;
	public Context context;


	//	public interface FragmentCommunicator {
	//		public void passDataToFragment(int max, int progress);
	//	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		String myTag = getTag();

		((MainActivity)getActivity()).setTagNetworkFilesFrag(myTag);

		View rootView = inflater.inflate(R.layout.frag_network_files, container, false);

		// get the listview
		expListView = (ExpandableListView) rootView.findViewById(R.id.lvExp);

		// preparing list data
		prepareListData();

		listAdapter = new ExpandableListAdapter(getActivity(), listDataHeader, listDataChild);

		// setting list adapter
		expListView.setAdapter(listAdapter);
		
		expListView.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				// TODO Auto-generated method stub
				
				String file_req =  listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).toString();
				
				String source_ID = listDataHeader.get(groupPosition).toString();
				
				String source_ip = ConfigData.getPeer(source_ID).IPAddr.toString();
				
				((MainActivity)getActivity()).passMesgToService(file_req, source_ip, Constants.request_file);
				
				Toast.makeText(getActivity(), source_ip + " : " + file_req, Toast.LENGTH_SHORT).show();
				
				return false;
			}
		});

		// draw progress bar
		progressBar = (TextProgressBar) rootView.findViewById(R.id.DLProgressBar);
		progressBar.setVisibility(progressBar.VISIBLE);
		progressBar.setMax(100);
		progressBar.setProgress(0);

		// URL text box
		URLTextBox = (EditText) rootView.findViewById(R.id.URL);

		// Download Button and its listener
		DLButton = (Button) rootView.findViewById(R.id.DownloadButton);
		DLButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				String link = URLTextBox.getText().toString();

				if(link != null && !link.equals("") && link.contains("http")){

					Toast.makeText(getActivity(), link, Toast.LENGTH_LONG ).show();
					
					Node cloud_leader = ConfigData.getCL();
					
//					String cl_ID = listDataHeader.get(groupPosition).toString();
					
					String cl_ip = cloud_leader.IPAddr.toString();
					
					((MainActivity)getActivity()).passMesgToService(link, cl_ip, Constants.request_url);

				}
				else{Toast.makeText(getActivity(), "Invalid link or address", Toast.LENGTH_LONG ).show();}				
			}
		});


		return rootView;
	}

	/*
	 * Preparing the list data
	 */
	private void prepareListData() {
		listDataHeader = new ArrayList<String>();
		listDataChild = new HashMap<String, List<String>>();
		int j = 0;

		Log.d(TAG, "prepare method called");

		//for printing contents on all the peers
		for (String id : ConfigData.getPeerIds()){

			// Adding header data
			Log.d(TAG, "Neighbor ID: " + ConfigData.getPeer(id).ID);
			listDataHeader.add(ConfigData.getPeer(id).ID);

			List<String> ContentList = new ArrayList<String>();
			Node temppeer = ConfigData.getPeer(id);
			int sizeofcontent = temppeer.contents.size(); 

			// Adding child data
			for (int i=0; i < sizeofcontent; i++)
			{
				ContentList.add((String) temppeer.contents.get(i));
			}

			listDataChild.put(listDataHeader.get(j), ContentList);

			j = j +1;
		}
	}

	public void setProgressBar(int max, int progress){

		progressBar.setVisibility(progressBar.VISIBLE);
		progressBar.setMax(max);
		progressBar.setProgress(progress);

	}
	
	public void setButton(boolean func){
		
		DLButton.setEnabled(func);
		
	}

}
