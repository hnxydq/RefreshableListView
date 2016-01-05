package com.tfxiaozi.refreshablelistview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

public class RefreshListView extends ListView implements OnScrollListener{

	private static final String TAG = RefreshListView.class.getSimpleName();
	private View header;
	private int startY;
	private int headerHeight;
	private int footerHeight;
	
	public static final int STATE_PULL_REFRESH = 0;
	public static final int STATE_RELEASE_REFRESH = 1;
	public static final int STATE_REFRESHING = 2;
	private int mCurState = STATE_PULL_REFRESH;
	private TextView refreshInfo;
	private ProgressBar pb;
	private ImageView arraw;
	private RotateAnimation animUp;
	private RotateAnimation animDown;
	private boolean isLoadingMore = false;
	
	public RefreshListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initHeader();
		initFooter();
	}

	

	public RefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initHeader();
		initFooter();
	}

	public RefreshListView(Context context) {
		super(context);
		initHeader();
		initFooter();
	}

	private void initHeader() {
		header = LayoutInflater.from(getContext()).inflate(R.layout.refresh_header, null);
		refreshInfo = (TextView) header.findViewById(R.id.refresh_info);
		pb = (ProgressBar) header.findViewById(R.id.pb);
		arraw = (ImageView) header.findViewById(R.id.arraw);
		this.addHeaderView(header);
		//得到父容器测量出来的高度
		header.measure(0, 0);
		headerHeight = header.getMeasuredHeight();
		Log.d(TAG, "headerHeight = " + headerHeight);
		header.setPadding(0, -headerHeight, 0, 0);
		//初始化箭头的转动
		initAnimation();
	}
	
	private void initFooter() {
		footer = LayoutInflater.from(getContext()).inflate(R.layout.refresh_footer, null);
		this.addFooterView(footer);
		footer.measure(0, 0);
		footerHeight = footer.getMeasuredHeight();
		Log.d(TAG, "footerHeight = " + footerHeight);
		header.setPadding(0, -headerHeight, 0, 0);
		//设置滑动监听
		this.setOnScrollListener(this);
	}
	
	private void initAnimation() {
		animUp = new RotateAnimation(0, -180,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		animUp.setDuration(200);
		animUp.setFillAfter(true);
		animDown = new RotateAnimation(-180, 0,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		animDown.setDuration(200);
		animDown.setFillAfter(true);
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			startY = (int) ev.getRawY();
			break;
		case MotionEvent.ACTION_MOVE:
			if(startY == -1) {
				startY = (int) ev.getRawY();
			}
			int endY = (int) ev.getRawY();
			int distance = endY - startY;
			if(distance > 0 && this.getVerticalScrollbarPosition() == 0) { //向下滑且在第一个条目
				int padding = distance - headerHeight;
				header.setPadding(0, padding, 0, 0);
				//头部完全显示
				if(padding > 0 && mCurState != STATE_RELEASE_REFRESH) {
					//padding大于0，意味着上面的header已经拉下来了
					mCurState = STATE_RELEASE_REFRESH;
					refreshState();
				} else if(distance < 0 && mCurState != STATE_PULL_REFRESH) {//头部没有完全显示
					mCurState = STATE_PULL_REFRESH;
					refreshState();
				}
				return true;//消费掉事件----事件传递机制
			} 
			break;
		case MotionEvent.ACTION_UP:
			startY = -1;
			if(mCurState == STATE_RELEASE_REFRESH) {
				mCurState = STATE_REFRESHING;
				header.setPadding(0, 0, 0, 0);
				refreshState();
			} else if(mCurState == STATE_PULL_REFRESH) {
				header.setPadding(0, -headerHeight, 0, 0);
			}
			break;

		default:
			break;
		}
		
		
		return super.onTouchEvent(ev);
	}

	public void onRefreshComplete() {
		if(isLoadingMore) {
			//隐藏底部
			footer.setPadding(0, -footerHeight, 0, 0);
			isLoadingMore = false;
		} else {
			mCurState = STATE_PULL_REFRESH;
			pb.setVisibility(View.INVISIBLE);
			arraw.setVisibility(View.VISIBLE);
			refreshInfo.setText("下拉刷新");
			header.setPadding(0, -headerHeight, 0, 0);
			//刷新时间。
		}
		
	}
	
	/**
	 * 根据状态改变文字和箭头的方向
	 */
	private void refreshState() {
		switch (mCurState) {
		case STATE_PULL_REFRESH:
			refreshInfo.setText("下拉刷新");
			pb.setVisibility(View.INVISIBLE);
			arraw.setVisibility(View.VISIBLE);
			//执行向上的动画
			arraw.setAnimation(animDown);
			
			break;
		case STATE_RELEASE_REFRESH:
			refreshInfo.setText("松开刷新");
			pb.setVisibility(View.INVISIBLE);
			arraw.setVisibility(View.VISIBLE);
			//执行向上的动画
			arraw.clearAnimation();//必须要先清除动画才能隐藏
			arraw.setAnimation(animUp);
			break;
		case STATE_REFRESHING:
			refreshInfo.setText("正在刷新");
			pb.setVisibility(View.VISIBLE);
			arraw.clearAnimation();//必须要先清除动画才能隐藏
			arraw.setVisibility(View.INVISIBLE);
			if(onRefreshListener != null) {
				onRefreshListener.onRefreshSuccess();
			}
			break;
		default:
			break;
		}
		
	}
	
	/**
	 * 设置监听
	 */
	public interface OnRefreshListener {
		void onRefreshSuccess();
		void onLoadMoreSuccess();
	}
	
	private OnRefreshListener onRefreshListener;
	private View footer;

	public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
		this.onRefreshListener = onRefreshListener;
	}

	/**
	 * 监听向上滑动的事件
	 */
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		//滑动到底部，且不是加载更多
		if(scrollState == SCROLL_STATE_IDLE || scrollState == SCROLL_STATE_FLING) {
			if(getLastVisiblePosition() == getCount() - 1 && !isLoadingMore) {
				Log.d(TAG, "滑到底部了。。");
				//显示底部视图
				footer.setPadding(0, 0, 0, 0);
				isLoadingMore = true;
				if(onRefreshListener != null) {
					onRefreshListener.onLoadMoreSuccess();
				}
			}
		}
	}
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		
	}
	
}
