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

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.isis.applib.services.background.BackgroundCommandService;

/**
 * Indicates how the {@link org.apache.isis.applib.services.command.Command Command} object provided by the
 * (request-scoped) {@link org.apache.isis.applib.services.command.CommandContext command context} service should be
 * used.
 * 
 */
@Inherited
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {

    public static enum Persistence {
        /**
         * The {@link org.apache.isis.applib.services.command.Command Command} object should be persisted.
         */
        PERSISTED,
        /**
         * The {@link org.apache.isis.applib.services.command.Command Command} object should only be persisted if
         * another service, such as the {@link BackgroundCommandService}, hints that it should.
         */
        IF_HINTED,
        /**
         * {@link org.apache.isis.applib.services.command.Command Command} object should not be persisted (even if
         * another service, such as the {@link BackgroundCommandService}, hints that it should).
         */
        NOT_PERSISTED
    }
    
    /**
     * How the {@link org.apache.isis.applib.services.command.Command Command} object provided by the
     * {@link org.apache.isis.applib.services.command.CommandContext CommandContext} domain service should be persisted.
     */
    Persistence persistence() default Persistence.PERSISTED;

    
    // //////////////////////////////////////

    
    public static enum ExecuteIn {
        /**
         * Execute synchronously in the &quot;foreground&quot;, wait for the results.
         */
        FOREGROUND,
        /**
         * Execute &quot;asynchronously&quot; through the {@link BackgroundCommandService}, returning (if possible) the
         * persisted {@link org.apache.isis.applib.services.command.Command command} object as a placeholder to the
         * result.
         */
        BACKGROUND
    }


    /**
     * How the command/action should be executed.
     * 
     * <p>
     * If the corresponding {@link org.apache.isis.applib.services.command.Command Command} object is persisted, 
     * then its {@link org.apache.isis.applib.services.command.Command#getExecuteIn() invocationType} property 
     * will be set to this value.
     */
    ExecuteIn executeIn() default ExecuteIn.FOREGROUND;

}
