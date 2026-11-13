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
package org.apache.causeway.applib.services.layout;

import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Value;
import org.apache.causeway.applib.layout.grid.bootstrap.BSGrid;

/**
 * Format option when generating a layout file (while prototyping).
 * <p>
 * Once a layout file is in place, its layout data takes precedence over any
 * conflicting layout data from annotations.
 *
 * @since 2.x {@index}
 */
@Value
public enum LayoutExportStyle {

    /**
     * Format that yields a full representation for the <code>layout.xml</code>,
     * such that any layout metadata annotations could be removed from code,
     * without affecting the resulting {@link BSGrid}, when loaded from <code>layout.xml</code>.
     *
     * The resulting {@link BSGrid} has all the metadata, broadly speaking corresponding to the
     * {@link DomainObjectLayout}, {@link ActionLayout}, {@link PropertyLayout} and {@link CollectionLayout}.
     *
     * <p>In other words: if a 'complete' grid is persisted as the <code>layout.xml</code>, then there should be no need
     * for any of the layout annotations,
     * to be required in the domain class itself.
     */
    COMPLETE,

    /**
     * Format that yields a minimal representation for the <code>layout.xml</code>,
     * such that layout annotations are required in code to at least 'bind'
     * the properties/collections/actions to their regions (groups and tabs).
     *
     * <p>In other words: the <code>layout.xml</code> is used only to specify the positioning of the
     * groups and tabs, but has no additional meta data. The expectation is that
     * most of the layout annotations ({@link DomainObjectLayout}, {@link ActionLayout}, {@link PropertyLayout},
     * {@link CollectionLayout} will still be retained in the domain class code.
     */
    MINIMAL,

// not used as an export format anymore
//    /**
//     * If a 'normalized' grid is persisted as the <code>layout.xml</code>, then the expectation is that
//     * any ordering metadata from layout annotations can be removed from the domain class
//     * because the binding of properties/collections/actions will be within the XML.  However, the layout
//     * annotations ({@link DomainObjectLayout}, {@link ActionLayout}, {@link PropertyLayout} and
//     * {@link CollectionLayout}) (if present) will continue to be used to provide additional layout metadata.  Of
//     * course, there is nothing to prevent the developer from extending the layout XML to also include the
//     * layout XML (in other words moving towards a {@link #complete(BSGrid) complete} grid.  Metadata within the
//     * <code>layout.xml</code> file takes precedence over any annotations.
//     */
//    NORMALIZED
    ;

    public static LayoutExportStyle defaults() {
        return MINIMAL;
    }

}
