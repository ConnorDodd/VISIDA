package au.edu.newcastle.jnc985.visida;

import com.google.android.material.textfield.TextInputLayout;
import android.view.View;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class TextInputLayoutMatcher {
    public static Matcher<View> hasTextInputLayoutErrorText(final String expectedErrorText){
        return new TypeSafeMatcher<View>(){

            @Override
            public void describeTo(Description description) {

            }

            @Override
            protected boolean matchesSafely(View view) {
                if(!(view instanceof TextInputLayout)){
                    return false;
                }

                CharSequence error = ((TextInputLayout)view).getError();

                if(error == null){
                    return false;
                }

                String errorString = error.toString();
                return expectedErrorText.equals(errorString);
            }
        };
    }
}
