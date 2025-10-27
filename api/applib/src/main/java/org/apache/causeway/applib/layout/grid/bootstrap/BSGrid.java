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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.layout.component.ActionLayoutData;
import org.apache.causeway.applib.layout.component.CollectionLayoutData;
import org.apache.causeway.applib.layout.component.PropertyLayoutData;
import org.apache.causeway.applib.mixins.dto.Dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is the top-level for rendering the domain object's properties, collections and actions.
 * It simply consists of a number of rows.
 *
 * @since 1.x revised for 4.0 {@index}
 */
@XmlRootElement(name = "grid")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "grid", propOrder = {"rows", "metadataErrors"})
public final class BSGrid implements BSElement, Dto, BSRowOwner {
    private static final long serialVersionUID = 1L;

    @XmlTransient @Getter @Accessors(fluent=true)  @Setter private Class<?> domainClass;

    /**
     * Indicates whether or not this grid is a fallback.
     * {@code True}, if this Grid originates from
     * {@link org.apache.causeway.applib.services.grid.GridSystemService#defaultGrid(Class)}.
     * <p>
     * Governs meta-model facet precedence, that is,
     * facets from annotations should overrule those from fallback XML grids.
     */
    @XmlTransient @Getter @Setter private boolean fallback;

    /**
     * Arbitrary additional 'runtime' data attributed to this grid,
     * but not part of the DTO specification.
     * @since 4.0
     */
    @XmlTransient @Getter @Accessors(fluent=true) private final Map<String, Serializable> attributes = Map.of();

    @XmlTransient @Getter @Setter private boolean normalized;

    @XmlAttribute(required = false)
    @Getter @Setter private String cssClass;

    @XmlElement(name = "row", required = true)
    @Getter private final List<BSRow> rows = new ArrayList<>();
    /**
     * For diagnostics; populated by the framework if and only if a metadata error.
     */
    @XmlElement(name = "metadataError", required = false)
    @Getter private final List<String> metadataErrors = new ArrayList<>();

    public void visit(final BSElementVisitor visitor) {
        new BSWalker(this).walk(visitor);
    }

    public Stream<PropertyLayoutData> streamPropertyLayoutData() {
        final var properties = new ArrayList<PropertyLayoutData>();
        visit(new BSElementVisitor() {
            @Override
            public void visit(final PropertyLayoutData propertyLayoutData) {
                properties.add(propertyLayoutData);
            }
        });
        return properties.stream();
    }

    public Stream<CollectionLayoutData> streamCollectionLayoutData() {
        final var collections = new ArrayList<CollectionLayoutData>();
        visit(new BSElementVisitor() {
            @Override
            public void visit(final CollectionLayoutData collectionLayoutData) {
                collections.add(collectionLayoutData);
            }
        });
        return collections.stream();
    }

    public Stream<ActionLayoutData> streamActionLayoutData() {
        final var actions = new ArrayList<ActionLayoutData>();
        visit(new BSElementVisitor() {
            @Override
            public void visit(final ActionLayoutData actionLayoutData) {
                actions.add(actionLayoutData);
            }
        });
        return actions.stream();
    }

}
