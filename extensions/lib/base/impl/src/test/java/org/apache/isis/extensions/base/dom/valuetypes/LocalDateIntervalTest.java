package org.apache.isis.extensions.base.dom.valuetypes;

import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.apache.isis.extensions.base.dom.valuetypes.AbstractInterval.IntervalEnding;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class LocalDateIntervalTest {

    LocalDateInterval interval120101to120401 = LocalDateInterval.excluding(new LocalDate(2012, 1, 1), new LocalDate(2012, 4, 1));
    LocalDateInterval interval120101to120331incl = LocalDateInterval.including(new LocalDate(2012, 1, 1), new LocalDate(2012, 3, 31));
    LocalDateInterval interval111101to120501 = LocalDateInterval.excluding(new LocalDate(2011, 11, 1), new LocalDate(2012, 5, 1));
    LocalDateInterval interval111101to120301 = LocalDateInterval.excluding(new LocalDate(2011, 11, 1), new LocalDate(2012, 3, 1));
    LocalDateInterval interval120201to120501 = LocalDateInterval.excluding(new LocalDate(2012, 2, 1), new LocalDate(2012, 5, 1));
    LocalDateInterval interval120201to120301 = LocalDateInterval.excluding(new LocalDate(2012, 2, 1), new LocalDate(2012, 3, 1));
    LocalDateInterval interval100101to110101 = LocalDateInterval.excluding(new LocalDate(2010, 1, 1), new LocalDate(2011, 1, 1));
    LocalDateInterval interval130101to140101 = LocalDateInterval.excluding(new LocalDate(2013, 1, 1), new LocalDate(2014, 1, 1));
    LocalDateInterval interval120201toOpen = LocalDateInterval.excluding(new LocalDate(2012, 2, 1), null);
    LocalDateInterval interval120101toOpen = LocalDateInterval.excluding(new LocalDate(2010, 2, 1), null);
    LocalDateInterval intervalOpen = LocalDateInterval.excluding(null, null);

    public static class Instantiate extends LocalDateIntervalTest {

        @Test
        public void test() {
            LocalDate startDate = new LocalDate(2012, 1, 1);
            LocalDate endDate = new LocalDate(2012, 1, 31);
            Interval jodaInterval = new Interval(startDate.toInterval().getStartMillis(), endDate.toInterval().getEndMillis());
            LocalDateInterval interval = new LocalDateInterval(jodaInterval);
            assertThat(interval.startDate(), is(startDate));
            assertThat(interval.endDate(), is(endDate));
        }

        @Test
        public void testEmptyInterval() {
            LocalDateInterval myInterval = new LocalDateInterval();
            assertNull(myInterval.startDate());
            assertNull(myInterval.endDate());
        }

    }

    public static class IsValid extends LocalDateIntervalTest {

        @Test
        public void testInvalid() {
            assertFalse(new LocalDateInterval(new LocalDate(2011, 1, 1), new LocalDate(2010, 1, 1)).isValid());
            assertFalse(new LocalDateInterval(new LocalDate(2011, 1, 1), new LocalDate(2010, 12, 30)).isValid());
            assertTrue(new LocalDateInterval(new LocalDate(2011, 1, 1), new LocalDate(2010, 12, 31)).isValid());
        }
    }

    public static class Overlaps extends LocalDateIntervalTest {

        @Test
        public void testIsWithinParent() {
            assertTrue(interval120101to120331incl.overlaps(interval120101to120401));
            assertTrue(interval111101to120501.overlaps(interval120101to120401));
            assertTrue(interval111101to120301.overlaps(interval120101to120401));
            assertTrue(interval120201to120501.overlaps(interval120101to120401));
            assertTrue(interval120201to120301.overlaps(interval120101to120401));
            assertFalse(interval100101to110101.overlaps(interval120101to120401));
            assertFalse(interval130101to140101.overlaps(interval120101to120401));
            assertTrue(interval120201toOpen.overlaps(interval120101to120401));
            assertTrue(intervalOpen.overlaps(interval120101to120401));
        }
    }

    public static class Contains extends LocalDateIntervalTest {

        @Test
        public void testIsFullInterval() {
            assertTrue(interval120101to120331incl.contains(interval120101to120401));
            assertTrue(interval111101to120501.contains(interval120101to120401));
            assertFalse(interval111101to120301.contains(interval120101to120401));
            assertFalse(interval120201to120501.contains(interval120101to120401));
            assertFalse(interval120201to120301.contains(interval120101to120401));
            assertFalse(interval100101to110101.contains(interval120101to120401));
            assertFalse(interval130101to140101.contains(interval120101to120401));
            assertFalse(interval120201toOpen.contains(interval120101to120401));
            assertTrue(intervalOpen.contains(interval120101to120401));
        }

        @Test
        public void testContainsDate() {
            assertTrue(interval120101to120401.contains(new LocalDate(2012, 1, 1)));
            assertTrue(interval120101to120401.contains(new LocalDate(2012, 3, 31)));
            assertTrue(interval120201toOpen.contains(new LocalDate(2099, 1, 1)));
            assertFalse(interval120201toOpen.contains(new LocalDate(2000, 1, 1)));
            assertTrue(interval120201toOpen.contains(new LocalDate(2012, 3, 1)));
            assertTrue(intervalOpen.contains(new LocalDate(2012, 3, 31)));
            assertFalse(interval120101to120401.contains(new LocalDate(2012, 4, 1)));
        }

        @Test
        public void  testContainsLocalDateInterval() {
            assertThat(LocalDateInterval.parseString("2010-01-01/2010-04-01").contains(LocalDateInterval.parseString("2010-01-01/2010-04-01")), is(true));
            assertThat(LocalDateInterval.parseString("2010-01-01/2010-04-01").contains(LocalDateInterval.parseString("2010-01-01/2010-05-01")), is(false));
        }
    }

    public static class StartDate extends LocalDateIntervalTest {

        @Test
        public void startDate() {
            assertEquals(new LocalDate(2012, 1, 1), interval120101to120331incl.overlap(interval120101to120401).startDate());
            assertEquals(new LocalDate(2012, 1, 1), interval111101to120501.overlap(interval120101to120401).startDate());
            assertEquals(new LocalDate(2012, 1, 1), interval111101to120301.overlap(interval120101to120401).startDate());
            assertEquals(new LocalDate(2012, 2, 1), interval120201to120501.overlap(interval120101to120401).startDate());
            assertEquals(new LocalDate(2012, 2, 1), interval120201to120301.overlap(interval120101to120401).startDate());
            assertEquals(new LocalDate(2012, 2, 1), interval120201toOpen.overlap(interval120101to120401).startDate());
            assertEquals(new LocalDate(2012, 1, 1), intervalOpen.overlap(interval120101to120401).startDate());
        }
    }

    public static class EndDateExcluding extends LocalDateIntervalTest {

        @Test
        public void endDateExcluding() {
            assertEquals(new LocalDate(2012, 4, 1), interval120101to120331incl.overlap(interval120101to120401).endDateExcluding());
            assertEquals(new LocalDate(2012, 4, 1), interval111101to120501.overlap(interval120101to120401).endDateExcluding());
            assertEquals(new LocalDate(2012, 3, 1), interval111101to120301.overlap(interval120101to120401).endDateExcluding());
            assertEquals(new LocalDate(2012, 4, 1), interval120201to120501.overlap(interval120101to120401).endDateExcluding());
            assertEquals(new LocalDate(2012, 3, 1), interval120201to120301.overlap(interval120101to120401).endDateExcluding());
            assertEquals(new LocalDate(2012, 4, 1), interval120201toOpen.overlap(interval120101to120401).endDateExcluding());
            assertEquals(new LocalDate(2012, 4, 1), intervalOpen.overlap(interval120101to120401).endDateExcluding());
        }
    }

    public static class Days extends LocalDateIntervalTest {

        @Test
        public void days() {
            assertEquals(91, interval120101to120331incl.overlap(interval120101to120401).days());
            assertEquals(91, interval111101to120501.overlap(interval120101to120401).days());
            assertEquals(60, interval111101to120301.overlap(interval120101to120401).days());
            assertEquals(60, interval120201to120501.overlap(interval120101to120401).days());
            assertEquals(29, interval120201to120301.overlap(interval120101to120401).days());
            assertEquals(60, interval120201toOpen.overlap(interval120101to120401).days());
            assertEquals(91, intervalOpen.overlap(interval120101to120401).days());
            assertEquals(91, interval120101to120401.overlap(intervalOpen).days());
            assertEquals(0, intervalOpen.overlap(intervalOpen).days());
        }
    }


    public static class EndDateFromStartDate extends LocalDateIntervalTest {

        @Test
        public void testEndDateFromStartDate() {
            assertThat(interval120101to120331incl.endDateFromStartDate(), is(interval120101to120331incl.startDate().minusDays(1)));
        }
    }

    public static class Overlap extends LocalDateIntervalTest {

        @Test
        public void testOverlap() {
            testOverlap("----------/----------", "----------/----------", "----------/----------");
            testOverlap("2010-01-01/----------", "----------/----------", "2010-01-01/----------");
            testOverlap("----------/----------", "2010-01-01/----------", "2010-01-01/----------");
            testOverlap("2010-01-01/----------", "2011-01-01/----------", "2011-01-01/----------");
            testOverlap("2011-01-01/----------", "2010-01-01/----------", "2011-01-01/----------");
            testOverlap("----------/2010-01-01", "2010-01-01/----------", null);
            testOverlap("----------/2010-02-01", "2010-01-01/----------", "2010-01-01/2010-02-01");
            testOverlap("2010-01-01/----------", "----------/2010-02-01", "2010-01-01/2010-02-01");
        }

        @Test
        public void testOpen() {
            assertThat(LocalDateInterval.parseString("*/*").overlap(LocalDateInterval.parseString("*/*")), is(LocalDateInterval.parseString("*/*")));
        }

        private void testOverlap(
                final String firstIntervalStr,
                final String secondIntervalStr,
                final String expectedIntervalStr) {
            LocalDateInterval first = LocalDateInterval.parseString(firstIntervalStr);
            LocalDateInterval second = LocalDateInterval.parseString(secondIntervalStr);
            LocalDateInterval overlap = first.overlap(second);
            LocalDateInterval expected = expectedIntervalStr == null ? null : LocalDateInterval.parseString(expectedIntervalStr);
            assertThat(overlap, is(expected));
        }

    }

    public static class ToString extends LocalDateIntervalTest {

        @Test
        public void testString() {
            assertThat(interval120201toOpen.toString(), is("2012-02-01/----------"));
        }


        @Test
        public void testStringWithFormat() {
            assertThat(LocalDateInterval.parseString("2010-07-01/2010-10-01").toString("dd-MM-yyy"), is("01-07-2010/30-09-2010"));
        }
    }

    public static class Equals extends LocalDateIntervalTest {

        @Test
        public void testEquals() {
            assertTrue(new LocalDateInterval().equals(new LocalDateInterval()));
            assertTrue(new LocalDateInterval(null, null, IntervalEnding.EXCLUDING_END_DATE).equals(new LocalDateInterval(null, null, IntervalEnding.INCLUDING_END_DATE)));
        }
    }

    public static class ParseString extends LocalDateIntervalTest {

        @Test
        public void testParseString() throws Exception {
            assertThat(LocalDateInterval.parseString("2010-07-01/2010-10-01").endDateExcluding(), is(new LocalDate(2010, 10, 1)));
        }
    }


}
