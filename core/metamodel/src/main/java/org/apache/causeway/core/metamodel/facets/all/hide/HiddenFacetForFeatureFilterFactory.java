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
package org.apache.causeway.core.metamodel.facets.all.hide;

import java.util.Optional;

import jakarta.inject.Inject;

import org.apache.causeway.commons.internal.ioc.SpringContextHolder;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

/**
 * Installs the {@link HiddenFacetForFeatureFilterImpl} on the
 * {@link ObjectSpecification}.
 */
public class HiddenFacetForFeatureFilterFactory
extends FacetFactoryAbstract {

	private final ApplicationFeatureFilters applicationFeatureFilters;

    @Inject
    public HiddenFacetForFeatureFilterFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.EVERYTHING_BUT_PARAMETERS);
        var springContext = Optional.ofNullable(mmc.getSystemEnvironment().springContextHolder())
        		.map(SpringContextHolder::springContext)
        		.orElse(null);
        this.applicationFeatureFilters = ApplicationFeatureFilters.collectFrom(springContext);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        var facetHolder = processClassContext.getFacetHolder();
        //TODO WIP FacetUtil.addFacet(new HiddenFacetForFeatureFilterImpl(facetHolder));
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
    	var facetHolder = processMethodContext.getFacetHolder();
    	//TODO WIP processMethodContext.getFeatureType().isProperty();
    	//TODO WIP FacetUtil.addFacet(new HiddenFacetForFeatureFilterImpl(facetHolder));
    }

}
