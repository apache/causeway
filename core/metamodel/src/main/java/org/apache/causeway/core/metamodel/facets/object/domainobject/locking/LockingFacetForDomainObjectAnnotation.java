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
package org.apache.causeway.core.metamodel.facets.object.domainobject.locking;

import java.util.Optional;

import org.apache.causeway.applib.annotation.Locking;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.metamodel.facets.DomainObjectConfigOptions.LockingPolicy;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.object.locking.LockingFacet;
import org.apache.causeway.core.metamodel.facets.object.locking.LockingFacetAbstract;

public class LockingFacetForDomainObjectAnnotation extends LockingFacetAbstract {

    public static LockingFacet create(final Optional<Locking> locking,
            final CausewayConfiguration configuration, final FacetHolder holder) {
        switch (locking.orElse(Locking.DEFAULT)) {
        case OPTIMISTIC:
            return new LockingFacetForDomainObjectAnnotation(holder, LockingPolicy.OPTIMISTIC);
        case PESSIMISTIC:
            return new LockingFacetForDomainObjectAnnotation(holder, LockingPolicy.PESSIMISTIC);
        default:
            final LockingPolicy policy = configuration.getApplib().getAnnotation().getDomainObject().getLocking();
            return locking.isPresent()
                    ? new LockingFacetForDomainObjectAnnotationAsConfigured(holder, policy)
                    : new LockingFacetFromConfiguration(holder, policy);
        }
    }

    protected LockingFacetForDomainObjectAnnotation(final FacetHolder holder, final LockingPolicy policy) {
        super(holder, policy);
    }
}
