package org.apache.isis.extensions.base.dom.utils;

import org.hamcrest.core.Is;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.junit.Assert;
import org.junit.Test;

public class JodaPeriodUtilsTest {

    public static class AsPeriod extends JodaPeriodUtilsTest {

        @Test
        public void happyCase() {
            Period period = JodaPeriodUtils.asPeriod("6y6m3d");
            LocalDate startDate = new LocalDate(2000, 1, 1);
            Assert.assertThat(startDate.plus(period), Is.is(new LocalDate(2006, 7, 4)));
        }

        @Test
        public void withSpaces() {
            Period period = JodaPeriodUtils.asPeriod("  6Y  6m  ");
            LocalDate startDate = new LocalDate(2000, 1, 1);
            Assert.assertThat(startDate.plus(period), Is.is(new LocalDate(2006, 7, 1)));
        }

        @Test
        public void whenMalformed() {
            Period period = JodaPeriodUtils.asPeriod("6x6y");
            LocalDate startDate = new LocalDate(2000, 1, 1);
            Assert.assertThat(startDate.plus(period), Is.is(new LocalDate(2000, 1, 1)));
        }
    }

    public static class AsString extends JodaPeriodUtilsTest {
        @Test
        public void happyCase() throws Exception {
            Period period = new Period(new LocalDate(2000, 1, 1), new LocalDate(2006, 7, 2));
            Assert.assertThat(JodaPeriodUtils.asString(period), Is.is("6 years, 6 months & 1 day"));
        }
    }

    public static class AsSimpleString extends JodaPeriodUtilsTest {
        @Test
        public void happyCase() throws Exception {
            Period period = new Period(new LocalDate(2000, 1, 1), new LocalDate(2006, 7, 2));
            Assert.assertThat(JodaPeriodUtils.asSimpleString(period), Is.is("6y6m1d"));
        }
    }

}
