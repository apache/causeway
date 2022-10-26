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
package org.apache.causeway.core.metamodel.feature;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.WhereValueFacet;
import org.apache.causeway.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;

class ObjectAssociationPredicatesTest_visibleWhere {

    private ObjectAssociation mockObjectAssociation;
    private HiddenFacet mockHiddenFacet;

    @BeforeEach
    public void setUp() throws Exception {
        mockHiddenFacet = Mockito.mock(HiddenFacet.class);
        mockObjectAssociation = Mockito.mock(ObjectAssociation.class);
    }

    private static Stream<Arguments> data() {
        return Stream.of(
          Arguments.of(Where.ANYWHERE, Where.ANYWHERE, false),
          Arguments.of(Where.OBJECT_FORMS, Where.OBJECT_FORMS, false),
          Arguments.of(Where.OBJECT_FORMS, Where.ALL_TABLES, true),
          Arguments.of(Where.OBJECT_FORMS, Where.PARENTED_TABLES, true),
          Arguments.of(Where.OBJECT_FORMS, Where.REFERENCES_PARENT, true),
          Arguments.of(Where.OBJECT_FORMS, Where.STANDALONE_TABLES, true),
          Arguments.of(Where.STANDALONE_TABLES, Where.OBJECT_FORMS, true),
          Arguments.of(Where.STANDALONE_TABLES, Where.PARENTED_TABLES, true),
          Arguments.of(Where.STANDALONE_TABLES, Where.REFERENCES_PARENT, true),
          Arguments.of(Where.STANDALONE_TABLES, Where.STANDALONE_TABLES, false),
          Arguments.of(Where.PARENTED_TABLES, Where.OBJECT_FORMS, true),
          Arguments.of(Where.PARENTED_TABLES, Where.PARENTED_TABLES, false),
          Arguments.of(Where.PARENTED_TABLES, Where.REFERENCES_PARENT, true),
          Arguments.of(Where.PARENTED_TABLES, Where.STANDALONE_TABLES, true),
          Arguments.of(Where.ALL_TABLES, Where.OBJECT_FORMS, true),
          Arguments.of(Where.ALL_TABLES, Where.PARENTED_TABLES, false),
          Arguments.of(Where.ALL_TABLES, Where.STANDALONE_TABLES, false),
          Arguments.of(Where.ALL_TABLES, Where.REFERENCES_PARENT, true)
        );
    }

    @ParameterizedTest
    @MethodSource("data")
    public void visibleWhere(
            final Where where, final Where whereContext, final boolean expectedVisibility) {
        Mockito.when(mockHiddenFacet.where()).thenReturn(where);
        Mockito.when(mockObjectAssociation.streamFacets())
            .thenReturn(_Lists.<Facet>of(mockHiddenFacet).stream());

        final Predicate<ObjectAssociation> predicate = association -> {
            final List<Facet> facets = association.streamFacets()
                    .filter(facet -> facet instanceof WhereValueFacet
                            && facet instanceof HiddenFacet)
                    .collect(Collectors.toList());
            for (Facet facet : facets) {
                final WhereValueFacet wawF = (WhereValueFacet) facet;
                if (wawF.where().includes(whereContext)) {
                    return false;
                }
            }
            return true;
        };
        assertThat(predicate.test(mockObjectAssociation), is(expectedVisibility));
    }

}
