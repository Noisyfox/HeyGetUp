package org.foxteam.noisyfox.heygetup;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.baidu.platform.comapi.basestruct.GeoPoint;

public class BusStationInfo {

	String uid = "";
	String mCity = "";
	String mStationName = "";
	String mBusHereDescription = "";
	double mDistance = 0;
	Map<String, BusLineInfo> mBusHere = new HashMap<String, BusLineInfo>();
	GeoPoint mLocation = null;

	void generateBusDescription() {
		Collection<BusLineInfo> all = mBusHere.values();
		BusLineInfo[] v = new BusLineInfo[all.size()];
		all.toArray(v);
		Arrays.sort(v, new Comparator<BusLineInfo>() {
			@Override
			public int compare(BusLineInfo pFirst, BusLineInfo pSecond) {
				String nameFirst = pFirst.mBusName;
				String nameSecond = pSecond.mBusName;
				boolean dsFirst = Character.isDigit(nameFirst.charAt(0));
				boolean dsSecond = Character.isDigit(nameSecond.charAt(0));
				if (dsFirst && dsSecond) {
					int vFirst = MapUtil.findIntegerInString(nameFirst, 0);
					int vSecond = MapUtil.findIntegerInString(nameSecond, 0);
					if(vFirst == vSecond){
						return nameFirst.compareTo(nameSecond);
					}
					return vFirst - vSecond;
				} else if (dsFirst && !dsSecond) {
					return -1;
				} else if (!dsFirst && dsSecond) {
					return 1;
				}

				return pFirst.mBusName.compareTo(pSecond.mBusName);
			}
		});

		StringBuffer sb = new StringBuffer();
		for (BusLineInfo info : v) {
			sb.append(info.mBusName);
			sb.append(';');
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.lastIndexOf(";"));
		}
		mBusHereDescription = sb.toString();
	}

}
