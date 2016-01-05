package com.tfxiaozi.refreshablelistview;

import com.tfxiaozi.refreshablelistview.RefreshListView.OnRefreshListener;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ArrayAdapter;

public class MainActivity extends Activity {

	RefreshListView refreshLv;
	String data[] = new String[]{"aaa", "bbb", "ccc", "ddd",
			"aaa", "bbb", "ccc", "ddd",
			"aaa", "bbb", "ccc", "ddd",
			"aaa", "bbb", "ccc", "ddd"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        refreshLv = (RefreshListView) findViewById(R.id.refreshLv);
        
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
				android.R.id.text1, data);
		refreshLv.setAdapter(adapter);
		
		//请求刷新成功了
		refreshLv.setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefreshSuccess() {
				new Handler() {
					@Override
					public void handleMessage(Message msg) {
						refreshLv.onRefreshComplete();
						super.handleMessage(msg);
					}
				}.sendEmptyMessageDelayed(0, 3000);
				
			}

			@Override
			public void onLoadMoreSuccess() {
				new Handler() {
					@Override
					public void handleMessage(Message msg) {
						refreshLv.onRefreshComplete();
						super.handleMessage(msg);
					}
				}.sendEmptyMessageDelayed(0, 3000);
				
			}
		});
		

    }

}
