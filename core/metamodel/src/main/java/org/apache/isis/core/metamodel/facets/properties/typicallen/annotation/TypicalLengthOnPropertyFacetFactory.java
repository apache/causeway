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

package org.apache.isis.core.metamodel.facets.properties.typicallen.annotation;

import java.util.Properties;
import org.apache.isis.applib.annotation.TypicalLength;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationAware;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.ContributeeMemberFacetFactory;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.objectvalue.typicallen.TypicalLengthFacet;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorForDeprecatedAnnotation;

public class TypicalLengthOnPropertyFacetFactory extends FacetFactoryAbstract implements ContributeeMemberFacetFactory , MetaModelValidatorRefiner, IsisConfigurationAware {

    private final MetaModelValidatorForDeprecatedAnnotation validator = new MetaModelValidatorForDeprecatedAnnotation(TypicalLength.class);

    public TypicalLengthOnPropertyFacetFactory() {
        super(FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        TypicalLengthFacet facet = createFromMetadataPropertiesIfPossible(processMethodContext);
        if(facet == null) {
            facet = validator.flagIfPresent(createFromAnnotationIfPossible(processMethodContext), processMethodContext);
        }

        // no-op if null
        FacetUtil.addFacet(facet);
    }

    @Override
    public void process(ProcessContributeeMemberContext processMemberContext) {
        TypicalLengthFacet facet = createFromMetadataPropertiesIfPossible(processMemberContext);

        // no-op if null
        FacetUtil.addFacet(facet);
    }

    private static TypicalLengthFacet createFromMetadataPropertiesIfPossible(
            final ProcessContextWithMetadataProperties<? extends FacetHolder> pcwmp) {
        
        final FacetHolder holder = pcwmp.getFacetHolder();
        
        final Properties properties = pcwmp.metadataProperties("typicalLength");
        return properties != null ? new TypicalLengthFacetOnPropertyFromProperties(properties, holder) : null;
    }

    private static TypicalLengthFacet createFromAnnotationIfPossible(final ProcessMethodContext processMethodContext) {
        final TypicalLength annotation = 
                Annotations.getAnnotation(processMethodContext.getMethod(), TypicalLength.class);
        return annotation != null 
                ? new TypicalLengthFacetOnPropertyAnnotation(annotation.value(), processMethodContext.getFacetHolder()) 
                : null;
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
