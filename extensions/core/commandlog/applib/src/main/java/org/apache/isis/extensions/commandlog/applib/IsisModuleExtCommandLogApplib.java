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
package org.apache.isis.extensions.commandlog.applib;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.core.config.util.SpringProfileUtil;
import org.apache.isis.extensions.commandlog.applib.app.CommandLogMenu;
import org.apache.isis.extensions.commandlog.applib.contributions.HasInteractionId_commandLogEntry;
import org.apache.isis.extensions.commandlog.applib.contributions.HasUsername_recentCommandsByUser;
import org.apache.isis.extensions.commandlog.applib.contributions.Object_recentCommands;
import org.apache.isis.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.isis.extensions.commandlog.applib.dom.mixins.CommandLogEntry_childCommands;
import org.apache.isis.extensions.commandlog.applib.dom.mixins.CommandLogEntry_openResultObject;
import org.apache.isis.extensions.commandlog.applib.dom.mixins.CommandLogEntry_siblingCommands;
import org.apache.isis.extensions.commandlog.applib.subscriber.CommandSubscriberForCommandLog;
import org.apache.isis.testing.fixtures.applib.modules.ModuleWithFixtures;

@Configuration
@Import({
        // @DomainService's
        CommandLogMenu.class,

        // mixins
        HasInteractionId_commandLogEntry.class,
        HasUsername_recentCommandsByUser.class,
        Object_recentCommands.class,
        CommandLogEntry_childCommands.class,
        CommandLogEntry_openResultObject.class,
        CommandLogEntry_siblingCommands.class,

        // @Service's
        CommandSubscriberForCommandLog.class,
        CommandLogEntry.TableColumnOrderDefault.class,
})
public class IsisModuleExtCommandLogApplib {

    public static final String NAMESPACE = "isis.ext.commandLog";
    public static final String SCHEMA = "isisExtCommandLog";

    public abstract static class TitleUiEvent<S>
        extends org.apache.isis.applib.events.ui.TitleUiEvent<S> { }

    public abstract static class IconUiEvent<S>
        extends org.apache.isis.applib.events.ui.IconUiEvent<S> { }

    public abstract static class CssClassUiEvent<S>
        extends org.apache.isis.applib.events.ui.CssClassUiEvent<S> { }

    public abstract static class LayoutUiEvent<S>
        extends org.apache.isis.applib.events.ui.LayoutUiEvent<S> { }

    public abstract static class ActionDomainEvent<S>
        extends org.apache.isis.applib.events.domain.ActionDomainEvent<S> { }

    public abstract static class CollectionDomainEvent<S,T>
        extends org.apache.isis.applib.events.domain.CollectionDomainEvent<S,T> { }

    public abstract static class PropertyDomainEvent<S,T>
        extends org.apache.isis.applib.events.domain.PropertyDomainEvent<S,T> { }

    public static final String NAMESPACE_REPLAY_PRIMARY = "isis.ext.commandReplayPrimary";
    public static final String NAMESPACE_REPLAY_SECONDARY = "isis.ext.commandReplaySecondary";

    public static final String SERVICE_REPLAY_PRIMARY_COMMAND_RETRIEVAL =
            NAMESPACE_REPLAY_PRIMARY + ".CommandRetrievalOnPrimaryService";

    public static void honorSystemEnvironment() {
        if("true".equalsIgnoreCase(System.getenv("PRIMARY"))) {
            SpringProfileUtil.removeActiveProfile("commandreplay-secondary"); // just in case
            SpringProfileUtil.addActiveProfile("commandreplay-primary");
        } else if("true".equalsIgnoreCase(System.getenv("SECONDARY"))) {
            SpringProfileUtil.removeActiveProfile("commandreplay-primary"); // just in case
            SpringProfileUtil.addActiveProfile("commandreplay-secondary");
        }
    }

}
