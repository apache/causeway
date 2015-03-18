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

package org.apache.isis.core.metamodel.facets.param.parameter.mandatory;

import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.mandatory.MandatoryFacetAbstract;

public abstract class MandatoryFacetForParameterAnnotation extends MandatoryFacetAbstract {

    public MandatoryFacetForParameterAnnotation(final FacetHolder holder, final Semantics semantics) {
        super(holder, semantics);
    }

    public static MandatoryFacet create(
            final Parameter parameter,
            final Class<?> parameterType,
            final FacetHolder holder) {

        if (parameter == null) {
            return null;
        }

        if (parameterType.isPrimitive()) {
            return new MandatoryFacetForParameterAnnotation.Primitive(holder);
        }

        final Optionality optionality = parameter.optionality();
        switch (optionality) {
            case DEFAULT:
                // do nothing here; instead will rely on MandatoryFromJdoColumnAnnotationFacetFactory to perform
                // the remaining processing
                return null;
            case MANDATORY:
                return new MandatoryFacetForParameterAnnotation.Required(holder);
            case OPTIONAL:
                return new MandatoryFacetForParameterAnnotation.Optional(holder);
        }
        return null;
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
