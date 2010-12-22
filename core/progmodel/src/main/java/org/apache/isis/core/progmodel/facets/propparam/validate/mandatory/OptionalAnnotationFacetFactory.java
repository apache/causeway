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


package org.apache.isis.core.progmodel.facets.propparam.validate.mandatory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.core.metamodel.facets.AnnotationBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.facets.FacetUtil;
import org.apache.isis.core.metamodel.facets.MethodRemover;
import org.apache.isis.core.metamodel.facets.propparam.validate.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.feature.FeatureType;


public class OptionalAnnotationFacetFactory extends AnnotationBasedFacetFactoryAbstract {

    public OptionalAnnotationFacetFactory() {
        super(FeatureType.PROPERTIES_AND_PARAMETERS);
    }

    @Override
    public boolean process(Class<?> cls, final Method method, final MethodRemover methodRemover, final FacetHolder holder) {
        final Class<?> returnType = method.getReturnType();
        if (returnType.isPrimitive()) {
            return false;
        }
        if (!isAnnotationPresent(method, Optional.class)) {
            return false;
        }
        final Optional annotation = getAnnotation(method, Optional.class);
        return FacetUtil.addFacet(create(annotation, holder));
    }


    @Override
    public boolean processParams(final Method method, final int paramNum, final FacetHolder holder) {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        if (paramNum >= parameterTypes.length) {
            // ignore
            return false;
        }
        if (parameterTypes[paramNum].isPrimitive()) {
            return false;
        }
        final Annotation[] parameterAnnotations = getParameterAnnotations(method)[paramNum];
        for (int j = 0; j < parameterAnnotations.length; j++) {
            if (parameterAnnotations[j] instanceof Optional) {
                return FacetUtil.addFacet(new OptionalFacet(holder));
            }
        }
        return false;
    }

    private MandatoryFacet create(final Optional annotation, final FacetHolder holder) {
        return annotation != null ? new OptionalFacet(holder) : null;
    }

}
