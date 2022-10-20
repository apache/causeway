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
 * Rendering mode for temporals (<i>time instants</i>) with time-zone or time-offset information.
 *
 * @since 2.0 {@index}
 */
public enum TimeZoneTranslation {

    UNSPECIFIED,

    /**
     * Renders temporals(<i>time instants</i>) - that have time-zone or time-offset information
     * - transformed to the user's local/current time-zone,
     * optionally with a tooltip providing information on the originating (non transformed) <i>time instant</i>.
     */
    TO_LOCAL_TIMEZONE,

    /**
     * Renders temporals (<i>time instants</i>) - that have time-zone or time-offset information - as is,
     * optionally with a tooltip providing information on the <i>time instant</i>
     * as transformed to the user's local/current time-zone.
     */
    NONE;

}
