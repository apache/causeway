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

package org.apache.isis.core.metamodel.facets.properties.property.mandatory;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.mandatory.MandatoryFacetAbstract;

public abstract class MandatoryFacetForPropertyAnnotation extends MandatoryFacetAbstract {

    public MandatoryFacetForPropertyAnnotation(final FacetHolder holder, final Semantics semantics) {
        super(holder, semantics);
    }

    public static MandatoryFacet create(
            final List<Property> properties,
            final Method method,
            final FacetHolder holder) {

        if(properties.isEmpty()) {
            return null;
        }

        final Class<?> returnType = method.getReturnType();
        if (returnType.isPrimitive()) {
            return new MandatoryFacetForPropertyAnnotation.Primitive(holder);
        }

        return properties.stream()
                .map(Property::optionality)
                .filter(optionality -> optionality != Optionality.NOT_SPECIFIED)
                .findFirst()
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
                })
                .orElse(null);
    }

    public static class Primitive extends MandatoryFacetForPropertyAnnotation {
        public Primitive(final FacetHolder holder) {
            super(holder, Semantics.REQUIRED);
        }
    }

    public static class Required extends MandatoryFacetForPropertyAnnotation {
        public Required(final FacetHolder holder) {
            super(holder, Semantics.REQUIRED);
        }
    }

    public static class Optional extends MandatoryFacetForPropertyAnnotation {
        public Optional(final FacetHolder holder) {
            super(holder, Semantics.OPTIONAL);
        }
    }

}
