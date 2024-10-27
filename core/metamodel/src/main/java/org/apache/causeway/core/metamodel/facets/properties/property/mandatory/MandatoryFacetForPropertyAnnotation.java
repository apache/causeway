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
package org.apache.causeway.core.metamodel.facets.properties.property.mandatory;

import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.mandatory.MandatoryFacetAbstract;

public abstract class MandatoryFacetForPropertyAnnotation
extends MandatoryFacetAbstract {

    public static java.util.Optional<MandatoryFacet> create(
            final java.util.Optional<Property> propertyIfAny,
            final MethodFacade method,
            final FacetHolder holder) {

        if(!propertyIfAny.isPresent()) {
            return null;
        }

        var returnType = method.getReturnType();
        if (returnType.isPrimitive()) {
            return java.util.Optional.of(new MandatoryFacetForPropertyAnnotation.Primitive(holder));
        }

        return propertyIfAny
                .map(Property::optionality)
                .filter(optionality -> optionality != Optionality.NOT_SPECIFIED)
                .map(optionality -> {
                    switch (optionality) {
                    case DEFAULT:
                        // do nothing here; instead will rely on MandatoryFromJdoColumnAnnotationFacetFactory to perform
                        // the remaining processing
                        return null;
                    case MANDATORY:
                        return new MandatoryFacetForPropertyAnnotation.Required(holder);
                    case OPTIONAL:
                        return new MandatoryFacetForPropertyAnnotation.Optional(holder);
                    default:
                    }
                    throw new IllegalStateException("optionality '" + optionality + "' not recognised");
                });
    }

    public MandatoryFacetForPropertyAnnotation(final Semantics semantics, final FacetHolder holder) {
        super(semantics, holder);
    }

    protected MandatoryFacetForPropertyAnnotation(
            final Semantics semantics, final FacetHolder holder, final Facet.Precedence precedence) {
        super(semantics, holder, precedence);
    }

    @Override
    public final String summarize() {
        return MandatoryFacetForPropertyAnnotation.class.getSimpleName() + "." + super.summarize();
    }

    // -- IMPLEMENTATIONS

    public static class Primitive extends MandatoryFacetForPropertyAnnotation {
        public Primitive(final FacetHolder holder) {
            super(Semantics.REQUIRED, holder);
        }
    }

    public static class Required extends MandatoryFacetForPropertyAnnotation {
        public Required(final FacetHolder holder) {
            super(Semantics.REQUIRED, holder, Precedence.HIGH); // allow UI to be more strict than data/store
        }
    }

    public static class Optional extends MandatoryFacetForPropertyAnnotation {
        public Optional(final FacetHolder holder) {
            super(Semantics.OPTIONAL, holder);
        }
    }

}
