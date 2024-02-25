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
import org.apache.causeway.applib.annotation.TableDecorator;
import org.apache.causeway.commons.internal.base._Optionals;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.object.tabledec.TableDecoratorFacet;
import org.apache.causeway.core.metamodel.facets.object.tabledec.TableDecoratorFacetAbstract;

public class TableDecoratorFacetForCollectionLayoutAnnotation
extends TableDecoratorFacetAbstract {

    TableDecoratorFacetForCollectionLayoutAnnotation(
            final Class<? extends TableDecorator> value, final FacetHolder holder) {
        super(value, holder);
    }

    public static Optional<TableDecoratorFacet> create(
            final Optional<CollectionLayout> collectionLayoutIfAny,
            final FacetHolder holder) {

        return _Optionals.orNullable(

        collectionLayoutIfAny
        .map(CollectionLayout::tableDecorator)
        .<TableDecoratorFacet>map(tableDecorator -> {
                return new TableDecoratorFacetForCollectionLayoutAnnotation(
                        tableDecorator, holder);
        })
        ,
        () -> new TableDecoratorFacetFromConfiguration(
                holder.getConfiguration().getApplib().getAnnotation().getCollectionLayout().getTableDecorator(), holder));

    }
}
