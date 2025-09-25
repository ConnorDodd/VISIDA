
package au.edu.newcastle.jnc985.visida.activity;

import android.app.Activity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import au.edu.newcastle.jnc985.visida.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.ActivityResultMatchers.hasResultCode;
import static androidx.test.espresso.contrib.ActivityResultMatchers.hasResultData;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static au.edu.newcastle.jnc985.visida.EspressoTestMatchers.withBackGroundDrawable;
import static bo.AppConstants.TEXT_DESCRIPTION;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringEndsWith.endsWith;

/**
 * Created by Josh on 13-Dec-17.
 */


@RunWith(MockitoJUnitRunner.class)
public class AudioActivityTextTest extends AudioActivityTest{

    @Test
    public void testInitialUIState() {
        //Check button says start recording
        onView(withId(R.id.btnRecordAudio)).check(matches(withBackGroundDrawable(R.drawable.ic_btn_microphone)));
        onView(withId(R.id.btnAddText)).check(matches(isDisplayed()));
        onView(withId(R.id.audioRecorProgressBar)).check(matches(isDisplayed()));
    }

    @Test
    public void clickAddTextOpensTextDialog(){
        onView(withId(R.id.btnAddText)).perform(click());

        onView(withText(R.string.add_text_description_dialog_title)).check(matches(isDisplayed()));
        String sampleText = "sample text blah blahb blah";
        onView(withClassName(endsWith("EditText"))).perform(typeText(sampleText), closeSoftKeyboard());

        onView(withId(R.id.btnTextAccept)).perform(click());
        mAudioActivityActivityTestRule.finishActivity();
    }

    @Test
    public void clickTickReturnsText(){
        onView(withId(R.id.btnAddText)).perform(click());

        String sampleText = "sample text blah blahb blah";
        onView(withClassName(endsWith("EditText"))).perform(typeText(sampleText), closeSoftKeyboard());

        onView(withId(R.id.btnTextAccept)).perform(click());

        assertThat(mAudioActivityActivityTestRule.getActivityResult(), hasResultCode(Activity.RESULT_OK));
        assertThat(mAudioActivityActivityTestRule.getActivityResult(), hasResultData(hasExtraWithKey(TEXT_DESCRIPTION)));
    }

    @Test
    public void recordingAudioThenClickingDeleteThenRecordingTextReturnsTextNoAudioFileCreated(){
        //Check no audio file is there yet
        assertThat(containsAudioFiles(mMediaDir), is(false));

        //Start recording
        //Click the record butotn
        onView(withId(R.id.btnRecordAudio)).perform(click());

        //Wait
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Stop Recording
        onView(withId(R.id.btnAudioFile)).perform(click());

        assertThat(containsAudioFiles(mMediaDir), is(true));

        //Click delete
        onView(withId(R.id.btnAudioDelete)).perform(click());

        //Check file is gone
        assertThat(containsAudioFiles(mMediaDir), is(false));

        //Enter text
        onView(withId(R.id.btnAddText)).perform(click());

        String sampleText = "sample text blah blahb blah";
        onView(withClassName(endsWith("EditText"))).perform(typeText(sampleText), closeSoftKeyboard());

        onView(withId(R.id.btnTextAccept)).perform(click());

        assertThat(mAudioActivityActivityTestRule.getActivityResult(), hasResultCode(Activity.RESULT_OK));
        assertThat(mAudioActivityActivityTestRule.getActivityResult(), hasResultData(hasExtraWithKey(TEXT_DESCRIPTION)));
    }


    @Test
    public void fragmentDoesntReturnWhenTextIsEmpty(){
        onView(withId(R.id.btnAddText)).perform(click());

        //Click the tick with no text entered.
        onView(withId(R.id.btnTextAccept)).perform(click());
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Check that window has not closed.
        onView(withId(R.id.btnTextAccept)).check(matches(isDisplayed()));

        //Enter Text
        String sampleText = "sample text blah blahb blah";
        onView(withClassName(endsWith("EditText"))).perform(typeText(sampleText), closeSoftKeyboard());

        // Click tick now that text has been entered
        onView(withId(R.id.btnTextAccept)).perform(click());


        assertThat(mAudioActivityActivityTestRule.getActivityResult(), hasResultCode(Activity.RESULT_OK));
        assertThat(mAudioActivityActivityTestRule.getActivityResult(), hasResultData(hasExtraWithKey(TEXT_DESCRIPTION)));
    }
}

