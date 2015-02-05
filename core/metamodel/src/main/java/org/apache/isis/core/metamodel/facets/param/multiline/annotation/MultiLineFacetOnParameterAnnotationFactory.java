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

package org.apache.isis.core.metamodel.facets.param.multiline.annotation;

import java.lang.annotation.Annotation;
import org.apache.isis.applib.annotation.MultiLine;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationAware;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.propparam.labelat.LabelAtFacetInferredFromMultiLineFacet;
import org.apache.isis.core.metamodel.facets.propparam.multiline.MultiLineFacet;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorForDeprecatedAnnotation;

public class MultiLineFacetOnParameterAnnotationFactory extends FacetFactoryAbstract implements MetaModelValidatorRefiner, IsisConfigurationAware {

    private final MetaModelValidatorForDeprecatedAnnotation validator = new MetaModelValidatorForDeprecatedAnnotation(MultiLine.class);

    public MultiLineFacetOnParameterAnnotationFactory() {
        super(FeatureType.PARAMETERS_ONLY);
    }

    @Override
    public void processParams(final ProcessParameterContext processParameterContext) {
        final Class<?>[] parameterTypes = processParameterContext.getMethod().getParameterTypes();
        if (processParameterContext.getParamNum() >= parameterTypes.length) {
            // ignore
            return;
        }
        if (!Annotations.isString(parameterTypes[processParameterContext.getParamNum()])) {
            return;
        }
        final Annotation[] parameterAnnotations = Annotations.getParameterAnnotations(processParameterContext.getMethod())[processParameterContext.getParamNum()];
        for (final Annotation parameterAnnotation : parameterAnnotations) {
            if (parameterAnnotation instanceof MultiLine) {
                process(processParameterContext, (MultiLine) parameterAnnotation);
                return;
            }
        }
    }

    private void process(ProcessParameterContext processParameterContext, MultiLine parameterAnnotation) {
        final MultiLine annotation = (MultiLine) parameterAnnotation;

        final MultiLineFacet facet = validator.invalidIfPresent(create(annotation, processParameterContext.getFacetHolder()));

        // no-op if null
        FacetUtil.addFacet(facet);

        // no-op if null
        inferPropParamLayoutFacet(facet);
    }

    private static void inferPropParamLayoutFacet(MultiLineFacet facet) {
        if (facet == null) {
            return;
        }
        FacetUtil.addFacet(new LabelAtFacetInferredFromMultiLineFacet(facet.getFacetHolder()));
    }

    private MultiLineFacet create(final MultiLine annotation, final FacetHolder holder) {
        return (annotation != null) ? new MultiLineFacetOnParameterAnnotation(annotation.numberOfLines(), annotation.preventWrapping(), holder) : null;
    }


    @Override
    public void refineMetaModelValidator(final MetaModelValidatorComposite metaModelValidator, final IsisConfiguration configuration) {
        metaModelValidator.add(validator);
    }

    @Override
    public void setConfiguration(final IsisConfiguration configuration) {
        validator.setConfiguration(configuration);
    }

}
