package ui;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

/**
 * Created by jnc985 on 22-Feb-18.
 */

public class TimePickerFragment extends DialogFragment  {

    private Context mContext;
    private TimePickerDialog.OnTimeSetListener mListener;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        mContext = context;
        try {
            mListener = (TimePickerDialog.OnTimeSetListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnTimeSetListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        //Set defaul time to 7pm
        final Calendar c = Calendar.getInstance();
        c.set(Calendar.AM_PM, Calendar.PM);
        c.set(Calendar.HOUR_OF_DAY, 19);
        c.set(Calendar.MINUTE, 0);


        int hour = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);

        TimePickerDialog tpDialog = new TimePickerDialog(getActivity(), mListener, hour, min, false);

        /*
        Time picker defaults to MODE_CLOCK. This mode is not accessible and cant be changed programmatically
        To get around this we have created a custom timepicker layout (literally the default plus android:timePickerMode
        set to spinner).tra
         */
        //final LayoutInflater inflater = LayoutInflater.from(getContext());
        //final View view = inflater.inflate(R.layout.spinonly_time_picker_dialog, null);
        //tpDialog.setView(view);

        return tpDialog;
    }

}
