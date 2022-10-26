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
package org.apache.causeway.core.metamodel.facets.param.parameter.mandatory;

import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.mandatory.MandatoryFacetAbstract;

public abstract class MandatoryFacetForParameterAnnotation extends MandatoryFacetAbstract {

    public MandatoryFacetForParameterAnnotation(final FacetHolder holder, final Semantics semantics) {
        super(holder, semantics);
    }

    public static java.util.Optional<MandatoryFacet> create(
            final java.util.Optional<Parameter> parameterIfAny,
            final Class<?> parameterType,
            final FacetHolder holder) {

        if (parameterType.isPrimitive()) {
            return java.util.Optional.of(new MandatoryFacetForParameterAnnotation.Primitive(holder));
        }

        return parameterIfAny
        .map(Parameter::optionality)
        .filter(optionality -> optionality != Optionality.NOT_SPECIFIED)
        .map(optionality -> {
            switch (optionality) {
            case DEFAULT:
                // do nothing here
                return null;
            case MANDATORY:
                return new MandatoryFacetForParameterAnnotation.Required(holder);
            case OPTIONAL:
                return new MandatoryFacetForParameterAnnotation.Optional(holder);
            default:
            }
            throw new IllegalStateException("optionality '" + optionality + "' not recognised");
        });
    }

    public static class Primitive extends MandatoryFacetForParameterAnnotation {
        public Primitive(final FacetHolder holder) {
            super(holder, Semantics.REQUIRED);
        }
    }

    public static class Required extends MandatoryFacetForParameterAnnotation {
        public Required(final FacetHolder holder) {
            super(holder, Semantics.REQUIRED);
        }
    }

    public static class Optional extends MandatoryFacetForParameterAnnotation {
        public Optional(final FacetHolder holder) {
            super(holder, Semantics.OPTIONAL);
        }
    }

}
