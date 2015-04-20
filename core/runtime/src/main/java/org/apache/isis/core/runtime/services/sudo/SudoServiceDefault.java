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

package org.apache.isis.core.runtime.services.sudo;

import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.sudo.SudoService;
import org.apache.isis.core.metamodel.services.container.DomainObjectContainerDefault;

@DomainService(
        nature = NatureOfService.DOMAIN
)
public class SudoServiceDefault implements SudoService {

    private DomainObjectContainerDefault containerDefault;

    @Programmatic
    @PostConstruct
    public void init() {
        if(container instanceof DomainObjectContainerDefault) {
            containerDefault = (DomainObjectContainerDefault) container;
        }
    }

    @Programmatic
    @Override
    public void sudo(final String user, final Runnable runnable) {
        ensureContainerOk();
        try {
            containerDefault.overrideUser(user);
            runnable.run();
        } finally {
            containerDefault.resetOverrides();
        }
    }

    @Programmatic
    @Override
    public <T> T sudo(final String user, final Callable<T> callable) {
        ensureContainerOk();
        try {
            containerDefault.overrideUser(user);
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            containerDefault.resetOverrides();
        }
    }

    @Programmatic
    @Override
    public void sudo(final String username, final List<String> roles, final Runnable runnable) {
        ensureContainerOk();
        try {
            containerDefault.overrideUserAndRoles(username, roles);
            runnable.run();
        } finally {
            containerDefault.resetOverrides();
        }
    }

    @Programmatic
    @Override
    public <T> T sudo(final String username, final List<String> roles, final Callable<T> callable) {
        ensureContainerOk();
        try {
            containerDefault.overrideUserAndRoles(username, roles);
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            containerDefault.resetOverrides();
        }
    }

    private void ensureContainerOk() {
        if(containerDefault == null) {
            throw new IllegalStateException("DomainObjectContainer does not support the user being overridden");
        }
    }


    @Inject
    private DomainObjectContainer container;

}
