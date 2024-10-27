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
package org.apache.causeway.core.metamodel.facets.object.ident.cssclass;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Introspection;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;
import org.apache.causeway.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.causeway.core.metamodel.facets.object.cssclass.method.CssClassFacetViaCssClassMethod;
import org.apache.causeway.core.metamodel.facets.object.support.ObjectSupportFacetFactory;

class CssClassFacetMethodTest
extends FacetFactoryTestAbstract {

    static final String SOME_CSS_CLASS = "someCssClass";

    @DomainObject(introspection = Introspection.ENCAPSULATION_ENABLED)
    static class DomainObjectInCssClassMethod {
        @MemberSupport public String cssClass() {
            return SOME_CSS_CLASS;
        }
    }

    private ObjectSupportFacetFactory facetFactory;

    @BeforeEach
    void setup() {
        facetFactory = new ObjectSupportFacetFactory(getMetaModelContext());
    }

    @AfterEach
    protected void tearDown() throws Exception {
        facetFactory = null;
    }

    @Test
    void cssClassFacetViaCssClassMethod() {

        var domainObject = getObjectManager().adapt(new DomainObjectInCssClassMethod());

        objectScenario(DomainObjectInCssClassMethod.class, (processClassContext, facetHolder) -> {
            //when
            facetFactory.process(processClassContext);
            //then
            var cssClassFacet = facetHolder.getFacet(CssClassFacet.class);
            assertNotNull(cssClassFacet, ()->"CssClassFacet required");
            assertTrue(cssClassFacet instanceof CssClassFacetViaCssClassMethod);
            var imperativeCssClassFacet = (CssClassFacetViaCssClassMethod)cssClassFacet;
            assertEquals(SOME_CSS_CLASS,
                    imperativeCssClassFacet.cssClass(domainObject));
        });
    }

}
