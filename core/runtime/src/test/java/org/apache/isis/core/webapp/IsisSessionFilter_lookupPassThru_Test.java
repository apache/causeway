package org.apache.isis.core.webapp;

import java.util.List;

import javax.servlet.FilterConfig;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;

import static org.hamcrest.CoreMatchers.is;

public class IsisSessionFilter_lookupPassThru_Test {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    IsisSessionFilter isisSessionFilter;

    @Mock
    FilterConfig mockFilterConfig;

    @Before
    public void setUp() throws Exception {
        isisSessionFilter = new IsisSessionFilter();
    }

    @Test
    public void when_null() throws Exception {
        context.checking(new Expectations() {{
            allowing(mockFilterConfig).getInitParameter(IsisSessionFilter.PASS_THRU_KEY);
            will(returnValue(null));
        }});

        final List<String> x = isisSessionFilter.lookupAndParsePassThru(mockFilterConfig);
        Assert.assertThat(x.size(), is(0));
    }

    @Test
    public void when_none() throws Exception {
        context.checking(new Expectations() {{
            allowing(mockFilterConfig).getInitParameter(IsisSessionFilter.PASS_THRU_KEY);
            will(returnValue(""));
        }});

        final List<String> x = isisSessionFilter.lookupAndParsePassThru(mockFilterConfig);
        Assert.assertThat(x.size(), is(0));
    }

    @Test
    public void when_one() throws Exception {
        context.checking(new Expectations() {{
            allowing(mockFilterConfig).getInitParameter(IsisSessionFilter.PASS_THRU_KEY);
            will(returnValue("/abc"));
        }});

        final List<String> x = isisSessionFilter.lookupAndParsePassThru(mockFilterConfig);
        Assert.assertThat(x.size(), is(1));
        Assert.assertThat(x.get(0), is("/abc"));
    }

    @Test
    public void when_several() throws Exception {
        context.checking(new Expectations() {{
            allowing(mockFilterConfig).getInitParameter(IsisSessionFilter.PASS_THRU_KEY);
            will(returnValue("/abc,/def"));
        }});

        final List<String> x = isisSessionFilter.lookupAndParsePassThru(mockFilterConfig);
        Assert.assertThat(x.size(), is(2));
        Assert.assertThat(x.get(0), is("/abc"));
        Assert.assertThat(x.get(1), is("/def"));
    }

}