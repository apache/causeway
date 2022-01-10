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
package org.apache.isis.core.runtimeservices.user;

import java.util.Collections;
import java.util.List;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.user.ImpersonateMenuAdvisor;

/**
 * This default implementation simply returns empty lists.
 *
 * <p>
 *  This has the effect that the
 *  {@link org.apache.isis.applib.services.user.ImpersonateMenu}'s
 *  {@link org.apache.isis.applib.services.user.ImpersonateMenu.impersonateWithRoles#act(String, List, String) impersonateWithRoles}
 *  action will be hidden.
 * </p>
 */
@Service
@Named("isis.runtimeservices.ImpersonateMenuAdvisorDefault")
@javax.annotation.Priority(PriorityPrecedence.LAST)
@Qualifier("Default")
public class ImpersonateMenuAdvisorDefault implements ImpersonateMenuAdvisor {

    @Override
    public List<String> allUserNames() {
        return Collections.emptyList();
    }

    @Override
    public List<String> allRoleNames() {
        return Collections.emptyList();
    }

    @Override
    public List<String> roleNamesFor(final String username) {
        return Collections.emptyList();
    }

    @Override
    public String multiTenancyTokenFor(final String username) {
        return null;
    }

}
