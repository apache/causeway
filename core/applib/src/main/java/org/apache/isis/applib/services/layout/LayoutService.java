/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.applib.services.layout;

import org.apache.isis.applib.annotation.MemberGroupLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Programmatic;

public interface LayoutService {

    enum Style {

        /**
         * The current layout for the domain class.
         * <p/>
         * <p>
         * If a <code>layout.xml</code> exists, then the grid returned will correspond to that
         * grid, having been {@link org.apache.isis.applib.services.grid.GridService#normalize(Grid) normalized}.  If there is no <code>layout.xml</code> file, then the grid returned will be the
         * {@link org.apache.isis.applib.services.grid.GridService#defaultGridFor(Class) default grid},
         * also {@link org.apache.isis.applib.services.grid.GridService#normalize(Grid) normalized}.
         * </p>
         */
        CURRENT,
        /**
         * As per {@link #NORMALIZED}, but also with all (non-null) facets for all
         * properties/collections/actions also included included in the grid.
         * <p/>
         * <p>
         * The intention here is that any layout metadata annotations can be removed from the code.
         * </p>
         */
        COMPLETE,
        /**
         * Default, whereby missing properties/collections/actions are added to regions,
         * and unused/empty regions are removed/trimmed.
         * <p/>
         * <p>
         * It should be possible to remove any {@link MemberOrder} and {@link MemberGroupLayout} annotations but
         * any layout annotations would need to be retained.
         * </p>
         */
        NORMALIZED,
        /**
         * As per {@link #NORMALIZED}, but with no properties/collections/actions.
         * <p/>
         * <p>
         * The intention here is for layout annotations that &quot;bind&quot; the properties/collections/actions
         * to the regions to be retained.
         * </p>
         */
        MINIMAL
    }

    /**
     * Obtains the serialized XML form of the layout (grid) for the specified domain class.
     */
    @Programmatic
    String toXml(Class<?> domainClass, Style style);

    /**
     * Obtains a zip file of the serialized XML of the layouts (grids) of all domain entities and view models.
     */
    @Programmatic
    byte[] toZip(final Style style);
}