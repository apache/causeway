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
package org.apache.causeway.core.metamodel.facets.object.navchild;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.postprocessors.MetaModelPostProcessorAbstract;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;

/**
 * Installs the {@link NavigableSubtreeFacet} 
 * as aggregated via {@link NavigableSubtreeSequenceFacet} collected from {@link ObjectAssociation}s.
 * {@link ObjectMember}s of the {@link ObjectSpecification}.
 */
public class NavigableSubtreeFacetPostProcessor extends MetaModelPostProcessorAbstract {

    public NavigableSubtreeFacetPostProcessor(final MetaModelContext metaModelContext) {
        super(metaModelContext);
    }

    @Override
    public void postProcessObject(final ObjectSpecification objSpec) {
        var navigableSubtreeSequenceFacets = 
            objSpec.streamAssociations(MixedIn.EXCLUDED)
                .flatMap(assoc->assoc.lookupFacet(NavigableSubtreeSequenceFacet.class).stream())
                .collect(Can.toCan());
        
        FacetUtil.addFacetIfPresent(NavigableSubtreeFacet.create(navigableSubtreeSequenceFacets, objSpec));
    }

}
