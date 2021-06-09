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
package org.apache.isis.extensions.secman.integration.facets;

import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.postprocessors.ObjectSpecificationPostProcessorAbstract;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.extensions.secman.applib.tenancy.spi.ApplicationTenancyEvaluator;
import org.apache.isis.extensions.secman.applib.user.dom.ApplicationUserRepository;

import lombok.val;

public class TenantedAuthorizationPostProcessor
        extends ObjectSpecificationPostProcessorAbstract {

    @Component
    public static class Register implements MetaModelRefiner {
        @Override
        public void refineProgrammingModel(ProgrammingModel programmingModel) {
            programmingModel.addPostProcessor(
                    ProgrammingModel.PostProcessingOrder.A2_AFTER_BUILTIN,
                    TenantedAuthorizationPostProcessor.class);
        }
    }

    @Override
    public void doPostProcess(ObjectSpecification objectSpecification) {
        FacetUtil.addFacetIfPresent(createFacet(objectSpecification.getCorrespondingClass(), objectSpecification));
    }

    @Override
    protected void doPostProcess(ObjectSpecification objectSpecification, ObjectAction act) {
        addFacetTo(objectSpecification, act);
    }

    @Override
    protected void doPostProcess(ObjectSpecification objectSpecification, ObjectAction objectAction, ObjectActionParameter param) {
        // no-op
    }

    @Override
    protected void doPostProcess(ObjectSpecification objectSpecification, OneToOneAssociation prop) {
        addFacetTo(objectSpecification, prop);
    }

    @Override
    protected void doPostProcess(ObjectSpecification objectSpecification, OneToManyAssociation coll) {
        addFacetTo(objectSpecification, coll);
    }

    private void addFacetTo(ObjectSpecification specification, ObjectFeature objectFeature) {
        FacetUtil.addFacetIfPresent(createFacet(specification.getCorrespondingClass(), objectFeature));
    }

    private TenantedAuthorizationFacetDefault createFacet(
            final Class<?> cls,
            final FacetHolder holder) {

        val evaluators = serviceRegistry
                .select(ApplicationTenancyEvaluator.class)
                .stream()
                .filter(evaluator -> evaluator.handles(cls))
                .collect(Collectors.<ApplicationTenancyEvaluator>toList());

        return evaluators.isEmpty()
                ? null
                : new TenantedAuthorizationFacetDefault(
                        evaluators, userRepository,
                        queryResultsCacheProvider, userService,
                        holder);
    }

    @Inject ServiceRegistry serviceRegistry;
    @Inject UserService userService;
    @Inject @Lazy ApplicationUserRepository userRepository;
    @Inject Provider<QueryResultsCache> queryResultsCacheProvider;

}
