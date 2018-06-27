package inshow.carl.com.ota_tool.view.radar;


import android.view.animation.Animation;
import android.view.animation.Transformation;


/**
 * 2018/06/26
 * created by ftc300
 */
public class LinearAnimation extends Animation
{
    private LinearAnimationListener mListener = null;

    public interface LinearAnimationListener
    {
        void applyTans(float interpolatedTime);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t)
    {
        super.applyTransformation(interpolatedTime, t);
        if (mListener != null)
            mListener.applyTans(interpolatedTime);
    }

    public void setLinearAnimationListener(LinearAnimationListener listener)
    {
        mListener = listener;
    }
}
