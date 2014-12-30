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

package org.apache.isis.core.metamodel.facets.actions.semantics.fallback;

import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.actions.semantics.ActionSemanticsFacet;

public class ActionSemanticsFacetFallbackToNonIdempotentFactory extends FacetFactoryAbstract {

    public ActionSemanticsFacetFallbackToNonIdempotentFactory() {
        super(FeatureType.ACTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        final FacetedMethod facetHolder = processMethodContext.getFacetHolder();
        if(facetHolder.containsDoOpFacet(ActionSemanticsFacet.class)) {

            // expect this always to be the case, because ActionSemanticsFacetAnnotationFactory will always install
            // an action semantics facet, either for @ActionSemantics or for @Action(semantics=...)

            // therefore, this facet factory is a no-op and can (probably, I reckon) be deleted
            return;
        }
        FacetUtil.addFacet(new ActionSemanticsFacetFallbackToNonIdempotent(facetHolder));
    }

}
