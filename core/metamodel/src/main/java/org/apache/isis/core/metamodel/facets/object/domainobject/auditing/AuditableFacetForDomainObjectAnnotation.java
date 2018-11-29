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
package org.apache.isis.core.metamodel.facets.object.domainobject.auditing;


import java.util.List;

import org.apache.isis.applib.annotation.Auditing;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.audit.AuditableFacet;
import org.apache.isis.core.metamodel.facets.object.audit.AuditableFacetAbstract;


public class AuditableFacetForDomainObjectAnnotation extends AuditableFacetAbstract {

    public static AuditableFacet create(
            final List<DomainObject> domainObjects,
            final IsisConfiguration configuration,
            final FacetHolder holder) {

        return domainObjects
                .stream()
                .filter(domainObject -> domainObject.auditing() != Auditing.NOT_SPECIFIED)
                .findFirst()
                .map(domainObject -> {
                    switch (domainObject.auditing()) {
                    case DISABLED:
                        return (AuditableFacet)new AuditableFacetForDomainObjectAnnotation(Enablement.DISABLED, holder);
                    case ENABLED:
                        return new AuditableFacetForDomainObjectAnnotation(Enablement.ENABLED, holder);
                    case AS_CONFIGURED:
                    case NOT_SPECIFIED:
                        return new AuditableFacetForDomainObjectAnnotationAsConfigured(holder);
                    default:
                        throw _Exceptions.unmatchedCase(domainObject.auditing());
                    }
                })
                .orElse(new AuditableFacetFromConfiguration(holder));
    }

    protected AuditableFacetForDomainObjectAnnotation(
            final Enablement enablement,
            final FacetHolder holder) {
        super(holder, enablement);
    }
}
