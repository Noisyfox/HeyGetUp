package org.foxteam.noisyfox.heygetup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import com.baidu.mapapi.search.MKAddrInfo;
import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKDrivingRouteResult;
import com.baidu.mapapi.search.MKPoiInfo;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKSearchListener;
import com.baidu.mapapi.search.MKShareUrlResult;
import com.baidu.mapapi.search.MKSuggestionResult;
import com.baidu.mapapi.search.MKTransitRouteResult;
import com.baidu.mapapi.search.MKWalkingRouteResult;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.platform.comapi.basestruct.GeoPoint;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.app.Activity;

public class RouteActivity extends Activity {
	static final int MESSAGE_SHOW_PROCESS_VIEW = 1;
	static final int MESSAGE_HIDE_PROCESS_VIEW = 2;
	static final int MESSAGE_SHOW_RESULT_STATION = 3;
	static final int MESSAGE_SHOW_RESULT_LINE = 4;

	static final int SEARCH_TYPE_ONBUS = 1;
	static final int SEARCH_TYPE_ATSTATION = 2;
	static final int SEARCH_TYPE_FINDBUS = 3;
	static final int SEARCH_TYPE_SETDESTINATION = 4;

	static RouteActivity mActivity = null;

	HeyGetUpApplication mMyApp = null;
	BusInfoManager mBusInfoManager = new BusInfoManager();
	List<BusLineInfo> mResultBusLine = new ArrayList<BusLineInfo>();
	List<BusStationInfo> mResultBusStation = new ArrayList<BusStationInfo>();
	AdapterBusStation mAdapterBusStation = null;
	AdapterBusLine mAdapterBusLine = null;
	BusDetailSearcher mBusDetailSearcher = null;
	OnDetailGetListener mOnDetailGetListener = new OnDetailGetListener() {

		@Override
		void onGetDetail(BusLineInfo info) {
			if (!mResultBusLine.contains(info)) {
				mResultBusLine.add(info);
			}
		}

		@Override
		void onSearchFinish() {
			// TODO Auto-generated method stub

			mHandler.sendMessage(mHandler
					.obtainMessage(MESSAGE_HIDE_PROCESS_VIEW));
			mHandler.sendMessage(mHandler
					.obtainMessage(MESSAGE_SHOW_RESULT_LINE));
		}

	};

	RadioGroup mRadioGroupSearchType = null;
	RelativeLayout mLayoutProgress = null;
	RelativeLayout mLayoutInfo = null;
	TextView mTextViewSearching = null;
	ListView mListViewSearchResult = null;
	OnCheckedChangeListener mListenerRadioGroupSearchType = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			switch (checkedId) {
			case R.id.radioButton_searchtype_onbus:
				startSearch(SEARCH_TYPE_ONBUS);
				break;
			case R.id.radioButton_searchtype_atstation:
				mResultBusLine.clear();
				mResultBusStation.clear();
				startSearch(SEARCH_TYPE_ATSTATION);
				break;
			case R.id.radioButton_searchtype_findbus:
				startSearch(SEARCH_TYPE_FINDBUS);
				break;
			case R.id.radioButton_searchtype_setdestination:
				startSearch(SEARCH_TYPE_SETDESTINATION);
				break;
			}
		}

	};
	OnItemClickListener mListenerOnItemClickSearchResult_station = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			Object tag = view.getTag();
			if (tag != null) {
				mHandler.sendMessage(mHandler.obtainMessage(
						MESSAGE_SHOW_PROCESS_VIEW,
						R.string.searchingmsg_at_station_bus, 0));
				BusStationInfo info = (BusStationInfo) tag;
				Set<Entry<String, BusLineInfo>> set = info.mBusHere.entrySet();
				for (Entry<String, BusLineInfo> e : set) {
					mBusDetailSearcher.addSearchRequest(e.getValue().mCity,
							e.getValue().mBusName);
				}
				mBusDetailSearcher.startSearch();
			}
		}

	};
	MKSearchListener mMKSearchListener = new MKSearchListener() {
		@Override
		public void onGetPoiDetailSearchResult(int type, int error) {
		}

		@Override
		public void onGetPoiResult(MKPoiResult res, int type, int error) {
			if (error != 0 || res == null) {
				return;
			}
			//boolean isStation = false;
			int totalPoiNum = res.getCurrentNumPois();
			for (int i = 0; i < totalPoiNum; i++) {
				MKPoiInfo poi = res.getPoi(i);
				// Log.d("name", poi.name);
				switch (poi.ePoiType) {
				case 1:// 公交站
					BusStationInfo stationInfo = mBusInfoManager.addBusStation(
							mMyApp.getCity(), poi.name, poi.pt);
					if (stationInfo != null) {
						//isStation = true;
						mBusInfoManager.addBusToStation(stationInfo, poi);
						if (!mResultBusStation.contains(stationInfo)) {
							mResultBusStation.add(stationInfo);
						}
						stationInfo.generateBusDescription();
						Log.d("name", stationInfo.mStationName + "\n"
								+ stationInfo.mBusHereDescription);
					}
					break;
				case 2:// 公交线路
					/*
					 * BusLineInfo lineInfo = mBusInfoManager.addBusLine(
					 * mMyApp.getCity(), poi.name); if (lineInfo != null) {
					 * 
					 * }
					 */
					break;
				}
			}
			// if (isStation) {
			calculateDistance();
			sortStationByDistance();
			mHandler.sendMessage(mHandler
					.obtainMessage(MESSAGE_HIDE_PROCESS_VIEW));
			mHandler.sendMessage(mHandler
					.obtainMessage(MESSAGE_SHOW_RESULT_STATION));
			// }
		}

		@Override
		public void onGetDrivingRouteResult(MKDrivingRouteResult res, int error) {
		}

		@Override
		public void onGetTransitRouteResult(MKTransitRouteResult res, int error) {
		}

		@Override
		public void onGetWalkingRouteResult(MKWalkingRouteResult res, int error) {
		}

		@Override
		public void onGetAddrResult(MKAddrInfo res, int error) {
		}

		@Override
		public void onGetBusDetailResult(MKBusLineResult result, int iError) {
			/*
			 * if (iError != 0 || result == null) { return; } BusLineInfo info =
			 * mBusInfoManager.addBusLine(mMyApp.getCity(), result); if (info !=
			 * null) { mResultBusLine.add(info); /* if
			 * (mBusDetailRequest.contains(info)) {
			 * mBusDetailRequest.remove(info); if (mBusDetailRequest.isEmpty())
			 * {
			 * 
			 * } else { busLineDetailSearchNext(); } }
			 * 
			 * }
			 */
		}

		@Override
		public void onGetSuggestionResult(MKSuggestionResult res, int arg1) {
		}

		@Override
		public void onGetShareUrlResult(MKShareUrlResult result, int type,
				int error) {
		}
	};

	static Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_SHOW_PROCESS_VIEW:
				mActivity.showProgressView(true, msg.arg1);
				break;
			case MESSAGE_HIDE_PROCESS_VIEW:
				mActivity.showProgressView(false, 0);
				break;
			case MESSAGE_SHOW_RESULT_STATION:
				mActivity.mListViewSearchResult
						.setOnItemClickListener(mActivity.mListenerOnItemClickSearchResult_station);
				mActivity.mListViewSearchResult
						.setAdapter(mActivity.mAdapterBusStation);
				break;
			case MESSAGE_SHOW_RESULT_LINE:
				mActivity.mListViewSearchResult.setOnItemClickListener(null);
				mActivity.mListViewSearchResult
						.setAdapter(mActivity.mAdapterBusLine);
				break;
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mActivity = this;
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_route);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.title_route);

		mMyApp = (HeyGetUpApplication) this.getApplication();
		mBusDetailSearcher = new BusDetailSearcher(mBusInfoManager);
		mAdapterBusStation = new AdapterBusStation(this, mResultBusStation);
		mAdapterBusLine = new AdapterBusLine(this, mResultBusLine);

		mBusDetailSearcher.setOnDetailGetListener(mOnDetailGetListener);

		mRadioGroupSearchType = (RadioGroup) findViewById(R.id.radioGroup_search_type);
		mLayoutProgress = (RelativeLayout) findViewById(R.id.relativeLayout_progress);
		mLayoutInfo = (RelativeLayout) findViewById(R.id.relativeLayout_info);
		mTextViewSearching = (TextView) findViewById(R.id.textView_searching);
		mListViewSearchResult = (ListView) findViewById(R.id.listView_search_result);
		Button btn_back = (Button) findViewById(R.id.button_back);
		btn_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				RouteActivity.this.finish();
			}
		});
		mRadioGroupSearchType
				.setOnCheckedChangeListener(mListenerRadioGroupSearchType);
		showProgressView(false, 0);
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		overridePendingTransition(R.anim.fade_in, R.anim.slide_out_bottom);
	}

	void showProgressView(boolean show, int messageRes) {
		mLayoutProgress.setVisibility(show ? View.VISIBLE : View.GONE);
		// mLayoutInfo.setEnabled(!show);
		mListViewSearchResult.setEnabled(!show);
		if (show) {
			mTextViewSearching.setText(messageRes);
		}
	}

	void startSearch(int type) {
		terminateSearch();

		switch (type) {
		case SEARCH_TYPE_ONBUS:
			break;
		case SEARCH_TYPE_ATSTATION:
			mHandler.sendMessage(mHandler.obtainMessage(
					MESSAGE_SHOW_PROCESS_VIEW,
					R.string.searchingmsg_at_station, 0));

			MKSearchOperator.poiSearchNearBy(mMKSearchListener, "公交车站",
					MapUtil.getGeoPoint(mMyApp.getLocData()), 1000);
			break;
		case SEARCH_TYPE_FINDBUS:
			break;
		case SEARCH_TYPE_SETDESTINATION:
			break;
		}
	}

	void terminateSearch() {
		mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_HIDE_PROCESS_VIEW));

	}

	void calculateDistance() {
		final GeoPoint currentLoc = MapUtil.getGeoPoint(mMyApp.getLocData());
		for (BusStationInfo info : mResultBusStation) {
			info.mDistance = DistanceUtil.getDistance(currentLoc,
					info.mLocation);
		}
	}

	void sortStationByDistance() {
		Collections.sort(mResultBusStation, new Comparator<BusStationInfo>() {

			@Override
			public int compare(BusStationInfo arg0, BusStationInfo arg1) {
				double d1 = arg0.mDistance;
				double d2 = arg1.mDistance;
				if (d1 == d2)
					return 0;
				else if (d1 < d2)
					return -1;

				return 1;
			}

		});
	}

	class BusDetailSearcher {

		BusInfoManager mBusInfoManager = null;

		static final long SEARCH_TIME_OUT = 5000L;

		Object mSync = new Object();

		OnDetailGetListener mOnDetailGetListener = null;
		Map<String, UidRequest> mUidRequests = new HashMap<String, UidRequest>();
		Map<String, NameRequest> mNameRequests = new HashMap<String, NameRequest>();
		List<UidRequest> mUidRequests_list = new LinkedList<UidRequest>();
		List<NameRequest> mNameRequests_list = new LinkedList<NameRequest>();
		Stack<Pair<String, String>> mUidRequests_stack = new Stack<Pair<String, String>>();
		String mUidRequests_requestedCity = "";

		Timer mTimer = new Timer();
		boolean mIsSearchStarted = false;
		TimerTask mTimerTask = null;

		MKSearchListener mSearchListener = new MKSearchListener() {

			@Override
			public void onGetPoiResult(MKPoiResult result, int type, int iError) {
				synchronized (mSync) {
					if (iError != 0 || result == null) {
						return;
					}
					if (!mIsSearchStarted)
						return;
					int totalPoiNum = result.getCurrentNumPois();
					boolean isResultGet = false;
					List<String> keys = new ArrayList<String>();
					for (int i = 0; i < totalPoiNum; i++) {
						MKPoiInfo poi = result.getPoi(i);
						if (poi.ePoiType == 2) {
							String simpleBusName = MapUtil
									.removeStationFromBusName(poi.name);
							Log.d("name", poi.name + ":" + simpleBusName);
							String key = poi.city + simpleBusName;
							if (mNameRequests.containsKey(key)) {
								Log.d("name", simpleBusName + "get!");
								isResultGet = true;
								addUidSearchRequest(poi.city, simpleBusName,
										poi.uid);
								keys.add(key);
							}
							/*
							 * else {// 开始搜索下一个名称 NameRequest nextRequest =
							 * mNameRequests_list .get(0);
							 * mSearch.poiSearchInCity(nextRequest.city,
							 * nextRequest.name); }
							 */
							// }
						}
					}
					if (isResultGet) {
						for (String key : keys) {
							NameRequest request = mNameRequests.get(key);
							mNameRequests.remove(key);
							mNameRequests_list.remove(request);
							if (!mNameRequests_list.isEmpty()) {
								mNameRequests_list.remove(0);
							} else {// 开始详情搜索
								Log.d("name", "Empty!");
								doUidSearch();
								break;
							}
						}
						delayOnce();
					}
				}
			}

			@Override
			public void onGetBusDetailResult(MKBusLineResult result, int iError) {
				synchronized (mSync) {
					if (iError != 0 || result == null) {
						return;
					}
					if (!mIsSearchStarted)
						return;

					Log.d("name", "Detail!");
					String simpleBusName = MapUtil
							.removeStationFromBusName(result.getBusName());
					Log.d("name", result.getBusName() + ":" + simpleBusName);
					String key = mUidRequests_requestedCity + simpleBusName;
					UidRequest request = mUidRequests.get(key);
					if (request != null) {
						if (mBusInfoManager != null) {
							BusLineInfo info = mBusInfoManager.addBusLine(
									mUidRequests_requestedCity, result);
							if (info != null) {
								if (mOnDetailGetListener != null) {
									mOnDetailGetListener.onGetDetail(info);
								}
							}
						}
						if (mUidRequests_stack.isEmpty()) {// 搜索结束
							finishSearch();
						} else {
							uidSearchNext();
						}
					}
				}
			}

			@Override
			public void onGetAddrResult(MKAddrInfo arg0, int arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onGetDrivingRouteResult(MKDrivingRouteResult arg0,
					int arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onGetPoiDetailSearchResult(int arg0, int arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onGetShareUrlResult(MKShareUrlResult arg0, int arg1,
					int arg2) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onGetSuggestionResult(MKSuggestionResult arg0, int arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onGetTransitRouteResult(MKTransitRouteResult arg0,
					int arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onGetWalkingRouteResult(MKWalkingRouteResult arg0,
					int arg1) {
				// TODO Auto-generated method stub

			}

		};

		public BusDetailSearcher(BusInfoManager manager) {
			mBusInfoManager = manager;
		}

		public void addSearchRequest(String city, String name, String uid) {
			synchronized (mSync) {
				if (mIsSearchStarted)
					return;

				String key = city + name;
				UidRequest request = mUidRequests.get(key);
				if (request == null) {
					request = new UidRequest();
					request.city = city;
					// request.name = name;
					mUidRequests.put(key, request);
					mUidRequests_list.add(request);
				}
				if (!request.uids.contains(uid)) {
					request.uids.add(uid);
				}
			}
		}

		private void addUidSearchRequest(String city, String name, String uid) {
			synchronized (mSync) {
				String key = city + name;
				UidRequest request = mUidRequests.get(key);
				if (request == null) {
					request = new UidRequest();
					request.city = city;
					// request.name = name;
					mUidRequests.put(key, request);
					mUidRequests_list.add(request);
				}
				if (!request.uids.contains(uid)) {
					request.uids.add(uid);
				}
			}
		}

		public void addSearchRequest(String city, String name) {
			synchronized (mSync) {
				if (mIsSearchStarted)
					return;

				String key = city + name;
				NameRequest request = mNameRequests.get(key);
				if (request == null) {
					request = new NameRequest();
					request.city = city;
					request.name = name;
					mNameRequests.put(key, request);
					mNameRequests_list.add(request);
				}
			}
		}

		private void doNameSearch() {
			for (NameRequest request : mNameRequests_list) {
				MKSearchOperator.poiSearchInCity(mSearchListener, request.city,
						request.name);
			}
		}

		private void uidSearchNext() {
			if (!mUidRequests_stack.isEmpty()) {
				Pair<String, String> p = mUidRequests_stack.pop();
				mUidRequests_requestedCity = p.first;
				MKSearchOperator.busLineSearch(mSearchListener, p.first,
						p.second);
				delayOnce();
			}
		}

		private void doUidSearch() {
			mUidRequests_stack.clear();
			for (UidRequest request : mUidRequests_list) {
				// mSearch.busLineSearch(request.city, request.)
				for (String uid : request.uids) {
					mUidRequests_stack.push(new Pair<String, String>(
							request.city, uid));
				}
			}
			uidSearchNext();
		}

		private void delayOnce() {
			if (!mIsSearchStarted)
				return;
			mTimer.cancel();
			mTimer.purge();
			mTimer = new Timer();
			mTimerTask = new TimerTask() {
				@Override
				public void run() {
					synchronized (mSync) {
						finishSearch();
					}
				}
			};
			mTimer.schedule(mTimerTask, SEARCH_TIME_OUT);
		}

		public void startSearch() {
			synchronized (mSync) {
				if (mIsSearchStarted)
					return;
				mIsSearchStarted = true;
				if (!mNameRequests_list.isEmpty()) {
					doNameSearch();
				} else {
					doUidSearch();
				}
				mTimerTask = new TimerTask() {
					@Override
					public void run() {
						synchronized (mSync) {
							finishSearch();
						}
					}
				};
				mTimer.schedule(mTimerTask, SEARCH_TIME_OUT);
			}
		}

		public void terminateSearch() {
			synchronized (mSync) {
				if (!mIsSearchStarted)
					return;

				mIsSearchStarted = false;
				clean();
			}
		}

		private void finishSearch() {
			if (!mIsSearchStarted)
				return;
			if (mOnDetailGetListener != null) {
				mOnDetailGetListener.onSearchFinish();
			}
			mIsSearchStarted = false;
			clean();
		}

		private void clean() {
			if (mIsSearchStarted)
				return;

			mTimer.cancel();
			mTimer.purge();
			mTimer = new Timer();
			mUidRequests.clear();
			mNameRequests.clear();
			mUidRequests_list.clear();
			mNameRequests_list.clear();
			mUidRequests_stack.clear();
		}

		public void setOnDetailGetListener(OnDetailGetListener listener) {
			synchronized (mSync) {
				if (mIsSearchStarted)
					return;

				mOnDetailGetListener = listener;
			}
		}

		private class UidRequest {
			String city = "";
			// String name = "";
			List<String> uids = new ArrayList<String>(2);
		}

		private class NameRequest {
			String city = "";
			String name = "";
		}
	}

	abstract class OnDetailGetListener {

		abstract void onGetDetail(BusLineInfo info);

		abstract void onSearchFinish();
	}
}
