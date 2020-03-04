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
package org.apache.isis.applib.annotation;

/**
 * Whether the command should be persisted.
 */
// tag::refguide[]
public enum CommandPersistence {

    // end::refguide[]
    /**
     * (If the configured {@link org.apache.isis.applib.services.command.spi.CommandService} supports it), indicates that the
     * {@link org.apache.isis.applib.services.command.Command Command} object should be persisted.
     */
    // tag::refguide[]
    PERSISTED,
    // end::refguide[]
    /**
     * (If the configured {@link org.apache.isis.applib.services.command.spi.CommandService} supports it), indicates that the
     * {@link org.apache.isis.applib.services.command.Command Command} object should only be persisted if
     * another service, such as the {@link org.apache.isis.applib.services.background.BackgroundCommandService}, hints that it should.
     */
    // tag::refguide[]
    IF_HINTED,
    // end::refguide[]
    /**
     * (Even if the configured {@link org.apache.isis.applib.services.command.spi.CommandService} supports it), indicates that the
     * {@link org.apache.isis.applib.services.command.Command Command} object should <i>not</i> be persisted (even if
     * another service, such as the {@link org.apache.isis.applib.services.background.BackgroundCommandService}, hints that it should).
     */
    // tag::refguide[]
    NOT_PERSISTED

}
// end::refguide[]
