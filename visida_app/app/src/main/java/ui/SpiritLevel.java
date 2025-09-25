package ui;

import android.content.Context;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import au.edu.newcastle.jnc985.visida.R;

/**
 * Created by Josh on 16-Feb-18.
 */

public class SpiritLevel extends FrameLayout implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private SeekBar mVerticalSeekBar;
    private SeekBar mHozirontalSeekBar;


    private int mHMax = 3600;
    private double mHGoalDegrees;
    private int mVMax = 3600;
    private double mVGoalDegrees;
    //Degress allowed either side to be correct angle
    private double mErrorMargin = 5;

    public SpiritLevel(Context context) {
        super(context);
        init();
    }

    public SpiritLevel(Context context, AttributeSet attrs){
        super(context, attrs);
        init();
    }

    public SpiritLevel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View v = inflater.inflate(R.layout.spiritlevel, this, true);
        this.mHozirontalSeekBar = findViewById(R.id.horizontalSeekBar);
        this.mVerticalSeekBar = findViewById(R.id.verticalSeekBar);

        mHozirontalSeekBar.setMax(mHMax);
        mVerticalSeekBar.setMax(mVMax);

        mSensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        OnTouchListener seekBarListener = new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        };
        mHozirontalSeekBar.setOnTouchListener(seekBarListener);
        mVerticalSeekBar.setOnTouchListener(seekBarListener);
        //Register for Accelerometer and Magnetic Field Sensors.
        register();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float aX = event.values[0];
            float aY = event.values[1];
            float aZ = event.values[2];
            //double anglez = Math.atan2(aX, aY) / (Math.PI / 180);
            double anglex = Math.atan2(aY, aZ) / (Math.PI / 180);
            double angley = Math.atan2(aZ, aX) / (Math.PI / 180);

            //When phone is upright the angle of x is 90degrees
            updateSeekbars(anglex, angley);
        }
    }

    private void updateSeekbars(double vertical, double horizontal) {
        //Flat phone is vertical 0, horizontal 90
        //X = 90 - vertical
        //Y =  0 - horizontal
        int vProg = 900 + ((int) vertical * 10);
        if(vProg < 0){
            int diff = (900 + vProg);
            vProg = 2700 + diff;
        }
        mVerticalSeekBar.setProgress(vProg);

        int hProg = 900 + ((int) horizontal * 10);
        if(hProg < 0){
            int diff = (900 + hProg);
            hProg = 2700 + diff;
        }
        mHozirontalSeekBar.setProgress(hProg);

        updateState(vertical, horizontal);
    }


    /**
     *
     * Gets the angular goal for the Horizontal axis
     * @return Angle in Degrees for the horizontal goal.
     */
    public double getHGoal() {
        return Math.toDegrees(mHGoalDegrees);
    }

    /**
     * Sets the Horizontal goal in Degrees.
     * @param HGoal Angle, in degrees for the horizontal goal.
     */
    public void setHGoal(double HGoal) {
        this.mHGoalDegrees = HGoal;
    }

    /**
     *
     * Gets the angular goal for the Vertical axis
     * @return Angle in Degrees for the Vertical goal.
     */
    public double getVGoal() {
        return Math.toDegrees(mVGoalDegrees);
    }

    /**
     * Sets the Vertical goal in Degrees.
     * @param VGoal Angle, in degrees for the Vertical goal.
     */
    public void setVGoal(double VGoal) {
        this.mVGoalDegrees = VGoal;
    }

    /**
     * Gets the error margin of the Spritir Level.
     * @return Error margin in Degrees.
     */
    public double getErrorMargin() {
        return mErrorMargin;
    }

    /**
     * Sets the Error margin, in degrees.
     * This allows for some room of error for
     * the spirit levels state to change.
     * @param errorMargin
     */
    public void setErrorMargin(double errorMargin) {
        this.mErrorMargin = errorMargin;
    }

    private boolean verticalGreen;
    private boolean horizontalGreen;

    public boolean allGreen(){
        return verticalGreen && horizontalGreen;
    }

    private void updateState(double vertical, double horizontal) {
        //Veritcal and Horizontal are 0 when te phone is flat.
        //Horizontal when flat gives a reading of 90 degrees. so offset this back to 0.
        horizontal = horizontal - 90;
        if(vertical < mVGoalDegrees - mErrorMargin || vertical > mVGoalDegrees + mErrorMargin){
            mVerticalSeekBar.getThumb().setColorFilter(0xFFFF0000, PorterDuff.Mode.SRC_ATOP);
            verticalGreen = false;
        }
        else{
            mVerticalSeekBar.getThumb().setColorFilter(0xFF00FF00,PorterDuff.Mode.SRC_ATOP);
            verticalGreen = true;
        }
        if(horizontal < mHGoalDegrees - mErrorMargin || horizontal > mHGoalDegrees + mErrorMargin){
            mHozirontalSeekBar.getThumb().setColorFilter(0xFFFF0000, PorterDuff.Mode.SRC_ATOP);
            horizontalGreen = false;
        }
        else{
            mHozirontalSeekBar.getThumb().setColorFilter(0xFF00FF00, PorterDuff.Mode.SRC_ATOP);
            horizontalGreen = true;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public void register() {
        mSensorManager.registerListener( this, mAccelerometer,
                SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
    }

    public void unregister() {
        mSensorManager.unregisterListener(this);
    }
}
