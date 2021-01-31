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
package org.apache.isis.extensions.commandlog.impl.ui;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.extensions.commandlog.impl.IsisModuleExtCommandLogImpl;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdoRepository;

import lombok.RequiredArgsConstructor;

/**
 * @since 2.0 {@index}
 */
@DomainService(
    nature = NatureOfService.VIEW,
    objectType = "isis.ext.commandLog.CommandServiceMenu"
)
@DomainServiceLayout(
    named = "Activity",
    menuBar = DomainServiceLayout.MenuBar.SECONDARY
)
@Service
@Named("isis.ext.commandLog.CommandServiceMenu")
@Order(OrderPrecedence.MIDPOINT)
@Qualifier("Jdo")
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class CommandServiceMenu {

    public static abstract class PropertyDomainEvent<T>
            extends IsisModuleExtCommandLogImpl.PropertyDomainEvent<CommandServiceMenu, T> { }
    public static abstract class CollectionDomainEvent<T>
            extends IsisModuleExtCommandLogImpl.CollectionDomainEvent<CommandServiceMenu, T> { }
    public static abstract class ActionDomainEvent
            extends IsisModuleExtCommandLogImpl.ActionDomainEvent<CommandServiceMenu> {
    }

    final CommandJdoRepository commandServiceRepository;
    final ClockService clockService;

    public static class ActiveCommandsDomainEvent extends ActionDomainEvent { }
    @Action(domainEvent = ActiveCommandsDomainEvent.class, semantics = SemanticsOf.SAFE)
    @ActionLayout(bookmarking = BookmarkPolicy.AS_ROOT, cssClassFa = "fa-bolt")
    @MemberOrder(sequence="10")
    public List<CommandJdo> activeCommands() {
        return commandServiceRepository.findCurrent();
    }
    public boolean hideActiveCommands() {
        return commandServiceRepository == null;
    }


    public static class FindCommandsDomainEvent extends ActionDomainEvent { }
    @Action(domainEvent = FindCommandsDomainEvent.class, semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa = "fa-search")
    @MemberOrder(sequence="20")
    public List<CommandJdo> findCommands(
            @Parameter(optionality= Optionality.OPTIONAL)
            @ParameterLayout(named="From")
            final LocalDate from,
            @Parameter(optionality= Optionality.OPTIONAL)
            @ParameterLayout(named="To")
            final LocalDate to) {
        return commandServiceRepository.findByFromAndTo(from, to);
    }
    public boolean hideFindCommands() {
        return commandServiceRepository == null;
    }
    public LocalDate default0FindCommands() {
        return now().minusDays(7);
    }
    public LocalDate default1FindCommands() {
        return now();
    }


    public static class FindCommandByIdDomainEvent extends ActionDomainEvent { }
    @Action(domainEvent = FindCommandByIdDomainEvent.class, semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa = "fa-crosshairs")
    @MemberOrder(sequence="30")
    public CommandJdo findCommandById(
            @ParameterLayout(named="Transaction Id")
            final UUID transactionId) {
        return commandServiceRepository.findByUniqueId(transactionId).orElse(null);
    }
    public boolean hideFindCommandById() {
        return commandServiceRepository == null;
    }


    public static class TruncateLogDomainEvent extends ActionDomainEvent { }
    @Action(domainEvent = TruncateLogDomainEvent.class, semantics = SemanticsOf.IDEMPOTENT_ARE_YOU_SURE, restrictTo = RestrictTo.PROTOTYPING)
    @ActionLayout(cssClassFa = "fa-trash")
    @MemberOrder(sequence="40")
    public void truncateLog() {
        commandServiceRepository.truncateLog();
    }


    private LocalDate now() {
        return clockService.getClock().localDate(ZoneId.systemDefault());
    }

}

