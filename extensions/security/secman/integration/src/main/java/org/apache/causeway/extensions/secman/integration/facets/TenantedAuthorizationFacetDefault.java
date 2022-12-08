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
package org.apache.causeway.extensions.secman.integration.facets;

import lombok.val;

import java.util.List;

import javax.inject.Provider;

import org.apache.causeway.applib.services.queryresultscache.QueryResultsCache;
import org.apache.causeway.applib.services.user.UserService;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.interactions.UsabilityContext;
import org.apache.causeway.core.metamodel.interactions.VisibilityContext;
import org.apache.causeway.extensions.secman.applib.tenancy.spi.ApplicationTenancyEvaluator;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserRepository;
import org.springframework.lang.Nullable;

public class TenantedAuthorizationFacetDefault
extends FacetAbstract
implements TenantedAuthorizationFacet {

    private static Class<? extends Facet> type() {
        return TenantedAuthorizationFacet.class;
    }

    private final List<ApplicationTenancyEvaluator> evaluators;
    private final ApplicationUserRepository applicationUserRepository;
    private final UserService userService;
    private final Provider<QueryResultsCache> queryResultsCacheProvider;

    public TenantedAuthorizationFacetDefault(
            final List<ApplicationTenancyEvaluator> evaluators,
            final ApplicationUserRepository applicationUserRepository,
            final Provider<QueryResultsCache> queryResultsCacheProvider,
            final UserService userService,
            final FacetHolder holder) {
        super(type(), holder);
        this.evaluators = evaluators;
        this.applicationUserRepository = applicationUserRepository;
        this.queryResultsCacheProvider = queryResultsCacheProvider;
        this.userService = userService;
    }

    @Override
    public String hides(final VisibilityContext ic) {
        return evaluate(ApplicationTenancyEvaluator::hides, ic.getHead());
    }

    @Override
    public String disables(final UsabilityContext ic) {
        return evaluate(ApplicationTenancyEvaluator::disables, ic.getHead());
    }

    @Nullable
    private String evaluate(EvaluationDispatcher evaluationDispatcher, InteractionHead head) {
        if(evaluators == null
                || evaluators.isEmpty()
                || userService.isCurrentUserWithSudoAccessAllRole()) {
            return null;
        }

        val domainObject = head.getOwner().getPojo();
        val userName = userService.currentUserNameElseNobody();

        val applicationUser = findApplicationUser(userName);
        if (applicationUser == null) {
            // not expected, but best to be safe...
            return "Could not locate application user for " + userName;
        }

        for (val evaluator : evaluators) {
            final String reason = evaluationDispatcher.dispatch(evaluator, domainObject, applicationUser);
            if(reason != null) {
                return reason;
            }
        }
        return null;
    }

    interface EvaluationDispatcher {
        String dispatch(ApplicationTenancyEvaluator evaluator, Object domainObject, ApplicationUser applicationUser);
    }


    /**
     * Per {@link #findApplicationUserNoCache(String)},
     * cached for the request using the {@link QueryResultsCache}.
     */
    protected ApplicationUser findApplicationUser(final String userName) {
        return queryResultsCacheProvider.get()
            .execute(
                    ()->findApplicationUserNoCache(userName),
                    TenantedAuthorizationFacetDefault.class,
                    "findApplicationUser",
                    userName);
    }

    protected ApplicationUser findApplicationUserNoCache(final String userName) {
        return applicationUserRepository.findByUsername(userName).orElse(null);
    }



}
