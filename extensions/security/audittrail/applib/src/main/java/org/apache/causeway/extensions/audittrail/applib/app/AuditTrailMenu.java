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

package org.apache.causeway.extensions.audittrail.applib.app;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.DomainServiceLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.extensions.audittrail.applib.CausewayModuleExtAuditTrailApplib;
import org.apache.causeway.extensions.audittrail.applib.dom.AuditTrailEntry;
import org.apache.causeway.extensions.audittrail.applib.dom.AuditTrailEntryRepository;

import lombok.RequiredArgsConstructor;


/**
 * This service exposes a set of menu actions to search and list {@link AuditTrailEntry audit record}s,
 * by default under the &quot;Activity&quot; secondary menu.
 *
 * @since 2.0 {@index}
 */
@Named(AuditTrailMenu.LOGICAL_TYPE_NAME)
@DomainService(nature = NatureOfService.VIEW)
@DomainServiceLayout(
    menuBar = DomainServiceLayout.MenuBar.SECONDARY,
    named = "Activity"
)
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class AuditTrailMenu {

    public static final String LOGICAL_TYPE_NAME =
            CausewayModuleExtAuditTrailApplib.NAMESPACE + ".AuditTrailMenu";

    public static abstract class ActionDomainEvent<T>
            extends CausewayModuleExtAuditTrailApplib.ActionDomainEvent<T> { }


    final AuditTrailEntryRepository<? extends AuditTrailEntry> auditTrailEntryRepository;
    final ClockService clockService;


    @Action(
            domainEvent = findMostRecent.DomainEvent.class,
            semantics = SemanticsOf.SAFE,
            typeOf = AuditTrailEntry.class
    )
    @ActionLayout(cssClassFa = "fa-search", sequence="20")
    public class findMostRecent {
        public class DomainEvent extends ActionDomainEvent<findMostRecent> { }

        @MemberSupport public List<? extends AuditTrailEntry> act() {
            return auditTrailEntryRepository.findMostRecent();
        }
    }


    @Action(
            domainEvent = findAuditEntries.DomainEvent.class,
            semantics = SemanticsOf.SAFE,
            typeOf = AuditTrailEntry.class
    )
    @ActionLayout(cssClassFa = "fa-search", sequence="30")
    public class findAuditEntries {
        public class DomainEvent extends ActionDomainEvent<findAuditEntries> { }

        @MemberSupport public List<? extends AuditTrailEntry> act(
                final @Nullable LocalDate from,
                final @Nullable LocalDate to) {
            return auditTrailEntryRepository.findByFromAndTo(from, to);
        }
        @MemberSupport public LocalDate default0Act() {
            return now().minusDays(7);
        }
        @MemberSupport public LocalDate default1Act() {
            return now();
        }
    }



    @Action(
            domainEvent = findAll.DomainEvent.class,
            restrictTo = RestrictTo.PROTOTYPING,
            semantics = SemanticsOf.SAFE,
            typeOf = AuditTrailEntry.class
    )
    @ActionLayout(cssClassFa = "fa-search", sequence="40")
    public class findAll {
        public class DomainEvent extends ActionDomainEvent<findAll> { }

        @MemberSupport public List<? extends AuditTrailEntry> act() {
            return auditTrailEntryRepository.findAll();
        }
    }




    private LocalDate now() {
        return clockService.getClock().nowAsLocalDate(ZoneId.systemDefault());
    }
}
