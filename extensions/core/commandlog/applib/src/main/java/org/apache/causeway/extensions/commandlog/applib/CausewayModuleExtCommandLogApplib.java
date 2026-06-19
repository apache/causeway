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
package org.apache.causeway.extensions.commandlog.applib;

import java.util.List;

import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.command.CommandExecutorService;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.metamodel.MetaModelService;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.applib.services.scratchpad.Scratchpad;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.util.SpringProfileUtil;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.extensions.commandlog.applib.app.CommandLogMenu;
import org.apache.causeway.extensions.commandlog.applib.contributions.HasInteractionId_commandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.contributions.HasUsername_recentCommandsByUser;
import org.apache.causeway.extensions.commandlog.applib.contributions.Object_recentCommands;
import org.apache.causeway.extensions.commandlog.applib.dom.BackgroundService;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.mixins.CommandLogEntry_childCommands;
import org.apache.causeway.extensions.commandlog.applib.dom.mixins.CommandLogEntry_openResultObject;
import org.apache.causeway.extensions.commandlog.applib.dom.mixins.CommandLogEntry_siblingCommands;
import org.apache.causeway.extensions.commandlog.applib.dom.mixins.CommandReplayResultMapping_delete;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandManager;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandManager_moveCommands;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.HasLimit_changeLimit;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandManager_deleteCommands;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandManager_nextPage;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandManager_previousPage;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandManager_replayOrRetryNext;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.HasBaseline_changeBaseline;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandManager_excludeCommands;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandManager_exportSelected;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandManager_unexcludeCommands;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.HasBaseline_nextHour;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.HasBaseline_previousHour;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandManager_importCommands;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandManager_replayOrRetrySelected;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.ReplayContext;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.ReplayableCommand_delete;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.ReplayableCommand_exclude;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.ReplayableCommand_unexclude;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.ReplayableCommand_next;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.ReplayableCommand_openCommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.ReplayableCommand_previous;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.ReplayableCommand_replayOrRetry;
import org.apache.causeway.extensions.commandlog.applib.fakescheduler.FakeScheduler;
import org.apache.causeway.extensions.commandlog.applib.job.BackgroundCommandsJobControl;
import org.apache.causeway.extensions.commandlog.applib.job.RunBackgroundCommandsJob;
import org.apache.causeway.extensions.commandlog.applib.spi.CommandReplayMappingListener;
import org.apache.causeway.extensions.commandlog.applib.spi.CommandReplayMappingListenerInMemory;
import org.apache.causeway.extensions.commandlog.applib.spi.CommandReplayReferenceDataService;
import org.apache.causeway.extensions.commandlog.applib.spi.CommandReplayReferenceDataServiceForRefData;
import org.apache.causeway.extensions.commandlog.applib.spi.RunBackgroundCommandsJobListener;
import org.apache.causeway.extensions.commandlog.applib.subscriber.CommandLogPauseState;
import org.apache.causeway.extensions.commandlog.applib.subscriber.CommandLogPauseStateListener;
import org.apache.causeway.extensions.commandlog.applib.subscriber.CommandSubscriberForCommandLog;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import({
        // @DomainService's
        CommandLogMenu.class,

        // viewmodels
        CommandManager.class,

        // mixins
        HasInteractionId_commandLogEntry.class,
        HasUsername_recentCommandsByUser.class,
        Object_recentCommands.class,
        CommandLogEntry_childCommands.class,
        CommandLogEntry_openResultObject.class,
        CommandLogEntry_siblingCommands.class,
        CommandReplayResultMapping_delete.class,
        ReplayableCommand_unexclude.class,
        ReplayableCommand_openCommandLogEntry.class,
        ReplayableCommand_replayOrRetry.class,
        ReplayableCommand_previous.class,
        ReplayableCommand_next.class,
        ReplayableCommand_exclude.class,
        ReplayableCommand_delete.class,
        HasBaseline_changeBaseline.class,
        HasBaseline_previousHour.class,
        HasBaseline_previousHour.class,
        HasBaseline_nextHour.class,
        CommandManager_exportSelected.class,
        CommandManager_excludeCommands.class,
        CommandManager_moveCommands.class,
        CommandManager_deleteCommands.class,
        CommandManager_unexcludeCommands.class,
        CommandManager_previousPage.class,
        CommandManager_nextPage.class,
        HasLimit_changeLimit.class,
        CommandManager_importCommands.class,
        CommandManager_replayOrRetryNext.class,
        CommandManager_replayOrRetrySelected.class,

        // @Component's
        RunBackgroundCommandsJob.class,
        RunBackgroundCommandsJobListener.Noop.class,
        CommandReplayMappingListenerInMemory.BeanFactory.class,
        CommandReplayReferenceDataServiceForRefData.class,

        // @Service's
        CommandLogPauseState.class,
        CommandLogPauseStateListener.class,
        CommandSubscriberForCommandLog.class,
        CommandLogEntry.TableColumnOrderDefault.class,

        BackgroundCommandsJobControl.class,

        BackgroundService.class,
        BackgroundService.PersistCommandExecutorService.class,

        FakeScheduler.class,
})
public class CausewayModuleExtCommandLogApplib {

    public static final String NAMESPACE = "causeway.ext.commandLog";
    public static final String SCHEMA = "causewayExtCommandLog";

    public abstract static class TitleUiEvent<S>
        extends org.apache.causeway.applib.events.ui.TitleUiEvent<S> { }

    public abstract static class IconUiEvent<S>
        extends org.apache.causeway.applib.events.ui.IconUiEvent<S> { }

    public abstract static class CssClassUiEvent<S>
        extends org.apache.causeway.applib.events.ui.CssClassUiEvent<S> { }

    public abstract static class LayoutUiEvent<S>
        extends org.apache.causeway.applib.events.ui.LayoutUiEvent<S> { }

    public abstract static class ActionDomainEvent<S>
        extends org.apache.causeway.applib.events.domain.ActionDomainEvent<S> { }

    public abstract static class CollectionDomainEvent<S,T>
        extends org.apache.causeway.applib.events.domain.CollectionDomainEvent<S,T> { }

    public abstract static class PropertyDomainEvent<S,T>
        extends org.apache.causeway.applib.events.domain.PropertyDomainEvent<S,T> { }

    public static final String NAMESPACE_REPLAY_PRIMARY = "causeway.ext.commandReplayPrimary";
    public static final String NAMESPACE_REPLAY_SECONDARY = "causeway.ext.commandReplaySecondary";

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

    @Bean ReplayContext replayContext(
            final RepositoryService repositoryService,
            final InteractionService interactionService,
            final TransactionService transactionService,
            final CommandLogEntryRepository commandLogEntryRepository,
            final CommandExecutorService commandExecutorService,
            final ClockService clockService,
            final java.util.List<CommandReplayMappingListener> commandReplayMappingListeners,
            final Scratchpad scratchpad,
            final MetaModelService metaModelService,
            final CausewayConfiguration causewayConfiguration,
            final List<CommandReplayReferenceDataService> commandReplayReferenceDataServices,
            final SpecificationLoader specificationLoader,
            final BookmarkService bookmarkService) {
        return new ReplayContext(
                repositoryService, interactionService, transactionService,
                commandLogEntryRepository, commandExecutorService, clockService,
                commandReplayMappingListeners,
                scratchpad,
                metaModelService,
                causewayConfiguration,
                commandReplayReferenceDataServices,
                specificationLoader,
                bookmarkService);
    }

}
