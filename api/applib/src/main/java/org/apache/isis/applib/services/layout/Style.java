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
package org.apache.isis.applib.services.layout;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.layout.grid.Grid;

/**
 * Mode of operation when downloading a layout file (while prototyping).
 *
 * <p>
 * It affects the way the file's
 * content is assembled. Once a layout file is in place, its layout data takes precedence over any
 * conflicting layout data from annotations.
 * </p>
 *
 * TODO update for v2 - @MemberGroupLayout and @MemberOrder have been removed
 *
 * @since 1.x {@index}
 */
@DomainObject(logicalTypeName = IsisModuleApplib.NAMESPACE + ".services.layout.Style")
public enum Style {

    /**
     * The current layout for the domain class.
     * <p>
     * If a <code>layout.xml</code> exists, then the grid returned will correspond to that
     * grid, having been {@link org.apache.isis.applib.services.grid.GridService#normalize(Grid) normalized}.
     * If there is no <code>layout.xml</code> file, then the grid returned will be the
     * {@link org.apache.isis.applib.services.grid.GridService#defaultGridFor(Class) default grid},
     * also {@link org.apache.isis.applib.services.grid.GridService#normalize(Grid) normalized}.
     */
    CURRENT,

    /**
     * As per {@link #NORMALIZED}, but also with all (non-null) facets for all
     * properties/collections/actions also included included in the grid.
     * <p>
     * The intention here is that any layout metadata annotations can be removed from the code.
     * <ul>
     * <li>{@code @MemberGroupLayout}: <b>serialized as XML</b></li>
     * <li>{@code @MemberOrder}: <b>serialized as XML</b></li>
     * <li>{@code @ActionLayout, @PropertyLayout, @CollectionLayout}: <b>serialized as XML</b></li>
     * </ul>
     */
    COMPLETE,

    /**
     * Default, whereby missing properties/collections/actions are added to regions,
     * and unused/empty regions are removed/trimmed.
     * <p>
     * It should be possible to remove any {@link MemberOrder} annotation but
     * any property/collection/action layout annotations would need to be retained.
     * <ul>
     * <li>{@code @MemberGroupLayout}: <b>serialized as XML</b></li>
     * <li>{@code @MemberOrder}: <b>serialized as XML</b></li>
     * <li>{@code @ActionLayout, @PropertyLayout, @CollectionLayout}: <b>ignored</b></li>
     * </ul>
     */
    NORMALIZED,

    /**
     * As per {@link #NORMALIZED}, but with no properties/collections/actions.
     * <p>
     * The intention here is for layout annotations that &quot;bind&quot; the properties/collections/actions
     * to the regions to be retained.
     * <ul>
     * <li>{@code @MemberGroupLayout}: <b>serialized as XML</b></li>
     * <li>{@code @MemberOrder}: <b>ignored</b></li>
     * <li>{@code @ActionLayout, @PropertyLayout, @CollectionLayout}: <b>ignored</b></li>
     * </ul>
     */
    MINIMAL
}
