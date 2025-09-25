package au.edu.newcastle.jnc985.visida;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;
import android.view.View;
import android.widget.TimePicker;

import org.hamcrest.Matcher;

public class ViewActions {
    public static ViewAction setTime(final int hour, final int min){
        return new ViewAction(){
            @Override
            public void perform(UiController uiController, View view){
                TimePicker tp = (TimePicker) view;
                tp.setHour(hour);
                tp.setMinute(min);
            }
            @Override
            public String getDescription() {
                return "Set the passed time into the TimePicker";
            }
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(TimePicker.class);
            }
        };
    }
}
