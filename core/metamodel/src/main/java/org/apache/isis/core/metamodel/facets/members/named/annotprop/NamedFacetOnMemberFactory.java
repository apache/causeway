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

package org.apache.isis.core.metamodel.facets.members.named.annotprop;

import java.util.Properties;

import org.apache.isis.applib.annotation.Named;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.ContributeeMemberFacetFactory;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorForDeprecatedAnnotation;

public class NamedFacetOnMemberFactory extends FacetFactoryAbstract implements ContributeeMemberFacetFactory, MetaModelValidatorRefiner {

    private final MetaModelValidatorForDeprecatedAnnotation validator = new MetaModelValidatorForDeprecatedAnnotation(Named.class);


    public NamedFacetOnMemberFactory() {
        super(FeatureType.MEMBERS);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        NamedFacet namedFacet = createFromMetadataPropertiesIfPossible(processMethodContext);
        if(namedFacet == null) {
            namedFacet = validator
                    .flagIfPresent(createFromAnnotationIfPossible(processMethodContext), processMethodContext);
        }
        // no-op if null
        FacetUtil.addFacet(namedFacet);
    }

    @Override
    public void process(ProcessContributeeMemberContext processMemberContext) {
        NamedFacet namedFacet = createFromMetadataPropertiesIfPossible(processMemberContext);
        // no-op if null
        FacetUtil.addFacet(namedFacet);
    }
    
    private static NamedFacet createFromMetadataPropertiesIfPossible(
            final ProcessContextWithMetadataProperties<? extends FacetHolder> pcwmp) {
        
        final FacetHolder holder = pcwmp.getFacetHolder();
        
        final Properties properties = pcwmp.metadataProperties("named");
        return properties != null ? new NamedFacetOnMemberFromProperties(properties, holder) : null;
    }

    private static NamedFacet createFromAnnotationIfPossible(final ProcessMethodContext processMethodContext) {
        final Named annotation = Annotations.getAnnotation(processMethodContext.getMethod(), Named.class);
        return annotation != null ? new NamedFacetOnMemberAnnotation(annotation.value(), processMethodContext.getFacetHolder()) : null;
    }

    @Override
    public void refineMetaModelValidator(final MetaModelValidatorComposite metaModelValidator, final IsisConfiguration configuration) {
        metaModelValidator.add(validator);
    }

    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        super.setServicesInjector(servicesInjector);
        IsisConfiguration configuration = servicesInjector.getConfigurationServiceInternal();
        validator.setConfiguration(configuration);
    }


}
