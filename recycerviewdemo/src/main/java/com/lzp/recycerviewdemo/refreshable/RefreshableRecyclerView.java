package com.lzp.recycerviewdemo.refreshable;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

public class RefreshableRecyclerView extends RecyclerView {
    private int STATE_IDLE = -1;
    private int STATE_PULLINGDOW = 0;
    private int STATE_REFRESHING = 1;

    private RefreshHeaderLayout mHeaderlayout;
    private View mHeader;

    private int mLastTouchX, mLastTouchY;
    private int mTouchSlop;
    private float mMulitiplier = 0.5f;
    private boolean mRhlFirstVisiable = false;

    private RefreshListener mRefreshListener;
    private int mState = STATE_IDLE;

    public interface RefreshListener {
        void onPullingDown(int dy);

        void onRefreshing();
    }

    public RefreshableRecyclerView(Context context) {
        this(context, null);
    }

    public RefreshableRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshableRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public void setRefreshHeader(View view) {
        if (mHeader != null) {
            removeRefreshHeader();
        }
        if (mHeader != view) {
            mHeader = view;
            initRefreshHeaderLayout();
            mHeaderlayout.addView(mHeader);
        }
    }

    public void setRefreshHeader(int resId) {
        initRefreshHeaderLayout();
        View view = LayoutInflater.from(getContext()).inflate(resId, mHeaderlayout, false);
        if (view != null) {
            setRefreshHeader(view);
        }
    }

    public void setRefreshListener(RefreshListener listener) {
        mRefreshListener = listener;
    }

    @Override
    public void setAdapter(RecyclerView.Adapter adapter) {
        initRefreshHeaderLayout();
        super.setAdapter(new ProxyAdapter(adapter, mHeaderlayout));
    }

    private void removeRefreshHeader() {
        if (mHeaderlayout != null) {
            mHeaderlayout.removeView(mHeader);
        }
    }

    private void initRefreshHeaderLayout() {
        if (mHeaderlayout == null) {
            mHeaderlayout = new RefreshHeaderLayout(getContext());
            mHeaderlayout.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
        }
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        if (isRefreshHeaderVisiable()) {
            if (mState == STATE_IDLE) {
                mLastTouchX = (int) e.getX();
                mLastTouchY = (int) e.getY();
            }
            return true;
        }
        return super.onInterceptTouchEvent(e);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (isRefreshHeaderVisiable()) {
            boolean result = false;
            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if (mState == STATE_IDLE) {
                        mLastTouchX = (int) e.getX();
                        mLastTouchY = (int) e.getY();
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    int curX = (int) e.getX();
                    int curY = (int) e.getY();
                    int deltaY = curY - mLastTouchY;
                    int deltaX = curX - mLastTouchX;

                    if (deltaY > 0
                            && Math.abs(deltaY) > mTouchSlop
                            && Math.abs(deltaY) > Math.abs(deltaX) / 2) {//向下滑动
                        setRefreshHeaderLayoutHeight(deltaY);
                        result = true;
                    } else if (deltaY < 0
                            && Math.abs(deltaY) > mTouchSlop
                            && Math.abs(deltaY) > Math.abs(deltaX) / 2) {//向上滑动
                        if (mRhlFirstVisiable) {
                            setRefreshHeaderLayoutHeight(Math.abs(deltaY));
                            result = true;
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    enterRefreshingState();
                    break;
            }
            if (result) {
                return true;
            }
        }

        return super.onTouchEvent(e);
    }

    private void setRefreshHeaderLayoutHeight(int height) {
        if (mRefreshListener != null) {
            mRefreshListener.onPullingDown(height);
        }

        int realHeight = (int) (height * mMulitiplier);
        if (mHeaderlayout != null) {
            mHeaderlayout.getLayoutParams().height = realHeight;
            mHeaderlayout.requestLayout();
        }
    }

    private void enterRefreshingState() {
        if (mHeaderlayout != null) {
            final ViewGroup.LayoutParams params = mHeaderlayout.getLayoutParams();
            int height = params.height;
            if (height > 0) {
                mRefreshing = true;
                if (mRefreshListener != null) {
                    mRefreshListener.onRefreshing();
                }
            }
        }
    }

    public void stopRefresh() {
        if (mRefreshing) {
            final ViewGroup.LayoutParams params = mHeaderlayout.getLayoutParams();
            int height = params.height;
            if (height > 0) {
                ValueAnimator animator = ValueAnimator.ofInt(height, 0);
                animator.setDuration(200);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int value = (int) animation.getAnimatedValue();
                        if (value == 0) {
                            mRefreshing = false;
                        }
                        params.height = value;
                        mHeaderlayout.requestLayout();
                    }
                });
                animator.start();
            }
        }
    }

    private boolean isRefreshHeaderVisiable() {
        if (getAdapter() == null || getAdapter().getItemCount() == 0) {
            return false;
        }
        int pos = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
//        Log.e("Test", "pos=" + pos);
        if (pos == 1) {
            mRhlFirstVisiable = false;
            return getLayoutManager().getChildAt(0).getTop() == 0;
        } else if (pos == 0) {
            mRhlFirstVisiable = true;
            return true;
        }
        return false;
    }

    /**********************************************************************/
    /*                             ProxyAdapter                           */

    /**********************************************************************/
    public static class ProxyAdapter extends RecyclerView.Adapter<ViewHolder> {
        private static final int ITEM_TYPE_HEADER = Integer.MIN_VALUE;
        private Adapter mBase;
        private RefreshHeaderLayout mHeaderLayout;

        public ProxyAdapter(RecyclerView.Adapter base, RefreshHeaderLayout headerLayout) {
            mBase = base;
            mHeaderLayout = headerLayout;
        }

        @Override
        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
            if (mBase != null) {
                mBase.onAttachedToRecyclerView(recyclerView);
            }
        }

        @Override
        public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
            if (mBase != null) {
                mBase.onViewAttachedToWindow(holder);
            }
        }

        @Override
        public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
            if (mBase != null) {
                mBase.onViewDetachedFromWindow(holder);
            }
        }


        @Override
        public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
            if (mBase != null) {
                mBase.onDetachedFromRecyclerView(recyclerView);
            }
        }

        @Override
        public int getItemCount() {
            return mBase.getItemCount() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return ITEM_TYPE_HEADER;
            }
            if (position > 0 && position < mBase.getItemCount()) {
                return mBase.getItemViewType(position - 1);
            }
            throw new IllegalArgumentException("Wrong type! position = " + position);
        }


        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == ITEM_TYPE_HEADER) {
                return new HeaderViewHolder(this.mHeaderLayout);
            } else {
                return mBase.onCreateViewHolder(parent, viewType);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (position > 0 && position < this.mBase.getItemCount()) {
                this.mBase.onBindViewHolder(holder, position - 1);
            }
        }

        public static class HeaderViewHolder extends ViewHolder {
            public HeaderViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}
