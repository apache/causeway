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
package org.apache.causeway.extensions.commandreplay.primary;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandreplay.primary.config.PrimaryConfig;
import org.apache.causeway.extensions.commandreplay.primary.mixins.Object_openOnSecondary;
import org.apache.causeway.extensions.commandreplay.primary.restapi.CommandRetrievalOnPrimaryService;
import org.apache.causeway.extensions.commandreplay.primary.spiimpl.CaptureResultOfCommand;
import org.apache.causeway.extensions.commandreplay.primary.ui.CommandReplayOnPrimaryService;

/**
 * Activates with <i>Spring profile</i> 'commandreplay-primary'.
 * @since 2.0 {@index}
 */
@Configuration
@Import({
        // @Configuration's

        // @Service's
        CaptureResultOfCommand.class,
        CommandRetrievalOnPrimaryService.class,
        CommandReplayOnPrimaryService.class,
        PrimaryConfig.class,

        // Mixins
        Object_openOnSecondary.class,

})
@Profile("commandreplay-primary")
public class CausewayModuleExtCommandReplayPrimary {

    public static final String NAMESPACE = CausewayModuleExtCommandLogApplib.NAMESPACE_REPLAY_PRIMARY;

    public abstract static class ActionDomainEvent<S>
            extends org.apache.causeway.applib.events.domain.ActionDomainEvent<S> { }

    public abstract static class CollectionDomainEvent<S,T>
            extends org.apache.causeway.applib.events.domain.CollectionDomainEvent<S,T> { }

    public abstract static class PropertyDomainEvent<S,T>
            extends org.apache.causeway.applib.events.domain.PropertyDomainEvent<S,T> { }

}
