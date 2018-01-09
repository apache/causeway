package org.apache.isis.applib.layout.component;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.annotation.Repainting;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class PropertyLayoutData_repaint_Test {

    PropertyLayoutData data;
    @Before
    public void setUp() throws Exception {
        data = new PropertyLayoutData();

        assertThat(data.getUnchanging(), is(nullValue()));
        assertThat(data.getRepainting(), is(nullValue()));
    }

    @Test
    public void derive_from_setUnchanging_TRUE_when_null() throws Exception {

        // when
        data.setUnchanging(true);

        // then
        assertThat(data.getRepainting(), is(Repainting.NO_REPAINT));
        assertThat(data.getUnchanging(), is(true));

    }

    @Test
    public void derive_from_setUnchanging_FALSE_when_null() throws Exception {

        // when
        data.setUnchanging(false);

        // then
        assertThat(data.getRepainting(), is(Repainting.REPAINT));
        assertThat(data.getUnchanging(), is(false));

    }

    @Test
    public void ignore_from_setUnchanging_once_set_to_NO_REPAINT() throws Exception {

        // given
        data.setRepainting(Repainting.NO_REPAINT);

        // when
        data.setUnchanging(false);

        // then (ignored)
        assertThat(data.getRepainting(), is(Repainting.NO_REPAINT));
        assertThat(data.getUnchanging(), is(true));
    }

    @Test
    public void ignore_from_setUnchanging_once_set_to_REPAINT() throws Exception {

        // given
        data.setRepainting(Repainting.REPAINT);

        // when
        data.setUnchanging(true);

        // then (ignored)
        assertThat(data.getRepainting(), is(Repainting.REPAINT));
        assertThat(data.getUnchanging(), is(false));
    }

}