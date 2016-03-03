package org.apache.isis.applib.layout.grid.bootstrap3;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SizeSpanTest {

    SizeSpan ss;

    @Before
    public void setUp() throws Exception {
        ss = new SizeSpan();
    }

    @Test
    public void with_no_offset() throws Exception {

        ss.setSize(Size.MD);
        ss.setSpan(4);

        final String s = ss.toCssClassFragment();

        assertThat(s, is(equalTo("col-md-4")));

    }

    @Test
    public void with_offset() throws Exception {

        ss.setSize(Size.SM);
        ss.setSpan(0);
        ss.setOffset(true);

        final String s = ss.toCssClassFragment();

        assertThat(s, is(equalTo("col-sm-offset-0")));

    }

}