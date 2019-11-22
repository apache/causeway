package org.incode.module.base.dom.with;

import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public abstract class WithIntervalMutableContractTestAbstract_changeDates<T extends WithIntervalMutable<T>> {
    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);
    protected T withIntervalMutable;
    @Mock
    private WithIntervalMutable.Helper<T> mockChangeDates;

    @Before
    public void setUpWithIntervalMutable() throws Exception {
        withIntervalMutable = doCreateWithIntervalMutable(mockChangeDates);
    }

    protected abstract T doCreateWithIntervalMutable(WithIntervalMutable.Helper<T> mockChangeDates);

    @Test
    public void default0ChangeDates() {
        final LocalDate localDate = new LocalDate(2013,7,1);
        context.checking(new Expectations() {
            {
                oneOf(mockChangeDates).default0ChangeDates();
                will(returnValue(localDate));
            }
        });
        assertThat(withIntervalMutable.default0ChangeDates(), is(localDate));
    }

    @Test
    public void default1ChangeDates() {
        final LocalDate localDate = new LocalDate(2013,7,1);
        context.checking(new Expectations() {
            {
                oneOf(mockChangeDates).default1ChangeDates();
                will(returnValue(localDate));
            }
        });
        assertThat(withIntervalMutable.default1ChangeDates(), is(localDate));
    }

    @Test
    public void validateChangeDates() {
        final LocalDate startDate = new LocalDate(2013,4,1);
        final LocalDate endDate = new LocalDate(2013,7,1);
        final String reason = "xxx";
        context.checking(new Expectations() {
            {
                oneOf(mockChangeDates).validateChangeDates(startDate, endDate);
                will(returnValue(reason));
            }
        });
        assertThat(withIntervalMutable.validateChangeDates(startDate, endDate), is(reason));
    }
}
