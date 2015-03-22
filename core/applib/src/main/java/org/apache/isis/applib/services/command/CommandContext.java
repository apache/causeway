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
package org.apache.isis.applib.services.command;

import javax.enterprise.context.RequestScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;

/**
 * This service (API and implementation) provides access to context information about any {@link Command}.
 *
 * This implementation has no UI and there is only one implementation (this class) in applib, so it is annotated with
 * {@link org.apache.isis.applib.annotation.DomainService}.  This means that it is automatically registered and
 * available for use; no further configuration is required.
 */
@DomainService(
        nature = NatureOfService.DOMAIN
)
@RequestScoped
public class CommandContext {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(CommandContext.class);

    private Command command;

    @Programmatic
    public Command getCommand() {
        return command;
    }
    
    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     */
    @Programmatic
    public void setCommand(Command command) {
        this.command = command;
    }

}
