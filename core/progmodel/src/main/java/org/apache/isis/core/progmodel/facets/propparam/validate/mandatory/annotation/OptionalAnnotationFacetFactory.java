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


package org.apache.isis.core.progmodel.facets.propparam.validate.mandatory.annotation;

import java.lang.annotation.Annotation;

import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.AnnotationBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.propparam.validate.mandatory.MandatoryFacet;


public class OptionalAnnotationFacetFactory extends AnnotationBasedFacetFactoryAbstract {

    public OptionalAnnotationFacetFactory() {
        super(FeatureType.PROPERTIES_AND_PARAMETERS);
    }

    @Override
    public void process(ProcessMethodContext processMethodContext) {
        final Class<?> returnType = processMethodContext.getMethod().getReturnType();
        if (returnType.isPrimitive()) {
            return;
        }
        if (!isAnnotationPresent(processMethodContext.getMethod(), Optional.class)) {
            return;
        }
        final Optional annotation = getAnnotation(processMethodContext.getMethod(), Optional.class);
        FacetUtil.addFacet(create(annotation, processMethodContext.getFacetHolder()));
    }


    @Override
    public void processParams(ProcessParameterContext processParameterContext) {
        final Class<?>[] parameterTypes = processParameterContext.getMethod().getParameterTypes();
        if (processParameterContext.getParamNum() >= parameterTypes.length) {
            // ignore
            return;
        }
        if (parameterTypes[processParameterContext.getParamNum()].isPrimitive()) {
            return;
        }
        final Annotation[] parameterAnnotations = getParameterAnnotations(processParameterContext.getMethod())[processParameterContext.getParamNum()];
        for (int j = 0; j < parameterAnnotations.length; j++) {
            if (parameterAnnotations[j] instanceof Optional) {
                FacetUtil.addFacet(new MandatoryFacetInvertedByOptional(processParameterContext.getFacetHolder()));
                return;
            }
        }
    }

    private MandatoryFacet create(final Optional annotation, final FacetHolder holder) {
        return annotation != null ? new MandatoryFacetInvertedByOptional(holder) : null;
    }

}
