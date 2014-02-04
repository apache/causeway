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

import org.apache.isis.applib.annotation.Programmatic;

/**
 * Register this as a service in order to access context information about any {@link Command}.
 */
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
    public void setCommand(Command command) {
        this.command = command;
    }

}
