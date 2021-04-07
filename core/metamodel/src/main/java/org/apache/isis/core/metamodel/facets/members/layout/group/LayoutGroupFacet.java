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
package org.apache.isis.core.metamodel.facets.members.layout.group;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.core.metamodel.facetapi.Facet;

/**
 * In the framework's default programming model corresponds to annotations
 * {@link Action#associateWith()} and {@link PropertyLayout#group()}.
 * <br>
 * Collections don't support grouping.
 * <p>
 *     An alternative is to use the <code>Xxx.layout.xml</code> file,
 *     where <code>Xxx</code> is the domain object name.
 * </p>
 * 
 * @see Action#associateWith() 
 * @see PropertyLayout#group()
 * 
 * @since 2.0
 */
public interface LayoutGroupFacet extends Facet {

    /**
     * Name of the (layout) group, this member belongs to.
     * Collections don't support grouping.
     */
    public String getGroup();
    
}
