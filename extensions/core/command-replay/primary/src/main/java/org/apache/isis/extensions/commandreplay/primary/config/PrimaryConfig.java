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
package org.apache.isis.extensions.commandreplay.primary.config;

import javax.inject.Named;
import javax.validation.constraints.NotNull;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.core.config.IsisConfiguration;

import lombok.Getter;
import lombok.val;

/**
 * @since 2.0 {@index}
 */
@Service
@Named("isis.ext.commandReplayPrimary.PrimaryConfig")
@Order(OrderPrecedence.MIDPOINT)
//@Log4j2
public class PrimaryConfig {

    @Getter final String secondaryBaseUrlWicket;

    public PrimaryConfig(@NotNull final IsisConfiguration isisConfiguration) {
        val config = isisConfiguration.getExtensions().getCommandReplay();

        val secondaryAccess = config.getSecondaryAccess();
        secondaryBaseUrlWicket = secondaryAccess.getBaseUrlWicket().orElse(null);
    }

    public boolean isConfigured() {
        return secondaryBaseUrlWicket != null;
    }
}
