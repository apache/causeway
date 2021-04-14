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

package org.apache.isis.core.metamodel.facets.object.navparent;

import org.apache.isis.core.metamodel.facetapi.Facet;

/**
 *
 * Mechanism for obtaining the navigable parent (a domain-object or a domain-view-model)
 * of an instance of a class, used to build a navigable parent chain as required by the
 * 'where-am-I' feature.
 *
 * @since 2.0
 *
 */
public interface NavigableParentFacet extends Facet {

    /**
     * Returns the navigable parent (a domain-object or a domain-view-model) for the target object
     * or null if there is no parent.
     * @param object
     */
    Object navigableParent(final Object object);

}
