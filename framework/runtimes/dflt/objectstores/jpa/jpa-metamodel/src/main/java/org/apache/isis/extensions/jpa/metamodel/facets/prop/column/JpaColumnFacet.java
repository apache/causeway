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
package org.apache.isis.extensions.jpa.metamodel.facets.prop.column;

import javax.persistence.Column;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.named.NamedFacet;

/**
 * Corresponds to a property with the {@link Column} annotation.
 * <p>
 * Maps onto the information in {@link Column} as follows:
 * <ul>
 * <li>{@link Column#name()} -> {@link #name()}. Note: not mapped onto a
 * {@link NamedFacet} subclass because this is a physical name, not a logical
 * name.</li>
 * <li>{@link Column#unique()} -> (no corresponding attribute or facet)</li>
 * <li>{@link Column#nullable()} ->
 * {@link MandatoryFacetDerivedFromJpaColumnAnnotation} or
 * {@link OptionalFacetDerivedFromJpaColumnAnnotation}</li>
 * <li>{@link Column#insertable()} -> (no corresponding attribute or facet)</li>
 * <li>{@link Column#updatable()} -> (no corresponding attribute or facet)</li>
 * <li>{@link Column#columnDefinition()} -> (no corresponding attribute or
 * facet)</li>
 * <li>{@link Column#table()} -> (no corresponding attribute or facet)</li>
 * <li>{@link Column#length()} ->
 * {@link MaxLengthFacetDerivedFromJpaColumnAnnotation}</li>
 * <li>{@link Column#precision()} -> (no corresponding attribute or facet)</li>
 * <li>{@link Column#scale()} -> (no corresponding attribute or facet)</li>
 * </ul>
 */
public interface JpaColumnFacet extends Facet {

    String name();

}
