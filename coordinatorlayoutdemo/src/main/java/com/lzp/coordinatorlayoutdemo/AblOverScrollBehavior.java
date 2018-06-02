package com.lzp.coordinatorlayoutdemo;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.lang.reflect.Method;

/**
 * Created by lillian on 2018/6/1.
 */

public class AblOverScrollBehavior extends AppBarLayout.Behavior {
    public AblOverScrollBehavior() {
        super();
    }

    public AblOverScrollBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private boolean mOverScrollMode = false;
    private boolean mIsBeingDragged = false;

    private int mLastMotionY;
    private int mActivePointerId = -1;
    private int mTouchSlop = -1;

    private float mOverScrollMulitiplier = DEFAULT_OVERSCROLL_MULITIPLIER;

    private static final float DEFAULT_OVERSCROLL_MULITIPLIER = 0.3f;


    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, AppBarLayout child, MotionEvent ev) {
        mIsBeingDragged = super.onInterceptTouchEvent(parent, child, ev);
        if (mTouchSlop < 0) {
            mTouchSlop = ViewConfiguration.get(parent.getContext()).getScaledTouchSlop();
        }

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (mIsBeingDragged) {
                    mLastMotionY = (int) ev.getY();
                    mActivePointerId = ev.getPointerId(0);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIsBeingDragged) {
                    final int activePointerId = mActivePointerId;
                    final int pointerIndex = ev.findPointerIndex(activePointerId);
                    if (pointerIndex == -1) {
                        break;
                    }
                    final int y = (int) ev.getY(pointerIndex);
                    mLastMotionY = y;
                }

                break;
        }
        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(CoordinatorLayout parent, AppBarLayout child, MotionEvent ev) {
        super.onTouchEvent(parent, child, ev);

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                final int x = (int) ev.getX();
                final int y = (int) ev.getY();

                if (parent.isPointInChildBounds(child, x, y) && getSuperCanDragView(child)) {
                    mLastMotionY = y;
                    mActivePointerId = ev.getPointerId(0);
                } else {
                    return false;
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                if (activePointerIndex == -1) {
                    return false;
                }

                final int y = (int) ev.getY(activePointerIndex);
                int dy = mLastMotionY - y;

                if (!mIsBeingDragged && Math.abs(dy) > mTouchSlop) {
                    mIsBeingDragged = true;
                    if (dy > 0) {
                        dy -= mTouchSlop;
                    } else {
                        dy += mTouchSlop;
                    }
                }

                if (mIsBeingDragged) {
                    mLastMotionY = y;
                    //判断是否处于overscroll状态，或者
                    //AppBarLayout现在位于开始位置，可以向下滚动
                    if (mOverScrollMode || child.getTop() == 0) {
                        if (!mOverScrollMode) {
                            mOverScrollMode = true;
                        }
                        overScroll(child, dy);
                    }
                }
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mOverScrollMode) {
                    resetOverScroll(child);
                    mOverScrollMode = false;
                }
                break;
        }
        return true;
    }

    private boolean getSuperCanDragView(AppBarLayout appBarLayout) {
        try {
            Method method = this.getClass().getDeclaredMethod("canDragView", AppBarLayout.class);
            method.setAccessible(true);
            return (Boolean) method.invoke(this, appBarLayout);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout parent, AppBarLayout child, View directTargetChild, View target, int nestedScrollAxes, int type) {
        boolean result = super.onStartNestedScroll(parent, child, directTargetChild, target, nestedScrollAxes, type);
        if (!result && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0) {
            if (child.getTop() == 0 && child.getLeft() == 0) {
                result = true;
            }
        }
        return result;
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, int dx, int dy, int[] consumed, int type) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);
        if (consumed[1] == 0) {
            if (dy < 0 && type != ViewCompat.TYPE_NON_TOUCH) {//向下滑动
                if (!mOverScrollMode) {
                    mOverScrollMode = child.getTop() == 0;
                }
                if (mOverScrollMode) {
                    consumed[1] = overScroll(child, dy);
                }
            }
        }
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        if (mOverScrollMode) return;

        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
    }

    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout abl, View target, int type) {
        super.onStopNestedScroll(coordinatorLayout, abl, target, type);
        if (mOverScrollMode) {
            resetOverScroll(abl);
            mOverScrollMode = false;
        }
    }

    private int overScroll(AppBarLayout appBarLayout, int overScrollDistance) {
        if (overScrollDistance > 0) return 0;

        int tmpDy = (int) (overScrollDistance * mOverScrollMulitiplier);
        appBarLayout.offsetTopAndBottom(-tmpDy);
        return tmpDy;
    }

    private void resetOverScroll(final AppBarLayout appBarLayout) {
        ValueAnimator animator = ValueAnimator.ofInt(0, appBarLayout.getTop());
        animator.setDuration(300);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            int last = 0;

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                int distance = value - last;
                last = value;
                if (appBarLayout.getTop() - distance < 0) {
                    distance = appBarLayout.getTop();
                }
                appBarLayout.offsetTopAndBottom(-distance);
            }
        });
        animator.start();
//        appBarLayout.offsetTopAndBottom(-appBarLayout.getTop());
    }
}
