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
 * Persists a {@link org.apache.isis.schema.cmd.v1.CommandDto command-reified} action such that it can be executed asynchronously,
 * for example through a Quartz scheduler.
 *
 * <p>
 * Separate from {@link BackgroundService} primarily so that the default
 * implementation, <tt>BackgroundServiceDefault</tt> (in <tt>isis-module-background</tt>) can
 * delegate to different implementations of this service.
 *
 * <p>
 * There is currently only implementation of this service, <tt>BackgroundCommandServiceJdo</tt> in
 * <tt>o.a.i.module:isis-module-command-jdo</tt>.  That implementation has no UI and no side-effects (the programmatic
 * API is through {@link org.apache.isis.applib.services.background.BackgroundService}).  It is therefore
 * annotated with {@link org.apache.isis.applib.annotation.DomainService} so that it is automatically registered as
 * a service.
 *
 */
public interface BackgroundCommandService extends AutoCloseable {

    void schedule(
            final CommandDto dto,
            final Command parentCommand,
            final String targetClassName,
            final String targetActionName,
            final String targetArgs);
    
    /**
     * @since 2.0
     */
    // refined from AutoCloseable to not throw catched exceptions
    default void close() { 
        
    }

}
