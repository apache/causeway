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
import org.apache.isis.applib.layout.component.Grid;
import org.apache.isis.applib.value.Blob;

public interface LayoutService {

    /**
     * Whether any <code>.layout.xml</code> file exists for this domain class, and if so then whether it is
     * valid or invalid.
     */
    @Programmatic
    boolean xmlExistsFor(Class<?> domainClass);

    /**
     * Returns a normalized grid for the domain class, either loaded from a
     * <code>.layout.xml</code> file, else using a default specific to the
     * grid service.
     *
     * <p>
     * Normalization generally means to add in any missing properties/collections/actions so that the
     * in-memory grid can be rendered.
     * </p>
     */
    @Programmatic
    Grid normalizedFromXmlElseDefault(Class<?> domainClass);

    @Programmatic
    Grid complete(Grid grid);

    @Programmatic
    Grid minimal(Grid grid);

    enum Style {

        /**
         * As per {@link #NORMALIZED}, but also with all (non-null) facets for all
         * properties/collections/actions also included included in the grid.
         *
         * <p>
         *     The intention here is that any layout metadata annotations can be removed from the code.
         * </p>
         */
        COMPLETE,
        /**
         * Default, whereby missing properties/collections/actions are added to regions,
         * and unused/empty regions are removed/trimmed.
         *
         * <p>
         *     It should be possible to remove any {@link MemberOrder} and {@link MemberGroupLayout} annotations but
         *     any layout annotations would need to be retained.
         * </p>
         */
        NORMALIZED,
        /**
         * As per {@link #NORMALIZED}, but with no properties/collections/actions.
         *
         * <p>
         *     The intention here is for layout annotations that &quot;bind&quot; the properties/collections/actions
         *     to the regions to be retained.
         * </p>
         */
        MINIMAL
    }


    /**
     * Obtains the grid for the specified domain class.
     *
     * @param style - how complete the information in the returned grid should be.
     */
    @Programmatic
    Grid toGrid(final Class<?> domainClass, final Style style);

    @Programmatic
    Blob downloadLayouts(final Style style);

}
