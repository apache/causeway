package org.apache.isis.viewer.restfulobjects.tck;

import org.junit.Test;
import org.apache.isis.viewer.restfulobjects.applib.Rel;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class RelTest_matches {

    @Test
    public void whenDoes() throws Exception {
        assertThat(Rel.ACTION.matches(Rel.ACTION), is(true));
    }

    @Test
    public void whenDoesNot() throws Exception {
        assertThat(Rel.ACTION.matches(Rel.ACTION_PARAM), is(false));
    }

    @Test
    public void whenMatchesOnStr() throws Exception {
        assertThat(Rel.ACTION.matches(Rel.ACTION.getName()), is(true));
    }

    @Test
    public void whenMatchesOnStrWithParams() throws Exception {
        assertThat(Rel.ACTION.matches(Rel.ACTION.andParam("foo", "bar")), is(true));
    }


}
