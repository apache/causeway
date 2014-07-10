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

package org.apache.isis.core.metamodel.facets.properties.validating.maskannot;

import org.apache.isis.applib.annotation.Mask;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.facets.object.mask.MaskFacet;
import org.apache.isis.core.metamodel.facets.object.mask.TitleFacetBasedOnMask;

public class MaskFacetOnPropertyAnnotationFactory extends FacetFactoryAbstract {

    public MaskFacetOnPropertyAnnotationFactory() {
        super(FeatureType.PROPERTIES_ONLY);
    }

    /**
     * In readiness for supporting <tt>@Value</tt> in the future.
     */
    @Override
    public void process(final ProcessClassContext processClassContaxt) {
        final Mask annotation = Annotations.getAnnotation(processClassContaxt.getCls(), Mask.class);
        FacetUtil.addFacet(createMaskFacet(annotation, processClassContaxt.getFacetHolder()));
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        if (processMethodContext.getMethod().getReturnType() == void.class) {
            return;
        }

        final Mask annotation = Annotations.getAnnotation(processMethodContext.getMethod(), Mask.class);
        addMaskFacetAndCorrespondingTitleFacet(processMethodContext.getFacetHolder(), annotation, processMethodContext.getMethod().getReturnType());
    }

    @Override
    public void processParams(final ProcessParameterContext processParameterContext) {
        final Class<?>[] parameterTypes = processParameterContext.getMethod().getParameterTypes();
        if (processParameterContext.getParamNum() >= parameterTypes.length) {
            // ignore
            return;
        }

        final java.lang.annotation.Annotation[] parameterAnnotations = Annotations.getParameterAnnotations(processParameterContext.getMethod())[processParameterContext.getParamNum()];
        for (int i = 0; i < parameterAnnotations.length; i++) {
            if (parameterAnnotations[i] instanceof Mask) {
                final Mask annotation = (Mask) parameterAnnotations[i];
                addMaskFacetAndCorrespondingTitleFacet(processParameterContext.getFacetHolder(), annotation, parameterTypes[i]);
                return;
            }
        }
    }

    private MaskFacet createMaskFacet(final Mask annotation, final FacetHolder holder) {
        return annotation != null ? new MaskFacetOnPropertyAnnotation(annotation.value(), null, holder) : null;
    }

    private boolean addMaskFacetAndCorrespondingTitleFacet(final FacetHolder holder, final Mask annotation, final Class<?> cls) {
        final MaskFacet maskFacet = createMaskFacet(annotation, holder);
        if (maskFacet == null) {
            return false;
        }
        FacetUtil.addFacet(maskFacet);

        final ObjectSpecification type = getSpecificationLoader().loadSpecification(cls);
        final TitleFacet underlyingTitleFacet = type.getFacet(TitleFacet.class);
        if (underlyingTitleFacet != null) {
            final TitleFacet titleFacet = new TitleFacetBasedOnMask(maskFacet, underlyingTitleFacet);
            FacetUtil.addFacet(titleFacet);
        }
        return true;
    }

}
