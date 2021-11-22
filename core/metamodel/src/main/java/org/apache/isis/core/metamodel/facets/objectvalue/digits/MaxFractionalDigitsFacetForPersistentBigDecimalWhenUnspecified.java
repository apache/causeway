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
package org.apache.isis.core.metamodel.facets.objectvalue.digits;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.OptionalInt;

import org.apache.isis.core.config.beans.IsisBeanTypeRegistry;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;

/**
 * With {@link BigDecimal}, both JDO and JPA, if left unspecified,
 * default their max-fractional digits to 0.
 * (However, I could not find specific documents to support this claim.)
 *
 * @apiNote This facet should be applied in the absence of a corresponding {@code @Column} annotation,
 * but only for properties of type {@link BigDecimal} that appear within a (persistable) entity
 * and are also persistable.
 * Entities might extend abstract classes, where the framework - on type introspection -
 * cannot distinguish entity from non-entity type.
 * It is safe to assume that mixed-in properties are not to consider here.
 * @deprecated remove (we don't know how to implement)
 */
@Deprecated
public class MaxFractionalDigitsFacetForPersistentBigDecimalWhenUnspecified
extends MaxFractionalDigitsFacetAbstract {

    public static Optional<MaxFractionalDigitsFacet> create(
            final OptionalInt scaleIfAny,
            final ProcessMethodContext processMethodContext,
            final IsisBeanTypeRegistry beanTypeRegistry) {

        // not properly implemented yet
        return Optional.empty();

//        val cls = processMethodContext.getCls();
//        val facetHolder = processMethodContext.getFacetHolder();
//
//        // only applies in a very specific context, see class java-doc
//        val isApplicable = scaleIfAny.orElse(-1)<0
//                && facetHolder.getFeatureType().isProperty()
//                && processMethodContext.getMethod().getReturnType().equals(BigDecimal.class)
//                && beanTypeRegistry.getEntityTypes().contains(cls)
//                && isPersistable(processMethodContext.getMethod());
//
//        return isApplicable
//                ? Optional.of(new MaxFractionalDigitsFacetForPersistentBigDecimalWhenUnspecified(
//                        facetHolder))
//                : Optional.empty();
    }

    private MaxFractionalDigitsFacetForPersistentBigDecimalWhenUnspecified(
            final FacetHolder holder) {
        super(0, holder);
    }

    // -- HELPER

    private static boolean isPersistable(final Method method) {
        // TODO don't know how to do that
        return false;
    }

}
