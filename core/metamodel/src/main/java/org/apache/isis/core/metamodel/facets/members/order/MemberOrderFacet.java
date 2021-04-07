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

package org.apache.isis.core.metamodel.facets.members.order;

import org.apache.isis.core.metamodel.facets.MultipleValueFacet;

/**
 * The preferred mechanism for determining the order in which the members of the
 * object should be rendered.
 *
 * <p>
 * In the standard Apache Isis Programming Model, corresponds to annotating each
 * of the member methods with the <tt>@MemberOrder</tt>.
 *
 * @see MemberOrderFacet
 * @deprecated
 */
public interface MemberOrderFacet extends MultipleValueFacet {

    /**
     * Groups or associate members with each other.
     *
     * <ul>
     *     <li>For actions, indicates the property or collection to associate.</li>
     *     <li>For properties, indicates the property group</li>
     *     <li>For collections, currently has no meaning</li>
     * </ul>
     */
    public String name();

    /**
     * The order of this member relative to other members in the same group, in
     * dewey-decimal notation.  For collections this is relative to each other
     * (collections aren't grouped).
     */
    public String sequence();

    /**
     * The untranslated name of this member order (to associate back to <code>MemberOrderLayout</code>).
     */
    public String untranslatedName();

}
