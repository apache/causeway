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
package org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.object.namedquery;

import java.util.List;

import org.apache.isis.core.metamodel.facetapi.Facet;


/**
 * In the standard JPA Model, corresponds to annotating the class with either
 * {@link javax.persistence.NamedQuery} or
 * {@link javax.persistence.NamedQueries}.
 * <p>
 * For a {@link javax.persistence.NamedQuery}, returns a singleton list of
 * {@link org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.object.namedquery.NamedQuery}.
 * For a {@link javax.persistence.NamedQueries}, returns a list of
 * {@link org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.object.namedquery.NamedQuery}s.
 * <p>
 * In both cases, mapping is as follows
 * <ul>
 * <li>{@link javax.persistence.NamedQuery#name()} ->
 * {@link org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.object.namedquery.NamedQuery#getName()
 * getName()} property of JPA Object Store's own
 * {@link org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.object.namedquery.NamedQuery
 * NamedQuery} value object</li>
 * <li>{@link javax.persistence.NamedQuery#query()} ->
 * {@link org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.object.namedquery.NamedQuery#getQuery()}
 * property of JPA Object Store's own
 * {@link org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.object.namedquery.NamedQuery
 * NamedQuery} value object</li>
 * <li>{@link javax.persistence.NamedQuery#hints()} -> (no corresponding
 * attribute or facet)</li>
 * </ul>
 */
public interface JpaNamedQueryFacet extends Facet {

    /**
     * Returns an immutable {@link List}.
     */
    List<org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.object.namedquery.NamedQuery> getNamedQueries();
}
