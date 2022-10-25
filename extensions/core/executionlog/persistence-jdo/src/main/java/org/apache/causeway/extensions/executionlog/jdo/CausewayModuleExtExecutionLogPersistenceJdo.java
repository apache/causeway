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
package org.apache.causeway.extensions.executionlog.jdo;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.causeway.extensions.executionlog.applib.CausewayModuleExtExecutionLogApplib;
import org.apache.causeway.extensions.executionlog.jdo.dom.ExecutionLogEntry;
import org.apache.causeway.extensions.executionlog.jdo.dom.ExecutionLogEntryPK;
import org.apache.causeway.extensions.executionlog.jdo.dom.ExecutionLogEntryRepository;
import org.apache.causeway.persistence.jdo.datanucleus.CausewayModulePersistenceJdoDatanucleus;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.causeway.testing.fixtures.applib.teardown.jdo.TeardownFixtureJdoAbstract;

/**
 * @since 2.0 {@index}
 */
@Configuration
@Import({
        // modules
        CausewayModuleExtExecutionLogApplib.class,
        CausewayModulePersistenceJdoDatanucleus.class,

        // @Service's
        ExecutionLogEntryRepository.class,
        ExecutionLogEntryPK.Semantics.class,

        // entities
        ExecutionLogEntry.class
})
public class CausewayModuleExtExecutionLogPersistenceJdo {

    public static final String NAMESPACE = CausewayModuleExtExecutionLogApplib.NAMESPACE;
    public static final String SCHEMA = CausewayModuleExtExecutionLogApplib.SCHEMA;

    /**
     * For tests that need to delete the command table first.
     * Should be run in the @BeforeEach of the test.
     */
    public FixtureScript getTeardownFixtureWillDelete() {
        return new TeardownFixtureJdoAbstract() {
            @Override
            protected void execute(final ExecutionContext executionContext) {
                deleteFrom(ExecutionLogEntry.class);
            }
        };
    }

}
