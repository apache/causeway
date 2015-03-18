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


import org.apache.isis.applib.annotation.Auditing;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.audit.AuditableFacet;
import org.apache.isis.core.metamodel.facets.object.audit.AuditableFacetAbstract;


public class AuditableFacetForDomainObjectAnnotation extends AuditableFacetAbstract {

    public static AuditableFacet create(
            final DomainObject domainObject,
            final IsisConfiguration configuration,
            final FacetHolder holder) {

        final Auditing auditing = domainObject != null ? domainObject.auditing() : Auditing.AS_CONFIGURED;
        switch (auditing) {
            case AS_CONFIGURED:

                final AuditObjectsConfiguration setting = AuditObjectsConfiguration.parse(configuration);
                switch (setting) {
                    case NONE:
                        return null;
                    default:
                        return domainObject != null
                                ? new AuditableFacetForDomainObjectAnnotationAsConfigured(holder)
                                : new AuditableFacetFromConfiguration(holder);
                }
            case DISABLED:
                return null;
            case ENABLED:
                return new AuditableFacetForDomainObjectAnnotation(Enablement.ENABLED, holder);
        }
        return null;
    }

    protected AuditableFacetForDomainObjectAnnotation(
            final Enablement enablement,
            final FacetHolder holder) {
        super(holder, enablement);
    }
}
