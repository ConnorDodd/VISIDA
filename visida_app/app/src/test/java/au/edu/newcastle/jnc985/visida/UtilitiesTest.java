package au.edu.newcastle.jnc985.visida;

import org.junit.Test;

import java.util.Date;

import bo.Utilities;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Josh on 19-Dec-17.
 */
public class UtilitiesTest {
    //Test the same day method
    @Test
    public void sameDayWithTwoDatesTheSameDay(){
        Date d1 = new Date();
        Date d2 = new Date();

        assertTrue(Utilities.sameDay(d1, d2));
    }

    //Test the same day method
    @Test
    public void sameDayWithTwoDatesDifferentDay(){
        Date d1 = new Date();
        //Add a day
        Date d2 = new Date(d1.getTime() + (1000 * 60 * 60 * 24));
        //Subtract a day
        Date d3 = new Date(d1.getTime() - (1000 * 60 * 60 * 24));
        assertFalse(Utilities.sameDay(d1, d2));
        assertFalse(Utilities.sameDay(d1, d3));
        assertFalse(Utilities.sameDay(d2, d3));

    }
}