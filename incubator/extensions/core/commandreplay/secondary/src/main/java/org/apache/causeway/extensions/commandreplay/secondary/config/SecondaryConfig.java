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
package org.apache.causeway.extensions.commandreplay.secondary.config;

import java.util.List;

import jakarta.inject.Named;
import jakarta.validation.constraints.NotNull;

import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.core.config.CausewayConfiguration;

import lombok.Getter;

/**
 * @since 2.0 {@index}
 */
@Service
@Named("causeway.ext.commandReplaySecondary.SecondaryConfig")
@jakarta.annotation.Priority(PriorityPrecedence.MIDPOINT)
//@Slf4j
public class SecondaryConfig {

    @Getter final String primaryUser;
    @Getter final String primaryPassword;
    @Getter final String primaryBaseUrlRestful;
    @Getter final String primaryBaseUrlWicket;
    @Getter final int batchSize;

    @Getter final String quartzUser;
    @Getter final List<String> quartzRoles;

    public SecondaryConfig(@NotNull final CausewayConfiguration causewayConfiguration) {
        var config = causewayConfiguration.getExtensions().getCommandReplay();

        var primaryAccess = config.getPrimaryAccess();
        primaryUser = primaryAccess.getUser().orElse(null);
        primaryPassword = primaryAccess.getPassword().orElse(null);
        primaryBaseUrlRestful = primaryAccess.getBaseUrlRestful().orElse(null);
        primaryBaseUrlWicket = primaryAccess.getBaseUrlWicket().orElse(null);
        batchSize = config.getBatchSize();

        quartzUser = config.getQuartzSession().getUser();
        quartzRoles = config.getQuartzSession().getRoles();
    }

    public boolean isConfigured() {
        return primaryUser != null &&
               primaryPassword != null &&
               primaryBaseUrlRestful != null &&
               quartzUser != null &&
               quartzRoles != null;
    }
}
