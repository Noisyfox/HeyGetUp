package org.foxteam.noisyfox.heygetup;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class SpotActivity extends Activity {

	ListView listView;
	View loadView;

	final String[] arrayListAction = new String[] { "标记为我想去的", "在地图上查看",
			"获取路线", "详细信息" };

	Dialog alertDialog;

	SpotInfo selectedSpot;

	List<SpotInfo> spots = new ArrayList<SpotInfo>();

	// alertDialog.show();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_spot);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.title_back_text);

		Button btn_back = (Button) findViewById(R.id.button_back);
		btn_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		TextView title = (TextView) findViewById(R.id.textView_title);
		title.setText("想去哪里玩呢");

		alertDialog = new AlertDialog.Builder(this).setItems(arrayListAction,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							break;
						case 1:
							String spotsName[] = new String[] { selectedSpot.name };
							Intent i = new Intent();
							i.putExtra("spots", spotsName);
							i.setClass(getApplicationContext(),
									SpotMapActivity.class);
							startActivity(i);
							break;
						case 2:
							break;
						case 3:
							break;
						}
					}
				}).create();

		loadView = findViewById(R.id.layout_load);

		spots.add(new SpotInfo(R.drawable.zhongshanling, "中山陵", "96%", "五星",
				"二星"));
		spots.add(new SpotInfo(R.drawable.fuzimiao, "夫子庙", "96%", "四星", "五星"));
		spots.add(new SpotInfo(R.drawable.zontongfu, "总统府", "93%", "三星", "五星"));
		spots.add(new SpotInfo(R.drawable.xuanwuhu, "玄武湖", "90%", "五星", "四星"));
		spots.add(new SpotInfo(R.drawable.qinghuaihe, "秦淮河", "88%", "五星", "五星"));
		spots.add(new SpotInfo(R.drawable.datusha, "大屠杀纪念馆", "80%", "三星", "二星"));
		spots.add(new SpotInfo(R.drawable.changjiangdaqiao, "长江大桥", "77%",
				"三星", "一星"));
		spots.add(new SpotInfo(R.drawable.tianwentai, "紫金山天文台", "56%", "五星",
				"一星"));

		listView = (ListView) findViewById(R.id.listView_everyone);
		listView.setAdapter(new SpotAdapter());

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				selectedSpot = (SpotInfo) arg1.getTag();
				alertDialog.show();
			}

		});

		findViewById(R.id.button_show_map).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {

						String spotsName[] = new String[spots.size()];
						for (int i = 0; i < spotsName.length; i++) {
							spotsName[i] = spots.get(i).name;
						}
						Intent i = new Intent();
						i.putExtra("spots", spotsName);
						i.setClass(getApplicationContext(),
								SpotMapActivity.class);
						startActivity(i);
					}

				});

		new LoadTask().execute();
	}

	class LoadTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPostExecute(Void result) {
			loadView.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.spot, menu);
		return true;
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		overridePendingTransition(R.anim.fade_in, R.anim.slide_out_bottom);
	}

	class SpotInfo {
		int img;
		String name;
		String famous;
		String beauty;
		String food;

		SpotInfo(int img, String name, String famous, String beauty, String food) {
			this.img = img;
			this.name = name;
			this.famous = famous;
			this.beauty = beauty;
			this.food = food;
		}
	}

	class SpotAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return spots.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return spots.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = mInflater.inflate(R.layout.item_spot, parent,
						false);
			}
			SpotInfo i = (SpotInfo) getItem(position);
			ImageView imageView = (ImageView) convertView
					.findViewById(R.id.imageView_pic);
			TextView textView_name = (TextView) convertView
					.findViewById(R.id.textView_name);
			TextView textView_famous = (TextView) convertView
					.findViewById(R.id.textView_famous);
			TextView textView_beauty = (TextView) convertView
					.findViewById(R.id.textView_beauty);
			TextView textView_food = (TextView) convertView
					.findViewById(R.id.textView_food);
			imageView.setImageResource(i.img);
			textView_name.setText(i.name);
			textView_famous.setText("热度:" + i.famous);
			textView_beauty.setText("风景指数:" + i.beauty);
			textView_food.setText("美食指数:" + i.food);
			convertView.setTag(i);
			return convertView;
		}

	}
}
