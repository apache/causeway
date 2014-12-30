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

package org.apache.isis.core.metamodel.facets.param.parameter;

import java.lang.reflect.Method;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjectorAware;

public class ParameterAnnotationFacetFactory extends FacetFactoryAbstract implements ServicesInjectorAware {

    private ServicesInjector servicesInjector;

    public ParameterAnnotationFacetFactory() {
        super(FeatureType.PARAMETERS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        processMaxLength(processMethodContext);
        processMustSatisfy(processMethodContext);
        processOptional(processMethodContext);
        processRegEx(processMethodContext);
    }

    private void processMaxLength(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final Parameter parameter = Annotations.getAnnotation(method, Parameter.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

        FacetUtil.addFacet(
                MaxLengthFacetForParameterAnnotation.create(parameter, holder));
    }

    private void processMustSatisfy(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final Parameter parameter = Annotations.getAnnotation(method, Parameter.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

        FacetUtil.addFacet(
                MustSatisfySpecificationFacetForParameterAnnotation.create(parameter, holder));
    }

    private void processOptional(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final Parameter parameter = Annotations.getAnnotation(method, Parameter.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

        FacetUtil.addFacet(
                MandatoryFacetForParameterAnnotation.create(parameter, method, holder));
    }

    private void processRegEx(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final Parameter parameter = Annotations.getAnnotation(method, Parameter.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

        FacetUtil.addFacet(
                RegExFacetForParameterAnnotation.create(parameter, holder));
    }


    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        this.servicesInjector = servicesInjector;
    }
}
