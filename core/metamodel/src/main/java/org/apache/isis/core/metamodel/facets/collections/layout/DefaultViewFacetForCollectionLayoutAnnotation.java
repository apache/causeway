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
package org.apache.isis.core.metamodel.facets.collections.layout;

import java.util.Optional;

import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.collections.collection.defaultview.DefaultViewFacet;
import org.apache.isis.core.metamodel.facets.collections.collection.defaultview.DefaultViewFacetAbstract;

public class DefaultViewFacetForCollectionLayoutAnnotation extends DefaultViewFacetAbstract {

    public static DefaultViewFacet create(
            final Optional<CollectionLayout> collectionLayoutIfAny,
            final IsisConfiguration configuration,
            final FacetHolder holder) {

        final String defaultView = collectionLayoutIfAny
                .map(CollectionLayout::defaultView)
                .filter(_Strings::isNotEmpty)
                .orElseGet(()->configuration.getApplib().getAnnotation().getCollectionLayout().getDefaultView().toNameLower());
        return new DefaultViewFacetForCollectionLayoutAnnotation(defaultView, holder);
    }

    private DefaultViewFacetForCollectionLayoutAnnotation(final String value, final FacetHolder holder) {
        super(value, holder);
    }
}
