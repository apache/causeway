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
package org.apache.causeway.core.metamodel.postprocessors.all.i18n;

import javax.inject.Inject;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facets.all.i8n.noun.Noun;
import org.apache.causeway.core.metamodel.facets.all.named.ObjectNamedFacet;
import org.apache.causeway.core.metamodel.facets.all.named.ObjectNamedFacetSynthesized;
import org.apache.causeway.core.metamodel.postprocessors.MetaModelPostProcessorAbstract;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

public class SynthesizeObjectNamingPostProcessor
extends MetaModelPostProcessorAbstract {

    @Inject
    public SynthesizeObjectNamingPostProcessor(final MetaModelContext metaModelContext) {
        super(metaModelContext);
    }

    @Override
    public void postProcessObject(final ObjectSpecification objectSpecification) {

        final boolean canProcess = objectSpecification.isEntityOrViewModelOrAbstract()
                || objectSpecification.isInjectable();
        if(!canProcess) return;

        var topRank = objectSpecification
                .getFacetRanking(ObjectNamedFacet.class) // don't use lookupFacet, as that would search up the
                                                         // inheritance hierarchy (which we don't want here)
                .map(facetRanking->facetRanking.getTopRank(ObjectNamedFacet.class))
                .orElse(Can.empty())
                .reverse(); // historically last have higher precedence, so when reverted we can use findFirst logic

        var singular = topRank
                .stream()
                .filter(objectNamedFacet->objectNamedFacet.isNounPresent())
                .findFirst()
                .map(ObjectNamedFacet::singular)
                .filter(_Strings::isNotEmpty)
                .orElseGet(()->getSingularFallbackNoun(objectSpecification));

        FacetUtil.addFacet(
                new ObjectNamedFacetSynthesized(
                        Noun.singular(singular),
                        objectSpecification)
                );

    }

    // -- HELEPR

    private String getSingularFallbackNoun(final ObjectSpecification spec) {
        return spec.getFeatureIdentifier().getClassNaturalName();
    }

}
