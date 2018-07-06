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

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.sudo.SudoService;
import org.apache.isis.applib.services.user.UserService;

@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + Integer.MAX_VALUE
        )
public class SudoServiceDefault implements SudoService {

    @Programmatic
    @Override
    public void sudo(final String username, final Runnable runnable) {
        try {
            runAs(username, null);
            runnable.run();
        } finally {
            releaseRunAs();
        }
    }

    @Programmatic
    @Override
    public <T> T sudo(final String username, final Callable<T> callable) {
        try {
            runAs(username, null);
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            releaseRunAs();
        }
    }

    @Programmatic
    @Override
    public void sudo(final String username, final List<String> roles, final Runnable runnable) {
        try {
            runAs(username, roles);
            runnable.run();
        } finally {
            releaseRunAs();
        }
    }

    @Programmatic
    @Override
    public <T> T sudo(final String username, final List<String> roles, final Callable<T> callable) {
        try {
            runAs(username, roles);
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            releaseRunAs();
        }
    }

    private void runAs(final String username, final List<String> roles) {
        if(spiServices != null) {
            for (SudoService.Spi spiService : spiServices) {
                spiService.runAs(username, roles);
            }
        }
    }

    private void releaseRunAs() {
        if(spiServices != null) {
            for (SudoService.Spi spiService : spiServices) {
                spiService.releaseRunAs();
            }
        }
    }


    @javax.inject.Inject
    private UserService userService;

    @javax.inject.Inject
    private List<Spi> spiServices;

}
