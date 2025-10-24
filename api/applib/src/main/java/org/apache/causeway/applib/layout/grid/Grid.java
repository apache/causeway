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
package org.apache.causeway.applib.layout.grid;

import java.util.stream.Stream;

import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.layout.component.ActionLayoutData;
import org.apache.causeway.applib.layout.component.CollectionLayoutData;
import org.apache.causeway.applib.layout.component.DomainObjectLayoutData;
import org.apache.causeway.applib.layout.component.FieldSet;
import org.apache.causeway.applib.layout.component.PropertyLayoutData;
import org.apache.causeway.applib.services.layout.LayoutService;

/**
 * All top-level page layout classes should implement this interface.
 *
 * <p>It is used by the {@link LayoutService} as a common based type for any layouts read in from XML.
 *
 * @since 1.x revised for 4.0 {@index}
 */
@Programmatic
public interface Grid {

    Class<?> domainClass();

    /**
     * Indicates whether or not this grid is a fallback.
     * {@code True}, if this Grid originates from
     * {@link org.apache.causeway.applib.services.grid.GridSystemService#defaultGrid(Class)}.
     * <p>
     * Governs meta-model facet precedence, that is,
     * facets from annotations should overrule those from fallback XML grids.
     */
    boolean isFallback();
    boolean isNormalized();

    Stream<PropertyLayoutData> streamPropertyLayoutData();
    Stream<CollectionLayoutData> streamCollectionLayoutData();
    Stream<ActionLayoutData> streamActionLayoutData();

    interface Visitor {
        default void visit(final DomainObjectLayoutData domainObjectLayoutData) {}
        default void visit(final ActionLayoutData actionLayoutData) {}
        default void visit(final PropertyLayoutData propertyLayoutData) {}
        default void visit(final CollectionLayoutData collectionLayoutData) {}
        default void visit(final FieldSet fieldSet) {}
    }

    void visit(final Grid.Visitor visitor);
}
