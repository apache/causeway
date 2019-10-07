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
package org.apache.isis.metamodel.facets.object.mixin;

import java.lang.reflect.Method;

import org.apache.isis.applib.Identifier;
import org.apache.isis.commons.internal.environment.IsisSystemEnvironment;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.metamodel.MetaModelContext;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facetapi.MethodRemover;
import org.apache.isis.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.metamodel.facets.FacetFactory;
import org.apache.isis.metamodel.facets.FacetedMethodParameter;
import org.apache.isis.metamodel.facets.MethodRemoverConstants;
import org.apache.isis.metamodel.progmodel.ProgrammingModelAbstract;
import org.apache.isis.metamodel.progmodel.ProgrammingModelInitFilterDefault;
import org.apache.isis.metamodel.progmodels.dflt.ProgrammingModelFacetsJava8;
import org.apache.isis.metamodel.specloader.SpecificationLoaderDefault;

import lombok.val;

abstract class MixinIntendedAs {
    
    protected ProgrammingModelFacetsJava8 programmingModel;

    protected void setUp() throws Exception {

        programmingModel = new ProgrammingModelFacetsJava8();
        
        // PRODUCTION

        MetaModelContext.preset(MetaModelContext.builder()
                .specificationLoader(SpecificationLoaderDefault.getInstance(
                        new IsisConfiguration(),
                        new IsisSystemEnvironment(),
                        programmingModel))
//                .serviceInjector(mockServiceInjector)
//                .serviceRegistry(mockServiceRegistry)
                .build());
        
        ((ProgrammingModelAbstract)programmingModel).init(new ProgrammingModelInitFilterDefault());
        MetaModelContext.current().getSpecificationLoader().init();
        
    }

    protected void tearDown() {
        programmingModel = null;
    }

    protected void newContext(
            final Class<?> cls,
            final Method method,
            final int paramNum,
            final MethodRemover methodRemover) {
        
    }

    protected FacetHolder runTypeContextOn(Class<?> type) {
        
        val facetHolder = new AbstractFacetFactoryTest.IdentifiedHolderImpl(
              Identifier.classIdentifier(type));
        
        val processClassContext = 
                new FacetFactory.ProcessClassContext(
                        type, 
                        MethodRemoverConstants.NOOP, 
                        facetHolder);
        
        programmingModel.streamFactories()
//        .filter(facetFactory->!facetFactory.getClass().getSimpleName().startsWith("Grid"))
//        .peek(facetFactory->System.out.println("### " + facetFactory.getClass().getName()))
        .forEach(facetFactory->facetFactory.process(processClassContext));
        
        return facetHolder;
    }
    
    protected FacetedMethodParameter runScalarParameterContextOn(Method actionMethod, int paramIndex) {
        
        val owningType = actionMethod.getDeclaringClass();
        val parameterType = actionMethod.getParameterTypes()[paramIndex];
        
        val facetedMethodParameter = new FacetedMethodParameter(
                FeatureType.ACTION_PARAMETER_SCALAR, 
                owningType, 
                actionMethod, 
                parameterType);
        
        val processParameterContext = 
                new FacetFactory.ProcessParameterContext(
                        owningType, 
                        actionMethod, 
                        paramIndex, 
                        MethodRemoverConstants.NOOP, 
                        facetedMethodParameter);
        
        programmingModel.streamFactories()
        .forEach(facetFactory->facetFactory.processParams(processParameterContext));
        
        return facetedMethodParameter;
    }
    
}
