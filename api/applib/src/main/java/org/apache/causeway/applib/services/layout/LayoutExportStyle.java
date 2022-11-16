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

import org.apache.causeway.applib.annotation.Value;
import org.apache.causeway.applib.layout.grid.Grid;

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
     * such that any layout metadata annotations could be removed from the code,
     * without affecting the resulting {@link Grid}, when loaded from <code>layout.xml</code>.
     */
    COMPLETE,

    /**
     * Format that yields a minimal representation for the <code>layout.xml</code>,
     * such that layout annotations are required in code to at least 'bind'
     * the properties/collections/actions to their regions (groups and tabs).
     * <p>
     * In other words: the <code>layout.xml</code> is used only to specify the positioning of the
     * groups and tabs.
     */
    MINIMAL,;

    public static LayoutExportStyle defaults() {
        return MINIMAL;
    }

}
