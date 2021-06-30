package org.foxteam.noisyfox.heygetup;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AdapterBusStation extends BaseAdapter {
	private List<BusStationInfo> resource;// 要绑定的数据
	private int itemView = R.layout.item_bus_station;// 绑定的一个条目界面的id，此例中即为item.xml
	private LayoutInflater inflater;// 布局填充器，它可以使用一个xml文件生成一个View对象，可以通过Context获取实例对象

	public AdapterBusStation(Context context, List<BusStationInfo> res) {
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		resource = res;
	}

	@Override
	public int getCount() {
		return resource.size();
	}

	@Override
	public Object getItem(int arg0) {
		return resource.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View thisView = inflater.inflate(itemView, null);
		TextView nameView = (TextView) thisView
				.findViewById(R.id.textView_station_name);
		TextView disView = (TextView) thisView
				.findViewById(R.id.textView_station_distance);
		TextView lineView = (TextView) thisView
				.findViewById(R.id.textView_buslines);
		BusStationInfo info = resource.get(position);
		thisView.setTag(info);
		nameView.setText(info.mStationName);
		disView.setText(" 相距约"
				+ MapUtil.getRoughDistanceExpress(info.mDistance));
		lineView.setText("途经公交: " + info.mBusHereDescription);
		return thisView;
	}

}
