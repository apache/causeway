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
package org.apache.isis.core.metamodel.feature;

import java.util.Arrays;
import java.util.Collection;

import com.google.common.collect.Lists;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.apache.isis.applib.annotation.When;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class ObjectAssociationFiltersTest_visibleWhere {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);
    
    @Mock
    private ObjectAssociation mockObjectAssociation;

    @Mock
    private HiddenFacet mockHiddenFacet;

    // given
    private When when;
    private Where where;

    // when
    private Where whereContext;

    // then
    private boolean expectedVisibility;

    
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {When.ALWAYS, Where.ANYWHERE, Where.ANYWHERE, false},
                {When.UNTIL_PERSISTED, Where.ANYWHERE, Where.ANYWHERE, true},
                {When.ONCE_PERSISTED, Where.ANYWHERE, Where.ANYWHERE, true},
                {When.NEVER, Where.ANYWHERE, Where.ANYWHERE, true},
                {When.ALWAYS, Where.OBJECT_FORMS, Where.OBJECT_FORMS, false},
                {When.ALWAYS, Where.OBJECT_FORMS, Where.ALL_TABLES, true},
                {When.ALWAYS, Where.OBJECT_FORMS, Where.PARENTED_TABLES, true},
                {When.ALWAYS, Where.OBJECT_FORMS, Where.REFERENCES_PARENT, true},
                {When.ALWAYS, Where.OBJECT_FORMS, Where.STANDALONE_TABLES, true},
                {When.ALWAYS, Where.STANDALONE_TABLES, Where.OBJECT_FORMS, true},
                {When.ALWAYS, Where.STANDALONE_TABLES, Where.PARENTED_TABLES, true},
                {When.ALWAYS, Where.STANDALONE_TABLES, Where.REFERENCES_PARENT, true},
                {When.ALWAYS, Where.STANDALONE_TABLES, Where.STANDALONE_TABLES, false},
                {When.ALWAYS, Where.PARENTED_TABLES, Where.OBJECT_FORMS, true},
                {When.ALWAYS, Where.PARENTED_TABLES, Where.PARENTED_TABLES, false},
                {When.ALWAYS, Where.PARENTED_TABLES, Where.REFERENCES_PARENT, true},
                {When.ALWAYS, Where.PARENTED_TABLES, Where.STANDALONE_TABLES, true},
                {When.ALWAYS, Where.ALL_TABLES, Where.OBJECT_FORMS, true},
                {When.ALWAYS, Where.ALL_TABLES, Where.PARENTED_TABLES, false},
                {When.ALWAYS, Where.ALL_TABLES, Where.STANDALONE_TABLES, false},
                {When.ALWAYS, Where.ALL_TABLES, Where.REFERENCES_PARENT, true},
                });
    }

    public ObjectAssociationFiltersTest_visibleWhere(
            final When when, final Where where, final Where context, final boolean visible) {
        this.when = when;
        this.where = where;
        this.whereContext = context;
        this.expectedVisibility = visible;
    }
    
    @Before
    public void setUp() throws Exception {
        context.checking(new Expectations(){{
            allowing(mockHiddenFacet).where();
            will(returnValue(where));

            allowing(mockHiddenFacet).when();
            will(returnValue(when));

            allowing(mockObjectAssociation).getFacets(with(any(Filter.class)));
            will(returnValue(Lists.newArrayList(mockHiddenFacet)));
        }});
    }

    private Matcher<Class<? extends Facet>> subclassOf(final Class<?> cls) {
        return new TypeSafeMatcher<Class<? extends Facet>>() {
            @Override
            protected boolean matchesSafely(final Class<? extends Facet> item) {
                return cls.isAssignableFrom(cls);
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("subclass of " + cls.getName());
            }
        };
    }

    @Test
    public void test() {
        final Filter<ObjectAssociation> filter = ObjectAssociation.Filters.staticallyVisible(whereContext);
        assertThat(filter.accept(mockObjectAssociation), is(expectedVisibility));
    }

}
