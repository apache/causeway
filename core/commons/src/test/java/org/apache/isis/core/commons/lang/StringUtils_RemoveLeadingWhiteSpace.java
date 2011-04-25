package org.apache.isis.core.commons.lang;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

public class StringUtils_RemoveLeadingWhiteSpace {

    @Test
    public void whenHasLeadingWhiteSpace() {
        final String removed = StringUtils.removeLeadingWhiteSpace(" 	 foo");
        Assert.assertThat(removed, CoreMatchers.is("foo"));
    }

    @Test
    public void whenNoLeadingWhiteSpace() {
        final String removed = StringUtils.removeLeadingWhiteSpace("foo");
        Assert.assertThat(removed, CoreMatchers.is("foo"));
    }

    @Test
    public void empty() {
        final String removed = StringUtils.removeLeadingWhiteSpace("");
        Assert.assertThat(removed, CoreMatchers.is(""));
    }

    @Test
    public void whenNull() {
        final String removed = StringUtils.removeLeadingWhiteSpace(null);
        Assert.assertThat(removed, CoreMatchers.is(CoreMatchers.nullValue()));
    }

}
