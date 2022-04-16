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
package org.apache.isis.extensions.commandlog.applib.command.ui;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.extensions.commandlog.applib.IsisModuleExtCommandLogApplib;
import org.apache.isis.extensions.commandlog.applib.command.CommandLog;
import org.apache.isis.extensions.commandlog.applib.command.ICommandLogRepository;

import lombok.RequiredArgsConstructor;

/**
 * @since 2.0 {@index}
 */
@Named(IsisModuleExtCommandLogApplib.NAMESPACE + ".CommandLogServiceMenu")
@DomainService(
    nature = NatureOfService.VIEW
)
@DomainServiceLayout(
    named = "Activity",
    menuBar = DomainServiceLayout.MenuBar.SECONDARY
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@Qualifier("Jdo")
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class CommandLogServiceMenu {

    public static abstract class PropertyDomainEvent<T>
            extends IsisModuleExtCommandLogApplib.PropertyDomainEvent<CommandLogServiceMenu, T> { }
    public static abstract class CollectionDomainEvent<T>
            extends IsisModuleExtCommandLogApplib.CollectionDomainEvent<CommandLogServiceMenu, T> { }
    public static abstract class ActionDomainEvent
            extends IsisModuleExtCommandLogApplib.ActionDomainEvent<CommandLogServiceMenu> {
    }

    final ICommandLogRepository<? extends CommandLog> commandLogRepository;
    final ClockService clockService;

    public static class ActiveCommandsDomainEvent extends ActionDomainEvent { }
    @Action(domainEvent = ActiveCommandsDomainEvent.class, semantics = SemanticsOf.SAFE,
            typeOf = CommandLog.class)
    @ActionLayout(bookmarking = BookmarkPolicy.AS_ROOT, cssClassFa = "fa-bolt", sequence="10")
    public List<? extends CommandLog> activeCommands() {
        return commandLogRepository.findCurrent();
    }
    @MemberSupport public boolean hideActiveCommands() {
        return commandLogRepository == null;
    }


    public static class FindCommandsDomainEvent extends ActionDomainEvent { }
    @Action(domainEvent = FindCommandsDomainEvent.class, semantics = SemanticsOf.SAFE,
            typeOf = CommandLog.class)
    @ActionLayout(cssClassFa = "fa-search", sequence="20")
    public List<? extends CommandLog> findCommands(
            @Parameter(optionality= Optionality.OPTIONAL)
            @ParameterLayout(named="From")
            final LocalDate from,
            @Parameter(optionality= Optionality.OPTIONAL)
            @ParameterLayout(named="To")
            final LocalDate to) {
        return commandLogRepository.findByFromAndTo(from, to);
    }
    @MemberSupport public boolean hideFindCommands() {
        return commandLogRepository == null;
    }
    @MemberSupport public LocalDate default0FindCommands() {
        return now().minusDays(7);
    }
    @MemberSupport public LocalDate default1FindCommands() {
        return now();
    }


    public static class FindCommandByIdDomainEvent extends ActionDomainEvent { }
    @Action(domainEvent = FindCommandByIdDomainEvent.class, semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa = "fa-crosshairs", sequence="30")
    public CommandLog findCommandById(
            @ParameterLayout(named="Transaction Id")
            final UUID transactionId) {
        return commandLogRepository.findByInteractionId(transactionId).orElse(null);
    }
    @MemberSupport public boolean hideFindCommandById() {
        return commandLogRepository == null;
    }


    public static class TruncateLogDomainEvent extends ActionDomainEvent { }
    @Action(domainEvent = TruncateLogDomainEvent.class, semantics = SemanticsOf.IDEMPOTENT_ARE_YOU_SURE, restrictTo = RestrictTo.PROTOTYPING)
    @ActionLayout(cssClassFa = "fa-trash", sequence="40")
    public void truncateLog() {
        commandLogRepository.truncateLog();
    }


    private LocalDate now() {
        return clockService.getClock().nowAsLocalDate(ZoneId.systemDefault());
    }

}

