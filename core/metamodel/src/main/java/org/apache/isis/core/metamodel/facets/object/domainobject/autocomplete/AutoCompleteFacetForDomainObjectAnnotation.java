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

package org.apache.isis.core.metamodel.facets.object.domainobject.autocomplete;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.autocomplete.AutoCompleteFacet;
import org.apache.isis.core.metamodel.facets.object.autocomplete.AutoCompleteFacetAbstract;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;

public class AutoCompleteFacetForDomainObjectAnnotation extends AutoCompleteFacetAbstract {

    private final Class<?> repositoryClass;
    private final String actionName;

    public static AutoCompleteFacet create(
            final DomainObject domainObject,
            final SpecificationLoader specificationLoader,
            final AdapterManager adapterManager,
            final ServicesInjector servicesInjector,
            final FacetHolder holder) {

        if(domainObject == null) {
            return null;
        }

        final Class<?> autoCompleteRepository = domainObject.autoCompleteRepository();
        if(autoCompleteRepository == null || autoCompleteRepository == Object.class) {
            return null;
        }
        final String autoCompleteAction = domainObject.autoCompleteAction();
        return new AutoCompleteFacetForDomainObjectAnnotation(holder, autoCompleteRepository, autoCompleteAction, specificationLoader, adapterManager, servicesInjector);
    }

    private AutoCompleteFacetForDomainObjectAnnotation(
            final FacetHolder holder, final Class<?> repositoryClass, final String actionName, final SpecificationLoader specificationLoader, final AdapterManager adapterManager, final ServicesInjector servicesInjector) {
        super(holder, repositoryClass, actionName, specificationLoader, adapterManager, servicesInjector);
        this.repositoryClass = repositoryClass;
        this.actionName = actionName;
    }


    /**
     * Introduced for testing only.
     */
    public Class<?> getRepositoryClass() {
        return repositoryClass;
    }

    /**
     * Introduced for testing only.
     */
    public String getActionName() {
        return actionName;
    }
}
