package inshow.carl.com.csd.view.capture;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.cjt2325.cameralibrary.CaptureLayout;
import com.cjt2325.cameralibrary.FoucsView;

import inshow.carl.com.csd.R;

/**
 * Comment:
 * Author: ftc300
 * Date: 2018/9/29
 * Blog: www.ftc300.pub
 * GitHub: https://github.com/ftc300
 */

public class ClockCaptureLayout extends FrameLayout{
    private CaptureLayout mCaptureLayout;
    private FoucsView mFoucsView;
    private ImageView image_flash,image_switch;
    private Paint paint;
    private final int maskColor = Color.parseColor("#60000000");                          //蒙在摄像头上面区域的半透明颜色
    private final int triAngleColor = Color.parseColor("#009CDE");                   //边角的颜色
    private final int lineColor = Color.parseColor("#009CDE");                            //中间线的颜色
    private final int textColor = Color.parseColor("#CCCCCC");                            //文字的颜色
    private final int triAngleLength = dp2px(20);                                         //每个角的点距离
    private final int triAngleWidth = dp2px(4);                                           //每个角的点宽度
    private final int textMarinTop = dp2px(30);
    private int mWith,mHeight;

    private int dp2px(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }//文字距离识别框的距离
    public ClockCaptureLayout(@NonNull Context context) {
        this(context,null);
    }

    public ClockCaptureLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ClockCaptureLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        mWith = display.getWidth();  // deprecated
        mHeight = display.getHeight();  // deprecated
        initView(context);

    }

    private void initView(Context context) {
        setWillNotDraw(false);
        paint = new Paint();
        paint.setColor(Color.YELLOW);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(40);


//
        View view = LayoutInflater.from(context).inflate(com.cjt2325.cameralibrary.R.layout.camera_view, this);
        mCaptureLayout = (CaptureLayout) view.findViewById(R.id.capture_layout);
//        mFoucsView = (FoucsView) view.findViewById(R.id.fouce_view);
//        image_flash = (ImageView) view.findViewById(R.id.image_flash);
//        image_switch = (ImageView) view.findViewById(R.id.image_switch);
//        image_flash.setVisibility(GONE);
//        image_switch.setVisibility(GONE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        canvas.drawCircle(200, 400, 90, paint);
//        invalidate();
//        Rect frame = new Rect();
//        int width = canvas.getWidth();
//        int height = canvas.getHeight();
//
//        // 除了中间的识别区域，其他区域都将蒙上一层半透明的图层
//        canvas.drawRect(0, 0, width, frame.top, maskPaint);
//        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, maskPaint);
//        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, maskPaint);
//        canvas.drawRect(0, frame.bottom + 1, width, height, maskPaint);

    }
}
