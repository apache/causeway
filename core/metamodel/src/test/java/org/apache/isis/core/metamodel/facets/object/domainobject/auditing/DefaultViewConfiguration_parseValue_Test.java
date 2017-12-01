package org.apache.isis.core.metamodel.facets.object.domainobject.auditing;

import org.junit.Assert;
import org.junit.Test;

import static org.apache.isis.core.metamodel.facets.object.domainobject.auditing.DefaultViewConfiguration.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class DefaultViewConfiguration_parseValue_Test {


    @Test
    public void when_hidden() throws Exception {
        Assert.assertThat(parseValue(null), is(equalTo(HIDDEN)));
        Assert.assertThat(parseValue(""), is(equalTo(HIDDEN)));
        Assert.assertThat(parseValue("hidden"), is(equalTo(HIDDEN)));
        Assert.assertThat(parseValue("garbage"), is(equalTo(HIDDEN)));
    }

    @Test
    public void when_table() throws Exception {
        Assert.assertThat(parseValue("table"), is(equalTo(TABLE)));
        Assert.assertThat(parseValue("TABLE"), is(equalTo(TABLE)));
        Assert.assertThat(parseValue("tAbLe"), is(equalTo(TABLE)));
        Assert.assertThat(parseValue("  table  "), is(equalTo(TABLE)));
        Assert.assertThat(parseValue("  \ntable \n "), is(equalTo(TABLE)));
    }

}