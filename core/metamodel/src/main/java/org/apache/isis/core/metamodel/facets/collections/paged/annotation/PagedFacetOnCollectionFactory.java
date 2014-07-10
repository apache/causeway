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

package org.apache.isis.core.metamodel.facets.collections.paged.annotation;

import java.util.Properties;

import org.apache.isis.applib.annotation.Paged;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationAware;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.ContributeeMemberFacetFactory;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.objcoll.paged.PagedFacetAnnotation;
import org.apache.isis.core.metamodel.facets.objcoll.paged.PagedFacetProperties;
import org.apache.isis.core.metamodel.facets.object.paged.PagedFacet;

public class PagedFacetOnCollectionFactory extends FacetFactoryAbstract
        implements ContributeeMemberFacetFactory, IsisConfigurationAware {

    private IsisConfiguration configuration;

    public PagedFacetOnCollectionFactory() {
        super(FeatureType.COLLECTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        
        PagedFacet pagedFacet = createFromMetadataPropertiesIfPossible(processMethodContext);
        if(pagedFacet == null) {
            pagedFacet = createFromPagedAnnotationIfPossible(processMethodContext);
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
        return properties != null ? new PagedFacetProperties(properties, processMethodContext.getFacetHolder()) : null;
    }

    private PagedFacet createFromPagedAnnotationIfPossible(final ProcessMethodContext processMethodContext) {
        final Paged annotation = Annotations.getAnnotation(processMethodContext.getMethod(), Paged.class);
        return annotation != null ? new PagedFacetAnnotation(processMethodContext.getFacetHolder(), annotation.value()) : null;
    }


    // //////////////////////////////////////

    public IsisConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(IsisConfiguration configuration) {
        this.configuration = configuration;
    }


}
