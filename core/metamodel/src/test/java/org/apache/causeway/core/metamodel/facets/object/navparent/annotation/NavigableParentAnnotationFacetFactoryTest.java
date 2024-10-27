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
package org.apache.causeway.core.metamodel.facets.object.navparent.annotation;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal._Constants;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;
import org.apache.causeway.core.metamodel.facets.object.navparent.NavigableParentFacet;
import org.apache.causeway.core.metamodel.facets.object.navparent.annotation.NavigableParentTestSamples.DomainObjectInvalidParentAnnot;
import org.apache.causeway.core.metamodel.facets.object.navparent.annotation.NavigableParentTestSamples.DomainObjectProperAnnot;
import org.apache.causeway.core.metamodel.facets.object.navparent.method.NavigableParentFacetViaMethod;

class NavigableParentAnnotationFacetFactoryTest
extends FacetFactoryTestAbstract {

    static Stream<Arguments> navigableTypeArgs() {
        return Stream.of(
                Arguments.of(new DomainObjectProperAnnot(), "root", null),
                Arguments.of(new DomainObjectInvalidParentAnnot(), "root",
                        "the object's navigable parent must no be void, plural, vetoed or a value-type; "
                        + "yet the parent type 'java.math.BigInteger' as discovered was value-type")); }

    @ParameterizedTest
    @MethodSource("navigableTypeArgs")
    protected void navigableType(
            final Object domainObject,
            final String parentMethodName,
            final @Nullable String expectedValidationMessage) throws Exception {

        var facetFactory = new NavigableParentAnnotationFacetFactory(getMetaModelContext());
        var parentMethod = findMethodExactOrFail(domainObject.getClass(), parentMethodName);

        objectScenario(domainObject.getClass(), (processClassContext, facetHolder) -> {
            //when
            facetFactory.process(processClassContext);
            //then
            if(expectedValidationMessage==null) {
                var navigableParentFacet = facetHolder.getFacet(NavigableParentFacet.class);
                assertNotNull(navigableParentFacet, ()->"NavigableParentFacet required");
                assertTrue(navigableParentFacet instanceof NavigableParentFacetViaMethod);
                assertEquals(
                        navigableParentFacet.navigableParent(domainObject),
                        Try.call(()->parentMethod.method().invoke(domainObject, _Constants.emptyObjects))
                            .valueAsNullableElseFail());
            } else {
                assertNull(facetHolder.getFacet(NavigableParentFacet.class));

                var validation = getSpecificationLoader().getOrAssessValidationResult();
                assertTrue(validation.getMessages().stream()
                        .anyMatch(msg->msg.contains(expectedValidationMessage)));
            }
        });
    }

}
