package org.apache.isis.extensions.bdd.common.util;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

public class Strings_RemoveLeadingWhiteSpace {

    @Test
    public void whenHasLeadingWhiteSpace() {
        final String removed = Strings.removeLeadingWhiteSpace(" 	 foo");
        Assert.assertThat(removed, CoreMatchers.is("foo"));
    }

    @Test
    public void whenNoLeadingWhiteSpace() {
        final String removed = Strings.removeLeadingWhiteSpace("foo");
        Assert.assertThat(removed, CoreMatchers.is("foo"));
    }

    @Test
    public void empty() {
        final String removed = Strings.removeLeadingWhiteSpace("");
        Assert.assertThat(removed, CoreMatchers.is(""));
    }

    @Test
    public void whenNull() {
        final String removed = Strings.removeLeadingWhiteSpace(null);
        Assert.assertThat(removed, CoreMatchers.is(CoreMatchers.nullValue()));
    }

}
