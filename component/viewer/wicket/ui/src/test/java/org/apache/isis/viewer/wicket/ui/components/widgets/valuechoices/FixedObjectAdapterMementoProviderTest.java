package org.apache.isis.viewer.wicket.ui.components.widgets.valuechoices;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.google.common.collect.Lists;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

public class FixedObjectAdapterMementoProviderTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    private List<ObjectAdapterMemento> mementos;

    private ObjectAdapterMemento mockMemento1;
    private ObjectAdapterMemento mockMemento2;
    private ValueChoicesSelect2Panel.FixedObjectAdapterMementoProvider provider;

    @Before
    public void setUp() throws Exception {
        mockMemento1 = mock("mockMemento1");
        mockMemento2 = mock("mockMemento2");

        mementos = Lists.newArrayList(
                mockMemento1, mockMemento2
        );

        provider = new ValueChoicesSelect2Panel.FixedObjectAdapterMementoProvider(null, mementos);
    }

    @Test
    public void whenInList() throws Exception {
        final Collection<ObjectAdapterMemento> mementos = provider.toChoices(Collections.singletonList("mockMemento1"));
        Assert.assertThat(mementos.size(), is(1));
        Assert.assertThat(mementos.iterator().next(), is(mockMemento1));
    }

    @Test
    public void whenNullPlaceholder() throws Exception {
        final Collection<ObjectAdapterMemento> mementos = provider.toChoices(Collections.singletonList("$$_isis_null_$$"));
        Assert.assertThat(mementos.size(), is(1));
        Assert.assertThat(mementos.iterator().next(), is(nullValue()));
    }

    private ObjectAdapterMemento mock(final String id) {
        final ObjectAdapterMemento mock = context.mock(ObjectAdapterMemento.class, id);
        context.checking(new Expectations() {{
            allowing(mock).asString();
            will(returnValue(id));
        }});
        return mock;
    }

}