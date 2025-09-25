package au.edu.newcastle.jnc985.visida;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodRecord;

import static junit.framework.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Josh on 19-Dec-17.
 */
public class TestFoodRecord {
    @Test
    public void testReturningNonFinalizedMethodWithNoEatingOccasions(){
        FoodRecord fr = new FoodRecord(1);
        assertThat(fr.getNonFinlizedEatingOccasions().isEmpty(), is(true));
    }

    @Test
    public void testReturningNonFinalizedMethodWithSingleNonEatingOccasion(){
        FoodRecord fr = new FoodRecord(1);
        EatingOccasion eo1 = new EatingOccasion();
        eo1.setFinalized(false);
        fr.addEatingOccasion(eo1);

        assertThat(fr.getNonFinlizedEatingOccasions().size(), is(1));
    }
    @Test
    public void testReturningNonFinalizedMethodWithSingleNonEatingOccasionAndSingleFinalized(){
        FoodRecord fr = new FoodRecord(1);
        EatingOccasion eo1 = new EatingOccasion();
        eo1.setFinalized(false);
        EatingOccasion eo2 = new EatingOccasion();
        eo1.setFinalized(true);
        fr.addEatingOccasion(eo1);
        fr.addEatingOccasion(eo2);
        assertThat(fr.getNonFinlizedEatingOccasions().size(), is(1));
    }

    @Test
    public void testReturningNonFinalizedMethodWithMultipleNonFinalizedEatingOccasion(){
        FoodRecord fr = new FoodRecord(1);
        EatingOccasion eo1 = new EatingOccasion();
        eo1.setFinalized(false);
        EatingOccasion eo2 = new EatingOccasion();
        eo1.setFinalized(false);
        fr.addEatingOccasion(eo1);
        fr.addEatingOccasion(eo2);
        assertThat(fr.getNonFinlizedEatingOccasions().size(), is(2));
    }

    @Test
    public void FoodRecordReturnsEOWithinHour(){
        Date now = new Date();
        FoodRecord fr = new FoodRecord(1);
        //Start time is new Date(); ie NOW
        EatingOccasion eo = new EatingOccasion();
        eo.setFinalized(false);
        eo.setFoodRecordId(fr.getFoodRecordId());
        eo.setStartTime(now);
        fr.addEatingOccasion(eo);

        EatingOccasion current = fr.getCurrentEatingOccasion();
        assertNotNull(current);
        assertThat(current, is(eo));

        //Update the time of the eating occasoin to 55minutes earlier
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.MINUTE, -55);
        eo.setStartTime(cal.getTime());

        EatingOccasion current2 = fr.getCurrentEatingOccasion();
        assertNotNull(current2);
        assertThat(current2, is(eo));
        assertThat(current2.getStartTime(), is(cal.getTime()));

        //Update the time to > 60 ago
        cal.setTime(now);
        cal.add(Calendar.MINUTE, -65);
        eo.setStartTime(cal.getTime());

        EatingOccasion current3 = fr.getCurrentEatingOccasion();
        assertNull(current3);
    }

}
