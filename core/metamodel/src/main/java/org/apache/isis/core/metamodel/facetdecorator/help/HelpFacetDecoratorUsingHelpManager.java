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

package org.apache.isis.core.metamodel.facetdecorator.help;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.facetdecorator.FacetDecoratorAbstract;
import org.apache.isis.core.metamodel.facets.all.help.HelpFacet;

public class HelpFacetDecoratorUsingHelpManager extends FacetDecoratorAbstract implements HelpFacetDecorator {
    private final HelpManager helpManager;

    public HelpFacetDecoratorUsingHelpManager(final HelpManager manager) {
        helpManager = manager;
    }

    @Override
    public Facet decorate(final Facet facet, final FacetHolder facetHolder) {
        if (facet.facetType() != HelpFacet.class) {
            return facet;
        }

        if (!(facetHolder instanceof IdentifiedHolder)) {
            return null;
        }

        final IdentifiedHolder identifiedHolder = (IdentifiedHolder) facetHolder;
        return decorateWithHelpFacet(facet, identifiedHolder);
    }

    private Facet decorateWithHelpFacet(final Facet facet, final IdentifiedHolder identifiedHolder) {
        final Identifier identifier = identifiedHolder.getIdentifier();

        final String helpText = helpManager.getHelpText(identifier);
        if (helpText != null) {
            final HelpFacetLookedUpViaHelpManager decoratingFacet = new HelpFacetLookedUpViaHelpManager(helpText, facet.getFacetHolder());
            identifiedHolder.addFacet(decoratingFacet);
            return decoratingFacet;
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Facet>[] getFacetTypes() {
        return new Class[] { HelpFacet.class };
    }
}
