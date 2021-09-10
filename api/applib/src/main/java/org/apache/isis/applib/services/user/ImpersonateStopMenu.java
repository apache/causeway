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
package org.apache.isis.applib.services.user;

import javax.inject.Inject;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.applib.annotation.Redirect;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.message.MessageService;

import lombok.RequiredArgsConstructor;

/**
 * Provides the UI to allow a current user to be impersonated.
 *
 * <p>
 *     All of the actions provided here are restricted to PROTOTYPE mode only;
 *     this feature is <i>not</i> intended for production use as it would imply
 *     a large security hole !
 * </p>
 *
 * @see UserService
 * @see ImpersonateMenuAdvisor
 * @see ImpersonatedUserHolder
 *
 * @since 2.0 {@index}
 */
@DomainService(
        nature = NatureOfService.VIEW,
        logicalTypeName = ImpersonateStopMenu.LOGICAL_TYPE_NAME
)
@DomainServiceLayout(
        named = "Security",
        menuBar = DomainServiceLayout.MenuBar.TERTIARY
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ImpersonateStopMenu {

    public static final String LOGICAL_TYPE_NAME = IsisModuleApplib.NAMESPACE + ".ImpersonateStopMenu";   // deliberately IS part of isis.applib

    final UserService userService;
    final MessageService messageService;


    public static abstract class ActionDomainEvent<T> extends IsisModuleApplib.ActionDomainEvent<T> {}


    @Action(
            domainEvent = stopImpersonating.ActionEvent.class,
            semantics = SemanticsOf.IDEMPOTENT,
            commandPublishing = Publishing.DISABLED,
            executionPublishing = Publishing.DISABLED,
            restrictTo = RestrictTo.PROTOTYPING
    )
    @ActionLayout(sequence = "100.3", redirectPolicy = Redirect.EVEN_IF_SAME)
    public class stopImpersonating{

        public class ActionEvent extends ActionDomainEvent<stopImpersonating> { }

        @MemberSupport public void act() {
            userService.stopImpersonating();
            messageService.informUser("No longer impersonating another user");
        }
        @MemberSupport public boolean hideAct() {
            return ! isImpersonating();
        }
    }

    private boolean isImpersonating() {
        return this.userService.supportsImpersonation() && this.userService.isImpersonating();
    }

}
