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
package org.apache.isis.core.metamodel.facets.actions.notinservicemenu.derived;

import java.lang.reflect.Method;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.object.domainservice.DomainServiceFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public class NotInServiceMenuFacetFromDomainServiceFacetFactory
extends FacetFactoryAbstract {

    @Inject
    public NotInServiceMenuFacetFromDomainServiceFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.ACTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        final Method method = processMethodContext.getMethod();
        final Class<?> declaringClass = method.getDeclaringClass();
        final ObjectSpecification spec = getSpecificationLoader().loadSpecification(declaringClass);

        if(spec == null) {
            return;
        }

        spec.lookupNonFallbackFacet(DomainServiceFacet.class)
        .ifPresent(domainServiceFacet->{
            final NatureOfService natureOfService = domainServiceFacet.getNatureOfService();
            if(natureOfService.isView()) {
                return;
            }
            final FacetedMethod facetHolder = processMethodContext.getFacetHolder();
            FacetUtil.addFacet(new NotInServiceMenuFacetFromDomainServiceFacet(natureOfService, facetHolder));
        });

    }

}
