/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.isis.extensions.audittrail.applib.app;

import java.time.LocalDate;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.extensions.audittrail.applib.IsisModuleExtAuditTrailApplib;
import org.apache.isis.extensions.audittrail.applib.dom.AuditTrailEntry;
import org.apache.isis.extensions.audittrail.applib.dom.AuditTrailEntryRepository;

import lombok.RequiredArgsConstructor;


/**
 * This service exposes a &lt;Sessions&gt; menu to the secondary menu bar for searching for sessions.
 */
@Named(AuditTrailMenu.LOGICAL_TYPE_NAME)
@DomainService(nature = NatureOfService.VIEW)
@DomainServiceLayout(
        menuBar = DomainServiceLayout.MenuBar.SECONDARY,
        named = "Activity"
)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class AuditTrailMenu {

    static final String LOGICAL_TYPE_NAME = IsisModuleExtAuditTrailApplib.NAMESPACE + ".AuditTrailMenu";

    public static abstract class ActionDomainEvent<T> extends IsisModuleExtAuditTrailApplib.ActionDomainEvent<T> { }

    final AuditTrailEntryRepository<? extends AuditTrailEntry> auditTrailEntryRepository;
    final ClockService clockService;

    @Action(
            domainEvent = findAuditEntries.ActionDomainEvent.class,
            semantics = SemanticsOf.SAFE
    )
    @ActionLayout(
            cssClassFa = "fa-search"
    )
    public class findAuditEntries {

        public class ActionDomainEvent extends AuditTrailMenu.ActionDomainEvent<findAuditEntries> { }

        @MemberSupport public List<? extends AuditTrailEntry> act(
                final @Nullable LocalDate from,
                final @Nullable LocalDate to) {
            return auditTrailEntryRepository.findByFromAndTo(from, to);
        }
        @MemberSupport public LocalDate default0Act() {
            return clockService.getClock().nowAsLocalDate().minusDays(7);
        }
        @MemberSupport public LocalDate default1Act() {
            return clockService.getClock().nowAsLocalDate();
        }
    }

}
