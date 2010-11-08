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


package org.apache.isis.metamodel.facets.propparam.multiline;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.apache.isis.applib.annotation.MultiLine;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeatureType;
import org.apache.isis.metamodel.facets.FacetHolder;
import org.apache.isis.metamodel.facets.FacetUtil;
import org.apache.isis.metamodel.facets.MethodRemover;
import org.apache.isis.metamodel.java5.AnnotationBasedFacetFactoryAbstract;


public class MultiLineAnnotationFacetFactory extends AnnotationBasedFacetFactoryAbstract {

    public MultiLineAnnotationFacetFactory() {
        super(ObjectFeatureType.OBJECTS_PROPERTIES_AND_PARAMETERS);
    }

    /**
     * In readiness for supporting <tt>@Value</tt> in the future.
     */
    @Override
    public boolean process(final Class<?> cls, final MethodRemover methodRemover, final FacetHolder holder) {
        final MultiLine annotation = getAnnotation(cls, MultiLine.class);
        return FacetUtil.addFacet(create(annotation, holder));
    }

    @Override
    public boolean process(Class<?> cls, final Method method, final MethodRemover methodRemover, final FacetHolder holder) {
        final Class<?> returnType = method.getReturnType();
        if (!isString(returnType)) {
            return false;
        }
        final MultiLine annotation = getAnnotation(method, MultiLine.class);
        return FacetUtil.addFacet(create(annotation, holder));
    }

    @Override
    public boolean processParams(final Method method, final int paramNum, final FacetHolder holder) {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        if (paramNum >= parameterTypes.length) {
            // ignore
            return false;
        }
        if (!isString(parameterTypes[paramNum])) {
            return false;
        }
        final Annotation[] parameterAnnotations = getParameterAnnotations(method)[paramNum];
        for (int j = 0; j < parameterAnnotations.length; j++) {
            if (parameterAnnotations[j] instanceof MultiLine) {
                final MultiLine annotation = (MultiLine) parameterAnnotations[j];
                return FacetUtil.addFacet(create(annotation, holder));
            }
        }
        return false;
    }

    private MultiLineFacet create(final MultiLine annotation, final FacetHolder holder) {
        return (annotation != null) ? new MultiLineFacetAnnotation(annotation.numberOfLines(), annotation.preventWrapping(),
                holder) : null;
    }

}
