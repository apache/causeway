/**
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
package org.apache.isis.applib.services.background;

import org.apache.isis.applib.services.command.Command;
import org.apache.isis.schema.cmd.v1.CommandDto;

/**
 * Persists a {@link org.apache.isis.schema.cmd.v1.CommandDto memento-ized} command such that it can be executed asynchronously,
 * for example through a Quartz scheduler.
 *
 * <p>
 *     If an implementation of {@link BackgroundCommandService} also implements this interface, then its version of {@link #schedule(ActionInvocationMemento, Command, String, String, String)}  will be used instead.
 * </p>
 *
 */
public interface BackgroundCommandService2 extends BackgroundCommandService {

    /**
     * Will be called instead of
     * {@link BackgroundCommandService#schedule(ActionInvocationMemento, Command, String, String, String)}
     * (ie if the implementation implements this interface rather than simply {@link BackgroundCommandService}).
     */
    void schedule(
            final CommandDto dto,
            final Command parentCommand,
            final String targetClassName,
            final String targetActionName,
            final String targetArgs);
}
