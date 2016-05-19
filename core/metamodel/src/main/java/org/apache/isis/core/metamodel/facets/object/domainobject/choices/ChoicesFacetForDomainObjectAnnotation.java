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

package org.apache.isis.core.metamodel.facets.object.domainobject.choices;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.choices.ChoicesFacetFromBoundedAbstract;
import org.apache.isis.core.metamodel.services.persistsession.PersistenceSessionServiceInternal;

public class ChoicesFacetForDomainObjectAnnotation extends ChoicesFacetFromBoundedAbstract {

    public static Facet create(
            final DomainObject domainObject,
            final FacetHolder facetHolder,
            final DeploymentCategory deploymentCategory,
            final AuthenticationSessionProvider authenticationSessionProvider,
            final PersistenceSessionServiceInternal persistenceSessionServiceInternal) {

        if(domainObject == null) {
            return null;
        }

        final boolean bounded = domainObject.bounded();
        return bounded
                ? new ChoicesFacetForDomainObjectAnnotation(
                    facetHolder, deploymentCategory, authenticationSessionProvider, persistenceSessionServiceInternal)
                : null;
    }

    private ChoicesFacetForDomainObjectAnnotation(
            final FacetHolder holder,
            final DeploymentCategory deploymentCategory,
            final AuthenticationSessionProvider authenticationSessionProvider, final PersistenceSessionServiceInternal persistenceSessionServiceInternal) {
        super(holder, deploymentCategory, authenticationSessionProvider, persistenceSessionServiceInternal);
    }

}
