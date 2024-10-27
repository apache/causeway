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
package org.apache.causeway.applib.value.semantics;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

/**
 * When implemented by a service {@link #getAvailableZoneIds()} 
 * can be customized to limit the time-zone choices 
 * as e.g. offered by the <i>Wicket Viewer<i> login page.
 * 
 * @since 2.1, 3.1
 */
public interface TimeZoneChoiceProvider {
    
    /**
     * For temporal value editing, provides the list of available time zones to choose from.
     */
    default List<ZoneId> getAvailableZoneIds() {
        return ZoneId.getAvailableZoneIds().stream()
            .sorted()
            .map(ZoneId::of)
            .collect(Collectors.toList());
    }

    /**
     * For temporal value editing, provides the list of available offsets to choose from.
     */
    default List<ZoneOffset> getAvailableOffsets() {
        var now = LocalDateTime.now();
        return getAvailableZoneIds().stream()
            .map(ZoneId::getRules)
            .flatMap(zoneIdRules->zoneIdRules.getValidOffsets(now).stream())
            .sorted()
            .distinct()
            .collect(Collectors.toList());
    }
    
    /**
     * Returns the fallback implementation, which provides all ZoneIds known to the JVM.
     */
    static TimeZoneChoiceProvider fallback() {
        return new TimeZoneChoiceProvider() {};
    }
    
}
