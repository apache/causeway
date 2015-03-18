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

package org.apache.isis.core.metamodel.facets.members.cssclass.annotprop;

import java.util.Properties;
import org.apache.isis.applib.annotation.CssClass;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationAware;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.ContributeeMemberFacetFactory;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorForDeprecatedAnnotation;

public class CssClassFacetOnMemberFactory extends FacetFactoryAbstract implements ContributeeMemberFacetFactory, MetaModelValidatorRefiner, IsisConfigurationAware {

    private final MetaModelValidatorForDeprecatedAnnotation validator = new MetaModelValidatorForDeprecatedAnnotation(CssClass.class);

    public CssClassFacetOnMemberFactory() {
        super(FeatureType.MEMBERS);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        CssClassFacet cssClassFacet = createFromMetadataPropertiesIfPossible(processMethodContext);
        if(cssClassFacet == null) {
            cssClassFacet = validator.flagIfPresent(createFromAnnotationIfPossible(processMethodContext), processMethodContext);
        }

        // no-op if null
        FacetUtil.addFacet(cssClassFacet);
    }

    
    @Override
    public void process(final ProcessContributeeMemberContext processMemberContext) {
        CssClassFacet cssClassFacet = createFromMetadataPropertiesIfPossible(processMemberContext);

        // no-op if null
        FacetUtil.addFacet(cssClassFacet);
    }

    private static CssClassFacet createFromMetadataPropertiesIfPossible(
            final ProcessContextWithMetadataProperties<? extends FacetHolder> pcwmp) {
        
        final FacetHolder holder = pcwmp.getFacetHolder();
        
        final Properties properties = pcwmp.metadataProperties("cssClass");
        return properties != null ? new CssClassFacetOnMemberFromProperties(properties, holder) : null;
    }


    private CssClassFacet createFromAnnotationIfPossible(final ProcessMethodContext processMethodContext) {
        final CssClass annotation = Annotations.getAnnotation(processMethodContext.getMethod(), CssClass.class);
        return annotation != null ? new CssClassFacetOnMemberAnnotation(annotation.value(), processMethodContext.getFacetHolder()) : null;
    }


    // //////////////////////////////////////


    @Override
    public void refineMetaModelValidator(final MetaModelValidatorComposite metaModelValidator, final IsisConfiguration configuration) {
        metaModelValidator.add(validator);
    }

    // //////////////////////////////////////


    //region > injected
    private IsisConfiguration configuration;

    @Override
    public void setConfiguration(final IsisConfiguration configuration) {
        this.configuration = configuration;
        validator.setConfiguration(configuration);
    }
    //endregion

}

