package inshow.carl.com.ota_tool.view.radar;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import inshow.carl.com.ota_tool.R;


/**
 * 2018/06/26
 * created by ftc300
 */
public class RadarScanView extends View {
    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 300;
    private int defaultWidth;
    private int defaultHeight;
    private int start;
    private int centerX;
    private int centerY;
    private int radarRadius;
    private int circleColor = Color.parseColor("#a2a2a2");
    private int radarColor = Color.parseColor("#99a2a2a2");
    private int tailColor = Color.parseColor("#50aaaaaa");
    private int pointColor = Color.parseColor("#009ada");
    private Paint mPaintCircle;
    private Paint mPaintRadar;
    private Paint mPainPoint;
    private Matrix matrix;
    private boolean runFlag = true;

    private Handler handler = new Handler();
    private Runnable run = new Runnable() {
        @Override
        public void run() {
            if(runFlag) {
                start += 1;
                matrix = new Matrix();
                matrix.postRotate(start, centerX, centerY);
                postInvalidate();
                handler.postDelayed(run, 10);
            }
        }
    };

    public void pauseRun(){
        runFlag = false;
    }

    public void restartRun(){
        runFlag = true;
    }

    public RadarScanView(Context context) {
        super(context);
        init(null, context);
    }

    public RadarScanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, context);
    }

    public RadarScanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, context);
    }

    @TargetApi(21)
    public RadarScanView(Context context, AttributeSet attrs, int defStyleAttr,
                         int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2;
        centerY = h / 2;
        radarRadius = Math.min(w, h);
    }

    private void init(AttributeSet attrs, Context context) {
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs,
                    R.styleable.RadarScanView);
            circleColor = ta.getColor(R.styleable.RadarScanView_circleColor, circleColor);
            radarColor = ta.getColor(R.styleable.RadarScanView_radarColor, radarColor);
            tailColor = ta.getColor(R.styleable.RadarScanView_tailColor, tailColor);
            ta.recycle();
        }

        initPaint();
        //得到当前屏幕的像素宽高

        defaultWidth = dip2px(context, DEFAULT_WIDTH);
        defaultHeight = dip2px(context, DEFAULT_HEIGHT);

        matrix = new Matrix();
        handler.post(run);
    }

    private void initPaint() {
        mPaintCircle = new Paint();
        mPaintCircle.setColor(circleColor);
        mPaintCircle.setAntiAlias(true);//抗锯齿
        mPaintCircle.setStyle(Paint.Style.STROKE);//设置实心
        mPaintCircle.setStrokeWidth(0.25f);//画笔宽度

        mPaintRadar = new Paint();
        mPaintRadar.setColor(radarColor);
        mPaintRadar.setAntiAlias(true);

        mPainPoint = new Paint();
        mPainPoint.setColor(pointColor);
        mPainPoint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int resultWidth = 0;
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);

        if (modeWidth == MeasureSpec.EXACTLY) {
            resultWidth = sizeWidth;
        } else {
            resultWidth = defaultWidth;
            if (modeWidth == MeasureSpec.AT_MOST) {
                resultWidth = Math.min(resultWidth, sizeWidth);
            }
        }

        int resultHeight = 0;
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (modeHeight == MeasureSpec.EXACTLY) {
            resultHeight = sizeHeight;
        } else {
            resultHeight = defaultHeight;
            if (modeHeight == MeasureSpec.AT_MOST) {
                resultHeight = Math.min(resultHeight, sizeHeight);
            }
        }

        setMeasuredDimension(resultWidth, resultHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //分别绘制四个圆
        canvas.drawCircle(centerX, centerY, radarRadius / 20, mPaintCircle);
        canvas.drawCircle(centerX, centerY, radarRadius / 10, mPaintCircle);
        canvas.drawCircle(centerX, centerY, 3 * radarRadius / 20, mPaintCircle);
        canvas.drawCircle(centerX, centerY, radarRadius / 5, mPaintCircle);
        canvas.drawCircle(centerX, centerY, dip2px(getContext(), 2), mPainPoint);

        //设置颜色渐变从透明到不透明
        //        Shader shader = new SweepGradient(centerX, centerY, Color.TRANSPARENT, tailColor);

        Shader shader = new SweepGradient(centerX, centerY, new int[]{Color.TRANSPARENT, Color.TRANSPARENT,Color.parseColor("#b3d9ff"), Color.parseColor("#99ccff"), Color.parseColor("#1a8cff")}, new float[]{0f, 0.98f,0.99f, 0.99999f, 1f});
        mPaintRadar.setShader(shader);
        canvas.concat(matrix);
        canvas.drawCircle(centerX, centerY, 3 * radarRadius / 7, mPaintRadar);
    }

    private int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    private int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

}
