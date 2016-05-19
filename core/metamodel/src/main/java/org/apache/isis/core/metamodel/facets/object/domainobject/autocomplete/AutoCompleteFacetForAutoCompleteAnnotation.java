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

import org.apache.isis.applib.annotation.AutoComplete;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.autocomplete.AutoCompleteFacet;
import org.apache.isis.core.metamodel.facets.object.autocomplete.AutoCompleteFacetAbstract;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

/**
 * @deprecated
 */
@Deprecated
public class AutoCompleteFacetForAutoCompleteAnnotation extends AutoCompleteFacetAbstract {

    public static AutoCompleteFacet create(
            final AutoComplete annotation,
            final FacetHolder holder,
            final DeploymentCategory deploymentCategory,
            final SpecificationLoader specificationLoader,
            final ServicesInjector servicesInjector,
            final AdapterManager adapterManager) {

        if(annotation == null) {
            return null;
        }

        final Class<?> repositoryClass = annotation.repository();
        final String actionName = annotation.action();

        return new AutoCompleteFacetForAutoCompleteAnnotation(holder, repositoryClass, actionName, deploymentCategory,
                specificationLoader, servicesInjector, adapterManager
        );
    }

    private AutoCompleteFacetForAutoCompleteAnnotation(
            final FacetHolder holder,
            final Class<?> repositoryClass,
            final String actionName,
            final DeploymentCategory deploymentCategory,
            final SpecificationLoader specificationLoader,
            final ServicesInjector servicesInjector,
            final AdapterManager adapterManager) {
        super(holder, repositoryClass, actionName, deploymentCategory, specificationLoader, servicesInjector,
                adapterManager
        );
    }




}
