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

package org.apache.isis.core.metamodel.facets.object.parented;

import java.util.List;
import java.util.Set;

import org.apache.isis.core.metamodel.facets.MarkerFacet;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacet;

/**
 * Indicates that this class is parented, that is, wholly contained within a
 * larger object.
 * 
 * <p>
 * There are two classes of object that are parented:
 * <ul>
 * <li>Aggregated objects</li>
 * <li>Internal collections</li>
 * </ul>
 * 
 * <p>
 * In the standard Apache Isis Programming Model, aggregated objects typically corresponds to
 * applying the <tt>@Aggregated</tt> annotation at the class level.  
 * In terms of an analogy, aggregated is similar to Hibernate's component types
 * (for larger mutable in-line objects) or to Hibernate's user-defined types
 * (for smaller immutable values).
 * 
 * <p>
 * Internal collections are the {@link List}s, {@link Set}s etc that hold references to
 * other root entities.
 * 
 * <p>
 * The object may or may not be {@link ImmutableFacet immutable}.  If an aggregated entity, then it may
 * reference regular entity domain objects or other aggregated objects.
 */
public interface ParentedFacet extends MarkerFacet {

}
