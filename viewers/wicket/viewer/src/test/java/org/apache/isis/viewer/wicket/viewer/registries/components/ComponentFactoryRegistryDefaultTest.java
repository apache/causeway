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
package org.apache.isis.viewer.wicket.viewer.registries.components;

import java.util.List;

import org.apache.wicket.model.IModel;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.apache.isis.viewer.common.model.components.ComponentType;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentFactory.ApplicationAdvice;
import org.apache.isis.viewer.wicket.ui.ComponentFactoryAbstract;
import org.apache.isis.viewer.wicket.ui.components.collection.selector.CollectionSelectorHelper;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.CollectionContentsAsAjaxTablePanelFactory;

import static org.hamcrest.MatcherAssert.assertThat;

class ComponentFactoryRegistryDefaultTest {

    private ComponentFactory one;
    private ComponentFactory two;
    private ComponentFactory ajaxTableComponentFactory;

    @BeforeEach
    void setUp() throws Exception {

        ajaxTableComponentFactory = new CollectionContentsAsAjaxTablePanelFactory() {
            private static final long serialVersionUID = 1L;
            @Override public ApplicationAdvice appliesTo(final IModel<?> model) {
                return ApplicationAdvice.APPLIES;
            }
        };

        one = Mockito.mock(ComponentFactoryAbstract.class);
        Mockito.when(one.getComponentType()).thenReturn(ComponentType.COLLECTION_CONTENTS);
        Mockito.when(one.appliesTo(ComponentType.COLLECTION_CONTENTS, null)).thenReturn(ApplicationAdvice.APPLIES);

        two = Mockito.mock(ComponentFactoryAbstract.class);
        Mockito.when(two.getComponentType()).thenReturn(ComponentType.COLLECTION_CONTENTS);
        Mockito.when(two.appliesTo(ComponentType.COLLECTION_CONTENTS, null)).thenReturn(ApplicationAdvice.APPLIES);
    }

    @Test
    void testOrderAjaxTableToEnd() {

        final var compRegistry = ComponentFactoryRegistryDefault.forTesting(List.of(
                one,
                ajaxTableComponentFactory,
                two));

        final var orderAjaxTableToEnd = new CollectionSelectorHelper(null, compRegistry)
                .getComponentFactories();

        assertThat(orderAjaxTableToEnd, Matchers.contains(
                one,
                two,
                ajaxTableComponentFactory));

    }

}
