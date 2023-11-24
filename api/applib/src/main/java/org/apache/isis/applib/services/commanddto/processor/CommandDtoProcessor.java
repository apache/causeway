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
package org.apache.isis.applib.services.commanddto.processor;

import org.apache.isis.schema.cmd.v2.CommandDto;

/**
 * Refine (or possibly ignore) a command when replicating from primary
 * to secondary.
 * 
 * @since 1.x {@index}
 */
public interface CommandDtoProcessor {

    /**
     * The implementation can if necessary refine or alter the
     * {@link CommandDto} to be replicated from primary to secondary.
     *
     * <p>
     *     That said, the most common use case is to return <code>null</code>,
     *     which results in the command effectively being ignore.
     * </p>
     *
     * @param commandDto - to be processed
     * @return <tt>null</tt> means that the command's DTO is effectively
     *         excluded.
     */
    CommandDto process(CommandDto commandDto);

    /**
     * Convenience implementation to simply indicate that no DTO should be
     * returned for a command, effectively ignoring it for replication purposes.
     */
    class Null implements CommandDtoProcessor {
        @Override
        public CommandDto process(final CommandDto commandDto) {
            return null;
        }
    }

}
