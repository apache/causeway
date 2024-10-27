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
package org.apache.causeway.viewer.graphql.viewer.test.domain.admin;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.commons.internal.base._Strings;

import lombok.RequiredArgsConstructor;

@Named("university.admin.AdminMenu")
@DomainService
@Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class AdminMenu {

    @Action(semantics = SemanticsOf.NON_IDEMPOTENT)
    @ActionLayout(hidden = Where.EVERYWHERE)
    public void adminAction() {
    }

    @Action(semantics = SemanticsOf.NON_IDEMPOTENT)
    public void otherAdminAction() {
    }
    public String disableOtherAdminAction() {
        return "yup, disabled!";
    }

    @Action(semantics = SemanticsOf.NON_IDEMPOTENT)
    public void actionWithDisabledParam(final String firstParam, final String secondParam, final String thirdParameter) {
    }
    public String disable0ActionWithDisabledParam() {
        return "yup, disabled!";
    }
    public String disable2ActionWithDisabledParam(final String firstParam, final String secondParam) {
        return _Strings.isNullOrEmpty(secondParam) ? null : "Disabled because secondParam is not empty";
    }

    @Action(semantics = SemanticsOf.NON_IDEMPOTENT)
    public void actionWithHiddenParam(final String firstParam, final String secondParam) {
    }
    public boolean hide0ActionWithHiddenParam() {
        return true;
    }

}
