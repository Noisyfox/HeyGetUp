package org.foxteam.noisyfox.heygetup;

import com.baidu.location.LocationClient;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.PopupClickListener;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnClosedListener;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnOpenedListener;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivity;

import android.os.Bundle;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class MainActivity extends SlidingActivity {

	// 定位相关
	// LocationClient mLocClient;
	// LocationData locData = null;
	HeyGetUpApplication mMyApp = null;

	public MyLocationListener myListener = new MyLocationListener() {

		@Override
		public void onLocationChanged(LocationData locationData) {
			// 更新定位数据
			myLocationOverlay.setData(locationData);
			// 更新图层数据执行刷新后生效
			mMapView.refresh();
			// 是手动触发请求或首次定位时，移动到定位点
			if (isRequest || isFirstLoc) {
				// 移动地图到定位点
				mMapController.animateTo(new GeoPoint(
						(int) (locationData.latitude * 1e6),
						(int) (locationData.longitude * 1e6)));
				isRequest = false;
			}
			// 首次定位完成
			isFirstLoc = false;
		}

		@Override
		public void onCityChanged(String city) {
		}

	};

	// 定位图层
	LocationOverlay myLocationOverlay = null;
	// 弹出泡泡图层
	private PopupOverlay pop = null;// 弹出泡泡图层，浏览节点时使用
	private TextView popupText = null;// 泡泡view
	private View viewCache = null;

	// 地图相关，使用继承MapView的MyLocationMapView目的是重写touch事件实现泡泡处理
	// 如果不处理touch事件，则无需继承，直接使用MapView即可
	MyLocationMapView mMapView = null; // 地图View
	private MapController mMapController = null;

	// UI相关
	OnCheckedChangeListener radioButtonListener = null;
	Button requestLocButton = null;
	boolean isRequest = false;// 是否手动触发请求定位
	boolean isFirstLoc = true;// 是否首次定位

	Button mZoomInButton = null;
	Button mZoomOutButton = null;
	Button mMenuButton = null;
	Button mSleepButton = null;

	OnOpenedListener mOnOpenedListener = new OnOpenedListener() {
		@Override
		public void onOpened() {
			mMenuButton.setSelected(true);
		}
	};
	OnClosedListener mOnClosedListener = new OnClosedListener() {
		@Override
		public void onClosed() {
			mMenuButton.setSelected(false);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

		mMyApp = (HeyGetUpApplication) this.getApplication();

		mMyApp.initEngineManager(this);// 确保初始化
		mMyApp.initLocationClient(this);

		setContentView(R.layout.activity_main);
		setBehindContentView(R.layout.slidemenu_right);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.title_main);

		SlidingMenu menu = this.getSlidingMenu();
		menu.setMode(SlidingMenu.RIGHT);
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		// menu.setShadowWidthRes(R.dimen.shadow_width);
		// menu.setShadowDrawable(R.drawable.shadow);
		// menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		menu.setFadeDegree(0.35f);
		menu.setBehindWidth(100);
		menu.setOnOpenedListener(mOnOpenedListener);
		menu.setOnClosedListener(mOnClosedListener);
		// menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		// menu.setMenu(R.layout.menu);
		requestLocButton = (Button) findViewById(R.id.button_relocate);
		mZoomInButton = (Button) findViewById(R.id.button_zoom_in);
		mZoomOutButton = (Button) findViewById(R.id.button_zoom_out);
		mMenuButton = (Button) findViewById(R.id.button_menu);
		mSleepButton = (Button) findViewById(R.id.button_iwantsleep);
		requestLocButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 手动定位请求
				requestLocClick();
			}
		});
		mZoomInButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mZoomOutButton.setEnabled(true);
				if (!mMapView.getController().zoomIn()) {
					mZoomInButton.setEnabled(false);
				}
			}
		});
		mZoomOutButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mZoomInButton.setEnabled(true);
				if (!mMapView.getController().zoomOut()) {
					mZoomOutButton.setEnabled(false);
				}
			}
		});
		mMenuButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mMenuButton.setSelected(true);
				getSlidingMenu().showMenu();
			}
		});
		mSleepButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, SpotActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_in_bottom,
						R.anim.fade_out);
			}
		});
		// 地图初始化
		mMapView = (MyLocationMapView) findViewById(R.id.bmapView);
		mMapController = mMapView.getController();
		mMapController.setZoom(14);
		mMapController.enableClick(true);
		LocationData ld = mMyApp.getLocData();
		mMapController.setCenter(new GeoPoint((int) (ld.latitude * 1e6),
				(int) (ld.longitude * 1e6)));
		mMapView.regMapViewListener(mMyApp.getMapManager(),
				new MyMapViewListener());
		// mMapView.setBuiltInZoomControls(true);
		// 创建 弹出泡泡图层
		createPaopao();

		// 定位初始化
		LocationClient locClient = mMyApp.getLocClient();
		LocationData locData = mMyApp.getLocData();
		mMyApp.registerLocationListener(myListener);
		locClient.start();

		// 定位图层初始化
		myLocationOverlay = new LocationOverlay(mMapView);
		// 设置定位数据
		myLocationOverlay.setData(locData);
		// 添加定位图层
		mMapView.getOverlays().add(myLocationOverlay);
		myLocationOverlay.enableCompass();
		// 修改定位数据后刷新图层生效
		mMapView.refresh();
	}

	/**
	 * 手动触发一次定位请求
	 */
	public void requestLocClick() {
		isRequest = true;
		mMyApp.getLocClient().requestLocation();
		Toast.makeText(this, "正在定位……", Toast.LENGTH_SHORT).show();
	}

	/**
	 * 修改位置图标
	 * 
	 * @param marker
	 */
	public void modifyLocationOverlayIcon(Drawable marker) {
		// 当传入marker为null时，使用默认图标绘制
		myLocationOverlay.setMarker(marker);
		// 修改图层，需要刷新MapView生效
		mMapView.refresh();
	}

	/**
	 * 创建弹出泡泡图层
	 */
	public void createPaopao() {
		viewCache = getLayoutInflater()
				.inflate(R.layout.custom_text_view, null);
		popupText = (TextView) viewCache.findViewById(R.id.textcache);
		// 泡泡点击响应回调
		PopupClickListener popListener = new PopupClickListener() {
			@Override
			public void onClickedPopup(int index) {
				Log.v("click", "clickapoapo");
			}
		};
		pop = new PopupOverlay(mMapView, popListener);
		MyLocationMapView.pop = pop;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onPause() {
		mMyApp.unregisterLocationListener(myListener);
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMyApp.registerLocationListener(myListener);
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// 退出时销毁定位
		mMyApp.destroyLocationClient();
		mMapView.destroy();
		mMyApp.destroyEngineManager();
		super.onDestroy();
		// System.exit(0);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mMapView.onSaveInstanceState(outState);

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mMapView.onRestoreInstanceState(savedInstanceState);
	}

	// -------------------------------------------------------------------------------
	// 内部类
	// -------------------------------------------------------------------------------

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			getSlidingMenu().toggle();
			return true;
		}

		if (super.onKeyUp(keyCode, event))
			return true;

		return false;
	}

	// 继承MyLocationOverlay重写dispatchTap实现点击处理
	public class LocationOverlay extends MyLocationOverlay {

		public LocationOverlay(MapView mapView) {
			super(mapView);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected boolean dispatchTap() {
			// TODO Auto-generated method stub
			// 处理点击事件,弹出泡泡
			LocationData locData = mMyApp.getLocData();
			popupText.setBackgroundResource(R.drawable.popup);
			popupText.setText("我的位置");
			pop.showPopup(MapUtil.getBitmapFromView(popupText), new GeoPoint(
					(int) (locData.latitude * 1e6),
					(int) (locData.longitude * 1e6)), 8);
			return true;
		}

	}

	// 地图事件监听类
	public class MyMapViewListener implements MKMapViewListener {

		@Override
		public void onClickMapPoi(MapPoi arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetCurrentMap(Bitmap arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onMapAnimationFinish() {
			float zoomLevel = mMapView.getZoomLevel();
			mZoomOutButton.setEnabled(zoomLevel > 3);
			mZoomInButton.setEnabled(zoomLevel < 19);
		}

		@Override
		public void onMapLoadFinish() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onMapMoveFinish() {
			float zoomLevel = mMapView.getZoomLevel();
			mZoomOutButton.setEnabled(zoomLevel > 3);
			mZoomInButton.setEnabled(zoomLevel < 19);

		}

	}
}
