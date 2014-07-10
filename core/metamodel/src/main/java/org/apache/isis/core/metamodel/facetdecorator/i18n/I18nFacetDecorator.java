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

package org.apache.isis.core.metamodel.facetdecorator.i18n;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.facetdecorator.FacetDecoratorAbstract;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.all.help.HelpFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facetdecorator.i18n.internal.DescribedAsFacetWrapI18n;
import org.apache.isis.core.metamodel.facetdecorator.i18n.internal.HelpFacetWrapI18n;
import org.apache.isis.core.metamodel.facetdecorator.i18n.internal.NamedFacetWrapI18n;

public class I18nFacetDecorator extends FacetDecoratorAbstract {
    private final I18nManager i18nManager;

    public I18nFacetDecorator(final I18nManager manager) {
        i18nManager = manager;
    }

    @Override
    public Facet decorate(final Facet facet, final FacetHolder facetHolder) {
        if (!(facetHolder instanceof IdentifiedHolder)) {
            return null;
        }

        final IdentifiedHolder identifiedHolder = (IdentifiedHolder) facetHolder;

        final Class<?> facetType = facet.facetType();
        if (facetType == NamedFacet.class) {
            return decorateWithNamedFacet(facet, identifiedHolder);
        }
        if (facetType == DescribedAsFacet.class) {
            return decorateWithDescribedAsFacet(facet, identifiedHolder);
        }
        if (facetType == HelpFacet.class) {
            return decorateWithHelpFacet(facet, identifiedHolder);
        }
        return null;
    }

    private Facet decorateWithNamedFacet(final Facet facet, final IdentifiedHolder identifiedHolder) {

        final Identifier identifier = identifiedHolder.getIdentifier();
        final String i18nName = i18nManager.getName(identifier);
        if (i18nName == null) {
            return null;
        }
        final NamedFacetWrapI18n decoratingFacet = new NamedFacetWrapI18n(i18nName, facet.getFacetHolder());
        identifiedHolder.addFacet(decoratingFacet);
        return decoratingFacet;
    }

    private Facet decorateWithDescribedAsFacet(final Facet facet, final IdentifiedHolder identifiedHolder) {
        final Identifier identifier = identifiedHolder.getIdentifier();
        final String i18nDescription = i18nManager.getDescription(identifier);
        if (i18nDescription == null) {
            return null;
        }
        final DescribedAsFacetWrapI18n decoratingFacet = new DescribedAsFacetWrapI18n(i18nDescription, facet.getFacetHolder());
        identifiedHolder.addFacet(decoratingFacet);
        return decoratingFacet;
    }

    private Facet decorateWithHelpFacet(final Facet facet, final IdentifiedHolder identifiedHolder) {
        final Identifier identifier = identifiedHolder.getIdentifier();
        final String i18nHelp = i18nManager.getHelp(identifier);
        if (i18nHelp == null) {
            return null;
        }
        final HelpFacetWrapI18n decoratingFacet = new HelpFacetWrapI18n(i18nHelp, facet.getFacetHolder());
        identifiedHolder.addFacet(decoratingFacet);
        return decoratingFacet;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Facet>[] getFacetTypes() {
        return new Class[] { NamedFacet.class, DescribedAsFacet.class, HelpFacet.class };
    }
}
