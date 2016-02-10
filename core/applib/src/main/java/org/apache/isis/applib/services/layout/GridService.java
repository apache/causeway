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

import java.util.List;

import org.apache.isis.applib.annotation.MemberGroupLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.layout.common.Grid;

public interface GridService {

    /**
     * Whether any metadata exists for this domain class, and if so then whether it is valid or invalid.
     */
    @Programmatic
    boolean exists(Class<?> domainClass);

    /**
     * Returns (raw unnormalized) metadata, eg per the <code>.layout.xml</code> file.
     */
    @Programmatic
    Grid fromXml(Class<?> domainClass);

    /**
     * Normalize the grid with respect to the specified domain class.
     *
     * <p>
     *     This will (often) modify the grid graph in order to add in any unreferenced actions and so forth.
     * </p>
     */
    Grid normalize(Grid grid);

    Grid complete(Grid grid);

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
     * Obtains the layout metadata, if any, for the (domain class of the) specified domain object.
     *
     * @param style - whether the returned grid should be complete, normalized, or as
     *              minimal as possible.
     */
    @Programmatic Grid toGrid(Object domainObject, final Style style);

    /**
     * Obtains the (normalized) layout metadata, if any, for the specified domain class.
     *
     * @param style - whether the returned grid should be complete (having been normalized), or should be as
     *              minimal as possible.
     */
    @Programmatic Grid toGrid(Class<?> domainClass, final Style style);

    String tnsAndSchemaLocation(final Grid grid);

    /**
     * For all of the available {@link GridNormalizerService}s available, return only the first one for any that
     * are for the same grid implementation.
     * 
     * <p>
     *   This allows default implementations (eg for bootstrap3) to be overridden while also allowing for the more
     *   general idea of multiple implementations.
     * </p>
     */
    @Programmatic
    List<GridNormalizerService<?>> gridNormalizerServices();

}
