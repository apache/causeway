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

package org.apache.isis.metamodel.facets.object.value;

import org.apache.isis.metamodel.facetapi.MultiTypedFacet;
import org.apache.isis.metamodel.facets.MarkerFacet;

/**
 * Indicates that this class has value semantics.
 *
 * <p>
 * In the standard Apache Isis Programming Model, corresponds to the
 * <tt>@Value</tt> annotation. However, note that value semantics is just a
 * convenient term for a number of mostly optional semantics all of which are
 * defined elsewhere.
 */
public interface ValueFacet extends MarkerFacet, MultiTypedFacet {

}
