package org.nakedobjects.object.value;

import org.nakedobjects.object.ValueParseException;

import java.util.Locale;

import junit.framework.TestCase;


public class TimePeriodTest extends TestCase {
    private TimePeriod tp1;
    private TimePeriod tp2;
    private TimePeriod tp3;

    public TimePeriodTest(String arg0) {
        super(arg0);
    }

    protected void setUp() throws Exception {
		Locale.setDefault(Locale.UK);
   	
        tp1 = new TimePeriod();
        tp2 = new TimePeriod();
        tp3 = new TimePeriod();
    }

    protected void tearDown() throws Exception {
        tp1 = null;
    }

    public void testClear() {
        tp1.clear();
        assertTrue(tp1.title().toString().equals("~"));
        assertTrue(tp1.isEmpty());
    }

    public void testOverlaps() throws Exception {
        tp1.parse("09:00 ~ 17:00");
        tp2.parse("11:00 ~ 18:00");
        assertTrue(tp1.overlaps(tp2));
        assertTrue(tp1.startsBefore(tp2));
        assertFalse(tp2.startsBefore(tp1));
        assertTrue(tp2.endsAfter(tp1));
        assertFalse(tp1.endsAfter(tp2));
        tp3 = tp1.overlap(tp2);
        assertTrue(tp3.title().toString().equals("11:00 ~ 17:00"));
        tp3 = tp1.leadDifference(tp2);
        assertTrue(tp3.title().toString().equals("09:00 ~ 11:00"));
        tp3 = tp2.leadDifference(tp1);
        assertTrue(tp3.title().toString().equals("09:00 ~ 11:00"));
        tp3 = tp1.tailDifference(tp2);
        assertTrue(tp3.title().toString().equals("17:00 ~ 18:00"));
        tp3 = tp2.tailDifference(tp1);
        assertTrue(tp3.title().toString().equals("17:00 ~ 18:00"));

        tp1.parse("09:00 ~ 13:00");
        tp2.parse("14:00 ~ 18:00");
        assertFalse(tp1.overlaps(tp2));
        assertFalse(tp2.overlaps(tp1));

        tp1.parse("09:00 ~ 13:00");
        tp2.parse("13:00 ~ 18:00");
        assertFalse(tp1.overlaps(tp2));
        assertFalse(tp2.overlaps(tp1));

        tp1.parse("~17:00");
        tp2.parse("15:15~");

        //May want to revise code to make following assertion true.
        assertFalse(tp2.overlaps(tp1));
        tp3 = tp1.overlap(tp2);
        assertTrue(tp3.title().toString().equals("~"));
    }

    public void testParse() throws Exception {
        tp1.parse("09:00 ~ 17:00");
        assertEquals(Time.HOUR * 9, tp1.getStart().longValue());
        assertEquals(Time.HOUR * 17, tp1.getEnd().longValue());
        tp1.parse("11:00  ~  13:15");
        assertEquals("11:00 ~ 13:15", tp1.title().toString());
        tp1.parse("7:00~19:12");
        assertEquals("07:00 ~ 19:12", tp1.title().toString());

        try {
            tp1.parse("hgjuiy");
            fail();
        } catch (ValueParseException expected) {
        }

        try {
            tp1.parse("8:16 09:00");
            fail();
        } catch (ValueParseException expected) {
        }

        try {
            tp1.parse("rtyu~ghjk");
            fail();
        } catch (ValueParseException expected) {
        }

        try {
            tp1.parse("13:05 ~ 13:01");
            fail();
        } catch (ValueParseException e) {
            assertTrue(e.getMessage().equals("End time before start time"));
        }

        tp1.parse("13:05~");
        assertTrue(tp1.title().toString().equals("13:05 ~"));
        tp1.parse("~19:15 ");
        assertTrue(tp1.title().toString().equals("~ 19:15"));
    }

    public void testSaveAndRestore() throws Exception {
        tp1.parse("09:00 ~ 17:00");

        String s = tp1.saveString();
        assertTrue(s.equals("09001700"));
        tp2.restoreString(s);
        assertTrue(tp2.title().toString().equals("09:00 ~ 17:00"));
        assertTrue(tp2.isSameAs(tp1));
    }

    public void testSaveStringLength() {
        tp1.reset();
        assertTrue(tp1.saveString().length() == 8);
    }
}
