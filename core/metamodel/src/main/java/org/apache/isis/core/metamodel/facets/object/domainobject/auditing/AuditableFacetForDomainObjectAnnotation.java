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


import java.util.Optional;

import org.apache.isis.applib.annotation.Auditing;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.metamodel.facets.PublishingPolicies;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.audit.AuditableFacet;
import org.apache.isis.core.metamodel.facets.object.audit.AuditableFacetAbstract;

import lombok.val;

public class AuditableFacetForDomainObjectAnnotation extends AuditableFacetAbstract {

    public static AuditableFacet create(
            Optional<Auditing> auditingIfAny,
            IsisConfiguration configuration,
            FacetHolder holder) {

        val auditing = auditingIfAny.orElse(Auditing.AS_CONFIGURED); 
                
        switch (auditing) {
        case NOT_SPECIFIED:
        case AS_CONFIGURED:

            val publishingPolicy = PublishingPolicies.entityChangePublishingPolicy(configuration);
            switch (publishingPolicy) {
            case NONE:
                return null;
            default:
                return auditingIfAny.isPresent()
                        ? new AuditableFacetForDomainObjectAnnotationAsConfigured(holder)
                        : new AuditableFacetFromConfiguration(holder);
            }
        case DISABLED:
            // explicitly disable
            return new AuditableFacetForDomainObjectAnnotation(Enablement.DISABLED, holder);
        case ENABLED:
            return new AuditableFacetForDomainObjectAnnotation(Enablement.ENABLED, holder);
            
        default:
            throw _Exceptions.unmatchedCase(auditing);
        }
    }

    protected AuditableFacetForDomainObjectAnnotation(
            Enablement enablement,
            FacetHolder holder) {
        
        super(holder, enablement);
    }
}

