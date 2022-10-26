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
package org.apache.causeway.core.metamodel.facets.object.logicaltype;

import javax.inject.Named;

import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.facetapi.Facet;

/**
 * Corresponds to the value of {@link Named#value()},
 * that specifies the <i>logical type name</i>
 * of a domain object or interface or abstract type.
 * <p>
 * The <i>logical type name</i> must be unique, among non-abstract classes, but
 * is allowed to be shared with interfaces and abstract classes.
 * <p>
 * Given an arbitrary type hierarchy
 * <pre>
 * class A
 * |__class B
 *    |__abstract class C
 *       |__interface D
 *          |__interface F
 *       |__interface E
 * </pre>
 * Class A and B must have different logical types. Types B, C, D, E, F are
 * in principle allowed to share the same logical type-name. In which case reverse
 * lookup from logical-type-name to type must always resolve the most specific one (B).
 */
public interface AliasedFacet extends Facet {

    Can<LogicalType> getAliases();

}
