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
package org.apache.causeway.core.metamodel.facets.object.ident.fa;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Introspection;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.fa.FontAwesomeLayers;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;
import org.apache.causeway.core.metamodel.facets.object.iconfa.IconFaLayersFacet;
import org.apache.causeway.core.metamodel.facets.object.iconfa.method.IconFaLayersFacetViaIconFaLayersMethod;
import org.apache.causeway.core.metamodel.facets.object.support.ObjectSupportFacetFactory;

import lombok.val;

class IconFaLayersFacetMethodTest
extends FacetFactoryTestAbstract {

    static final FontAwesomeLayers FONTAWESOME_LAYERS_SAMPLE = new FontAwesomeLayers(null, null, null, null);

    @DomainObject(introspection = Introspection.ENCAPSULATION_ENABLED)
    static class DomainObjectWithFontAwesomeLayersMethod {
        @ObjectSupport public FontAwesomeLayers iconFaLayers() {
            return FONTAWESOME_LAYERS_SAMPLE;
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

        val domainObject = getObjectManager().adapt(new DomainObjectWithFontAwesomeLayersMethod());

        objectScenario(DomainObjectWithFontAwesomeLayersMethod.class, (processClassContext, facetHolder) -> {
            //when
            facetFactory.process(processClassContext);
            //then
            val iconFaLayersFacet = facetHolder.getFacet(IconFaLayersFacet.class);
            assertNotNull(iconFaLayersFacet, ()->"IconFaLayersFacet required");
            assertTrue(iconFaLayersFacet instanceof IconFaLayersFacetViaIconFaLayersMethod);
            val imperativeCssClassFacet = (IconFaLayersFacetViaIconFaLayersMethod)iconFaLayersFacet;
            assertEquals(FONTAWESOME_LAYERS_SAMPLE,
                    imperativeCssClassFacet.layers(domainObject));
        });
    }

}
