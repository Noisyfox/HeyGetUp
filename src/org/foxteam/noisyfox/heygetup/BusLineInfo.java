package org.foxteam.noisyfox.heygetup;

import java.util.ArrayList;
import java.util.List;

import com.baidu.mapapi.search.MKRoute;

public class BusLineInfo {

	String uid = "";
	String mBusName = "";
	String mCity = "";
	MKRoute mMKRouteForward = null;
	MKRoute mMKRouteBackward = null;

	List<BusStationInfo> mBusStationInfoForward = new ArrayList<BusStationInfo>();
	List<BusStationInfo> mBusStationInfoBackward = new ArrayList<BusStationInfo>();

}
