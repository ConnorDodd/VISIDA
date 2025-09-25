package ui;

import android.content.Context;
import android.util.AttributeSet;

/**
 * A placeholder class extending image view to appear as a marker for the fiducial marker
 * in the {@link au.edu.newcastle.jnc985.visida.activity.TakePhotoFragment}. In the future
 * if we decide to do fiducial marker image recogition in the app real time. It will go here
 * in this class.
 */
public class FiducialOverlay extends androidx.appcompat.widget.AppCompatImageView {

    public FiducialOverlay(Context context) {
        super(context);
    }

    public FiducialOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FiducialOverlay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
