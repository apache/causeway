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
package org.apache.isis.extensions.commandlog.jpa;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.extensions.commandlog.applib.IsisModuleExtCommandLogApplib;
import org.apache.isis.extensions.commandlog.applib.app.CommandLogServiceMenu;
import org.apache.isis.extensions.commandlog.applib.subscriber.CommandSubscriberForCommandLog;
import org.apache.isis.extensions.commandlog.jpa.dom.CommandLogEntry;
import org.apache.isis.extensions.commandlog.jpa.dom.CommandLogEntryRepository;

/**
 * @since 2.0 {@index}
 */
@Configuration
@Import({
        // modules
        IsisModuleExtCommandLogApplib.class,

        // @DomainService's
        CommandLogServiceMenu.class,

        // @Service's
        CommandLogEntryRepository.class,
        org.apache.isis.extensions.commandlog.applib.dom.CommandLogEntry.TableColumnOrderDefault.class,
        CommandSubscriberForCommandLog.class,

        // entities
        CommandLogEntry.class
})
@EntityScan(basePackageClasses = {
        CommandLogEntry.class,
})
public class IsisModuleExtCommandLogJpa {

    public static final String NAMESPACE = IsisModuleExtCommandLogApplib.NAMESPACE;
    public static final String SCHEMA = IsisModuleExtCommandLogApplib.SCHEMA;


}
