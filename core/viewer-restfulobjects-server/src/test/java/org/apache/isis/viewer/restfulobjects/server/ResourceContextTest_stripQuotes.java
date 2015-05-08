package org.apache.isis.viewer.restfulobjects.server;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class ResourceContextTest_stripQuotes {

    @Test
    public void whenQuotes() throws Exception {
        final String x = ResourceContext.stripQuotes("\"123\"");
        assertThat(x, is("123"));
    }

    @Test
    public void whenNoQuotes() throws Exception {
        final String x = ResourceContext.stripQuotes("123");
        assertThat(x, is("123"));
    }

    @Test
    public void whenFirstQuote() throws Exception {
        final String x = ResourceContext.stripQuotes("\"123");
        assertThat(x, is("\"123"));
    }

    @Test
    public void whenEndQuote() throws Exception {
        final String x = ResourceContext.stripQuotes("123\"");
        assertThat(x, is("123\""));
    }

    @Test
    public void whenCharsAfter() throws Exception {
        final String x = ResourceContext.stripQuotes("\"123\" ");
        assertThat(x, is("\"123\" "));
    }

    @Test
    public void whenCharsBefore() throws Exception {
        final String x = ResourceContext.stripQuotes(" \"123\"");
        assertThat(x, is(" \"123\""));
    }

    @Test
    public void whenEmpty() throws Exception {
        final String x = ResourceContext.stripQuotes("");
        assertThat(x, is(""));
    }

    @Test
    public void whenNull() throws Exception {
        final String x = ResourceContext.stripQuotes(null);
        assertThat(x, is(nullValue()));
    }

}