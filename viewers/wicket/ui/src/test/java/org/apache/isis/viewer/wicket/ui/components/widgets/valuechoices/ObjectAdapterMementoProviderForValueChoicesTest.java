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
import java.util.Optional;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.widgets.select2.providers.ObjectAdapterMementoProviderForValueChoices;

import lombok.val;

public class ObjectAdapterMementoProviderForValueChoicesTest {

    @Rule public JUnitRuleMockery2 context =
            JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    private Can<ObjectMemento> mementos;

    private ObjectMemento mockMemento1;
    private ObjectMemento mockMemento2;
    private ObjectAdapterMementoProviderForValueChoices provider;

    @Mock private SpecificationLoader mockSpecificationLoader;
    @Mock private ObjectSpecification mockSpec;
    @Mock private ScalarModel mockScalarModel;
    @Mock private IsisAppCommonContext mockCommonContext;
    @Mock private WicketViewerSettings mockWicketViewerSettings;

    @Before
    public void setUp() throws Exception {
        final String fakeObjectType = "FAKE";

        val fakeLocalType = LogicalType.lazy(getClass(), ()->fakeObjectType);

        mockMemento1 = mock(fakeLocalType, "mockMemento1");
        mockMemento2 = mock(fakeLocalType, "mockMemento2");

        mementos = Can.of(mockMemento1, mockMemento2);

        context.checking(new Expectations() {        {

            allowing(mockScalarModel).getCommonContext();
            will(returnValue(mockCommonContext));

            allowing(mockCommonContext).lookupServiceElseFail(WicketViewerSettings.class);
            will(returnValue(mockWicketViewerSettings));

            allowing(mockCommonContext).getSpecificationLoader();
            will(returnValue(mockSpecificationLoader));

            allowing(mockSpecificationLoader).specForLogicalType(fakeLocalType);
            will(returnValue(Optional.of(mockSpec)));

            allowing(mockSpec).isEncodeable();
            will(returnValue(true));
        }});

        provider = new ObjectAdapterMementoProviderForValueChoices(mockScalarModel) {
            private static final long serialVersionUID = 1L;
            @Override public org.apache.isis.commons.collections.Can<ObjectMemento> getChoiceMementos() {
                return mementos; };
        };

    }

    @Test
    public void whenInList() throws Exception {
        final Collection<ObjectMemento> mementos = provider.toChoices(Collections.singletonList("FAKE:mockMemento1"));
        Assert.assertThat(mementos.size(), is(1));
        Assert.assertThat(mementos.iterator().next(), is(mockMemento1));
    }

    private ObjectMemento mock(
            final LogicalType logicalType,
            final String id) {
        final ObjectMemento mock = context.mock(ObjectMemento.class, id);
        context.checking(new Expectations() {{
            allowing(mock).getLogicalType();
            will(returnValue(logicalType));

            allowing(mock).asString();
            will(returnValue(id));
        }});
        return mock;
    }

}
