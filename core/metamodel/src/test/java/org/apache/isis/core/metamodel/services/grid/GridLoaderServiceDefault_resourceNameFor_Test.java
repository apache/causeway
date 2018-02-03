package org.apache.isis.core.metamodel.services.grid;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

public class GridLoaderServiceDefault_resourceNameFor_Test {

    private GridLoaderServiceDefault gridLoaderServiceDefault;

    @Before
    public void setUp() throws Exception {
        gridLoaderServiceDefault = new GridLoaderServiceDefault();
    }

    @Test
    public void when_default_exists() {
        final String s = gridLoaderServiceDefault.resourceNameFor(Foo.class);
        Assert.assertThat(s, is(equalTo("Foo.layout.xml")));
    }

    @Test
    public void when_fallback_exists() {
        final String s = gridLoaderServiceDefault.resourceNameFor(Foo2.class);
        Assert.assertThat(s, is(equalTo("Foo2.layout.fallback.xml")));
    }
    @Test
    public void when_default_and_fallback_both_exist() {
        final String s = gridLoaderServiceDefault.resourceNameFor(Foo3.class);
        Assert.assertThat(s, is(equalTo("Foo3.layout.xml")));
    }
    @Test
    public void when_neither_exist() {
        final String s = gridLoaderServiceDefault.resourceNameFor(Foo4.class);
        Assert.assertNull(s);
    }
}