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
package org.apache.causeway.core.metamodel.facets.object.ident.icon;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Introspection;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.ObjectSupport.IconWhere;
import org.apache.causeway.applib.fa.FontAwesomeLayers;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;
import org.apache.causeway.core.metamodel.facets.object.icon.IconFacet;
import org.apache.causeway.core.metamodel.facets.object.icon.method.IconFacetViaIconMethod;
import org.apache.causeway.core.metamodel.facets.object.support.ObjectSupportFacetFactory;

class IconFacetMethodFaTest
extends FacetFactoryTestAbstract {

    static final FontAwesomeLayers FONTAWESOME_LAYERS_SAMPLE = FontAwesomeLayers.singleIcon("fa-solid fa-bookmark");

    @DomainObject(introspection = Introspection.ENCAPSULATION_ENABLED)
    static class DomainObjectWithFontAwesomeIconViaMethod {
        @ObjectSupport public ObjectSupport.IconResource icon(final ObjectSupport.IconWhere iconWhere) {
            return new ObjectSupport.FontAwesomeIconResource(FONTAWESOME_LAYERS_SAMPLE);
        }
    }

    private ObjectSupportFacetFactory facetFactory;

    @BeforeEach
    final void beforeEach() {
        facetFactory = new ObjectSupportFacetFactory(getMetaModelContext());
        super.setup();
    }

    @AfterEach
    protected void tearDown() throws Exception {
        facetFactory = null;
    }

    @Test
    void fontAwesomeLayersViaIconMethod() {
        var domainObject = getObjectManager().adapt(new DomainObjectWithFontAwesomeIconViaMethod());

        objectScenario(DomainObjectWithFontAwesomeIconViaMethod.class, (processClassContext, facetHolder) -> {
            //when
            facetFactory.process(processClassContext);
            //then
            var iconFacet = facetHolder.getFacet(IconFacet.class);
            assertNotNull(iconFacet, ()->"IconFacet required");
            assertTrue(iconFacet instanceof IconFacetViaIconMethod);
            var imperativeFacet = (IconFacetViaIconMethod)iconFacet;

            var actual = imperativeFacet.icon(domainObject, IconWhere.OBJECT_HEADER)
                .filter(ObjectSupport.FontAwesomeIconResource.class::isInstance)
                .map(ObjectSupport.FontAwesomeIconResource.class::cast)
                .map(ObjectSupport.FontAwesomeIconResource::faLayers)
                .orElse(null);
            assertEquals(FONTAWESOME_LAYERS_SAMPLE, actual);
            assertEquals(
                    FontAwesomeLayers.normalizeCssClasses("fa-solid fa-bookmark", "fa"),
                    actual.iconEntries().get(0).cssClasses());
        });
    }

}
