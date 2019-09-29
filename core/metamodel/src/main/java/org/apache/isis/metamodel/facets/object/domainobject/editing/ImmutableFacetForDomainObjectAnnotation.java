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

package org.apache.isis.metamodel.facets.object.domainobject.editing;

import java.util.List;
import java.util.Map;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.metamodel.facets.object.immutable.ImmutableFacetAbstract;
import org.apache.isis.metamodel.spec.ManagedObject;

public class ImmutableFacetForDomainObjectAnnotation extends ImmutableFacetAbstract {

    private final String reason;

    public static ImmutableFacet create(
            final List<DomainObject> domainObjects,
            final IsisConfiguration configuration,
            final FacetHolder holder) {

        final EditingObjectsConfiguration setting = configuration.getObjects().getEditing();

        return domainObjects.stream()
                .filter(domainObject -> domainObject.editing() != Editing.NOT_SPECIFIED)
                .findFirst()
                .map(domainObject -> {
                    final String disabledReason = domainObject.editingDisabledReason();
                    switch (domainObject.editing()) {
                    case AS_CONFIGURED:

                        if(holder.containsDoOpFacet(ImmutableFacet.class)) {
                            // do not replace
                            return null;
                        }

                        return setting == EditingObjectsConfiguration.FALSE
                                ? (ImmutableFacet) new ImmutableFacetForDomainObjectAnnotationAsConfigured(disabledReason, holder)
                                        : null;
                    case DISABLED:
                        return new ImmutableFacetForDomainObjectAnnotation(disabledReason, holder);
                    case ENABLED:
                        return null;
                    default:
                    }
                    throw new IllegalStateException("domainObject.editing() not recognised, is " + domainObject.editing());
                })
                .orElseGet(() -> setting == EditingObjectsConfiguration.FALSE
                ? new ImmutableFacetFromConfiguration("Disabled", holder)
                        : null
                        );
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
