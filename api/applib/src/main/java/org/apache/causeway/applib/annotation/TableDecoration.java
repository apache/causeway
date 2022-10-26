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
package org.apache.causeway.applib.annotation;

/**
 * The options for defining how a parented or standalone collection when represented in a table form should additionally
 * be &quot;decorated&quot; with client-side (javascript) enhancements.
 *
 * <p>
 *     This is supported by the Wicket viewer, the principle use case being to enable <a href="https://datatables.net>datatables.net</a>
 *     to be used for client-side paging and filtering.
 * </p>
 *
 * @since 1.x {@index}
 */
public enum TableDecoration {

    /**
     * The collection's table representation should be decorated, if at all, as configured in <tt>application.properties</tt>.
     *
     * <p>
     *     If there is no configuration, then default to use {@link TableDecoration#NONE no} decoration.
     * </p>
     */
    AS_CONFIGURED,

    /**
     * If this option declares that the collection's table representation should not be decorated.
     */
    NONE,

    /**
     * If this option declares that the collection's table representation be decorated using
     * <a href="https://datatables.net>datatables.net</a> for client-side paging and filtering.
     */
    DATATABLES_NET,

    /**
     * Ignore the value provided by this annotation (meaning that the framework will keep searching, in meta
     * annotations or superclasses/interfaces).
     */
    NOT_SPECIFIED

}
