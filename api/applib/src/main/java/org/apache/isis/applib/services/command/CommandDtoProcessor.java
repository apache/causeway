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

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.schema.cmd.v2.CommandDto;

public interface CommandDtoProcessor {

    /**
     * Returning <tt>null</tt> means that the command's DTO is effectively excluded from any list.
     * If replicating from master to slave, this allows commands that can't be replicated to be ignored.
     * @param command
     * @param commandDto
     */
    @Programmatic
    CommandDto process(final Command command, CommandDto commandDto);


    /**
     * Convenience implementation to simply indicate that no DTO should be returned for a command,
     * effectively ignoring it for replay purposes.
     */
    public static class Null implements CommandDtoProcessor {
        @Override
        public CommandDto process(
                final Command command,
                final CommandDto commandDto) {
            return null;
        }
    }

}
