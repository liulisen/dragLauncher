package com.demo.simple;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;

/**
 * Created by Lisen.Liu on 2018/11/2.
 */

public class DragLayerLayout<T> extends FrameLayout {
    public DragLayerLayout(@NonNull Context context) {
        super(context);
    }

    public DragLayerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DragLayerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public interface IDragActionCallback<T> {
        void onStartDrag(View dragView, int dragPage, int dragPosition, T data);

        void onSlide2Page(View dragView, int dragPage, int dragPosition, int currentPage, T data);

        void onDrop(View dragView, int dragPage, int dragPosition, int dropPage, T data);
    }

    public interface IDragDataCallback<T> {
        void addItem(T data);

        void removeItem(T data);
    }

    private IDragActionCallback<T> mDragCallback;
    private int mStartDragPageIndex = -1;
    private RectF mRect = new RectF();
    private static final int DRAG_STATE_IDLE = -1;
    private static final int DRAG_STATE_START = 0;
    private static final int DRAG_STATE_DRAG = 1;
    private int mDragState = -1;
    private PointF mLastPoint = new PointF();
    private Bitmap mDragSnapShot;
    private View mDragView;
    private ViewPager mViewPager;
    private T mData;
    private int mStartPosition;

    public void setDragCallback(IDragActionCallback<T> dragCallback) {
        mDragCallback = dragCallback;
    }

    public void startDrag(View childView, T data, int startPosition) {
        mViewPager = null;
        mDragView = childView;
        mData = data;
        mStartPosition = startPosition;

        float offsetX = 0;
        float offsetY = 0;

        ViewParent vp = childView.getParent();
        while (vp != null && vp instanceof View && vp != this) {
            if (!(vp.getParent() instanceof ViewPager)) {
                offsetX += ((View) vp).getX();
                offsetY += ((View) vp).getY();
            } else if (mViewPager == null) {
                mViewPager = (ViewPager) vp.getParent();
            }
            vp = vp.getParent();
        }

        if (mViewPager == null) {
            return;
        }

        if (mDragSnapShot != null) {
            mDragSnapShot.recycle();
        }
        mDragSnapShot = Utils.getViewSnapshot(childView, 0xA0);

        mRect.set(childView.getX(), childView.getY(), childView.getX() + childView.getWidth(), childView.getY() + childView.getHeight());
        mRect.offset(offsetX, offsetY);
        mDragState = DRAG_STATE_START;
        Utils.vibrate(getContext());
        postInvalidate();

        mStartDragPageIndex = mViewPager.getCurrentItem();
        if (mDragCallback != null) {
            mDragCallback.onStartDrag(mDragView, mStartDragPageIndex, mStartPosition, mData);
        }
    }

    private void endDrag() {
        mDragState = DRAG_STATE_IDLE;
        postInvalidate();
    }


    private void onDrop() {
        if (mDragCallback != null && mViewPager != null) {
            PagerAdapter adapter = mViewPager.getAdapter();
            if (adapter != null) {
                int index = mViewPager.getCurrentItem();
                mDragCallback.onDrop(mDragView, mStartDragPageIndex, mStartPosition, index, mData);
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return (mDragState >= DRAG_STATE_START) || super.onInterceptTouchEvent(ev);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                float x = event.getX();
                float y = event.getY();
                checkSlide(x, y);
                if (mDragState == DRAG_STATE_START) {
                    mDragState = DRAG_STATE_DRAG;
                } else if (mDragState == DRAG_STATE_DRAG) {
                    float dx = x - mLastPoint.x;
                    float dy = y - mLastPoint.y;
                    mRect.offset(dx, dy);
                    postInvalidate();
                }
                mLastPoint.set(x, y);
                break;
            }
            case MotionEvent.ACTION_UP: {
                resetSlideState();
                endDrag();
                onDrop();
                break;
            }

        }
        return super.onTouchEvent(event);
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mDragState >= DRAG_STATE_START && mDragSnapShot != null && !mDragSnapShot.isRecycled()) {
            canvas.drawBitmap(mDragSnapShot, mRect.left, mRect.top, null);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        resetSlideState();
        super.onDetachedFromWindow();
    }

    private static final int SLIDE_EDGE = 200;
    private static final int SLIDE_CRITICAL = 80;
    private static final int SLIDE_IDLE = 0;
    private static final int SLIDE_CAN_LEFT = 1;
    private static final int SLIDE_CAN_RIGHT = 2;
    private static final int SLIDE_WAIT_TO_AUTO_SLIDE_LEFT = 3;
    private static final int SLIDE_WAIT_TO_AUTO_SLIDE_RIGHT = 4;
    private int mSlideState = SLIDE_IDLE;
    private float mSlideX;
    private float mCurrentX = SLIDE_EDGE + SLIDE_CRITICAL;

    private void resetSlideState() {
        mSlideState = SLIDE_IDLE;
        mCurrentX = SLIDE_EDGE + SLIDE_CRITICAL;
        mAutoSlideHandler.removeMessages(0);
    }

    @SuppressLint("HandlerLeak")
    private Handler mAutoSlideHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (mSlideState) {
                case SLIDE_WAIT_TO_AUTO_SLIDE_LEFT: {
                    if (mSlideX - mCurrentX >= SLIDE_CRITICAL) {
                        slide2Page(false);
                    }
                    break;
                }
                case SLIDE_WAIT_TO_AUTO_SLIDE_RIGHT: {
                    if (mCurrentX - mSlideX >= SLIDE_CRITICAL) {
                        slide2Page(true);
                    }
                    break;
                }
            }
        }
    };

    private void checkSlide(float x, float y) {
        mCurrentX = x;
        if (mSlideState == SLIDE_IDLE) {
            if (x <= SLIDE_EDGE) {
                mSlideState = SLIDE_CAN_LEFT;
            } else if (x >= getWidth() - SLIDE_EDGE) {
                mSlideState = SLIDE_CAN_RIGHT;
            }
            mSlideX = x;
        } else {
            if (x > SLIDE_EDGE && x < getWidth() - SLIDE_EDGE) {
                resetSlideState();
            } else {
                switch (mSlideState) {
                    case SLIDE_CAN_LEFT: {
                        if (mSlideX - x >= SLIDE_CRITICAL) {
                            mSlideState = SLIDE_WAIT_TO_AUTO_SLIDE_LEFT;
                            slide2Page(false);
                        }
                        break;
                    }
                    case SLIDE_CAN_RIGHT: {
                        if (x - mSlideX >= SLIDE_CRITICAL) {
                            mSlideState = SLIDE_WAIT_TO_AUTO_SLIDE_RIGHT;
                            slide2Page(true);
                        }
                        break;
                    }
                }
            }
        }
    }

    private void slide2Page(boolean next) {
        if (mViewPager != null) {
            PagerAdapter adapter = mViewPager.getAdapter();
            if (adapter != null) {
                int index = mViewPager.getCurrentItem();
                if (next) {
                    index++;
                } else {
                    index--;
                }
                if (index >= 0 && index < adapter.getCount()) {
                    mAutoSlideHandler.sendEmptyMessageDelayed(0, 800);
                    mViewPager.setCurrentItem(index, true);
                    if (mDragCallback != null) {
                        mDragCallback.onSlide2Page(mDragView, mStartDragPageIndex, mStartPosition, index, mData);
                    }
                }
            }
        }
    }
}
