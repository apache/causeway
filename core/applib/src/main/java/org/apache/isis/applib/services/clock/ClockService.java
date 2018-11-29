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
package org.apache.isis.applib.services.clock;

import java.sql.Timestamp;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.clock.Clock;

/**
 * This service allows an application to be decoupled from the system time.  The most common use case is in support of
 * testing scenarios, to &quot;mock the clock&quot;.  Use of this service also opens up the use of centralized
 * co-ordinated time management through a centralized time service.
 *
 * <p>
 * This service has only one implementation and so is automatically registered. and available for use; no further
 * configuration is required.
 */
@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + Integer.MAX_VALUE
        )
public class ClockService {

    @Programmatic
    public LocalDate now() {
        return Clock.getTimeAsLocalDate();
    }

    @Programmatic
    public LocalDateTime nowAsLocalDateTime() {
        return Clock.getTimeAsLocalDateTime();
    }

    @Programmatic
    public DateTime nowAsDateTime() {
        return Clock.getTimeAsDateTime();
    }

    @Programmatic
    public Timestamp nowAsJavaSqlTimestamp() {
        return Clock.getTimeAsJavaSqlTimestamp();
    }

    @Programmatic
    public long nowAsMillis() {
        return Clock.getTime();
    }

}
