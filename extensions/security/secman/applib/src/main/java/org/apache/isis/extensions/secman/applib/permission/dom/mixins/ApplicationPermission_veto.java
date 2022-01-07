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
package org.apache.isis.extensions.secman.applib.permission.dom.mixins;

import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.annotations.ActionLayout;
import org.apache.isis.applib.annotations.MemberSupport;
import org.apache.isis.applib.annotations.SemanticsOf;
import org.apache.isis.extensions.secman.applib.IsisModuleExtSecmanApplib;
import org.apache.isis.extensions.secman.applib.permission.dom.ApplicationPermission;
import org.apache.isis.extensions.secman.applib.permission.dom.ApplicationPermissionRule;
import org.apache.isis.extensions.secman.applib.permission.dom.mixins.ApplicationPermission_veto.DomainEvent;

import lombok.RequiredArgsConstructor;

@Action(
        domainEvent = DomainEvent.class,
        semantics = SemanticsOf.IDEMPOTENT
)
@ActionLayout(
        associateWith = "rule",
        sequence = "2"
)
@RequiredArgsConstructor
public class ApplicationPermission_veto {

    public static class DomainEvent
            extends IsisModuleExtSecmanApplib.ActionDomainEvent<ApplicationPermission_veto> {}

    private final ApplicationPermission target;

    @MemberSupport public ApplicationPermission act() {
        target.setRule(ApplicationPermissionRule.VETO);
        return target;
    }

    @MemberSupport public String disableAct() {
        return target.getRule() == ApplicationPermissionRule.VETO? "Rule is already set to VETO": null;
    }

}
