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


package org.apache.isis.runtime.system.facetdecorators;

import java.util.HashMap;
import java.util.Map;

import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetdecorator.FacetDecorator;
import org.apache.isis.metamodel.facets.Facet;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.i18n.resourcebundle.I18nFacetDecorator;
import org.apache.isis.runtime.i18n.resourcebundle.ResourceBasedI18nManager;
import org.apache.isis.runtime.transaction.facetdecorator.standard.StandardTransactionFacetDecorator;


/**
 * @deprecated
 */
@Deprecated
public class ReflectionPeerBuilder implements ApplicationScopedComponent {
    private final Map<Class<? extends Facet>, FacetDecorator> facetDecoratorsByFacetType = new HashMap<Class<? extends Facet>, FacetDecorator>();

    // ////////////////////////////////////////////////////////////////
    // init, shutdown
    // ////////////////////////////////////////////////////////////////

    public void init() {
        // TODO these need to be added via configuration
        IsisConfiguration configuration = IsisContext.getConfiguration();
        final ResourceBasedI18nManager resourceBasedI18nManager = new ResourceBasedI18nManager(configuration);
        resourceBasedI18nManager.init();
        addToFacetDecoratorsMap(new I18nFacetDecorator(resourceBasedI18nManager));
        addToFacetDecoratorsMap(new StandardTransactionFacetDecorator(configuration));
    }

    private void addToFacetDecoratorsMap(final FacetDecorator facetDecorator) {
        final Class<? extends Facet>[] forFacetTypes = facetDecorator.getFacetTypes();
        for (int i = 0; i < forFacetTypes.length; i++) {
            facetDecoratorsByFacetType.put(forFacetTypes[i], facetDecorator);
        }
    }

    public void shutdown() {}

    // ////////////////////////////////////////////////////////////////
    // Debug
    // ////////////////////////////////////////////////////////////////

    public void debugData(final DebugString str) {
        str.appendTitle("Reflective peers");
        for(final Class<? extends Facet> key: facetDecoratorsByFacetType.keySet()) {
            str.appendln(key.getName(), facetDecoratorsByFacetType.get(key));
        }
        str.appendln();
    }

}
