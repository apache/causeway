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
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.core.metamodel.facetapi.Facet;

/**
 * With the framework's default programming model corresponds to annotations
 * {@link ActionLayout#fieldSetId()} and {@link PropertyLayout#fieldSetId()},
 * with friendly names
 * {@link ActionLayout#fieldSetName()} and {@link PropertyLayout#fieldSetName()}.
 * <p>
 * Collections don't support grouping, but can be associated using
 * {@link Action#associateWith()}.
 * </p><p>
 * An alternative is to use the <code>Xxx.layout.xml</code> file,
 * where <code>Xxx</code> is the domain object name.
 * </p><p>
 * For a more in depth description see {@link PropertyLayout#fieldSetId()}.
 * </p>
 *
 * @see Action#associateWith()
 * @see ActionLayout#fieldSetId()
 * @see ActionLayout#fieldSetName()
 * @see PropertyLayout#fieldSetId()
 * @see PropertyLayout#fieldSetName()
 *
 * @since 2.0
 */
public interface LayoutGroupFacet extends Facet {

    /**
     * Id and (Friendly) name of the layout group this member is associated with.
     */
    public GroupIdAndName getGroupIdAndName();

    /**
     * Id of the layout group this member is associated with.
     */
    public default String getGroupId() {
        return getGroupIdAndName().getId();
    }

    /**
     * (Friendly) name of the layout group this member is associated with.
     */
    public default String getGroupName() {
        return getGroupIdAndName().getName();
    }

    /**
     * {@code true} for layouts originating from XML,
     * {@code false} for layouts originating from annotations.
     */
    public default boolean isExplicitBinding() {
        return false;
    }

}
