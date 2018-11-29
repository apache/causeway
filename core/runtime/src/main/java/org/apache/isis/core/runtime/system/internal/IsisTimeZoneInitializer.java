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

package org.apache.isis.core.runtime.system.internal;

import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.config.ConfigurationConstants;
import org.apache.isis.config.IsisConfiguration;

public class IsisTimeZoneInitializer {

    public static final Logger LOG = LoggerFactory.getLogger(IsisTimeZoneInitializer.class);

    public void initTimeZone(final IsisConfiguration configuration) {
        final String timeZoneSpec = configuration.getString(ConfigurationConstants.ROOT + "timezone");
        if (timeZoneSpec != null) {
            TimeZone timeZone;
            timeZone = TimeZone.getTimeZone(timeZoneSpec);
            TimeZone.setDefault(timeZone);
            LOG.info("time zone set to {}", timeZone);
        }
        LOG.debug("time zone is {}", TimeZone.getDefault());
    }

}
