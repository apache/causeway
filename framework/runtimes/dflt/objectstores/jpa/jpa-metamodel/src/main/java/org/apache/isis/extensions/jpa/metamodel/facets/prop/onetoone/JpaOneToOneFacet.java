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
package org.apache.isis.extensions.jpa.metamodel.facets.prop.onetoone;

import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.apache.isis.core.metamodel.facets.MarkerFacet;


/**
 * Corresponds to the property with the {@link ManyToOne} annotation.
 * <p>
 * Maps onto the information in {@link ManyToOne} as follows:
 * <ul>
 * <li>{@link OneToOne#targetEntity()} -> (no corresponding attribute or facet)</li>
 * <li>{@link OneToOne#cascade()} -> (no corresponding attribute or facet)</li>
 * <li>{@link OneToOne#fetch()} ->
 * {@link JpaFetchTypeFacetDerivedFromJpaOneToOneAnnotation}</li>
 * <li>{@link OneToOne#optional()} ->
 * {@link MandatoryFacetDerivedFromJpaOneToOneAnnotation} or
 * {@link OptionalFacetDerivedFromJpaOneToOneAnnotation}</li>
 * <li>{@link OneToOne#mappedBy()} -> (no corresponding attribute or facet)</li>
 * </ul>
 */
public interface JpaOneToOneFacet extends MarkerFacet {


}
