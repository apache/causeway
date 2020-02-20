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

package org.apache.isis.core.metamodel.facets.object.domainobject.editing;

import java.util.Map;
import java.util.Optional;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.metamodel.facets.EditingObjectsConfiguration;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacetAbstract;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.val;

public class ImmutableFacetForDomainObjectAnnotation extends ImmutableFacetAbstract {

    private final String reason;

    public static ImmutableFacet create(
            final Optional<DomainObject> domainObjectIfAny,
            final IsisConfiguration configuration,
            final FacetHolder holder) {

        final boolean editingDisabledByDefault = 
                configuration.getApplib().getAnnotation().getDomainObject().getEditing()
                == EditingObjectsConfiguration.FALSE;

        if(domainObjectIfAny.isPresent()) {
            val domainObject = domainObjectIfAny.get();
            val disabledReason = domainObject.editingDisabledReason();

            switch (domainObject.editing()) {
            case NOT_SPECIFIED:
            case AS_CONFIGURED:

                if(holder.containsNonFallbackFacet(ImmutableFacet.class)) {
                    // do not replace
                    return null;
                }

                return editingDisabledByDefault
                        ? (ImmutableFacet) new ImmutableFacetForDomainObjectAnnotationAsConfigured(disabledReason, holder)
                                : null;
            case DISABLED:
                return new ImmutableFacetForDomainObjectAnnotation(disabledReason, holder);
            case ENABLED:
                return null;
            default:
                throw _Exceptions.unmatchedCase(domainObject.editing());
            }
        }
        
        return editingDisabledByDefault
                    ? new ImmutableFacetFromConfiguration("Disabled", holder)
                    : null;
    }
    
    public ImmutableFacetForDomainObjectAnnotation(final String reason, final FacetHolder holder) {
        super(holder);
        this.reason = reason;
    }

    @Override
    public String disabledReason(final ManagedObject targetAdapter) {
        return !_Strings.isNullOrEmpty(reason)
                ? reason
                : super.disabledReason(targetAdapter);
    }

    @Override
    public void copyOnto(final FacetHolder holder) {
        final Facet facet = new ImmutableFacetForDomainObjectAnnotation(reason, holder);
        FacetUtil.addFacet(facet);
    }

    @Override
    public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("reason", reason);
    }
    

}
