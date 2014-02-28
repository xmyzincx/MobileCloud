package com.cwc.mobilecloud.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.cwc.mobilecloud.ConfigData;
import com.cwc.mobilecloud.LazyAdapter;
import com.cwc.mobilecloud.R;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class NodeFiles extends Fragment {

	static final String KEY_FILENAME = "name";
	static final String KEY_SIZE = "size";
	static final String KEY_DATE = "downloaded_on";
	static final String KEY_TS = "ts";

	// Drawable items

	private ImageView jpgview;
	private ListView loglist;

	private ListView fileListView ;  
	private LazyAdapter fileListAdapter ;

	private Context ctx;


	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.frag_node_files, container, false);

		ArrayList<HashMap<String, String>> fileList = new ArrayList<HashMap<String, String>>();

		fileListView = (ListView) rootView.findViewById( R.id.listView1 );

		fileListAdapter = new LazyAdapter(getActivity(), null);

		// Set the ArrayAdapter as the ListView's adapter.  
		fileListView.setAdapter( fileListAdapter );

		fileListView.setOnItemClickListener(new OnItemClickListener(){
			
			public void onItemClick(AdapterView<?> arg0, View v, int position, long id){

				HashMap<String, String> map = new HashMap<String, String>();
				map = (HashMap<String, String>) fileListView.getItemAtPosition(position);
				String fileName = map.get(KEY_FILENAME);
				String fullPath = ConfigData.getAppPath() + "/" + fileName;
				File sdPath = new File(fullPath);
				Intent intent = new Intent();
				intent.setAction(android.content.Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(sdPath), getMimeType(fileName));
				startActivity(intent);          
			}
		});

		return rootView;
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

}
