package org.foxteam.noisyfox.heygetup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKPoiInfo;
import com.baidu.mapapi.search.MKRoute;
import com.baidu.mapapi.search.MKStep;
import com.baidu.platform.comapi.basestruct.GeoPoint;

public class BusInfoManager {

	static Map<String, BusLineInfo> mBusLineInfoMap = new HashMap<String, BusLineInfo>();
	static Map<String, BusStationInfo> mBusStationInfoMap = new HashMap<String, BusStationInfo>();

	public BusLineInfo addBusLine(String city, MKBusLineResult result) {
		if (city == null || result == null) {
			return null;
		}

		String simpleName = MapUtil.removeStationFromBusName(result
				.getBusName());

		String uid = city + simpleName;
		BusLineInfo info = mBusLineInfoMap.get(uid);
		if (info == null) {
			info = new BusLineInfo();
			mBusLineInfoMap.put(uid, info);
			info.uid = uid;
			info.mCity = city;
			info.mBusName = simpleName;
		}

		MKRoute route = result.getBusRoute();

		List<BusStationInfo> stationList = null;
		if (info.mMKRouteForward == null) {
			info.mMKRouteForward = route;
			stationList = info.mBusStationInfoForward;
		} else if (info.mMKRouteBackward == null) {
			info.mMKRouteBackward = route;
			stationList = info.mBusStationInfoBackward;
		} else {
			return null;
		}

		int stationCount = route.getNumSteps();
		for (int i = 0; i < stationCount; i++) {
			BusStationInfo station = addBusStation(city, route.getStep(i));
			stationList.add(station);
			addBusToStation(station, info);
		}

		return info;
	}

	public BusLineInfo addBusLine(String city, String name) {
		String uid = city + name;
		BusLineInfo info = mBusLineInfoMap.get(uid);
		if (info == null) {
			info = new BusLineInfo();
			mBusLineInfoMap.put(uid, info);
			info.uid = uid;
			info.mCity = city;
			info.mBusName = name;
		}

		return info;
	}

	public BusStationInfo addBusStation(String city, MKStep station) {
		if (city == null || station == null) {
			return null;
		}

		return addBusStation(city, station.getContent(), station.getPoint());
	}

	public BusStationInfo addBusStation(String city, String name,
			GeoPoint location) {
		if (city == null || name == null || location == null) {
			return null;
		}

		String uid = city + name;
		BusStationInfo info = mBusStationInfoMap.get(uid);
		if (info == null) {
			info = new BusStationInfo();
			mBusStationInfoMap.put(uid, info);
			info.uid = uid;
			info.mCity = city;
			info.mStationName = name;
			info.mLocation = new GeoPoint(location.getLatitudeE6(),
					location.getLongitudeE6());
		}

		return info;
	}

	public void addBusToStation(BusStationInfo station, BusLineInfo bus) {
		if (station == null || bus == null) {
			return;
		}

		if (!station.mBusHere.containsKey(bus.uid)) {
			station.mBusHere.put(bus.uid, bus);
		}
	}

	public void addBusToStation(BusStationInfo station, MKPoiInfo info) {
		if (station == null || info == null) {
			return;
		}
		String city = info.city;
		String[] buslines = info.address.split(";");
		for (String bus : buslines) {
			// String uid = city + bus;
			BusLineInfo businfo = addBusLine(city, bus);
			addBusToStation(station, businfo);
		}
	}

	public BusLineInfo findBusLine(String city, String name) {
		String uid = city + name;

		return mBusLineInfoMap.get(uid);
	}

	public BusStationInfo findBusStation(String city, String name) {
		String uid = city + name;

		return mBusStationInfoMap.get(uid);

	}

	public void saveToCache() {

	}

	public void loadFromCache() {

	}
}
