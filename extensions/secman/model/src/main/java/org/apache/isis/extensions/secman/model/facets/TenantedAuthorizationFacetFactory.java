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
package org.apache.isis.extensions.secman.model.facets;

import java.util.Collections;
import java.util.stream.Collectors;

import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancyEvaluator;
import org.apache.isis.extensions.secman.api.user.ApplicationUserRepository;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.runtime.system.context.IsisContext;

import lombok.val;

public class TenantedAuthorizationFacetFactory extends FacetFactoryAbstract {

    public TenantedAuthorizationFacetFactory() {
        super(FeatureType.EVERYTHING);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();

        FacetHolder facetHolder = processClassContext.getFacetHolder();
        FacetUtil.addFacet(createFacet(cls, facetHolder));
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        final Class<?> cls = processMethodContext.getCls();
        FacetHolder facetHolder = processMethodContext.getFacetHolder();
        FacetUtil.addFacet(createFacet(cls, facetHolder));
    }

    @Override
    public void processParams(final ProcessParameterContext processParameterContext) {
        final Class<?> cls = processParameterContext.getCls();
        FacetHolder facetHolder = processParameterContext.getFacetHolder();
        FacetUtil.addFacet(createFacet(cls, facetHolder));
    }

    private TenantedAuthorizationFacetDefault createFacet(
            final Class<?> cls, 
            final FacetHolder holder) {

        val serviceRegistry = IsisContext.getServiceRegistry();

        val evaluators = serviceRegistry
                .select(ApplicationTenancyEvaluator.class)
                .stream()
                .collect(Collectors.<ApplicationTenancyEvaluator>toList());

        val evaluatorsForCls = Collections.unmodifiableList(
                _NullSafe.stream(evaluators)
                .filter(applicationTenancyEvaluator->applicationTenancyEvaluator.handles(cls))
                .collect(Collectors.<ApplicationTenancyEvaluator>toList()));

        if(evaluatorsForCls.isEmpty()) {
            return null;
        }

        val applicationUserRepository =
                serviceRegistry.lookupService(ApplicationUserRepository.class).orElse(null);
        val queryResultsCache = 
                serviceRegistry.lookupService(QueryResultsCache.class).orElse(null);
        val userService = 
                serviceRegistry.lookupService(UserService.class).orElse(null);

        return new TenantedAuthorizationFacetDefault(
                evaluatorsForCls, applicationUserRepository, queryResultsCache, userService, holder);
    }


}
