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
		//�õ����������������ĸ߶�
		header.measure(0, 0);
		headerHeight = header.getMeasuredHeight();
		Log.d(TAG, "headerHeight = " + headerHeight);
		header.setPadding(0, -headerHeight, 0, 0);
		//��ʼ����ͷ��ת��
		initAnimation();
	}
	
	private void initFooter() {
		footer = LayoutInflater.from(getContext()).inflate(R.layout.refresh_footer, null);
		this.addFooterView(footer);
		footer.measure(0, 0);
		footerHeight = footer.getMeasuredHeight();
		Log.d(TAG, "footerHeight = " + footerHeight);
		header.setPadding(0, -headerHeight, 0, 0);
		//���û�������
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
			if(distance > 0 && this.getVerticalScrollbarPosition() == 0) { //���»����ڵ�һ����Ŀ
				int padding = distance - headerHeight;
				header.setPadding(0, padding, 0, 0);
				//ͷ����ȫ��ʾ
				if(padding > 0 && mCurState != STATE_RELEASE_REFRESH) {
					//padding����0����ζ�������header�Ѿ���������
					mCurState = STATE_RELEASE_REFRESH;
					refreshState();
				} else if(distance < 0 && mCurState != STATE_PULL_REFRESH) {//ͷ��û����ȫ��ʾ
					mCurState = STATE_PULL_REFRESH;
					refreshState();
				}
				return true;//���ѵ��¼�----�¼����ݻ���
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
			//���صײ�
			footer.setPadding(0, -footerHeight, 0, 0);
			isLoadingMore = false;
		} else {
			mCurState = STATE_PULL_REFRESH;
			pb.setVisibility(View.INVISIBLE);
			arraw.setVisibility(View.VISIBLE);
			refreshInfo.setText("����ˢ��");
			header.setPadding(0, -headerHeight, 0, 0);
			//ˢ��ʱ�䡣
		}
		
	}
	
	/**
	 * ����״̬�ı����ֺͼ�ͷ�ķ���
	 */
	private void refreshState() {
		switch (mCurState) {
		case STATE_PULL_REFRESH:
			refreshInfo.setText("����ˢ��");
			pb.setVisibility(View.INVISIBLE);
			arraw.setVisibility(View.VISIBLE);
			//ִ�����ϵĶ���
			arraw.setAnimation(animDown);
			
			break;
		case STATE_RELEASE_REFRESH:
			refreshInfo.setText("�ɿ�ˢ��");
			pb.setVisibility(View.INVISIBLE);
			arraw.setVisibility(View.VISIBLE);
			//ִ�����ϵĶ���
			arraw.clearAnimation();//����Ҫ�����������������
			arraw.setAnimation(animUp);
			break;
		case STATE_REFRESHING:
			refreshInfo.setText("����ˢ��");
			pb.setVisibility(View.VISIBLE);
			arraw.clearAnimation();//����Ҫ�����������������
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
	 * ���ü���
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
	 * �������ϻ������¼�
	 */
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		//�������ײ����Ҳ��Ǽ��ظ���
		if(scrollState == SCROLL_STATE_IDLE || scrollState == SCROLL_STATE_FLING) {
			if(getLastVisiblePosition() == getCount() - 1 && !isLoadingMore) {
				Log.d(TAG, "�����ײ��ˡ���");
				//��ʾ�ײ���ͼ
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
