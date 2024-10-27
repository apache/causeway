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
package org.apache.causeway.core.metamodel.facets.ordering.memberorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.applib.services.i18n.TranslationService;
import org.apache.causeway.commons.internal.context._Context;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.members.layout.group.GroupIdAndName;
import org.apache.causeway.core.metamodel.facets.members.layout.group.LayoutGroupFacetAbstract;
import org.apache.causeway.core.metamodel.facets.members.layout.order.LayoutOrderFacetAbstract;
import org.apache.causeway.core.metamodel.layout.memberorderfacet.MemberOrderComparator;

class DeweyOrderComparatorTest  {

    private MemberOrderComparator comparator, laxComparator;

    static class Customer {
        private String abc;

        public String getAbc() {
            return abc;
        }
    }

    private final MetaModelContext mmc = MetaModelContext_forTesting.buildDefault();
    private final FacetedMethod m1 = FacetedMethod.testing.createGetterForProperty(mmc, Customer.class, "abc");
    private final FacetedMethod m2 = FacetedMethod.testing.createGetterForProperty(mmc, Customer.class, "abc");

    @Mock TranslationService mockTranslationService;

	static TranslationContext ctx = TranslationContext.named("test");

	@BeforeEach
    protected void setUp() {
        _Context.clear();
        comparator = new MemberOrderComparator(true);
        laxComparator = new MemberOrderComparator(false);
    }

	@Test
    void defaultGroupOneComponent() {
        setupLayoutFacets("", "1", m1);
        setupLayoutFacets("", "2", m2);
        assertEquals(-1, comparator.compare(m1, m2));
    }

	@Test
    void defaultGroupOneComponentOtherWay() {
        setupLayoutFacets("", "2", m1);
        setupLayoutFacets("", "1", m2);
        assertEquals(+1, comparator.compare(m1, m2));
    }

	@Test
    void defaultGroupOneComponentSame() {
        setupLayoutFacets("", "1", m1);
        setupLayoutFacets("", "1", m2);
        assertEquals(0, comparator.compare(m1, m2));
    }

	@Test
    void defaultGroupOneSideRunsOutOfComponentsFirst() {
        setupLayoutFacets("", "1", m1);
        setupLayoutFacets("", "1.1", m2);
        assertEquals(-1, comparator.compare(m1, m2));
    }

	@Test
    void defaultGroupOneSideRunsOutOfComponentsFirstOtherWay() {
        setupLayoutFacets("", "1.1", m1);
        setupLayoutFacets("", "1", m2);
        assertEquals(+1, comparator.compare(m1, m2));
    }

	@Test
    void defaultGroupOneSideRunsTwoComponents() {
        setupLayoutFacets("", "1.1", m1);
        setupLayoutFacets("", "1.2", m2);
        assertEquals(-1, comparator.compare(m1, m2));
    }

	@Test
    void defaultGroupOneSideRunsTwoComponentsOtherWay() {
        setupLayoutFacets("", "1.2", m1);
        setupLayoutFacets("", "1.1", m2);
        assertEquals(+1, comparator.compare(m1, m2));
    }

	@Test
    void defaultGroupOneSideRunsLotsOfComponents() {
        setupLayoutFacets("", "1.2.5.8.3.3", m1);
        setupLayoutFacets("", "1.2.5.8.3.4", m2);
        assertEquals(-1, comparator.compare(m1, m2));
    }

	@Test
    void defaultGroupOneSideRunsLotsOfComponentsOtherWay() {
        setupLayoutFacets("", "1.2.5.8.3.4", m1);
        setupLayoutFacets("", "1.2.5.8.3.3", m2);
        assertEquals(+1, comparator.compare(m1, m2));
    }

	@Test
    void defaultGroupOneSideRunsLotsOfComponentsSame() {
        setupLayoutFacets("", "1.2.5.8.3.3", m1);
        setupLayoutFacets("", "1.2.5.8.3.3", m2);
        assertEquals(0, comparator.compare(m1, m2));
    }

	@Test
    void namedGroupOneSideRunsLotsOfComponents() {
        setupLayoutFacets("abc", "1.2.5.8.3.3", m1);
        setupLayoutFacets("abc", "1.2.5.8.3.4", m2);
        assertEquals(-1, comparator.compare(m1, m2));
    }

	@Test
    void ensuresInSameGroup() {
        setupLayoutFacets("abc", "1", m1);
        setupLayoutFacets("def", "2", m2);
        try {
            assertEquals(-1, comparator.compare(m1, m2));
            fail("Exception should have been thrown");
        } catch (final IllegalArgumentException ex) {
            // expected
        }
    }

	@Test
    void ensuresInSameGroupCanBeDisabled() {
        setupLayoutFacets("abc", "1", m1);
        setupLayoutFacets("def", "2", m2);
        assertEquals(-1, laxComparator.compare(m1, m2));
    }

	@Test
    void nonAnnotatedAfterAnnotated() {
        // don't annotate m1
        setupLayoutFacets("def", "2", m2);
        assertEquals(+1, comparator.compare(m1, m2));
    }

    // -- HELPER

    void setupLayoutFacets(final String groupId, final String sequence, final FacetedMethod facetedMethod) {
        facetedMethod.addFacet(new LayoutGroupFacetAbstract(GroupIdAndName.of(groupId, ""), facetedMethod) {});
        facetedMethod.addFacet(new LayoutOrderFacetAbstract(sequence, facetedMethod) {});
    }

}
