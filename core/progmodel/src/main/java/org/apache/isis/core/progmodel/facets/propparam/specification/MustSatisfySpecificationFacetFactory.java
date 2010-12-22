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


package org.apache.isis.core.progmodel.facets.propparam.specification;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.isis.applib.annotation.MustSatisfy;
import org.apache.isis.applib.spec.Specification;
import org.apache.isis.core.metamodel.facets.AnnotationBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.facets.FacetUtil;
import org.apache.isis.core.metamodel.facets.MethodRemover;
import org.apache.isis.core.metamodel.feature.FeatureType;

public class MustSatisfySpecificationFacetFactory  extends AnnotationBasedFacetFactoryAbstract {

    public MustSatisfySpecificationFacetFactory() {
        super(FeatureType.OBJECTS_PROPERTIES_AND_PARAMETERS);
    }

    @Override
    public boolean process(Class<?> clazz, MethodRemover methodRemover,
    		FacetHolder holder) {
        return FacetUtil.addFacet(create(clazz, holder));
    }

    private MustSatisfySpecificationFacet create(Class<?> clazz, FacetHolder holder) {
        return create(getAnnotation(clazz, MustSatisfy.class), holder);
    }


    @Override
    public boolean process(Class<?> cls, Method method, MethodRemover methodRemover, FacetHolder holder) {
        return FacetUtil.addFacet(create(method, holder));
    }

    private MustSatisfySpecificationFacet create(Method method, FacetHolder holder) {
        return create(getAnnotation(method, MustSatisfy.class), holder);
    }


    @Override
    public boolean processParams(Method method, int paramNum, FacetHolder holder) {
        final java.lang.annotation.Annotation[] parameterAnnotations = getParameterAnnotations(method)[paramNum];

        for (int j = 0; j < parameterAnnotations.length; j++) {
            if (parameterAnnotations[j] instanceof MustSatisfy) {
                final MustSatisfy annotation = (MustSatisfy) parameterAnnotations[j];
                return FacetUtil.addFacet(create(annotation, holder));
            }
        }
        return false;
    }
    
    private MustSatisfySpecificationFacet create(final MustSatisfy annotation, final FacetHolder holder) {
        if (annotation == null) {
            return null;
        }
        Class<?>[] values = annotation.value();
        List<Specification> specifications = new ArrayList<Specification>();
        for(Class<?> value: values) {
            Specification specification = newSpecificationElseNull(value);
            if (specification != null) {
                specifications.add(specification);
            }
        }
        return specifications.size() > 0 ? new MustSatisfySpecificationFacet(specifications, holder) : null;
    }

    private Specification newSpecificationElseNull(Class<?> value) {
        if (!(Specification.class.isAssignableFrom(value))) {
            return null;
        }
        try {
            return (Specification) value.newInstance();
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

}
