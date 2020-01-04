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

package org.apache.isis.metamodel.facets.collections.layout.annotation;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.SortedSet;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.metamodel.facets.collections.layout.CollectionLayoutFacetFactory;
import org.apache.isis.metamodel.facets.collections.layout.NamedFacetForCollectionLayoutAnnotation;

import lombok.val;

public class NamedFacetForCollectionLayoutAnnotationFactoryTest extends AbstractFacetFactoryTest {

    public void testCollectionLayoutAnnotationNamed() {
        val facetFactory = new CollectionLayoutFacetFactory();
        facetFactory.setMetaModelContext(super.metaModelContext);

        class Customer {
            @CollectionLayout(named = "1st names")
            public SortedSet<String> getFirstNames() {
                return _Sets.newTreeSet();
            }
        }
        final Method method = findMethod(Customer.class, "getFirstNames");

        facetFactory.process(new ProcessMethodContext(Customer.class, null, method, methodRemover, facetedMethod));

        final NamedFacet facet = facetedMethod.getFacet(NamedFacet.class);
        assertThat(facet, is(notNullValue()));
        assertThat(facet, is(instanceOf(NamedFacetForCollectionLayoutAnnotation.class)));
        assertThat(facet.value(), is(equalTo("1st names")));
        assertThat(facet.escaped(), is(true));
    }

    public void testCollectionLayoutAnnotationNamedEscapedFalse() {
        val facetFactory = new CollectionLayoutFacetFactory();
        facetFactory.setMetaModelContext(super.metaModelContext);

        class Customer {
            @CollectionLayout(named = "1st names", namedEscaped = false)
            public Set<String> getFirstNames() {
                return _Sets.newTreeSet();
            }
        }
        final Method method = findMethod(Customer.class, "getFirstNames");

        facetFactory.process(new ProcessMethodContext(Customer.class, null, method, methodRemover, facetedMethod));

        final NamedFacet facet = facetedMethod.getFacet(NamedFacet.class);
        assertThat(facet, is(notNullValue()));
        assertThat(facet, is(instanceOf(NamedFacetForCollectionLayoutAnnotation.class)));
        assertThat(facet.value(), is(equalTo("1st names")));
        assertThat(facet.escaped(), is(false));
    }

}
