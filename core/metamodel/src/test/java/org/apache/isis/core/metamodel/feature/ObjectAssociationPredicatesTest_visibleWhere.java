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
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.WhereValueFacet;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;

//FIXME[ISIS-3207]
@DisabledIfSystemProperty(named = "isRunningWithSurefire", matches = "true")
public class ObjectAssociationPredicatesTest_visibleWhere {

    private ObjectAssociation mockObjectAssociation;
    private HiddenFacet mockHiddenFacet;

    // given
    private Where where;

    // when
    private Where whereContext;

    // then
    private boolean expectedVisibility;


    //@Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {Where.ANYWHERE, Where.ANYWHERE, false},
            {Where.OBJECT_FORMS, Where.OBJECT_FORMS, false},
            {Where.OBJECT_FORMS, Where.ALL_TABLES, true},
            {Where.OBJECT_FORMS, Where.PARENTED_TABLES, true},
            {Where.OBJECT_FORMS, Where.REFERENCES_PARENT, true},
            {Where.OBJECT_FORMS, Where.STANDALONE_TABLES, true},
            {Where.STANDALONE_TABLES, Where.OBJECT_FORMS, true},
            {Where.STANDALONE_TABLES, Where.PARENTED_TABLES, true},
            {Where.STANDALONE_TABLES, Where.REFERENCES_PARENT, true},
            {Where.STANDALONE_TABLES, Where.STANDALONE_TABLES, false},
            {Where.PARENTED_TABLES, Where.OBJECT_FORMS, true},
            {Where.PARENTED_TABLES, Where.PARENTED_TABLES, false},
            {Where.PARENTED_TABLES, Where.REFERENCES_PARENT, true},
            {Where.PARENTED_TABLES, Where.STANDALONE_TABLES, true},
            {Where.ALL_TABLES, Where.OBJECT_FORMS, true},
            {Where.ALL_TABLES, Where.PARENTED_TABLES, false},
            {Where.ALL_TABLES, Where.STANDALONE_TABLES, false},
            {Where.ALL_TABLES, Where.REFERENCES_PARENT, true},
        });
    }

    public ObjectAssociationPredicatesTest_visibleWhere(
            final Where where, final Where context, final boolean visible) {
        this.where = where;
        this.whereContext = context;
        this.expectedVisibility = visible;
    }

    @BeforeEach
    public void setUp() throws Exception {
        mockHiddenFacet = Mockito.mock(HiddenFacet.class);
        mockObjectAssociation = Mockito.mock(ObjectAssociation.class);
        Mockito.when(mockHiddenFacet.where()).thenReturn(where);
        Mockito.when(mockObjectAssociation.streamFacets()).thenReturn(_Lists.<Facet>of(mockHiddenFacet).stream());
    }

    @Test
    public void test() {
        final Predicate<ObjectAssociation> predicate = new Predicate<ObjectAssociation>() {
            @Override
            public boolean test(final ObjectAssociation association) {
                final List<Facet> facets = association.streamFacets()
                        .filter(new Predicate<Facet>() {
                            @Override public boolean test(final Facet facet) {
                                return facet instanceof WhereValueFacet && facet instanceof HiddenFacet;
                            }
                        })
                        .collect(Collectors.toList());
                for (Facet facet : facets) {
                    final WhereValueFacet wawF = (WhereValueFacet) facet;
                    if (wawF.where().includes(whereContext)) {
                        return false;
                    }
                }
                return true;
            }
        };
        assertThat(predicate.test(mockObjectAssociation), is(expectedVisibility));
    }

}
