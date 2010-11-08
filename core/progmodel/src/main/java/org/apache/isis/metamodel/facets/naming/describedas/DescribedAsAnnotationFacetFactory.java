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


package org.apache.isis.metamodel.facets.naming.describedas;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.apache.isis.applib.annotation.DescribedAs;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.facets.FacetUtil;
import org.apache.isis.core.metamodel.facets.MethodRemover;
import org.apache.isis.core.metamodel.facets.naming.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.java5.AnnotationBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeatureType;


public class DescribedAsAnnotationFacetFactory extends AnnotationBasedFacetFactoryAbstract {

    public DescribedAsAnnotationFacetFactory() {
        super(ObjectFeatureType.EVERYTHING);
    }

    @Override
    public boolean process(final Class<?> cls, final MethodRemover methodRemover, final FacetHolder holder) {
        final DescribedAs annotation = (DescribedAs) getAnnotation(cls, DescribedAs.class);
        return FacetUtil.addFacet(create(annotation, holder));
    }

    @Override
    public boolean process(Class<?> cls, final Method method, final MethodRemover methodRemover, final FacetHolder holder) {

        // look for annotation on the property
        final DescribedAs annotation = getAnnotation(method, DescribedAs.class);
        DescribedAsFacet facet = create(annotation, holder);
        if (facet != null) {
            return FacetUtil.addFacet(facet);
        }

        // otherwise, look for annotation on the type
        final Class<?> returnType = method.getReturnType();
        final DescribedAsFacet returnTypeDescribedAsFacet = getDescribedAsFacet(returnType);
        if (returnTypeDescribedAsFacet != null) {
            facet = new DescribedAsFacetDerivedFromType(returnTypeDescribedAsFacet, holder);
            return FacetUtil.addFacet(facet);
        }
        return false;
    }

    @Override
    public boolean processParams(final Method method, final int paramNum, final FacetHolder holder) {

        final Class<?> parameterType = method.getParameterTypes()[paramNum];
        final Annotation[] parameterAnnotations = getParameterAnnotations(method)[paramNum];
        for (int j = 0; j < parameterAnnotations.length; j++) {
            if (parameterAnnotations[j] instanceof DescribedAs) {
                return FacetUtil.addFacet(create((DescribedAs) parameterAnnotations[j], holder));
            }
        }

        // otherwise, fall back to a description on the parameter'ss type, if available
        final DescribedAsFacet parameterTypeDescribedAsFacet = getDescribedAsFacet(parameterType);
        if (parameterTypeDescribedAsFacet != null) {
            return FacetUtil.addFacet(new DescribedAsFacetDerivedFromType(parameterTypeDescribedAsFacet, holder));
        }

        return false;
    }

    private DescribedAsFacet create(final DescribedAs annotation, final FacetHolder holder) {
        return annotation == null ? null : new DescribedAsFacetAnnotation(annotation.value(), holder);
    }

    private DescribedAsFacet getDescribedAsFacet(final Class<?> type) {
        final ObjectSpecification paramTypeSpec = getSpecificationLoader().loadSpecification(type);
        return paramTypeSpec.getFacet(DescribedAsFacet.class);
    }

}
