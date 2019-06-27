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

package org.apache.isis.metamodel.facets.properties.searchable;

import org.apache.isis.metamodel.facets.MultipleValueFacet;

/**
 * Indicates that this property should be used as part of a generic searching
 * capability (for example, query by example).
 *
 * <p>
 * In the standard Apache Isis Programming Model, corresponds to annotating the
 * property with the <tt>@Searchable</tt> annotation.
 *
 * <p>
 * TODO: not yet implemented by the framework or any viewer. Originally
 * introduced for the adapterrcp.sourceforge.net viewer as an extension point
 * plug-in for the Search menu ( <tt>org.eclipse.search.searchPages</tt>).
 */
public interface SearchableFacet extends MultipleValueFacet {

    /**
     * The (class of the) repository to delegate to.
     */
    public Class<?> repository();

    /**
     * Whether this is a query by example search.
     */
    public boolean queryByExample();

}
