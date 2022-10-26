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
 *
 */
package org.apache.causeway.core.metamodel.facets.collections.layout.tabledec;

import java.util.Optional;

import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.commons.internal.base._Optionals;
import org.apache.causeway.core.config.metamodel.facets.CollectionLayoutConfigOptions;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.SingleValueFacet;

/**
 * Determines how dependent parameter values should be updated,
 * if one of the earlier parameter values is changed.
 * <p>
 * Corresponds to annotating the action method {@link Parameter#dependentDefaultsPolicy()}.
 *
 * @since 2.0
 */
public interface CollectionLayoutTableDecorationFacet
extends SingleValueFacet<CollectionLayoutConfigOptions.TableDecoration> {

    static Optional<CollectionLayoutTableDecorationFacet> create(
            final Optional<CollectionLayout> collectionLayoutIfAny,
            final FacetHolder holder) {

        final CollectionLayoutConfigOptions.TableDecoration defaultPolicyFromConfig =
                CollectionLayoutConfigOptions.tableDecoration(holder.getConfiguration());

        return _Optionals.orNullable(

        collectionLayoutIfAny
        .map(CollectionLayout::tableDecoration)
        .<CollectionLayoutTableDecorationFacet>map(tableDecoration -> {
            switch (tableDecoration) {
            case NONE:
                return new CollectionLayoutTableDecorationFacetForCollectionLayoutAnnotation(
                        CollectionLayoutConfigOptions.TableDecoration.NONE, holder);
            case DATATABLES_NET:
                return new CollectionLayoutTableDecorationFacetForCollectionLayoutAnnotation(
                        CollectionLayoutConfigOptions.TableDecoration.DATATABLES_NET, holder);
            case NOT_SPECIFIED:
            case AS_CONFIGURED:
                return new CollectionLayoutTableDecorationFacetForCollectionLayoutAnnotation(defaultPolicyFromConfig, holder);
            default:
            }
            throw new IllegalStateException("tableDecoration '" + tableDecoration + "' not recognised");
        })
        ,
        () -> new CollectionLayoutTableDecorationFacetFromConfiguration(defaultPolicyFromConfig, holder));

    }
}
