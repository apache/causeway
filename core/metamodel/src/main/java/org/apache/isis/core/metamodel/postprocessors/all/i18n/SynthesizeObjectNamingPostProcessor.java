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
package org.apache.isis.core.metamodel.postprocessors.all.i18n;


import javax.inject.Inject;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.commons.StringExtensions;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.all.i8n.noun.NounForm;
import org.apache.isis.core.metamodel.facets.all.named.ObjectNamedFacet;
import org.apache.isis.core.metamodel.postprocessors.ObjectSpecificationPostProcessorAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.val;

public class SynthesizeObjectNamingPostProcessor
extends ObjectSpecificationPostProcessorAbstract {

    @Inject
    public SynthesizeObjectNamingPostProcessor(final MetaModelContext metaModelContext) {
        super(metaModelContext);
    }

    @Override
    protected void doPostProcess(final ObjectSpecification objectSpecification) {

        val topRank = objectSpecification
        .lookupFacet(ObjectNamedFacet.class)
        .flatMap(Facet::getSharedFacetRanking)
        .map(facetRanking->facetRanking.getTopRank(ObjectNamedFacet.class))
        .orElse(Can.empty())
        .reverse(); // historically last have higher precedence, so when reverted we can use findFirst logic

        val singular = topRank
                .stream()
                .filter(objectNamedFacet->objectNamedFacet.getSupportedNounForms().contains(NounForm.SINGULAR))
                .findFirst()
                .map(ObjectNamedFacet::singular)
                .filter(_Strings::isNotEmpty)
                .orElseGet(()->getSingularFallbackNoun(objectSpecification));

        val plural = topRank
                .stream()
                .filter(objectNamedFacet->objectNamedFacet.getSupportedNounForms().contains(NounForm.PLURAL))
                .findFirst()
                .map(ObjectNamedFacet::plural)
                .filter(_Strings::isNotEmpty)
                .orElseGet(()->getPluralFallbackNoun(singular));

//        FacetUtil.addFacet(
//                new ObjectNamedFacetSynthesized(
//                        NounForms.builder()
//                            .singular(singular)
//                            .plural(plural)
//                            .build(),
//                        objectSpecification)
//                );

    }

    @Override
    protected void doPostProcess(final ObjectSpecification objectSpecification, final ObjectAction act) {
    }

    @Override
    protected void doPostProcess(final ObjectSpecification objectSpecification, final ObjectAction objectAction, final ObjectActionParameter param) {
    }

    @Override
    protected void doPostProcess(final ObjectSpecification objectSpecification, final OneToOneAssociation prop) {
    }

    @Override
    protected void doPostProcess(final ObjectSpecification objectSpecification, final OneToManyAssociation coll) {
    }

    // -- HELEPR

    private String getSingularFallbackNoun(final ObjectSpecification spec) {
        return spec.getFeatureIdentifier().getClassNaturalName();
    }

    private String getPluralFallbackNoun(final String singular) {
        return StringExtensions.asPluralName(singular);
    }

}
