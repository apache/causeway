/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.extensions.commandreplay.secondary.executor;

import java.util.function.Supplier;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandExecutorService;
import org.apache.isis.applib.services.command.CommandOutcomeHandler;
import org.apache.isis.extensions.commandreplay.secondary.clock.TickingClockService;
import org.apache.isis.schema.cmd.v2.CommandDto;

import lombok.extern.log4j.Log4j2;

/**
 * Override of {@link CommandExecutorService} that also sets the time (using the {@link TickingClockService}) to that
 * of the {@link Command}'s {@link Command#getTimestamp() timestamp} before executing the command.
 *
 * <p>
 *     It then delegates down to the default implementation.
 * </p>
 *
 * @since 2.0 {@index}
 */
@Service
@Named("isis.ext.commandReplaySecondary.CommandExecutorServiceWithTime")
@Order(OrderPrecedence.MIDPOINT - 10) // before CommandExecutorServiceDefault
@Qualifier("WithTime")
@Log4j2
public class CommandExecutorServiceWithTime implements CommandExecutorService {

    final CommandExecutorService delegate;
    final TickingClockService tickingClockService;

    public CommandExecutorServiceWithTime(
            @Qualifier("Default") final CommandExecutorService delegate,
            final TickingClockService tickingClockService) {
        this.delegate = delegate;
        this.tickingClockService = tickingClockService;
    }

    @Override
    public Bookmark executeCommand(final Command command) {
        final Supplier<Bookmark> executeCommand = () -> delegate.executeCommand(command);
        return tickingClockService.isInitialized()
                ? tickingClockService.at(command.getTimestamp(), executeCommand)
                : executeCommand.get();
    }

    @Override
    public Bookmark executeCommand(
            final SudoPolicy sudoPolicy,
            final Command command) {
        final Supplier<Bookmark> executeCommand = () -> delegate.executeCommand(sudoPolicy, command);
        return tickingClockService.isInitialized()
                ? tickingClockService.at(command.getTimestamp(), executeCommand)
                : executeCommand.get();
    }

    @Override
    public Bookmark executeCommand(
            final CommandDto dto,
            final CommandOutcomeHandler outcomeHandler) {
        final Supplier<Bookmark> executeCommand = () -> delegate.executeCommand(dto, outcomeHandler);
        return tickingClockService.isInitialized()
                ? tickingClockService.at(
                        JavaSqlXMLGregorianCalendarMarshalling.toTimestamp(dto.getTimestamp()), executeCommand)
                : executeCommand.get();
    }

    @Override
    public Bookmark executeCommand(
            final SudoPolicy sudoPolicy,
            final CommandDto dto,
            final CommandOutcomeHandler outcomeHandler) {
        final Supplier<Bookmark> executeCommand = () -> delegate.executeCommand(sudoPolicy, dto, outcomeHandler);
        return tickingClockService.isInitialized()
                ? tickingClockService.at(
                    JavaSqlXMLGregorianCalendarMarshalling.toTimestamp(dto.getTimestamp()), executeCommand)
                : executeCommand.get();
    }

}
