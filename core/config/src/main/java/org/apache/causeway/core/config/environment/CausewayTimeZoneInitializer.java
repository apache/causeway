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
package org.apache.causeway.core.config.environment;

import java.util.TimeZone;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import org.springframework.stereotype.Component;

import org.apache.causeway.core.config.CausewayConfiguration;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class CausewayTimeZoneInitializer {

    @Inject CausewayConfiguration configuration;

    @PostConstruct
    public void initTimeZone() {
        final String timeZoneSpec = configuration.getCore().getRuntime().getTimezone();
        if (timeZoneSpec != null) {
            TimeZone timeZone;
            timeZone = TimeZone.getTimeZone(timeZoneSpec);
            TimeZone.setDefault(timeZone);
            log.info("time zone set to {}", timeZone);
        }
        log.debug("time zone is {}", TimeZone.getDefault());
    }

}
