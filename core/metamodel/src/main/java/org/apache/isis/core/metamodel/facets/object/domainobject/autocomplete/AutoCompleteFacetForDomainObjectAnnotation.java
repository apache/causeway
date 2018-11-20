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

import java.lang.reflect.Method;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.autocomplete.AutoCompleteFacetAbstract;
import org.apache.isis.core.metamodel.services.ServicesInjector;

public class AutoCompleteFacetForDomainObjectAnnotation extends AutoCompleteFacetAbstract {

    private final String actionName;

    public AutoCompleteFacetForDomainObjectAnnotation(
            final FacetHolder facetHolder,
            final Class<?> repositoryClass,
            final Method repositoryMethod,
            final ServicesInjector servicesInjector) {
        super(facetHolder, repositoryClass, repositoryMethod, servicesInjector);
        this.actionName = repositoryMethod.getName();
    }


    /**
     * Introduced for testing only.
     */
    public String getActionName() {
        return actionName;
    }
}
