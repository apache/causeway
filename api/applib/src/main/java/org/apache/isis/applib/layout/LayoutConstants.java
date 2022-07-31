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
 *
 */
package org.apache.isis.applib.layout;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Constant identifiers of the <code>layout.xml</code> that are commonly used in layouts.
 *
 * <p>
 *     All of the mixins provided by the framework are bound to the {@link #METADATA_FIELDSET_ID metadata} field set,
 *     and so - unless explicitly specified in the object's layout to some other location, will expect that the layout
 *     includes a field set with this id.
 * </p>
 *
 * <p>
 *     The service that loads layouts is {@link org.apache.isis.applib.services.grid.GridSystemService}, and the
 *     framework's default implementation of this service supports the notion of a fallback layout, used whenever
 *     an object has no associated layout file.  This fallback layout <i>does</i> expose a
 *     {@link #METADATA_FIELDSET_ID metadata} fieldset.  (The fallback layout itself can be found in the
 *     <code>GridFallbackLayout.xml</code> file).
 * </p>
 *
 * @since 2.x {@index}
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LayoutConstants {

    /**
     * The id for a field set intended to hold the properties/fields that uniquely identify the object from the
     * end-user's perspective.
     *
     * <p>
     *     The fallback layout places the 'identity' and 'metadata' fieldsets as tabs within a single tab group.
     * </p>
     */
    public static final String IDENTITY_FIELDSET_ID = "identity";

    /**
     * The id for a field set intended to hold various metadata, such as the id or version.  All of the framework
     * provided mixins are associated with this fieldset, either properties or actions.
     *
     * <p>
     *     The fallback layout places the 'identity' and 'metadata' fieldsets as tabs within a single tab group.
     * </p>
     */
    public static final String METADATA_FIELDSET_ID = "metadata";

    /**
     * The id for a field set intended to hold additional details.
     *
     * <p>
     *     The fallback layout places the 'details' fieldset under the above 'identity' and 'metadata' fieldsets.
     * </p>
     */
    public static final String DETAILS_FIELDSET_ID = "details";

}
