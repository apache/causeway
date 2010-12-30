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


package org.apache.isis.core.progmodel.facets.propparam.validate.mask;

import java.lang.reflect.Method;

import org.apache.isis.applib.annotation.Mask;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MethodRemover;
import org.apache.isis.core.metamodel.facets.AnnotationBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.ident.title.TitleFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;


public class MaskAnnotationFacetFactory extends AnnotationBasedFacetFactoryAbstract {

    public MaskAnnotationFacetFactory() {
        super(FeatureType.OBJECTS_PROPERTIES_AND_PARAMETERS);
    }

    /**
     * In readiness for supporting <tt>@Value</tt> in the future.
     */
    @Override
    public boolean process(final Class<?> cls, final MethodRemover methodRemover, final FacetHolder holder) {
        final Mask annotation = getAnnotation(cls, Mask.class);
        return FacetUtil.addFacet(createMaskFacet(annotation, holder));
    }

    @Override
    public boolean process(Class<?> cls, final Method method, final MethodRemover methodRemover, final FacetHolder holder) {
        if (method.getReturnType() == void.class) {
            return false;
        }

        final Mask annotation = getAnnotation(method, Mask.class);
        return addMaskFacetAndCorrespondingTitleFacet(holder, annotation, method.getReturnType());
    }

    @Override
    public boolean processParams(final Method method, final int paramNum, final FacetHolder holder) {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        if (paramNum >= parameterTypes.length) {
            // ignore
            return false;
        }

        final java.lang.annotation.Annotation[] parameterAnnotations = getParameterAnnotations(method)[paramNum];
        for (int i = 0; i < parameterAnnotations.length; i++) {
            if (parameterAnnotations[i] instanceof Mask) {
                final Mask annotation = (Mask) parameterAnnotations[i];
                return addMaskFacetAndCorrespondingTitleFacet(holder, annotation, parameterTypes[i]);
            }
        }
        return false;
    }

    private MaskFacet createMaskFacet(final Mask annotation, final FacetHolder holder) {
        return annotation != null ? new MaskFacetAnnotation(annotation.value(), null, holder) : null;
    }

    private boolean addMaskFacetAndCorrespondingTitleFacet(final FacetHolder holder, final Mask annotation, Class<?> cls) {
        final MaskFacet maskFacet = createMaskFacet(annotation, holder);
        if (maskFacet == null) {
            return false;
        }
        FacetUtil.addFacet(maskFacet);

        ObjectSpecification type = getSpecificationLookup().loadSpecification(cls);
        final TitleFacet underlyingTitleFacet = type.getFacet(TitleFacet.class);
        if (underlyingTitleFacet != null) {
            final TitleFacet titleFacet = new TitleFacetBasedOnMask(maskFacet, underlyingTitleFacet);
            FacetUtil.addFacet(titleFacet);
        }
        return true;
    }

}
