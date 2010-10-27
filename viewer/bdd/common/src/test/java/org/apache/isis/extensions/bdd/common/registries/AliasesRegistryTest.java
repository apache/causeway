package org.apache.isis.extensions.bdd.common.registries;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.bdd.common.story.registries.AliasRegistryDefault;
import org.hamcrest.CoreMatchers;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class AliasesRegistryTest {

    private final Mockery mockery = new JUnit4Mockery();

    private AliasRegistryDefault registry;

    private ObjectAdapter mockAdapter1;
    private ObjectAdapter mockAdapter2;
    @SuppressWarnings("unused")
    private ObjectAdapter mockAdapter3;

    @Before
    public void setUp() throws Exception {
        mockAdapter1 = mockery.mock(ObjectAdapter.class, "adapter1");
        mockAdapter2 = mockery.mock(ObjectAdapter.class, "adapter2");
        mockAdapter3 = mockery.mock(ObjectAdapter.class, "adapter3");
        registry = new AliasRegistryDefault();
    }

    @Test
    public void registerOneAdapter() {
        final String heldAs1 = registry.aliasPrefixedAs("Foo", mockAdapter1);
        Assert.assertThat(heldAs1, CoreMatchers.is("Foo#1"));
    }

    @Test
    public void registerTwoAdaptersOfSamePrefix() {
        @SuppressWarnings("unused")
        final String heldAs1 = registry.aliasPrefixedAs("Foo", mockAdapter1);
        final String heldAs2 = registry.aliasPrefixedAs("Foo", mockAdapter2);
        Assert.assertThat(heldAs2, CoreMatchers.is("Foo#2"));
    }

    @Test
    public void registerAdaptersOfDiffereingPrefixes() {
        @SuppressWarnings("unused")
        final String heldAs1 = registry.aliasPrefixedAs("Foo", mockAdapter1);
        final String heldAs2 = registry.aliasPrefixedAs("Bar", mockAdapter2);
        Assert.assertThat(heldAs2, CoreMatchers.is("Bar#1"));
    }

}
