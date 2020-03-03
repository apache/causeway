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
package org.apache.isis.applib.services.command;

// tag::refguide[]
public interface CommandExecutorService {

    // end::refguide[]
    // tag::refguide-2[]
    enum SudoPolicy {

        // end::refguide-2[]
        /**
         * For example, regular background commands.
         */
        // tag::refguide-2[]
        NO_SWITCH,

        // end::refguide-2[]
        /**
         * For example, replayable commands.
         */
        // tag::refguide-2[]
        SWITCH,
    }
    // end::refguide-2[]

    /**
     * Executes the specified command.
     *
     * @param sudoPolicy
     * @param commandWithDto
     * @return - any exception raised by the command.
     */
    // tag::refguide[]
    void executeCommand(
            SudoPolicy sudoPolicy,              // <.>
            CommandWithDto commandWithDto       // <.>
    );

}
// end::refguide[]
