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
package org.apache.causeway.core.metamodel.facets.object.mixin;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.internal.reflection._MethodFacades;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facetapi.MethodRemover;
import org.apache.causeway.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.causeway.core.metamodel.facets.FacetFactory.ProcessParameterContext;
import org.apache.causeway.core.metamodel.facets.FacetedMethodParameter;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel;

abstract class MixinIntendedAs {

    protected ProgrammingModel programmingModel;
    private MetaModelContext_forTesting metaModelContext;

    protected void setUp() throws Exception {

        metaModelContext = MetaModelContext_forTesting.builder()
                .build();

        programmingModel = metaModelContext.getProgrammingModel();
    }

    protected void tearDown() {
        metaModelContext.getSpecificationLoader().disposeMetaModel();
    }

    protected void newContext(
            final Class<?> cls,
            final ResolvedMethod method,
            final int paramNum,
            final MethodRemover methodRemover) {

    }

    protected FacetHolder runTypeContextOn(final Class<?> type) {

        var facetHolder = FacetHolder.simple(
                metaModelContext,
                Identifier.classIdentifier(LogicalType.fqcn(type)));

        var processClassContext = ProcessClassContext
                .forTesting(type, MethodRemover.NOOP, facetHolder);

        programmingModel.streamFactories()
//        .filter(facetFactory->!facetFactory.getClass().getSimpleName().startsWith("Grid"))
//        .peek(facetFactory->System.out.println("### " + facetFactory.getClass().getName()))
        .forEach(facetFactory->facetFactory.process(processClassContext));

        return facetHolder;
    }

    protected FacetedMethodParameter runScalarParameterContextOn(final ResolvedMethod actionMethod, final int paramIndex) {

        var owningType = actionMethod.method().getDeclaringClass();

        var facetedMethodParameter = new FacetedMethodParameter(
                metaModelContext,
                FeatureType.ACTION_PARAMETER_SINGULAR,
                owningType,
                _MethodFacades.regular(actionMethod),
                0);

        var processParameterContext =
                ProcessParameterContext.forTesting(
                        owningType,
                        IntrospectionPolicy.ANNOTATION_OPTIONAL,
                        actionMethod,
                        MethodRemover.NOOP,
                        facetedMethodParameter);

        programmingModel.streamFactories()
        .forEach(facetFactory->facetFactory.processParams(processParameterContext));

        return facetedMethodParameter;
    }

}
