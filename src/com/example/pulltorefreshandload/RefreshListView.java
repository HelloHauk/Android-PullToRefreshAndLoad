package com.example.pulltorefreshandload;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * @author haopi
 * @创建时间 2016年7月23日 上午10:46:39
 * @描述 下拉刷新数据，下拉加载更多
 * 
 * 
 * @修改提交者 $Author: chp $
 * @提交时间 $Date: 2016-07-31 10:50:29 +0800 (Sun, 31 Jul 2016) $
 * @当前版本 $Rev: 19 $
 * 
 */
public class RefreshListView extends ListView
{
	private LinearLayout mHeadView;
	private View mFootView;
	private ImageView mHeadArrow;
	private TextView mHeadNotifyText;
	private TextView mHeadDateText;
	private TextView mFootNotifyText;
	private int mHeadViewMeasuredHeight;
	private int mFootViewMeasuredHeight;

	// 顶部下拉刷新
	public final static int STATE_HEAD_REFRESH_NONE = 0;
	public final static int STATE_HEAD_REFRESH_PULLDOWN = 1;
	// 顶部释放刷新
	public final static int STATE_HEAD_REFRESH_RELEASE = 2;
	// 顶部正在刷新
	public final static int STATE_HEAD_REFRESH_REFRESHING = 3;
	// 底部加载更多
	public boolean isLoadingMore = false;
	// 底部上拉刷新
	public final static int STATE_FOOT_LOADMORE_NONE = 0;
	public final static int STATE_FOOT_LOADMORE_PULLUP = 1;
	// 底部加载更多中
	public final static int STATE_FOOT_LOADMORE_LOADING = 2;
	// 下拉状态
	private static int refresh_current_state = STATE_HEAD_REFRESH_NONE;
	// 上拉状态
	private static int loadmore_current_state = STATE_FOOT_LOADMORE_NONE;
	// 顶部布局
	private LinearLayout mHeadRootView;
	// 顶部轮播图
	private View lunBoView;
	// 是否需要下拉刷新功能，用户自己选择
	private boolean isEnablePullRefresh;
	// 是否需要上拉加载更多，用户自己选择
	private boolean isEnablePullLoadMore;
	private float mDownY = -1;
	private int mListViewOnScreenY;
	// 下拉刷新箭头的动画
	RotateAnimation up_ra;
	// 下拉释放刷新箭头的动画
	RotateAnimation down_ra;
	// 下拉刷新时头部的环形加载进度
	private ProgressBar mHeadProgressBar;
	// 上拉加载更多时脚部的环形加载进度
	@SuppressWarnings("unused")
	private ProgressBar mFootLoadmorePb;
	
	private boolean isCanRefresh = true;
	private boolean isCanLoadMore = true;

	/** 设置是否有下拉刷新，给使用时设置的 */
	public void setEnablePullRefresh(boolean isEnablePullRefresh) {
		this.isEnablePullRefresh = isEnablePullRefresh;
	}
	/** 设置是否有上拉加载更多，给使用时设置的 */
	public void setEnablePullLoadMore(boolean isEnablePullLoadMore) {
		this.isEnablePullLoadMore = isEnablePullLoadMore;
	}


	public RefreshListView(Context context) {
		super(context, null);
		initView();
		initAnimation();
	}

	public RefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
		initAnimation();
	}

	private void initView() {
		// 初始化下拉刷新控件
		initHead();
		// 初始化上拉加载更多控件
		initFoot();
	}

	/** 初始化下拉刷新控件 */
	private void initHead() {
		mHeadRootView = (LinearLayout) View.inflate(getContext(), R.layout.listview_refresh_head_container, null);
		// 顶部刷新视图
		mHeadView = (LinearLayout) mHeadRootView.findViewById(R.id.ll_refresh_head_view);
		// 头部刷新的箭头
		mHeadArrow = (ImageView) mHeadRootView.findViewById(R.id.iv_refresh_head_arrow);
		// 头部加载进度条
		mHeadProgressBar = (ProgressBar) mHeadRootView.findViewById(R.id.pb_loading);
		// 头部刷新的文字描述
		mHeadNotifyText = (TextView) mHeadRootView.findViewById(R.id.tv_refresh_head_notify);
		// 头部刷新的时间
		mHeadDateText = (TextView) mHeadRootView.findViewById(R.id.tv_refresh_head_date);
		// 测量头部加载更多控件的高度
		mHeadView.measure(0, 0);
		// 头部刷新控件的高度
		mHeadViewMeasuredHeight = mHeadView.getMeasuredHeight();
		// 设置Padding值，使头部下拉控件默认不可见
		mHeadView.setPadding(0, -mHeadViewMeasuredHeight, 0, 0);
		// 把头布局加到自定义ListView中头部
		addHeaderView(mHeadRootView);
	}

	@Override
	public void addHeaderView(View v) {
		if (isEnablePullRefresh) {
			lunBoView = v;
			mHeadRootView.addView(v);
		} else {
			super.addHeaderView(v);
		}
	}

	/** 初始化上拉加载更多控件 */
	private void initFoot() {
		mFootView = View.inflate(getContext(), R.layout.listview_refresh_foot, null);
		// 脚部上拉后显示加载更多的进度条
		mFootLoadmorePb = (ProgressBar) mFootView.findViewById(R.id.pb_refresh_foot_loadingmore);
		// 脚部加载更多的文字描述
		mFootNotifyText = (TextView) mFootView.findViewById(R.id.tv_refresh_foot_notify);
		// 策略脚部上拉加载更多控件的高度
		mFootView.measure(0, 0);
		mFootViewMeasuredHeight = mFootView.getMeasuredHeight();
		// 默认不可见
		mFootView.setPadding(0, 0, 0, -mFootViewMeasuredHeight);
		// 加入自定义ListView中
		addFooterView(mFootView);
	}

	/** 初始化动画 */
	private void initAnimation() {
		up_ra = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		up_ra.setDuration(500);
		up_ra.setFillAfter(true);

		down_ra = new RotateAnimation(-180, -360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		down_ra.setDuration(500);
		down_ra.setFillAfter(true);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mDownY = ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			// 如果没有采集到Y点坐标
			if (mDownY == -1) {
				mDownY = ev.getY();
			}
			float moveY = ev.getY();
			float dy = moveY - mDownY;
			// 这里轮播图已经完全显示
			// 如果是上下滑动.处理自己的事件，不让listview原生的拖动事件生效
			// 如果是往下拉，且划到最顶部
			if (dy > 0 && getFirstVisiblePosition() == 0) {
				// 如果不能下拉刷新
				if (!isEnablePullRefresh) {
					break;
				}
				// 如果当前为正在刷新状态
				if (refresh_current_state == STATE_HEAD_REFRESH_REFRESHING) {
					break;
				}
				// 如果轮播图没有完全显示
				if (!isLunBOFullShow()) {
					break;
				}
				// 设置顶部控件Padding的参数
				float scrollYDistance = -mHeadViewMeasuredHeight + dy;
				// 如果顶部刷新条没有完全滑出来，且当前不是下拉刷新状态
				if (scrollYDistance < 0 && refresh_current_state != STATE_HEAD_REFRESH_PULLDOWN) {
					refresh_current_state = STATE_HEAD_REFRESH_PULLDOWN;
					refreshRefreshState();
					// 如果顶部刷新条完全出现，且当前不为释放刷新状态
				} else if (scrollYDistance >= 0 && refresh_current_state != STATE_HEAD_REFRESH_RELEASE) {
					refresh_current_state = STATE_HEAD_REFRESH_RELEASE;
					refreshRefreshState();
				}
				mHeadView.setPadding(0, (int) scrollYDistance, 0, 0);
				// 自己处理
				// return true;
			}
			// 如果往上拉，并且在最后一个位置，且当前不是加载更多状态
			if (dy < 0 && getLastVisiblePosition() == getAdapter().getCount() - 1) {
				// 如果不能下拉刷新
				if (!isEnablePullLoadMore) {
					break;
				}
				// 如果当前为正在加载中状态
				if (loadmore_current_state == STATE_FOOT_LOADMORE_LOADING) {
					break;
				}
				// 设置顶部控件Padding的参数
				float scrollYDistance = -mFootViewMeasuredHeight - dy;

				if (scrollYDistance < 0 && loadmore_current_state != STATE_FOOT_LOADMORE_PULLUP) {
					loadmore_current_state = STATE_FOOT_LOADMORE_PULLUP;
					refreshLoadmoreState();
				}
				if (scrollYDistance >= 0 && loadmore_current_state != STATE_FOOT_LOADMORE_LOADING) {
					loadmore_current_state = STATE_FOOT_LOADMORE_LOADING;
					refreshLoadmoreState();
				}
				mFootView.setPadding(0, 0, 0, (int) scrollYDistance);
				//setSelection(getAdapter().getCount());
			}
			break;
		case MotionEvent.ACTION_UP:
			// 如果当前状态为下拉刷新
			if (refresh_current_state == STATE_HEAD_REFRESH_PULLDOWN) {
				mHeadView.setPadding(0, -mHeadViewMeasuredHeight, 0, 0);
			} else if (refresh_current_state == STATE_HEAD_REFRESH_RELEASE) {
				// 如果当前状态为释放刷新
				mHeadView.setPadding(0, 0, 0, 0);
				refresh_current_state = STATE_HEAD_REFRESH_REFRESHING;
				refreshRefreshState();
				if(isCanRefresh){
					if (mOnRefreshOrLoadMoreListener != null) {
						isCanRefresh = false;
						mOnRefreshOrLoadMoreListener.refresh();
					}
				}
			}
			// 如果是底部加载更多状态
			if (loadmore_current_state == STATE_FOOT_LOADMORE_PULLUP) {
				mFootView.setPadding(0, 0, 0, -mFootViewMeasuredHeight);
			} else if (loadmore_current_state == STATE_FOOT_LOADMORE_LOADING) {
				mFootView.setPadding(0, 0, 0, 0);
				if(isCanLoadMore){
					if (mOnRefreshOrLoadMoreListener != null) {
						isCanLoadMore = false;
						mOnRefreshOrLoadMoreListener.loadMore();
					}
				}
			}
			break;

		default:
			break;
		}
		return super.onTouchEvent(ev);
	}

	/** 轮播图是否完全显示 */
	private boolean isLunBOFullShow() {
		if(lunBoView != null){
			int[] location = new int[2];
			// 获取listView的位置
			this.getLocationOnScreen(location);
			// 获取ListView第一次显示的位置
			if (mListViewOnScreenY == 0) {
				mListViewOnScreenY = location[1];
			}
			// 获取轮播图的位置
			lunBoView.getLocationOnScreen(location);
			int lunBoViewOnScreenY = location[1];

			// 如果轮播图没有完全显示
			if (lunBoViewOnScreenY < mListViewOnScreenY) {
				return false;
			}
			return true;
		}
		return true;
	}

	/** 根据下拉状态刷新控件显示 */
	private void refreshRefreshState() {
		switch (refresh_current_state) {
		case STATE_HEAD_REFRESH_PULLDOWN:
			mHeadProgressBar.setVisibility(GONE);
			mHeadArrow.setVisibility(VISIBLE);
			mHeadNotifyText.setText("下拉刷新");
			mHeadArrow.setImageResource(R.drawable.common_listview_headview_red_arrow);
			mHeadArrow.startAnimation(down_ra);
			break;
		case STATE_HEAD_REFRESH_RELEASE:
			mHeadProgressBar.setVisibility(GONE);
			mHeadArrow.setVisibility(VISIBLE);
			mHeadNotifyText.setText("释放刷新");
			mHeadArrow.setImageResource(R.drawable.common_listview_headview_red_arrow);
			mHeadArrow.startAnimation(up_ra);
			break;
		case STATE_HEAD_REFRESH_REFRESHING:
			mHeadArrow.clearAnimation(); // 清除所有动画
			mHeadNotifyText.setText("正在刷新");
			mHeadArrow.setVisibility(GONE);
			mHeadProgressBar.setVisibility(VISIBLE);
			break;
		default:
			break;
		}
	}

	/** 根据上拉状态刷新控件显示 */
	private void refreshLoadmoreState() {
		switch (loadmore_current_state) {
		case STATE_FOOT_LOADMORE_PULLUP:
			mFootNotifyText.setText("加载更多");
			break;
		case STATE_FOOT_LOADMORE_LOADING:
			mFootNotifyText.setText("正在加载");
			break;

		default:
			break;
		}
	}

	/** 格式化时间 */
	@SuppressLint("SimpleDateFormat")
	public String getCurrentFormatDate() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// 格式化当前刷新的时间
		String format = simpleDateFormat.format(new Date());
		return format;
	}

	/** 如果下拉刷新或上拉加载更多结束 */
	public void refreshOrLoadMoreFinish() {
		if (refresh_current_state == STATE_HEAD_REFRESH_REFRESHING) {// 如果当前状态为刷新中
			refresh_current_state = STATE_HEAD_REFRESH_PULLDOWN;
			// 隐藏下拉视图
			mHeadView.setPadding(0, -mHeadViewMeasuredHeight, 0, 0);
			mHeadProgressBar.setVisibility(GONE);
			mHeadArrow.setVisibility(VISIBLE);
			mHeadArrow.setImageResource(R.drawable.common_listview_headview_red_arrow);
			mHeadDateText.setText(getCurrentFormatDate());
			mHeadNotifyText.setText("下拉刷新");
			isCanRefresh = true;
		}
		if (loadmore_current_state == STATE_FOOT_LOADMORE_LOADING) {// 如果当前状态为上拉加载更多
			loadmore_current_state = STATE_FOOT_LOADMORE_PULLUP;
			mFootNotifyText.setText("加载更多");
			// 隐藏上拉视图
			mFootView.setPadding(0, 0, 0, -mFootViewMeasuredHeight);
			isCanLoadMore = true;
		}
	}

	/** 设置上拉刷新和下拉加载更多的监听 */
	public interface OnRefreshOrLoadMoreListener
	{
		// 下拉正在刷新时调用
		void refresh();

		// 上拉正在加载时调用
		void loadMore();
	}

	/** 上拉刷新和下拉加载更多的监听 */
	private OnRefreshOrLoadMoreListener mOnRefreshOrLoadMoreListener;

	/** 设置上拉刷新和下拉加载更多的监听事件 */
	public void setOnRefreshOrLoadMoreListener(OnRefreshOrLoadMoreListener onRefreshOrLoadMoreListener) {
		this.mOnRefreshOrLoadMoreListener = onRefreshOrLoadMoreListener;
	}
}
