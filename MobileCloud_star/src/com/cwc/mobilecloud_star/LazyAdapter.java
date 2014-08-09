package com.cwc.mobilecloud_star;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cwc.mobilecloud_star.R;
import com.cwc.mobilecloud_star.R.array;
import com.cwc.mobilecloud_star.R.id;
import com.cwc.mobilecloud_star.R.layout;

public class LazyAdapter extends BaseAdapter {
	private static final String TAG = "LazyAdapter"; 
	private Activity activity;
	private ArrayList<HashMap<String, String>> data;
	private static LayoutInflater inflater=null;


	public LazyAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
		activity = a;
		data = new ArrayList<HashMap<String, String>>();;
		inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		data.clear();
		data.addAll(getDownloadedItems());

	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public void notifyDataSetChanged (){

		data.clear();
		data.addAll(getDownloadedItems());
		super.notifyDataSetChanged();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View vi=convertView;
		if(convertView==null)
			vi = inflater.inflate(R.layout.list_row, null);

		TextView title = (TextView)vi.findViewById(R.id.title); // title
		TextView artist = (TextView)vi.findViewById(R.id.artist); // artist name
		TextView timestamp = (TextView)vi.findViewById(R.id.timestamp); // artist name
		TextView duration = (TextView)vi.findViewById(R.id.duration); // duration
		VerticalTextView textIcon = (VerticalTextView)vi.findViewById(R.id.textIcon); // title
		ImageView delImg = (ImageView)vi.findViewById(R.id.delImg); // title
		LinearLayout thumbnail = (LinearLayout)vi.findViewById(R.id.thumbnail); // title

		HashMap<String, String> file = new HashMap<String, String>();
		file = data.get(position);

		// Setting all values in listview
		title.setText(file.get(MainActivity.KEY_FILENAME));
		artist.setText(file.get(MainActivity.KEY_SIZE));
		duration.setText("");
		artist.setText(file.get(MainActivity.KEY_SIZE) );


		//textIcon.setText(String.valueOf(file.get(MainActivity.KEY_FILENAME).charAt(0)).toUpperCase());
		String ch = String.valueOf(file.get(MainActivity.KEY_FILENAME).charAt(0)).toUpperCase();
		String fileExt = file.get(MainActivity.KEY_FILENAME).substring(file.get(MainActivity.KEY_FILENAME).lastIndexOf('.') + 1).toUpperCase();
		int index = ((int)((char)ch.charAt(0) - 'A') % 9) + 1 ;
		if (index<1){
			index = 0 - index;
		}
		textIcon.setText(fileExt);
		textIcon.setBackgroundColor(Color.parseColor(activity.getResources().getStringArray(R.array.bgColors)[index]) );
		thumbnail.setBackgroundColor(Color.parseColor(activity.getResources().getStringArray(R.array.bgColors)[index]));
		timestamp.setText(file.get(MainActivity.KEY_DATE));

		delImg.setTag((Object)((Integer)position));
		delImg.setOnClickListener(new View.OnClickListener()   
		{               
			@Override
			public void onClick(View v) {
				int pos = (Integer)v.getTag();

				HashMap<String, String> map = new HashMap<String, String>();
				map = (HashMap<String, String>) getItem(pos);
				String fileName = map.get(MainActivity.KEY_FILENAME);



				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which){
						case DialogInterface.BUTTON_POSITIVE:
							//Yes button clicked
							// Delete file
							String fileName = ConfigData.getdelFile();
							String fullPath = ConfigData.getAppPath() + "/" + fileName;
							File sdPath = new File(fullPath);
							sdPath.delete();							
							notifyDataSetChanged();
							break;

						case DialogInterface.BUTTON_NEGATIVE:
							//No button clicked
							break;
						}
					}
				};
				ConfigData.setdelFile(fileName);
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				builder.setMessage("Delete " + fileName + " ?").setPositiveButton("Yes", dialogClickListener)
				.setNegativeButton("No", dialogClickListener).show();

			}
		});


		return vi;
	}




	private ArrayList<HashMap<String, String>> getDownloadedItems(){
		ArrayList<HashMap<String, String>> fileList = new ArrayList<HashMap<String, String>>();
		File sdPath = null;
		sdPath = new File(ConfigData.getAppPath());
		sdPath.mkdir();
		File[] items= sdPath.listFiles();

		for( int i=0; i< items.length; i++)
		{
			if ((!items[i].isDirectory()) && items[i].exists()){
				HashMap<String, String> map = new HashMap<String, String>();
				map.put(MainActivity.KEY_FILENAME, items[i].getName());
				map.put(MainActivity.KEY_SIZE, "File Size : " + getReadableFileSizeString(items[i].length()));

				Date d = new Date(items[i].lastModified());
				String strDate = String.valueOf(d.getDate()) + "/" + String.valueOf(d.getMonth()) + "/" + String.valueOf(d.getYear()+1900);
				strDate += " " + String.valueOf(d.getHours()) + ":" + String.valueOf(d.getMinutes());
				map.put(MainActivity.KEY_DATE, "Downloaded On : " + strDate);
				map.put(MainActivity.KEY_TS, String.valueOf(items[i].lastModified()));

				fileList.add( map );

			}

		}


		Collections.sort (fileList, new FileMapComparator ());

		return (ArrayList<HashMap<String, String>>) fileList.clone();
	}

	private String getReadableFileSizeString(long mBytes) {

		int i = -1;
		long fileSizeInBytes = mBytes;
		String[] byteUnits = {" kB", " MB", " GB", " TB", " PB", " EB", " ZB", " YB"};
		do {
			fileSizeInBytes = fileSizeInBytes / 1024;
			i++;
		} while (fileSizeInBytes > 1024);

		return String.valueOf(fileSizeInBytes) + byteUnits[i];
	};

	// Comparator interface to sort files based on TimeStamp
	public class FileMapComparator implements Comparator<HashMap <String, String>> 
	{
		@Override
		public int compare (HashMap<String, String> o1, HashMap<String, String> o2) 
		{
			int c;
			long val1 = Long.valueOf(o1.get(MainActivity.KEY_TS));
			long val2 = Long.valueOf(o2.get(MainActivity.KEY_TS));

			if (val1 > val2)
				return -1;

			if (val1 < val2)
				return 1;

			return 0;
		}
	}

}
