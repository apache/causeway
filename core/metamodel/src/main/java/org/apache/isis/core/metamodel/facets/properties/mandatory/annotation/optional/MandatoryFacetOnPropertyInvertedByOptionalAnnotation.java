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

package org.apache.isis.core.metamodel.facets.properties.mandatory.annotation.optional;

import java.lang.reflect.Method;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.propparam.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.facets.propparam.mandatory.MandatoryFacetAbstract;

/**
 * Derived by presence of an <tt>@Optional</tt> annotation.
 * 
 * <p>
 * This implementation indicates that the {@link FacetHolder} is <i>not</i>
 * mandatory, as per {@link #isInvertedSemantics()}.
 */
public class MandatoryFacetOnPropertyInvertedByOptionalAnnotation extends MandatoryFacetAbstract {

    public MandatoryFacetOnPropertyInvertedByOptionalAnnotation(final FacetHolder holder) {
        super(holder, Semantics.OPTIONAL);
    }

    static MandatoryFacet create(final Optional annotation, Method method, final FacetHolder holder) {
        final Class<?> returnType = method.getReturnType();
        if (returnType.isPrimitive()) {
            return null;
        }
        return new MandatoryFacetOnPropertyInvertedByOptionalAnnotation(holder);
    }
}
