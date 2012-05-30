package org.apache.isis.core.commons.config;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Iterator;

import org.jmock.auto.Mock;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2.Mode;

public class IsisConfigurationDefaultTest_createSubset {
    
    private IsisConfigurationDefault configuration;

    @Before
    public void setUp() throws Exception {
        configuration = new IsisConfigurationDefault();
    }

    @After
    public void tearDown() throws Exception {
        configuration = null;
    }

    @Test
    public void empty() {
        final IsisConfiguration subset = configuration.createSubset("foo");
        assertThat(subset.iterator().hasNext(), is(false));
    }

    @Test
    public void nonEmptyButNoneInSubset() {
        configuration.add("bar", "barValue");
        final IsisConfiguration subset = configuration.createSubset("foo");
        assertThat(subset.iterator().hasNext(), is(false));
    }

    @Test
    public void nonEmptyButSingleKeyedInSubset() {
        configuration.add("foo", "fooValue");
        final IsisConfiguration subset = configuration.createSubset("foo");
        final Iterator<String> iterator = subset.iterator();
        assertThat(iterator.hasNext(), is(false));
    }

    @Test
    public void nonEmptyAndMultiKeyedInSubset() {
        configuration.add("foo.foz", "fozValue");
        final IsisConfiguration subset = configuration.createSubset("foo");
        final Iterator<String> iterator = subset.iterator();
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is("foz"));
        assertThat(iterator.hasNext(), is(false));
        assertThat(subset.getString("foz"), is("fozValue"));
    }

    @Test
    public void propertiesOutsideOfSubsetAreIgnored() {
        configuration.add("foo.foz", "fozValue");
        configuration.add("foo.faz", "fazValue");
        configuration.add("bar.baz", "bazValue");
        final IsisConfiguration subset = configuration.createSubset("foo");
        assertThat(subset.getString("foz"), is("fozValue"));
        assertThat(subset.getString("faz"), is("fazValue"));
        
        final Iterator<String> iterator = subset.iterator();
        assertThat(iterator.hasNext(), is(true));
        iterator.next();
        assertThat(iterator.hasNext(), is(true));
        iterator.next();
        assertThat(iterator.hasNext(), is(false));
    }

}
