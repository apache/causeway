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
package org.apache.isis.core.metamodel.facets.all.i18n;


import com.google.common.base.Strings;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.facets.ContributeeMemberFacetFactory;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjectorAware;

public class TranslationFacetFactory extends FacetFactoryAbstract implements ContributeeMemberFacetFactory, ServicesInjectorAware {

    private ServicesInjector servicesInjector;

    private TranslationService translationService;

    public TranslationFacetFactory() {
        super(FeatureType.EVERYTHING);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        final FacetHolder facetHolder = processClassContext.getFacetHolder();
        if(facetHolder instanceof IdentifiedHolder) {
            final IdentifiedHolder holder = (IdentifiedHolder) facetHolder;
            final String context = holder.getIdentifier().toClassIdentityString();
            translateName(holder, context);
            translateDescription(holder, context);
        }
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        final IdentifiedHolder holder = processMethodContext.getFacetHolder();

        final String context = holder.getIdentifier().toClassAndNameIdentityString();
        translateName(holder, context);
        translateDescription(holder, context);
    }

    @Override
    public void processParams(final ProcessParameterContext processParameterContext) {
        final IdentifiedHolder holder = processParameterContext.getFacetHolder();

        final String context = holder.getIdentifier().toFullIdentityString();
        translateName(holder, context);
        translateDescription(holder, context);
    }

    @Override
    public void process(final ProcessContributeeMemberContext processMemberContext) {
        final IdentifiedHolder holder = processMemberContext.getFacetHolder();

        final String context = holder.getIdentifier().toClassAndNameIdentityString();
        translateName(holder, context);
        translateDescription(holder, context);
    }

    // //////////////////////////////////////

    void translateName(final IdentifiedHolder facetHolder, final String context) {
        final NamedFacet facet = facetHolder.getFacet(NamedFacet.class);
        if(facet == null) {
            // not expected...
            return;
        }
        final String originalText = facet.value();
        if (isNullOrEmptyWhenTrimmed(originalText)) {
            // not expected...
            return;
        }

        final TranslationService translationService = lookupTranslationService();
        FacetUtil.addFacet(new NamedFacetTranslated(context, originalText, translationService, facetHolder));
    }

    void translateDescription(final FacetHolder facetHolder, final String context) {

        final IdentifiedHolder holder = (IdentifiedHolder) facetHolder;
        final DescribedAsFacet facet = facetHolder.getFacet(DescribedAsFacet.class);
        if(facet == null) {
            return;
        }
        final String originalText = facet.value();
        if (isNullOrEmptyWhenTrimmed(originalText)) {
            return;
        }

        final TranslationService translationService = lookupTranslationService();
        FacetUtil.addFacet(new DescribedAsFacetTranslated(context, originalText, translationService, holder));

    }

    private boolean isNullOrEmptyWhenTrimmed(final String originalText) {
        return originalText == null || Strings.isNullOrEmpty(originalText.trim());
    }

    // //////////////////////////////////////

    /**
     * Looks up from {@link org.apache.isis.core.metamodel.runtimecontext.ServicesInjector}.
     *
     * <p>
     *     There is guaranteed to be an instance because <code>TranslationServicePo</code> (in runtime) is annotated
     *     as a {@link org.apache.isis.applib.annotation.DomainService &#64;DomainService}.
     * </p>
     */
    TranslationService lookupTranslationService() {
        if(translationService == null) {
            translationService = servicesInjector.lookupService(TranslationService.class);
        }
        return translationService;
    }

    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        this.servicesInjector = servicesInjector;
    }
}
