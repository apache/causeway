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
package org.apache.isis.core.metamodel.facets.members.layout.order;

import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.core.metamodel.facetapi.Facet;

/**
 * In the framework's default programming model corresponds to annotations
 * {@link ActionLayout#sequence()}, {@link CollectionLayout#sequence()} and 
 * {@link PropertyLayout#sequence()}.
 * <p>
 *     An alternative is to use the <code>Xxx.layout.xml</code> file,
 *     where <code>Xxx</code> is the domain object name.
 * </p>
 * 
 * @see ActionLayout#sequence() 
 * @see CollectionLayout#sequence()
 * @see PropertyLayout#sequence()
 * 
 * @since 2.0
 */
public interface LayoutOrderFacet extends Facet {

    /**
     * The order of this member relative to other members in the same (layout) group, 
     * in dewey-decimal notation. For collections this is relative to each other
     * (collections aren't grouped).
     */
    public String getSequence();
    
}
