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

package org.apache.isis.core.metamodel.facets.actions.contributing.derived;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.actions.contributing.ContributingFacet.Contributing;
import org.apache.isis.core.metamodel.facets.actions.contributing.ContributingFacetAbstract;
import org.apache.isis.core.metamodel.facets.object.mixin.MixinFacet;

import lombok.val;

public class ContributingFacetDerivedFromMixinFacetFactory extends FacetFactoryAbstract {

    public ContributingFacetDerivedFromMixinFacetFactory() {
        super(FeatureType.ACTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        if(!processMethodContext.isMixinMain()) {
            // skip processing if not mixin main
            return;
        }

        val method = processMethodContext.getMethod();
        val declaringClass = method.getDeclaringClass();
        val spec = getSpecificationLoader().loadSpecification(declaringClass);

        if(!spec.lookupNonFallbackFacet(MixinFacet.class).isPresent()) {
            return;
        }

        val facetedMethod = processMethodContext.getFacetHolder();

        //[1998] if @Action or @ActionLayout detected on type level infer:
        //@ActionLayout(contributed=ACTION)
        val isForceContributedAsAction =
                processMethodContext.synthesizeOnType(Action.class).isPresent()
                || processMethodContext.synthesizeOnType(ActionLayout.class).isPresent();

        if(isForceContributedAsAction) {
            FacetUtil.addFacetIfPresent(new ContributingFacetAbstract(Contributing.AS_ACTION, facetedMethod) {});
        }

    }

}
