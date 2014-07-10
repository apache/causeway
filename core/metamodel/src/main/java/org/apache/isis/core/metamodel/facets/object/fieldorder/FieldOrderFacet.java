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

package org.apache.isis.core.metamodel.facets.object.fieldorder;

import org.apache.isis.core.metamodel.facets.SingleStringValueFacet;
import org.apache.isis.core.metamodel.facets.members.order.MemberOrderFacet;
import org.apache.isis.core.metamodel.facets.object.actionorder.ActionOrderFacet;

/**
 * (One of the) mechanism(s) for determining the order in which the properties
 * and collections of the object should be rendered.
 * 
 * <p>
 * In the standard Apache Isis Programming Model, typically corresponds to the
 * <tt>fieldOrder</tt> method which returns a comma-separated list of action
 * names. An alternative (and preferred, because it is refactoring-safe)
 * mechanism is to annotate each of the methods using <tt>@MemberOrder</tt>.
 * 
 * @see MemberOrderFacet
 * @see ActionOrderFacet
 */
public interface FieldOrderFacet extends SingleStringValueFacet {

}
