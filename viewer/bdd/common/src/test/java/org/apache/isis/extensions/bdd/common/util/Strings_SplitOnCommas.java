package org.apache.isis.extensions.bdd.common.util;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

public class Strings_SplitOnCommas {

    @Test
    public void length() {
        final String[] list = Strings.splitOnCommas("foo,bar");
        Assert.assertThat(list.length, CoreMatchers.is(2));
    }

    @Test
    public void elements() {
        final String[] list = Strings.splitOnCommas("foo,bar");
        Assert.assertThat(list[0], CoreMatchers.is("foo"));
        Assert.assertThat(list[1], CoreMatchers.is("bar"));
    }

    @Test
    public void whenHasWhiteSpaceAfterComma() {
        final String[] list = Strings.splitOnCommas("foo, bar");
        Assert.assertThat(list[0], CoreMatchers.is("foo"));
        Assert.assertThat(list[1], CoreMatchers.is("bar"));
    }

    @Test
    public void whenHasLeadingWhiteSpace() {
        final String[] list = Strings.splitOnCommas(" foo, bar");
        Assert.assertThat(list[0], CoreMatchers.is("foo"));
        Assert.assertThat(list[1], CoreMatchers.is("bar"));
    }

    @Test
    public void whenNull() {
        final String[] list = Strings.splitOnCommas(null);
        Assert.assertThat(list, CoreMatchers.is(CoreMatchers.nullValue()));
    }

    @Test
    public void whenEmpty() {
        final String[] list = Strings.splitOnCommas("");
        Assert.assertThat(list.length, CoreMatchers.is(0));
    }

    @Test
    public void whenOnlyWhiteSpace() {
        final String[] list = Strings.splitOnCommas(" ");
        Assert.assertThat(list.length, CoreMatchers.is(0));
    }

    @Test
    public void whenOnlyWhiteSpaceTabs() {
        final String[] list = Strings.splitOnCommas("\t");
        Assert.assertThat(list.length, CoreMatchers.is(0));
    }

}
