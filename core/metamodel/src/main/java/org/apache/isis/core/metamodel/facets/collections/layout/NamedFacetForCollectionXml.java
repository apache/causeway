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

import org.apache.isis.applib.layout.component.CollectionLayoutData;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.all.i8n.NounForms;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacetAbstract;

public class NamedFacetForCollectionXml
extends NamedFacetAbstract {

    public static Optional<NamedFacet> create(
            final CollectionLayoutData collectionLayout,
            final FacetHolder holder) {
        if(collectionLayout == null) {
            return Optional.empty();
        }
        final String named = _Strings.emptyToNull(collectionLayout.getNamed());
        final Boolean escaped = collectionLayout.getNamedEscaped();
        return named != null
                ? Optional.of(new NamedFacetForCollectionXml(named, escaped == null || escaped, holder))
                : Optional.empty();
    }

    private NamedFacetForCollectionXml(
            final String pluralName,
            final boolean escaped,
            final FacetHolder holder) {

        super(NounForms.preferredPlural().plural(pluralName).build(), escaped, holder);
    }

}
