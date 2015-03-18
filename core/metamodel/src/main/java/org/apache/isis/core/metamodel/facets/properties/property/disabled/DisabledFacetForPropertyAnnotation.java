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

import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.When;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacetAbstractImpl;

public class DisabledFacetForPropertyAnnotation extends DisabledFacetAbstractImpl {

    public static DisabledFacet create(final Property property, final FacetHolder holder) {

        if (property == null) {
            return null;
        }

        final Editing editing = property.editing();
        final String disabledReason = property.editingDisabledReason();

        switch (editing) {
            case AS_CONFIGURED:

                // nothing needs to be done here; the DomainObjectFactory (processing @DomainObject annotation)
                // will install an ImmutableFacetForDomainObjectAnnotation on the domain object and then a
                // DisabledFacetOnPropertyDerivedFromImmutable facet will be installed.

                return null;

            case DISABLED:
                return new DisabledFacetForPropertyAnnotation(disabledReason, holder);
            case ENABLED:
                return null;
        }
        return null;
    }

    private DisabledFacetForPropertyAnnotation(final String reason, final FacetHolder holder) {
        super(When.ALWAYS, Where.EVERYWHERE, reason, holder);
    }

}
