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

import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Provider;

import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancyEvaluator;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUser;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUserRepository;

public class TenantedAuthorizationFacetDefault extends FacetAbstract implements TenantedAuthorizationFacet {

    public static Class<? extends Facet> type() {
        return TenantedAuthorizationFacet.class;
    }

    private final List<ApplicationTenancyEvaluator> evaluators;
    private final ApplicationUserRepository<? extends ApplicationUser> applicationUserRepository;
    private final Provider<QueryResultsCache> queryResultsCacheProvider;
    private final UserService userService;

    public TenantedAuthorizationFacetDefault(
            final List<ApplicationTenancyEvaluator> evaluators,
            final ApplicationUserRepository<? extends ApplicationUser> applicationUserRepository,
            final Provider<QueryResultsCache> queryResultsCacheProvider,
            final UserService userService,
            final FacetHolder holder) {
        super(type(), holder, Derivation.NOT_DERIVED);
        this.evaluators = evaluators;
        this.applicationUserRepository = applicationUserRepository;
        this.queryResultsCacheProvider = queryResultsCacheProvider;
        this.userService = userService;
    }

    @Override
    public String hides(final VisibilityContext ic) {

        if(evaluators == null || evaluators.isEmpty()) {
            return null;
        }

        final Object domainObject = ic.getHead().getOwner().getPojo();
        final String userName = userService.currentUserNameElseNobody();

        final ApplicationUser applicationUser = findApplicationUser(userName);
        if (applicationUser == null) {
            // not expected, but best to be safe...
            return "Could not locate application user for " + userName;
        }

        for (ApplicationTenancyEvaluator evaluator : evaluators) {
            final String reason = evaluator.hides(domainObject, applicationUser);
            if(reason != null) {
                return reason;
            }
        }
        return null;
    }


    @Override
    public String disables(final UsabilityContext ic) {
        if(evaluators == null || evaluators.isEmpty()) {
            return null;
        }

        final Object domainObject = ic.getHead().getOwner().getPojo();
        final String userName = userService.currentUserNameElseNobody();

        final ApplicationUser applicationUser = findApplicationUser(userName);
        if (applicationUser == null) {
            // not expected, but best to be safe...
            return "Could not locate application user for " + userName;
        }

        for (ApplicationTenancyEvaluator evaluator : evaluators) {
            final String reason = evaluator.disables(domainObject, applicationUser);
            if(reason != null) {
                return reason;
            }
        }
        return null;
    }


    /**
     * Per {@link #findApplicationUserNoCache(String)}, cached for the request using the {@link QueryResultsCache}.
     */
    protected ApplicationUser findApplicationUser(final String userName) {
        return queryResultsCacheProvider.get().execute(new Callable<ApplicationUser>() {
            @Override
            public ApplicationUser call() throws Exception {
                return findApplicationUserNoCache(userName);
            }
        }, TenantedAuthorizationFacetDefault.class, "findApplicationUser", userName);
    }

    protected ApplicationUser findApplicationUserNoCache(final String userName) {
        return applicationUserRepository.findByUsername(userName).orElse(null);
    }

}
