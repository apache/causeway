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

import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.postprocessors.ObjectSpecificationPostProcessorAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.val;

public class TranslationPostProcessor
extends ObjectSpecificationPostProcessorAbstract {

    @Override
    protected void doPostProcess(ObjectSpecification objectSpecification) {
        addFacetsFor(objectSpecification);
    }

    @Override
    protected void doPostProcess(ObjectSpecification objectSpecification, ObjectAction act) {
        addFacetsFor(act);
    }

    @Override
    protected void doPostProcess(ObjectSpecification objectSpecification, ObjectAction objectAction, ObjectActionParameter param) {
        addFacetsFor(param);
    }

    @Override
    protected void doPostProcess(ObjectSpecification objectSpecification, OneToOneAssociation prop) {
        addFacetsFor(prop);
    }

    @Override
    protected void doPostProcess(ObjectSpecification objectSpecification, OneToManyAssociation coll) {
        addFacetsFor(coll);

    }

    // -- HELPER

    private void addFacetsFor(final IdentifiedHolder identifiedHolder) {
        val translationContext = TranslationContext.forTranslationContextHolder(identifiedHolder.getIdentifier());
        translateName(identifiedHolder, translationContext);
        translateDescription(identifiedHolder, translationContext);
    }

    void translateName(final IdentifiedHolder identifiedHolder, final TranslationContext translationContext) {
        val namedFacet = identifiedHolder.getFacet(NamedFacet.class);
        if(namedFacet == null) {
            // not expected...
            return;
        }
        final String originalText = namedFacet.value();
        if (isNullOrEmptyWhenTrimmed(originalText)) {
            // not expected...
            return;
        }

        FacetUtil.addFacetIfPresent(
                new NamedFacetTranslated(
                        namedFacet,
                        translationContext,
                        originalText,
                        translationService,
                        identifiedHolder));
    }

    void translateDescription(
            final IdentifiedHolder identifiedHolder,
            final TranslationContext translationContext) {

        val describedAsFacet = identifiedHolder.getFacet(DescribedAsFacet.class);
        if(describedAsFacet == null) {
            return;
        }
        final String originalText = describedAsFacet.value();
        if (isNullOrEmptyWhenTrimmed(originalText)) {
            return;
        }

        FacetUtil.addFacetIfPresent(
                new DescribedAsFacetTranslated(
                        translationContext,
                        originalText,
                        translationService,
                        identifiedHolder));
    }

    static boolean isNullOrEmptyWhenTrimmed(final String originalText) {
        return originalText == null || _Strings.isNullOrEmpty(originalText.trim());
    }

    @Inject TranslationService translationService;

}
