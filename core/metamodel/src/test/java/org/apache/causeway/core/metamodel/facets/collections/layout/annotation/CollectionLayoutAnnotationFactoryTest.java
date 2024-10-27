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
package org.apache.causeway.core.metamodel.facets.collections.layout.annotation;

import java.util.SortedSet;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;
import org.apache.causeway.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.causeway.core.metamodel.facets.all.i8n.staatic.HasStaticText;
import org.apache.causeway.core.metamodel.facets.all.named.MemberNamedFacet;
import org.apache.causeway.core.metamodel.facets.collections.layout.CollectionLayoutFacetFactory;
import org.apache.causeway.core.metamodel.facets.collections.layout.HiddenFacetForCollectionLayoutAnnotation;
import org.apache.causeway.core.metamodel.facets.collections.layout.MemberNamedFacetForCollectionLayoutAnnotation;

class CollectionLayoutAnnotationFactoryTest
extends FacetFactoryTestAbstract {

    @Test
    void collectionLayoutAnnotation_named() {
        var facetFactory = new CollectionLayoutFacetFactory(getMetaModelContext());
        class Customer {
            @CollectionLayout(named = "1st names")
            public SortedSet<String> getFirstNames() { return _Sets.newTreeSet(); }
        }
        collectionScenario(Customer.class, "firstNames", (processMethodContext, facetHolder, facetedMethod)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            var facet = facetedMethod.getFacet(MemberNamedFacet.class);
            assertThat(facet, is(notNullValue()));
            assertThat(facet, is(instanceOf(MemberNamedFacetForCollectionLayoutAnnotation.class)));
            assertThat(((HasStaticText)facet).text(), is(equalTo("1st names")));
        });
    }

    @Test
    void collectionLayoutAnnotation_hidden() {
        var facetFactory = new CollectionLayoutFacetFactory(getMetaModelContext());
        class Customer {
            @CollectionLayout(hidden = Where.OBJECT_FORMS)
            public SortedSet<String> getFirstNames() { return _Sets.newTreeSet(); }
        }
        collectionScenario(Customer.class, "firstNames", (processMethodContext, facetHolder, facetedMethod)->{
            // when
            facetFactory.process(processMethodContext);
            // then
            final Facet facet = facetedMethod.getFacet(HiddenFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof HiddenFacetForCollectionLayoutAnnotation);
            final HiddenFacetForCollectionLayoutAnnotation collLayoutFacetAnnotation = (HiddenFacetForCollectionLayoutAnnotation) facet;
            assertEquals(Where.OBJECT_FORMS, collLayoutFacetAnnotation.where());
        });
    }

}
