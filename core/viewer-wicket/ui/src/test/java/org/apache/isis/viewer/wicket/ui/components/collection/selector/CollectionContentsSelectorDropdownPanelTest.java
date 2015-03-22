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
package org.apache.isis.viewer.wicket.ui.components.collection.selector;

import java.util.Arrays;
import java.util.List;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.CollectionContentsAsAjaxTablePanelFactory;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CollectionContentsSelectorDropdownPanelTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private ComponentFactory one;
    
    @Mock
    private ComponentFactory two;

    private ComponentFactory ajaxTableComponentFactory;
    
    @Before
    public void setUp() throws Exception {
        ajaxTableComponentFactory = new CollectionContentsAsAjaxTablePanelFactory();
    }
    
    @Test
    public void testOrderAjaxTableToEnd() {
        
        List<ComponentFactory> componentFactories = 
                Arrays.<ComponentFactory>asList(
                        one,
                        ajaxTableComponentFactory, 
                        two);
        List<ComponentFactory> orderAjaxTableToEnd = CollectionSelectorHelper.orderAjaxTableToEnd(componentFactories);
        assertThat(orderAjaxTableToEnd.get(0), is(one));
        assertThat(orderAjaxTableToEnd.get(1), is(two));
        assertThat(orderAjaxTableToEnd.get(2), is(ajaxTableComponentFactory));
    }

}
