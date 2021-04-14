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
package org.apache.isis.core.config.environment;

import java.util.TimeZone;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import org.apache.isis.core.config.IsisConfiguration;

import lombok.extern.log4j.Log4j2;

import jakarta.annotation.PostConstruct;

@Component
@Log4j2
public class IsisTimeZoneInitializer {

    @PostConstruct @Inject
    public void initTimeZone(final IsisConfiguration configuration) {
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
