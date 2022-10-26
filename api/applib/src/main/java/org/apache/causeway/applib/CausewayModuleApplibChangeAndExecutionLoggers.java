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
package org.apache.causeway.applib;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.causeway.applib.services.publishing.log.CommandLogger;
import org.apache.causeway.applib.services.publishing.log.EntityChangesLogger;
import org.apache.causeway.applib.services.publishing.log.EntityPropertyChangeLogger;
import org.apache.causeway.applib.services.publishing.log.ExecutionLogger;

/**
 * Registers logging subscribers for the command/execution/change publishing subsystem.
 *
 * @since 2.0 {@index}
 */
@Configuration
@Import({
    // Modules
    CausewayModuleApplib.class,

    // Execution/Change Loggers
    CommandLogger.class,
    EntityChangesLogger.class,
    EntityPropertyChangeLogger.class,
    ExecutionLogger.class,
})
public class CausewayModuleApplibChangeAndExecutionLoggers {

}
