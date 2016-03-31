package ch.hes_so.glassrally.compass;

import android.content.Context;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.google.android.glass.widget.CardBuilder;

import ch.hes_so.glassrally.R;

public class CompassView implements OrientationManager.OnChangedListener
{
    private View mCompassView;
    private ImageView mCompassImageView;

    private float mTargetDegree = 0f;
    private float mCurrentDegree = 0f;

    private OrientationManager mOrientationManager;

    public CompassView(Context context) {

        mCompassView = new CardBuilder(context, CardBuilder.Layout.EMBED_INSIDE)
                .setEmbeddedLayout(R.layout.compass_layout)
                .getView();

        mCompassImageView = (ImageView) mCompassView.findViewById(R.id.imageViewCompass);

        SensorManager sensorManager =
                (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        mOrientationManager = new OrientationManager(sensorManager);
        mOrientationManager.addOnChangedListener(this);
        startOrientationManager();
    }

    public View getCompassView()
    {
        return mCompassView;
    }

    public void setTargetDegree(float targetDegree)
    {
        mTargetDegree = targetDegree;
        updateOrientation();
    }

    public void startOrientationManager()
    {
        mOrientationManager.start();
    }

    public void stopOrientationManager()
    {
        mOrientationManager.stop();
    }

    @Override
    public void onOrientationChanged(OrientationManager orientationManager) {
        updateOrientation();
    }

    @Override
    public void onAccuracyChanged(OrientationManager orientationManager) {
        //nothing
    }

    private void updateOrientation()
    {
        // get the angle around the z-axis rotated
        float degree = Math.round(mOrientationManager.getHeading());

        //TODO do something with the target degree

        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                mCurrentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        // how long the animation will take place
        ra.setDuration(210);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        mCompassImageView.startAnimation(ra);
        mCurrentDegree = -degree;
    }
}
