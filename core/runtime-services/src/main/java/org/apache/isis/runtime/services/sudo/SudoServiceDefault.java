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

package org.apache.isis.runtime.services.sudo;

import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.sudo.SudoService;

import lombok.extern.log4j.Log4j2;

@Service
@Named("isisRuntimeServices.SudoServiceDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
@Log4j2
public class SudoServiceDefault implements SudoService {

    @Override
    public void sudo(final String username, final Runnable runnable) {
        try {
            runAs(username, null);
            runnable.run();
        } finally {
            releaseRunAs();
        }
    }

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

    @Override
    public void sudo(final String username, final List<String> roles, final Runnable runnable) {
        try {
            runAs(username, roles);
            runnable.run();
        } finally {
            releaseRunAs();
        }
    }

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

    @Inject private List<SudoService.Spi> spiServices;

}
