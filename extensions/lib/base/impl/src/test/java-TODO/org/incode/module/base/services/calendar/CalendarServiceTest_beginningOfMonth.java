package org.incode.module.base.services.calendar;

import java.util.Arrays;
import java.util.Collection;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import org.apache.isis.applib.services.clock.ClockService;

import org.incode.module.base.services.calendar.CalendarService;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class CalendarServiceTest_beginningOfMonth {

    private CalendarService calendarService;
    private ClockService stubClockService;
    protected LocalDate now;
    private LocalDate expected;

    @Parameters
    public static Collection<Object[]> data() {
      return Arrays.asList(
              new Object[][] { 
                      { new LocalDate(2013,4,15), new LocalDate(2013,4,1)}, 
                      { new LocalDate(2013,4,1),  new LocalDate(2013,4,1)}, 
                      { new LocalDate(2013,4,30),  new LocalDate(2013,4,1)}, 
              });
    }
    
    public CalendarServiceTest_beginningOfMonth(LocalDate date, LocalDate expected) {
        this.now = date;
        this.expected = expected;
    }
    
    @Before
    public void setUp() throws Exception {

        stubClockService = new ClockService() {
            @Override
            public LocalDate now() {
                return now;
            }
        };

        calendarService = new CalendarService();
        calendarService.clockService = stubClockService;
    }
    
    @Test
    public void test() throws Exception {
        assertThat(calendarService.beginningOfMonth(), is(expected));
    }


}
