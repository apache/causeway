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
package org.apache.isis.objectstore.jdo.metamodel.facets.object.query;

import java.util.List;

import javax.jdo.annotations.Query;

import org.apache.isis.metamodel.facetapi.Facet;


/**
 * In the standard JDO Model, corresponds to annotating the class with either
 * {@link javax.jdo.annotations.Query} or
 * {@link javax.jdo.annotations.Queries}.
 * <p>
 * For a {@link javax.jdo.annotations.Query}, returns a singleton list of
 * {@link JdoNamedQuery}.
 * For a {@link javax.jdo.annotations.Queries}, returns a list of
 * {@link JdoNamedQuery}s.
 * <p>
 * In both cases, mapping is as follows
 * <ul>
 * <li>{@link javax.jdo.annotations.Query#name()} ->
 * {@link JdoNamedQuery#getName()
 * getName()} property of JDO Object Store's own
 * {@link JdoNamedQuery} value object</li>
 * <li>{@link javax.jdo.annotations.Query#value()} ->
 * {@link JdoNamedQuery#getQuery()}
 * property of JDO Object Store's own
 * {@link JdoNamedQuery} value object</li>
 * </ul>
 *
 * <p>
 * Optional attributes of the {@link Query} annotation are not currently mapped.
 */
public interface JdoQueryFacet extends Facet {

    /**
     * Returns an immutable {@link List}.
     */
    List<JdoNamedQuery> getNamedQueries();
}
