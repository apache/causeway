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

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.widgets.select2.providers.ObjectAdapterMementoProviderForValueChoices;
import org.apache.isis.webapp.context.IsisWebAppCommonContext;
import org.apache.isis.webapp.context.memento.ObjectMemento;

public class ObjectAdapterMementoProviderForValueChoicesTest {

    @Rule public JUnitRuleMockery2 context = 
            JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    private List<ObjectMemento> mementos;

    private ObjectMemento mockMemento1;
    private ObjectMemento mockMemento2;
    private ObjectAdapterMementoProviderForValueChoices provider;

    @Mock private SpecificationLoader mockSpecificationLoader;
    @Mock private ObjectSpecification mockSpec;
    @Mock private ScalarModel mockScalarModel;
    @Mock private IsisWebAppCommonContext mockCommonContext;
    @Mock private WicketViewerSettings mockWicketViewerSettings;

    @Before
    public void setUp() throws Exception {
        final ObjectSpecId fakeSpecId = ObjectSpecId.of("FAKE");

        mockMemento1 = mock(fakeSpecId, "mockMemento1");
        mockMemento2 = mock(fakeSpecId, "mockMemento2");

        mementos = _Lists.of(mockMemento1, mockMemento2);
        
        context.checking(new Expectations() {        {
            
            allowing(mockScalarModel).getCommonContext();
            will(returnValue(mockCommonContext));
            
            allowing(mockCommonContext).lookupServiceElseFail(WicketViewerSettings.class);
            will(returnValue(mockWicketViewerSettings));
            
            allowing(mockCommonContext).getSpecificationLoader();
            will(returnValue(mockSpecificationLoader));
            
            allowing(mockSpecificationLoader).lookupBySpecIdElseLoad(fakeSpecId);
            will(returnValue(mockSpec));

            allowing(mockSpec).isEncodeable();
            will(returnValue(true));
        }});
        
        provider = new ObjectAdapterMementoProviderForValueChoices(mockScalarModel, mementos) {
            private static final long serialVersionUID = 1L;
        };

    }

    @Test
    public void whenInList() throws Exception {
        final Collection<ObjectMemento> mementos = provider.toChoices(Collections.singletonList("FAKE:mockMemento1"));
        Assert.assertThat(mementos.size(), is(1));
        Assert.assertThat(mementos.iterator().next(), is(mockMemento1));
    }

    private ObjectMemento mock(
            final ObjectSpecId specId,
            final String id) {
        final ObjectMemento mock = context.mock(ObjectMemento.class, id);
        context.checking(new Expectations() {{
            allowing(mock).getObjectSpecId();
            will(returnValue(specId));

            allowing(mock).asString();
            will(returnValue(id));
        }});
        return mock;
    }

}
