package com.example.pulltorefreshandload;

import java.util.ArrayList;
import java.util.List;

import com.example.pulltorefreshandload.RefreshListView.OnRefreshOrLoadMoreListener;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Window;
import android.widget.ArrayAdapter;

public class MainActivity extends Activity
{

	private List<String> mData;
	private RefreshListView mRefreshListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		mRefreshListView = (RefreshListView) findViewById(R.id.refresh_listview);

		// 初始化数据
		mData = new ArrayList<String>();
		for (int i = 0; i < 20; i++) {
			mData.add("item" + i);
		}

		// 设置允许用户下拉刷新和上拉加载更多
		mRefreshListView.setEnablePullRefresh(true);

		// 设置列表样式
		mRefreshListView.setCacheColorHint((Color.TRANSPARENT));
		mRefreshListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		mRefreshListView.setFastScrollEnabled(true);

		// 设置适配器
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, mData);
		mRefreshListView.setAdapter(adapter);

		// 设置监听
		mRefreshListView.setOnRefreshOrLoadMoreListener(new OnRefreshOrLoadMoreListener() {

			@Override
			public void refresh() {
				new Thread(new Runnable() {
					public void run() {
						// 下拉刷新的操作
						SystemClock.sleep(3000);
						MainActivity.this.runOnUiThread(new Runnable() {
							public void run() {
								// 刷新完成后隐藏下拉或上拉控件
								mRefreshListView.refreshOrLoadMoreFinish();
							}
						});
					}
				}).start();
			}

			@Override
			public void loadMore() {
				new Thread(new Runnable() {
					public void run() {
						// 下拉刷新的操作
						SystemClock.sleep(3000);
						MainActivity.this.runOnUiThread(new Runnable() {
							public void run() {
								// 刷新完成后隐藏下拉或上拉控件
								mRefreshListView.refreshOrLoadMoreFinish();
							}
						});
					}
				}).start();
			}
		});
	}
}
