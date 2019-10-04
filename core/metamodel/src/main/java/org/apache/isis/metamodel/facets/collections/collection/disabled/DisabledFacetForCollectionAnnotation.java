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

package org.apache.isis.metamodel.facets.collections.collection.disabled;

import java.util.Optional;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.metamodel.facets.members.disabled.DisabledFacetAbstractImpl;

public class DisabledFacetForCollectionAnnotation extends DisabledFacetAbstractImpl {

    public static DisabledFacet create(
            final Optional<Collection> collectionIfAny,
            final FacetHolder holder) {

        return collectionIfAny
                .filter(collection -> collection.editing() != Editing.NOT_SPECIFIED)
                .map(collection -> {
                    final Editing editing = collection.editing();
                    final String disabledReason = collection.editingDisabledReason();

                    switch (editing) {
                    case AS_CONFIGURED:

                        // nothing needs to be done here; the DomainObjectFactory (processing @DomainObject annotation)
                        // will install an ImmutableFacetForDomainObjectAnnotation on the domain object and then a
                        // DisabledFacetOnCollectionDerivedFromImmutable facet will be installed.

                        return null;

                    case DISABLED:
                        return (DisabledFacet)new DisabledFacetForCollectionAnnotation(disabledReason, holder);

                    case ENABLED:
                        return null;
                    default:
                        break;
                    }
                    throw new IllegalStateException("editing '" + editing + "' not recognised");
                })
                .orElse(null);
    }

    private DisabledFacetForCollectionAnnotation(final String reason, final FacetHolder holder) {
        super(Where.EVERYWHERE, reason, holder);
    }

}
