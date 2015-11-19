package com.dragon.lib;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2015/11/1.
 */
public class StopwatchView extends View {
    public final String TAG = "StopWatch View Tag";

    private final int START_STOPWATCH = 1;
    private final int STOP_STOPWATCH = 2;
    private final int UPDATE_STOPWATCH = 3;
    private final int PAUSE_STOPWATCH = 4;

    private int mLineNum = 90;

    private float mRadius;
    private float mTriangleSpace;

    private Paint mCirclePaint;
    private final int mCirleColor = Color.MAGENTA;
    private final float mCircleWidth = 2f;

    private Paint mTrianglePaint;
    private Path mTrianglePath = new Path();
    private int mTriangleColor;
    private float mTriangleHeight;
    private int mTriangleOffsetDegree;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private Handler mHandler;

    private Paint mLinePaint;
    private final int mLineColor = Color.GRAY;
    private Paint mHighlightLinePaint;
    private int mHighlightLineColor;
    private final float mLineWidth = 2f;
    private float mLineLength;
    private float mCircleLineMargin;

    private Paint mTextPaint;
    private float mTextSize;
    private Rect mBound;
    private String mTimerTime = "00:00:00.0";

    private int mMillisecond = 0;
    private int mSecond = 0;
    private int mMinute = 0;
    private int mHour = 0;

    private boolean mIsStart;
    private boolean mIsPause = false;

    private int mScreenHeight;
    private int mScreenWidth;


    public StopwatchView(Context context) {
        this(context, null);
    }

    public StopwatchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StopwatchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()) {
            return;
        }


        initDefaultValue();
        initPaint();

        mHandler = new StopwatchHandler();

        mTimer = new Timer("triangle");
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                Message msg = mHandler.obtainMessage();
                msg.what = UPDATE_STOPWATCH;
                mHandler.sendMessage(msg);
            }
        };
        mTimer.schedule(mTimerTask, 200, 100);


    }

    private void initDefaultValue() {
        Resources resources = getResources();
        mHighlightLineColor = resources.getColor(R.color.highlight_line);
        mTriangleColor = resources.getColor(R.color.triangle);
        mRadius = resources.getDimension(R.dimen.radius);
        mLineLength = resources.getDimension(R.dimen.line_length);
        mCircleLineMargin = resources.getDimension(R.dimen.circle_line_margin);
        mScreenHeight = resources.getDisplayMetrics().heightPixels;
        mScreenWidth = resources.getDisplayMetrics().widthPixels;
        mTextSize = resources.getDimension(R.dimen.text_size);
        mBound = new Rect();
        mIsStart = false;

        mTriangleSpace = resources.getDimension(R.dimen.triangle_space);
        mTriangleHeight = resources.getDimension(R.dimen.triangle_height);
        mTriangleOffsetDegree = 0;
    }

    private void initPaint() {
        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setStrokeWidth(mCircleWidth);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setColor(mCirleColor);

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(mLineColor);
        mLinePaint.setStrokeWidth(mLineWidth);

        mHighlightLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHighlightLinePaint.setColor(mHighlightLineColor);
        mHighlightLinePaint.setStrokeWidth(mLineWidth);

        mTrianglePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTrianglePaint.setColor(mTriangleColor);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTriangleColor);
        mTextPaint.setTextSize(mTextSize);
    }

    public void start() {
        Message msg = mHandler.obtainMessage();
        msg.what = START_STOPWATCH;
        mHandler.sendMessage(msg);
    }

    public void stop() {
        Message msg = mHandler.obtainMessage();
        msg.what = STOP_STOPWATCH;
        mHandler.sendMessage(msg);
    }

    public void pause() {
        Message msg = mHandler.obtainMessage();
        msg.what = PAUSE_STOPWATCH;
        mHandler.sendMessage(msg);
    }

    private boolean isInHighLight(int degree) {
        Resources res = getResources();
        if ((mTriangleOffsetDegree >= 270) && (degree >= 0 && degree <= 90)) {
            degree += 360;
        }

        if (degree <= mTriangleOffsetDegree + 90 && degree >= mTriangleOffsetDegree + 90 - 20) {
            mHighlightLinePaint.setColor(res.getColor(R.color.highlight_line));
        } else if (degree < mTriangleOffsetDegree + 90 - 20 && degree >= mTriangleOffsetDegree + 90 - 40) {
            mHighlightLinePaint.setColor(res.getColor(R.color.highlight_line1));
        } else if (degree < mTriangleOffsetDegree + 90 - 40 && degree >= mTriangleOffsetDegree + 90 - 60) {
            mHighlightLinePaint.setColor(res.getColor(R.color.highlight_line2));
        } else {
            return false;
        }

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float pl = getPaddingLeft();
        float pt = getPaddingTop();
        float x = pl + mTriangleSpace;
        float y = pt + mTriangleSpace;
        float cx = x + mRadius;
        float cy = y + mRadius;
        float startX;
        Paint paint;

        canvas.save();
        canvas.rotate(mTriangleOffsetDegree, cx, cy);
        mTrianglePath.reset();
        mTrianglePath.moveTo(cx - mTriangleHeight / 2, mTriangleSpace - mTriangleHeight);
        mTrianglePath.lineTo(cx + mTriangleHeight / 2, mTriangleSpace - mTriangleHeight);
        mTrianglePath.lineTo(cx, mTriangleSpace);
        mTrianglePath.close();
        canvas.drawPath(mTrianglePath, mTrianglePaint);
        canvas.restore();

        int degree = 360 / mLineNum;
        for (int i = 0; i < mLineNum; i++) {
            canvas.save();
            canvas.rotate(degree * i, cx, cy);
            startX = pl + mCircleWidth + mTriangleSpace + mCircleLineMargin;
            if (isInHighLight(degree * i) && (mIsPause || mIsStart)) {
                paint = mHighlightLinePaint;
            } else {
                paint = mLinePaint;
            }
            canvas.drawLine(startX, cy, startX + mLineLength, cy, paint);
            canvas.restore();
        }

        drawText(canvas, cx, cy);
    }

    private void drawText(Canvas canvas, float cx, float cy) {
        float width;
        float height;

        mTextPaint.getTextBounds(mTimerTime, 0, mTimerTime.length(), mBound);
        width = mBound.width();
        height = mBound.height();

        canvas.drawText(mTimerTime, cx - width / 2, cy + height / 2, mTextPaint);
    }


    private float calRadius(int width, int height) {
        return (width > height ? height / 2 : width / 2) - mTriangleSpace;
    }

    enum PaddingType {
        VERTICAL, HORIZONTAL
    }

    private int calPadding(PaddingType type) {
        if (type == PaddingType.HORIZONTAL) {
            return getPaddingLeft() + getPaddingRight();
        } else {
            return getPaddingTop() + getPaddingBottom();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if ((widthMode == MeasureSpec.EXACTLY) && (heightMode != MeasureSpec.EXACTLY)) {
            height = width > mScreenHeight ? mScreenHeight : width;
        } else if ((widthMode != MeasureSpec.EXACTLY) && (heightMode == MeasureSpec.EXACTLY)) {
            width = height > mScreenWidth ? mScreenWidth : height;
        } else if ((widthMode != MeasureSpec.EXACTLY) && (heightMode != MeasureSpec.EXACTLY)) {
            width = (int) (calPadding(PaddingType.HORIZONTAL) + mRadius * 2);
            height = (int) (calPadding(PaddingType.VERTICAL) + mRadius * 2);
        }

        mRadius = calRadius(width - calPadding(PaddingType.HORIZONTAL), height - calPadding(PaddingType.VERTICAL));
        setMeasuredDimension(width, height);
    }

    void calTimerTime(){
        mMillisecond ++;
        if (mMillisecond >= 10){
            mMillisecond = 0;
            mSecond ++;
            if (mSecond >= 60){
                mSecond = 0;
                mMinute ++;
                if (mMinute >= 60){
                    mMinute = 0;
                    mHour ++;
                }
            }
        }
        mTimerTime = String.format("%02d:%02d:%02d.%d", mHour, mMinute, mSecond, mMillisecond);
    }

    private class StopwatchHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case START_STOPWATCH:
                    mIsStart = true;
                    mIsPause = false;
                    break;
                case STOP_STOPWATCH:
                    mIsStart = false;
                    mIsPause = false;
                    mTriangleOffsetDegree = 0;
                    mTimerTime = "00:00:00.0";
                    invalidate();
                    break;
                case PAUSE_STOPWATCH:
                    mIsStart = false;
                    mIsPause = true;
                    break;
                case UPDATE_STOPWATCH:
                    if (mIsStart) {
                        mTriangleOffsetDegree = (mTriangleOffsetDegree + 360 / 120) % 360;
                        calTimerTime();
                        invalidate();
                    }
                    break;
            }
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SaveState(superState, new int[]{mTriangleOffsetDegree, mHour, mMinute, mSecond, mMillisecond}, new boolean[]{mIsStart, mIsPause});
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SaveState saveState = (SaveState) state;
        mTriangleOffsetDegree = saveState.mTriangleOffsetDegree;
        mIsStart = saveState.mIsStart;
        mIsPause = saveState.mIsPause;
        mHour = saveState.mHour;
        mMinute = saveState.mMinute;
        mSecond = saveState.mSecond;
        mMillisecond = saveState.mMillisecond;

        try{
            super.onRestoreInstanceState(state);
        } catch (Exception e) {
        }
    }

    private static class SaveState extends BaseSavedState {

        int mTriangleOffsetDegree;
        boolean mIsStart;
        boolean mIsPause;
        private int mMillisecond;
        private int mSecond;
        private int mMinute;
        private int mHour;

        public SaveState(Parcel in){
            super(in);
            int[] ints = new int[5];
            in.readIntArray(ints);
            mTriangleOffsetDegree = ints[0];
            mMillisecond = ints[4];
            mSecond = ints[3];
            mMinute = ints[2];
            mHour = ints[1];

            boolean[] temp = new boolean[2];
            in.readBooleanArray(temp);
            mIsStart = temp[0];
            mIsPause = temp[1];
        }

        public SaveState(Parcelable superState, int[] ints, boolean [] booleans) {
            super(superState);
            mTriangleOffsetDegree = ints[0];
            mMillisecond = ints[4];
            mSecond = ints[3];
            mMinute = ints[2];
            mHour = ints[1];
            mIsPause = booleans[1];
            mIsStart = booleans[0];
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeIntArray(new int[]{mTriangleOffsetDegree, mHour, mMinute, mSecond, mMillisecond});
            dest.writeBooleanArray(new boolean[]{mIsStart, mIsPause});
        }

        public static final Parcelable.Creator<SaveState> CREATOR = new Creator<SaveState>() {
            @Override
            public SaveState createFromParcel(Parcel source) {
                return new SaveState(source);
            }

            @Override
            public SaveState[] newArray(int size) {
                return new SaveState[size];
            }
        };
    }
}
