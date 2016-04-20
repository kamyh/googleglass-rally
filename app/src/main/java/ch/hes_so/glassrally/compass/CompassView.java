package ch.hes_so.glassrally.compass;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.android.glass.widget.CardBuilder;

import ch.hes_so.glassrally.R;

public class CompassView implements OrientationManager.OnChangedListener {
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
        mCompassView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                startOrientationManager();
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                stopOrientationManager();
            }
        });
        startOrientationManager();
    }

    public View getCompassView() {
        return mCompassView;
    }

    public void setTargetDegree(float targetDegree) {
        mTargetDegree = targetDegree;
        updateOrientation();
    }

    public void startOrientationManager() {
        mOrientationManager.start();
    }

    public void stopOrientationManager() {
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

    private void updateOrientation() {
        // get the angle around the z-axis rotated
        float degree = Math.round(mOrientationManager.getHeading());

        //TODO do something with the target degree

        float scale = 0.3f;
        Matrix mat = new Matrix();

        mat.postRotate(-degree, mCompassImageView.getWidth() / 2.0f, mCompassImageView.getHeight() / 2.0f);
        mat.postScale(scale, scale);
        mat.postTranslate(mCompassImageView.getWidth() / 2 - mCompassImageView.getWidth() * scale * 0.5f, mCompassImageView.getHeight() / 2 - mCompassImageView.getHeight() * scale * 0.5f);

        mCompassImageView.getImageMatrix().set(mat);

        mCompassImageView.invalidate();
        mCompassView.invalidate();

        mCurrentDegree = -degree;
    }
}
