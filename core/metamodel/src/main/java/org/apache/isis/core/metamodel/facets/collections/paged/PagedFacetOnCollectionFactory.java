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

package org.apache.isis.core.metamodel.facets.collections.paged;

import java.util.Properties;

import org.apache.isis.applib.annotation.Paged;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.ContributeeMemberFacetFactory;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.paged.PagedFacet;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorForDeprecatedAnnotation;

/**
 * @deprecated
 */
@Deprecated
public class PagedFacetOnCollectionFactory extends FacetFactoryAbstract
        implements ContributeeMemberFacetFactory, MetaModelValidatorRefiner {

    private final MetaModelValidatorForDeprecatedAnnotation validator = new MetaModelValidatorForDeprecatedAnnotation(Paged.class);


    public PagedFacetOnCollectionFactory() {
        super(FeatureType.COLLECTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        
        PagedFacet pagedFacet = createFromMetadataPropertiesIfPossible(processMethodContext);
        if(pagedFacet == null) {
            pagedFacet = validator.flagIfPresent(createFromPagedAnnotationIfPossible(processMethodContext), processMethodContext);
        }
        // no-op if null
        FacetUtil.addFacet(pagedFacet);
    }

    @Override
    public void process(ProcessContributeeMemberContext processMemberContext) {
        final PagedFacet pagedFacet = createFromMetadataPropertiesIfPossible(processMemberContext);
        // no-op if null
        FacetUtil.addFacet(pagedFacet);
    }

    private PagedFacet createFromMetadataPropertiesIfPossible(final ProcessContextWithMetadataProperties<?> processMethodContext) {
        final Properties properties = processMethodContext.metadataProperties("paged");
        return properties != null ? new PagedFacetPropertiesOnCollection(properties, processMethodContext.getFacetHolder()) : null;
    }

    private PagedFacet createFromPagedAnnotationIfPossible(final ProcessMethodContext processMethodContext) {
        final Paged annotation = Annotations.getAnnotation(processMethodContext.getMethod(), Paged.class);
        return annotation != null ? new PagedFacetForPagedAnnotationOnCollection(processMethodContext.getFacetHolder(), annotation.value()) : null;
    }


    @Override
    public void refineMetaModelValidator(final MetaModelValidatorComposite metaModelValidator, final IsisConfiguration configuration) {
        metaModelValidator.add(validator);
    }

    // //////////////////////////////////////



    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        super.setServicesInjector(servicesInjector);
        final IsisConfiguration configuration = getConfiguration();

        validator.setConfiguration(configuration);
    }




}
