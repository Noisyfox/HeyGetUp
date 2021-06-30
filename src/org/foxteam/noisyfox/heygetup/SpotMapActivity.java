package org.foxteam.noisyfox.heygetup;

import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.platform.comapi.basestruct.GeoPoint;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SpotMapActivity extends Activity {

	HeyGetUpApplication mMyApp = null;

	MyLocationMapView mMapView = null; // 地图View
	private MapController mMapController = null;

	Button mZoomInButton = null;
	Button mZoomOutButton = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_spot_map);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.title_back_text);

		mMyApp = (HeyGetUpApplication) this.getApplication();

		Button btn_back = (Button) findViewById(R.id.button_back);
		btn_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		TextView title = (TextView) findViewById(R.id.textView_title);
		title.setText("查看地图");

		mZoomInButton = (Button) findViewById(R.id.button_zoom_in);
		mZoomOutButton = (Button) findViewById(R.id.button_zoom_out);

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

		// 地图初始化
		mMapView = (MyLocationMapView) findViewById(R.id.bmapView);
		mMapController = mMapView.getController();
		mMapController.setZoom(14);
		mMapController.enableClick(true);
		LocationData ld = mMyApp.getLocData();
		mMapController.setCenter(new GeoPoint((int) (ld.latitude * 1e6),
				(int) (ld.longitude * 1e6)));
		mMapView.regMapViewListener(mMyApp.getMapManager(),
				new MKMapViewListener() {

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

				});

		String[] viewSpots = getIntent().getStringArrayExtra("spots");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.spot_map, menu);
		return true;
	}

	@Override
	protected void onPause() {
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// 退出时销毁定位
		mMapView.destroy();
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
}
