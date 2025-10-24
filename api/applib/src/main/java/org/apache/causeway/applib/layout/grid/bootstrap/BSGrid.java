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
package org.apache.causeway.applib.layout.grid.bootstrap;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.causeway.applib.layout.component.ActionLayoutData;
import org.apache.causeway.applib.layout.component.CollectionLayoutData;
import org.apache.causeway.applib.layout.component.PropertyLayoutData;
import org.apache.causeway.applib.layout.grid.Grid;
import org.apache.causeway.applib.mixins.dto.Dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is the top-level for rendering the domain object's properties, collections and actions.
 * It simply consists of a number of rows.
 *
 * @since 1.x revised for 4.0 {@index}
 */
@RequiredArgsConstructor
public final class BSGrid implements Grid, BSElement, Dto, BSRowOwner {

    private static final long serialVersionUID = 1L;

    @Getter @Accessors(fluent=true) private final Class<?> domainClass;

    @Getter @Setter private boolean fallback;
    @Getter @Setter private boolean normalized;
    @Getter @Setter private String cssClass;

    @Getter private final List<BSRow> rows = new ArrayList<>();
    /**
     * For diagnostics; populated by the framework if and only if a metadata error.
     */
    @Getter private final List<String> metadataErrors = new ArrayList<>();

    @Override
    public void visit(final Grid.Visitor visitor) {
        new BSWalker(this).walk(visitor);
    }

    @Override
    public Stream<PropertyLayoutData> streamPropertyLayoutData() {
        final var properties = new ArrayList<PropertyLayoutData>();
        visit(new BSElement.Visitor() {
            @Override
            public void visit(final PropertyLayoutData propertyLayoutData) {
                properties.add(propertyLayoutData);
            }
        });
        return properties.stream();
    }

    @Override
    public Stream<CollectionLayoutData> streamCollectionLayoutData() {
        final var collections = new ArrayList<CollectionLayoutData>();
        visit(new BSElement.Visitor() {
            @Override
            public void visit(final CollectionLayoutData collectionLayoutData) {
                collections.add(collectionLayoutData);
            }
        });
        return collections.stream();
    }

    @Override
    public Stream<ActionLayoutData> streamActionLayoutData() {
        final var actions = new ArrayList<ActionLayoutData>();
        visit(new BSElement.Visitor() {
            @Override
            public void visit(final ActionLayoutData actionLayoutData) {
                actions.add(actionLayoutData);
            }
        });
        return actions.stream();
    }

}
