/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.viewer.wicket.ui.components.widgets.valuechoices;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.isis.commons.internal.collections._Lists;

import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.ui.components.widgets.select2.providers.ObjectAdapterMementoProviderForValueChoices;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

public class ObjectAdapterMementoProviderForValueChoicesTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    private List<ObjectAdapterMemento> mementos;

    private ObjectAdapterMemento mockMemento1;
    private ObjectAdapterMemento mockMemento2;
    private ObjectAdapterMementoProviderForValueChoices provider;

    @Before
    public void setUp() throws Exception {
        mockMemento1 = mock("mockMemento1");
        mockMemento2 = mock("mockMemento2");

        mementos = _Lists.of(
                mockMemento1, mockMemento2
        );

        WicketViewerSettings wicketViewerSettings = context.mock(WicketViewerSettings.class);
        provider = new ObjectAdapterMementoProviderForValueChoices(null, mementos, wicketViewerSettings);
    }

    @Test
    public void whenInList() throws Exception {
        final Collection<ObjectAdapterMemento> mementos = provider.toChoices(Collections.singletonList("mockMemento1"));
        Assert.assertThat(mementos.size(), is(1));
        Assert.assertThat(mementos.iterator().next(), is(mockMemento1));
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
