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

package org.apache.isis.core.metamodel.facets.properties.property.disabled;

import java.util.Optional;

import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacetAbstract;

public class DisabledFacetForPropertyAnnotation
extends DisabledFacetAbstract {

    public static DisabledFacet create(
            final Optional<Property> propertyIfAny,
            final FacetHolder holder) {

        return propertyIfAny
                .filter(property -> property.editing() != Editing.NOT_SPECIFIED)
                .map(property -> {
                    switch (property.editing()) {
                    case AS_CONFIGURED:

                        // nothing needs to be done here; the DomainObjectFactory (processing @DomainObject annotation)
                        // will install an ImmutableFacetForDomainObjectAnnotation on the domain object and then a
                        // DisabledFacetOnPropertyDerivedFromImmutable facet will be installed.

                        return null;

                    case DISABLED:
                        final String disabledReason = property.editingDisabledReason();
                        return new DisabledFacetForPropertyAnnotation(disabledReason, holder);
                    case ENABLED:
                        return new DisabledFacetForPropertyAnnotationInvertedSemantics(holder);
                    default:
                    }
                    return null;
                })
                .orElse(null);
    }

    private DisabledFacetForPropertyAnnotation(final String reason, final FacetHolder holder) {
        super(Where.EVERYWHERE, reason, holder);
    }

}
