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
package org.apache.isis.viewer.wicket.model.models;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.apache.isis.core.metamodel.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.webapp.context.IsisAppCommonContext;

import lombok.val;

public class EntityModel_hintsTest {

    @Rule public JUnitRuleMockery2 context = 
            JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Mock MarkupContainer mockParent;
    @Mock Component mockComponent1;
    @Mock Component mockComponent2;
    
    EntityModel target;
    MetaModelContext metaModelContext;

    @Before
    public void setUp() throws Exception {
        
        metaModelContext = MetaModelContext_forTesting.buildDefault();
        val commonContext = IsisAppCommonContext.of(metaModelContext);
        
        target = new EntityModel(commonContext, EntityModel.Mode.VIEW, EntityModel.RenderingHint.REGULAR);

//        mockParent = context.mock(MarkupContainer.class, "parent");
//        mockComponent1 = context.mock(Component.class, "component1");
//        mockComponent2 = context.mock(Component.class, "component2");

        context.checking(new Expectations() {{
            allowing(mockParent).getId();
            will(returnValue("parent"));

            allowing(mockComponent1).getId();
            will(returnValue("id1"));

            allowing(mockComponent2).getId();
            will(returnValue("id2"));

            ignoring(mockComponent1);
            ignoring(mockComponent2);

        }});

        mockComponent1.setParent(mockParent);
        mockComponent2.setParent(mockParent);
    }

    public static class Hints extends EntityModel_hintsTest {

        @Ignore
        @Test
        public void empty() throws Exception {
            assertThat(target.getHint(mockComponent1, "key1"), is(nullValue()));
        }

        @Ignore
        @Test
        public void single() throws Exception {
            target.setHint(mockComponent1, "key1", "value1");
            assertThat(target.getHint(mockComponent1, "key1"), is("value1"));
        }

        @Ignore
        @Test
        public void clear() throws Exception {
            target.setHint(mockComponent1, "key1", "value1");
            assertThat(target.getHint(mockComponent1, "key1"), is("value1"));
            target.clearHint(mockComponent1, "key1");
            assertThat(target.getHint(mockComponent1, "key1"), is(nullValue()));
        }

        @Ignore
        @Test
        public void setToNull() throws Exception {
            target.setHint(mockComponent1, "key1", "value1");
            assertThat(target.getHint(mockComponent1, "key1"), is("value1"));
            target.setHint(mockComponent1, "key1", null);
            assertThat(target.getHint(mockComponent1, "key1"), is(nullValue()));
        }

        @Ignore
        @Test
        public void multipleKeys() throws Exception {
            target.setHint(mockComponent1, "key1", "value1");
            target.setHint(mockComponent1, "key2", "value2");
            assertThat(target.getHint(mockComponent1, "key1"), is("value1"));
            assertThat(target.getHint(mockComponent1, "key2"), is("value2"));
        }

        @Ignore
        @Test
        public void multipleComponents() throws Exception {
            target.setHint(mockComponent1, "key", "valueA");
            target.setHint(mockComponent2, "key", "valueB");
            assertThat(target.getHint(mockComponent1, "key"), is("valueA"));
            assertThat(target.getHint(mockComponent2, "key"), is("valueB"));
        }

        @Ignore
        @Test
        public void smoke() throws Exception {
            target.setHint(mockComponent1, "X", "1.X");
            target.setHint(mockComponent1, "A", "1.A");
            target.setHint(mockComponent1, "B", "1.B");
            target.setHint(mockComponent1, "C", "1.C");
            target.setHint(mockComponent2, "X", "2.X");
            target.setHint(mockComponent2, "P", "2.P");
            target.setHint(mockComponent2, "Q", "2.Q");
            target.setHint(mockComponent2, "R", "2.R");

            assertThat(target.getHint(mockComponent1, "id-X"), is("1.X"));
            assertThat(target.getHint(mockComponent1, "id2-X"), is("2.X"));
            assertThat(target.getHint(mockComponent1, "id1-B"), is("1.B"));
            assertThat(target.getHint(mockComponent1, "id2-R"), is("2.R"));
        }
    }

}
