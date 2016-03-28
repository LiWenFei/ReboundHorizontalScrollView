package com.lwf.reboundhorizontalscrollview;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.HorizontalScrollView;

/**
 * Created by liwenfei on 2015/12/23.
 */
public class ReboundHorizontalScrollView extends HorizontalScrollView {

    private static final String TAG = ReboundHorizontalScrollView.class.getSimpleName();

    /**
     * 目的是达到一个延迟的效果
     */
    private static final float MOVE_FACTOR = 0.5f;
    /**
     * 松开手指后, 界面回到正常位置需要的动画时间
     */
    private static final int ANIM_TIME = 300;
    /**
     * 经过延迟后移动的实际距离
     */
    private int offset = 0;
    /**
     * ScrollView的子View， 也是ScrollView的唯一一个子View
     */
    private View contentView;
    /**
     * 用于记录正常的布局位置
     */
    private Rect originalRect = new Rect();
    /**
     * 手指按下时记录是否可以继续右拉
     */
    private boolean canPullRight = false;
    /**
     * 手指按下时记录是否可以继续左拉
     */
    private boolean canPullLeft = false;
    /**
     * 手指按下时的X值, 用于在移动时计算移动距离
     * 如果按下时不能左拉和右拉， 会在手指移动时更新为当前手指的X值
     */
    private float startX;
    /**
     * 在手指滑动的过程中记录是否移动了布局
     */
    private boolean isMoved = false;
    /**
     * 是否触发左侧的事件
     */
    private boolean isTriggerLeft = false;
    /**
     * 是否触发右侧的事件
     */
    private boolean isTriggerRight = false;
    /**
     * 回弹事件
     */
    private OnReboundListener mOnReboundListener;

    public ReboundHorizontalScrollView(Context context) {
        super(context);
    }

    public ReboundHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 当View中所有的子控件均被映射成xml后触发
     */
    @Override
    protected void onFinishInflate() {
        if (getChildCount() > 0) {
            contentView = getChildAt(0);
        }
        super.onFinishInflate();
    }

    /**
     * 是ViewGroup中子View的布局方法
     * @param changed
     * @param l
     * @param t
     * @param r
     * @param b
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (null == contentView)
            return;
        //HorizontalScrollView中的唯一子控件的位置信息, 这个位置信息在整个控件的生命周期中保持不变
        originalRect.set(contentView.getLeft(), contentView.getTop(), contentView.getRight(),
                contentView.getBottom());
    }

    /**
     * 在触摸事件中, 处理左拉和右拉的逻辑
     * @param ev
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (contentView == null) {
            return super.dispatchTouchEvent(ev);
        }
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                //判断是否可以右拉和左拉
                canPullRight = isCanPullRight();
                canPullLeft = isCanPullLeft();
                //记录按下时的X值
                startX = ev.getX();
                break;

            case MotionEvent.ACTION_UP:
                if (!isMoved)
                    break;
                // 开启动画
                TranslateAnimation anim = new TranslateAnimation(contentView.getLeft(), originalRect.left, 0, 0);
                anim.setDuration(ANIM_TIME);
                contentView.startAnimation(anim);

                // 设置回到正常的布局位置
                contentView.layout(originalRect.left, originalRect.top,
                        originalRect.right, originalRect.bottom);

                if (isTriggerLeft) {
                    if ((float)offset / getWidth() > 0.3) {
                        if (null != mOnReboundListener) {
                            mOnReboundListener.OnLeftRebound();
                        }
                    }
                }

                if (isTriggerRight) {
                    if (offset < 0 && Math.abs((float)offset / getWidth()) > 0.3) { //绝对值
                        if (null != mOnReboundListener) {
                            mOnReboundListener.OnRightRebound();
                        }
                    }
                }

                //将标志位设回false
                canPullRight = false;
                canPullLeft = false;
                isMoved = false;
                isTriggerLeft = false;
                isTriggerRight = false;
                break;

            case MotionEvent.ACTION_MOVE:
                //在移动的过程中， 既没有滚动到可以右拉的程度， 也没有滚动到可以左拉的程度
                if (!canPullLeft && !canPullRight) {
                    startX = ev.getX();
                    canPullRight = isCanPullRight();
                    canPullLeft = isCanPullLeft();
                    isTriggerLeft = false;
                    isTriggerRight = false;
                    break;
                }
                //计算手指移动的距离
                float nowX = ev.getX();
                int deltaX = (int) (nowX - startX);
                //是否应该移动布局
                boolean shouldMove = (canPullLeft && deltaX < 0)  //可以左拉， 并且手指向左移动
                                || (canPullRight && deltaX > 0)    //可以右拉， 并且手指向右移动
                                || (canPullLeft && canPullRight); //既可以上拉也可以下拉（这种情况出现在ScrollView包裹的控件比ScrollView还小）
                if (shouldMove) {
                    //计算偏移量
                    offset = (int)(deltaX * MOVE_FACTOR);

                    //随着手指的移动而移动布局
                    contentView.layout(originalRect.left + offset, originalRect.top,
                            originalRect.right + offset, originalRect.bottom);
                    isMoved = true;  //记录移动了布局
                    //
                    if (canPullLeft && !canPullRight) {
                        isTriggerRight = true;
                    }

                    if (canPullRight && !canPullLeft) {
                        isTriggerLeft = true;
                    }

                    if (canPullRight && canPullRight) {
                        if (offset > 0) {
                            isTriggerLeft = true;
                        }else {
                            isTriggerRight = true;
                        }
                    }
                }
                break;

            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 是否滚动到了顶部
     * @return
     */
    private boolean isCanPullRight() {
        return getScrollX() == 0 || contentView.getWidth() < getWidth() + getScrollX();
    }

    /**
     * 是否滚动到了底部
     * @return
     */
    private boolean isCanPullLeft() {
        return contentView.getWidth() <= getWidth() + getScrollX();
    }

    public void setOnReboundListtener(OnReboundListener listener) {
        mOnReboundListener = listener;
    }

    public interface OnReboundListener {
        public void OnLeftRebound();

        public void OnRightRebound();
    }
}
