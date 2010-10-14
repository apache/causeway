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


package org.apache.isis.runtime.i18n.resourcebundle;

import org.apache.isis.applib.Identifier;
import org.apache.isis.metamodel.facetdecorator.FacetDecoratorAbstract;
import org.apache.isis.metamodel.facets.Facet;
import org.apache.isis.metamodel.facets.FacetHolder;
import org.apache.isis.metamodel.facets.help.HelpFacet;
import org.apache.isis.metamodel.facets.naming.describedas.DescribedAsFacet;
import org.apache.isis.metamodel.facets.naming.named.NamedFacet;
import org.apache.isis.metamodel.spec.identifier.Identified;
import org.apache.isis.runtime.i18n.resourcebundle.facets.DescribedAsFacetWrapI18n;
import org.apache.isis.runtime.i18n.resourcebundle.facets.HelpFacetWrapI18n;
import org.apache.isis.runtime.i18n.resourcebundle.facets.NamedFacetWrapI18n;


public class I18nFacetDecorator extends FacetDecoratorAbstract {
    private final I18nManager i18nManager;

    public I18nFacetDecorator(final I18nManager manager) {
        i18nManager = manager;
    }

    public Facet decorate(final Facet facet, FacetHolder requiredHolder) {
    	if (!(requiredHolder instanceof Identified)) {
            return null;
        }
        
        Identified identified = (Identified) requiredHolder;
        final Identifier identifier = identified.getIdentifier();

        final Class<?> facetType = facet.facetType();
        if (facetType == NamedFacet.class) {
            final String i18nName = i18nManager.getName(identifier);
            if (i18nName == null) {
                return null;
            }
            NamedFacetWrapI18n decoratingFacet = new NamedFacetWrapI18n(i18nName, facet.getFacetHolder());
			return replaceFacetWithDecoratingFacet(facet, decoratingFacet, requiredHolder);
        }
        if (facetType == DescribedAsFacet.class) {
            final String i18nDescription = i18nManager.getDescription(identifier);
            if (i18nDescription == null) {
                return null;
            }
            DescribedAsFacetWrapI18n decoratingFacet = new DescribedAsFacetWrapI18n(i18nDescription, facet.getFacetHolder());
            return replaceFacetWithDecoratingFacet(facet, decoratingFacet, requiredHolder);
        }
        if (facetType == HelpFacet.class) {
            final String i18nHelp = i18nManager.getHelp(identifier);
            if (i18nHelp == null) {
                return null;
            }
            HelpFacetWrapI18n decoratingFacet = new HelpFacetWrapI18n(i18nHelp, facet.getFacetHolder());
            return replaceFacetWithDecoratingFacet(facet, decoratingFacet, requiredHolder);
        }
        return facet;
    }

    public Class<? extends Facet>[] getFacetTypes() {
        return new Class[] { NamedFacet.class, DescribedAsFacet.class, HelpFacet.class };
    }
}

