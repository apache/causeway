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

package org.apache.isis.metamodel.facets.properties.mandatory.dflt;

import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.FacetFactory;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.isis.metamodel.facets.objectvalue.mandatory.MandatoryFacetDefault;
import org.apache.isis.metamodel.specloader.facetprocessor.FacetProcessor;

/**
 * Simply installs a {@link MandatoryFacetDefault} onto all properties and
 * parameters.
 *
 * <p>
 * The idea is that this {@link FacetFactory} is included early on in the
 * {@link FacetProcessor}, but other {@link MandatoryFacet} implementations
 * which don't require mandatory semantics will potentially replace these where
 * the property or parameter is annotated or otherwise indicated as being
 * optional.
 */
public class MandatoryFacetOnProperyDefaultFactory extends FacetFactoryAbstract {

    public MandatoryFacetOnProperyDefaultFactory() {
        super(FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        FacetUtil.addFacet(create(processMethodContext.getFacetHolder()));
    }

    private MandatoryFacet create(final FacetHolder holder) {
        return new MandatoryFacetDefault(holder);
    }

}
