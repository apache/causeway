package org.apache.isis.core.commons.lang;

import java.util.List;

import org.apache.isis.core.commons.lang.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

public class StringUtils_SplitOnCommas {

    @Test
    public void length() {
        final List<String> list = StringUtils.splitOnCommas("foo,bar");
        Assert.assertThat(list.size(), CoreMatchers.is(2));
    }

    @Test
    public void elements() {
        final List<String> list = StringUtils.splitOnCommas("foo,bar");
        Assert.assertThat(list.get(0), CoreMatchers.is("foo"));
        Assert.assertThat(list.get(1), CoreMatchers.is("bar"));
    }

    @Test
    public void whenHasWhiteSpaceAfterComma() {
        final List<String> list = StringUtils.splitOnCommas("foo, bar");
        Assert.assertThat(list.get(0), CoreMatchers.is("foo"));
        Assert.assertThat(list.get(1), CoreMatchers.is("bar"));
    }

    @Test
    public void whenHasLeadingWhiteSpace() {
        final List<String> list = StringUtils.splitOnCommas(" foo, bar");
        Assert.assertThat(list.get(0), CoreMatchers.is("foo"));
        Assert.assertThat(list.get(1), CoreMatchers.is("bar"));
    }

    @Test
    public void whenNull() {
        final List<String> list = StringUtils.splitOnCommas(null);
        Assert.assertThat(list, CoreMatchers.is(CoreMatchers.nullValue()));
    }

    @Test
    public void whenEmpty() {
        final List<String> list = StringUtils.splitOnCommas("");
        Assert.assertThat(list.size(), CoreMatchers.is(0));
    }

    @Test
    public void whenOnlyWhiteSpace() {
        final List<String> list = StringUtils.splitOnCommas(" ");
        Assert.assertThat(list.size(), CoreMatchers.is(0));
    }

    @Test
    public void whenOnlyWhiteSpaceTabs() {
        final List<String> list = StringUtils.splitOnCommas("\t");
        Assert.assertThat(list.size(), CoreMatchers.is(0));
    }

}
