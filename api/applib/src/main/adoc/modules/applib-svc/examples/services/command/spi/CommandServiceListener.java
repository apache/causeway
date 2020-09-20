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
package org.apache.isis.applib.services.command.spi;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.command.Command;

import lombok.extern.log4j.Log4j2;

/**
 * SPI
 */
// tag::refguide[]
public interface CommandServiceListener {

    /**
     * Notifies that the command has completed.
     *
     * <p>
     *     This is an opportunity for implementations to process the command,
     *     for example to persist a representation of it.
     * </p>
     */
    // tag::refguide[]
    void onComplete(final Command command);           // <.>

    /**
     * At least one implementation is required to satisfy injection point into
     * {@link org.apache.isis.applib.services.command.CommandService}.
     */
    @Service
    @Named("isisApplib.CommandServiceListenerNull")
    @Order(OrderPrecedence.LATE)
    @Qualifier("Null")
    @Log4j2
    public static class Null implements CommandServiceListener {

        @Override
        public void onComplete(Command command) {

        }
    }
}
// end::refguide[]
