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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import org.apache.causeway.applib.services.queryresultscache.QueryResultsCache;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.applib.services.user.UserService;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.causeway.core.metamodel.postprocessors.ObjectSpecificationPostProcessorAbstract;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.extensions.secman.applib.tenancy.spi.ApplicationTenancyEvaluator;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserRepository;

import lombok.val;

public class TenantedAuthorizationPostProcessor
extends ObjectSpecificationPostProcessorAbstract {

    @Component
    public static class Register implements MetaModelRefiner {
        @Override
        public void refineProgrammingModel(final ProgrammingModel programmingModel) {
            programmingModel.addPostProcessor(
                    ProgrammingModel.PostProcessingOrder.A2_AFTER_BUILTIN,
                    new TenantedAuthorizationPostProcessor(programmingModel.getMetaModelContext()));
        }
    }

    @Inject ServiceRegistry serviceRegistry;
    @Inject UserService userService;
    @Inject @Lazy ApplicationUserRepository userRepository;
    @Inject Provider<QueryResultsCache> queryResultsCacheProvider;

    @Autowired(required=false) List<ApplicationTenancyEvaluator> applicationTenancyEvaluators;

    @Inject
    public TenantedAuthorizationPostProcessor(final MetaModelContext metaModelContext) {
        super(metaModelContext);
    }

    @Override
    public void postProcessObject(final ObjectSpecification objectSpecification) {
        FacetUtil.addFacetIfPresent(createFacet(objectSpecification.getCorrespondingClass(), objectSpecification));
    }

    @Override
    public void postProcessAction(final ObjectSpecification objectSpecification, final ObjectAction act) {
        addFacetTo(objectSpecification, act);
    }

    @Override
    public void postProcessProperty(final ObjectSpecification objectSpecification, final OneToOneAssociation prop) {
        addFacetTo(objectSpecification, prop);
    }

    @Override
    public void postProcessCollection(final ObjectSpecification objectSpecification, final OneToManyAssociation coll) {
        addFacetTo(objectSpecification, coll);
    }

    // -- HELPER

    private void addFacetTo(final ObjectSpecification specification, final ObjectFeature objectFeature) {
        FacetUtil.addFacetIfPresent(createFacet(specification.getCorrespondingClass(), objectFeature));
    }



    private Optional<TenantedAuthorizationFacetDefault> createFacet(
            final Class<?> cls,
            final FacetHolder holder) {

        val evaluators = _NullSafe.stream(applicationTenancyEvaluators)
                .filter(evaluator -> evaluator.handles(cls))
                .collect(Collectors.<ApplicationTenancyEvaluator>toList());

        return evaluators.isEmpty()
                ? Optional.empty()
                : Optional.of(new TenantedAuthorizationFacetDefault(
                        evaluators, userRepository,
                        queryResultsCacheProvider, userService,
                        holder));
    }

}
