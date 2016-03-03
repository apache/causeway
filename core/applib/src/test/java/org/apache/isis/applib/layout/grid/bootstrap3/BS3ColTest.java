package org.apache.isis.applib.layout.grid.bootstrap3;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BS3ColTest {

    BS3Col bs3Col;
    @Before
    public void setUp() throws Exception {
        bs3Col = new BS3Col();
    }

    @Test
    public void size_and_span() throws Exception {
        bs3Col.setSize(Size.MD);
        bs3Col.setSpan(4);

        assertThat(bs3Col.toCssClass(), is(equalTo("col-md-4")));
    }

    @Test
    public void extra_css_class() throws Exception {
        bs3Col.setSize(Size.SM);
        bs3Col.setSpan(8);
        bs3Col.setCssClass("foobar");

        assertThat(bs3Col.toCssClass(), is(equalTo("col-sm-8 foobar")));
    }

    @Test
    public void with_additional_classes() throws Exception {
        bs3Col.setSize(Size.SM);
        bs3Col.setSpan(6);

        bs3Col.getSizeSpans().add(SizeSpan.with(Size.MD, 5));
        bs3Col.getSizeSpans().add(SizeSpan.offset(Size.MD, 2));

        assertThat(bs3Col.toCssClass(), is(equalTo("col-sm-6 col-md-5 col-md-offset-2")));
    }
}