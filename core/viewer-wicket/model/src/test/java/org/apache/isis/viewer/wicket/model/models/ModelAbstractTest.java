/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.wicket.model.models;

import java.util.Map;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.model.IModel;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.viewer.wicket.model.hints.UiHintPathSignificant;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class ModelAbstractTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    ModelAbstract<String> target;

    static class UiHintPathSignificantComponent extends Component implements UiHintPathSignificant {
        public UiHintPathSignificantComponent(String id) {
            super(id);
        }

        public UiHintPathSignificantComponent(String id, IModel<?> model) {
            super(id, model);
        }

        @Override
        protected void onRender() {
        }
    }

    static class UiHintPathSignificantMarkupContainer extends MarkupContainer implements UiHintPathSignificant {
        public UiHintPathSignificantMarkupContainer(String id) {
            super(id);
        }

        public UiHintPathSignificantMarkupContainer(String id, IModel<?> model) {
            super(id, model);
        }

        @Override
        protected void onRender() {
        }
    }

    MarkupContainer mockParent;
    Component mockComponent1;
    Component mockComponent2;

    @Before
    public void setUp() throws Exception {
        target = new ModelAbstract<String>("foo"){
            @Override
            protected String load() {
                return null;
            }
        };

        mockParent = context.mock(UiHintPathSignificantMarkupContainer.class, "parent");
        mockComponent1 = context.mock(UiHintPathSignificantComponent.class, "component1");
        mockComponent2 = context.mock(UiHintPathSignificantComponent.class, "component2");

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

    public static class Hints extends ModelAbstractTest {

        @Test
        public void empty() throws Exception {
            assertThat(target.getHint(mockComponent1, "key1"), is(nullValue()));
        }

        @Test
        public void single() throws Exception {
            target.setHint(mockComponent1, "key1", "value1");
            assertThat(target.getHint(mockComponent1, "key1"), is("value1"));
        }

        @Test
        public void clear() throws Exception {
            target.setHint(mockComponent1, "key1", "value1");
            assertThat(target.getHint(mockComponent1, "key1"), is("value1"));
            target.clearHint(mockComponent1, "key1");
            assertThat(target.getHint(mockComponent1, "key1"), is(nullValue()));
        }

        @Test
        public void setToNull() throws Exception {
            target.setHint(mockComponent1, "key1", "value1");
            assertThat(target.getHint(mockComponent1, "key1"), is("value1"));
            target.setHint(mockComponent1, "key1", null);
            assertThat(target.getHint(mockComponent1, "key1"), is(nullValue()));
        }

        @Test
        public void multipleKeys() throws Exception {
            target.setHint(mockComponent1, "key1", "value1");
            target.setHint(mockComponent1, "key2", "value2");
            assertThat(target.getHint(mockComponent1, "key1"), is("value1"));
            assertThat(target.getHint(mockComponent1, "key2"), is("value2"));
        }

        @Test
        public void multipleComponents() throws Exception {
            target.setHint(mockComponent1, "key", "valueA");
            target.setHint(mockComponent2, "key", "valueB");
            assertThat(target.getHint(mockComponent1, "key"), is("valueA"));
            assertThat(target.getHint(mockComponent2, "key"), is("valueB"));
        }

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

            final Map<String, String> hints = target.getHints();
            assertThat(hints.size(), is(8));
            assertThat(hints.get("id1-X"), is("1.X"));
            assertThat(hints.get("id2-X"), is("2.X"));
            assertThat(hints.get("id1-B"), is("1.B"));
            assertThat(hints.get("id2-R"), is("2.R"));
        }
    }

}