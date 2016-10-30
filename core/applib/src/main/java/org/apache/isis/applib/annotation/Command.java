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

/**
 * @deprecated - use {@link Action#command()} instead
 */
@Deprecated
@Inherited
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {

    /**
     * @deprecated - see {@link CommandPersistence}
     */
    @Deprecated
    public static enum Persistence {
        /**
         * @deprecated - see {@link CommandPersistence#PERSISTED}
         */
        @Deprecated
        PERSISTED,
        /**
         * @deprecated - see {@link CommandPersistence#IF_HINTED}
         */
        @Deprecated
        IF_HINTED,
        /**
         * @deprecated - see {@link CommandPersistence#NOT_PERSISTED}
         */
        @Deprecated
        NOT_PERSISTED
    }
    
    /**
     * @deprecated - see {@link org.apache.isis.applib.annotation.Action#commandPersistence()}.
     */
    @Deprecated
    Persistence persistence() default Persistence.PERSISTED;

    
    // //////////////////////////////////////


    /**
     * @deprecated - use {@link CommandExecuteIn}
     */
    @Deprecated
    public static enum ExecuteIn {
        /**
         * @deprecated - use {@link CommandExecuteIn#FOREGROUND}
         */
        @Deprecated
        FOREGROUND,
        /**
         * @deprecated - use {@link CommandExecuteIn#BACKGROUND}
         */
        @Deprecated
        BACKGROUND;



    }


    /**
     * @deprecated - use {@link Action#commandExecuteIn()}
     */
    @Deprecated
    ExecuteIn executeIn() default ExecuteIn.FOREGROUND;

    
    /**
     * @deprecated - use {@link Action#command()} to specify if the action is handled as a command or not.
     */
    @Deprecated
    boolean disabled() default false;

}
