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

/**
 * Whether a command should be executed immediately and synchronously in the foreground or rather should only be
 * persisted (such that it can be executed asynchronously in the background by some other mechanism).
 *
 * <p>
 *     Note: this enum is <i>not</i> an inner class of the {@link org.apache.isis.applib.annotation.Action} annotation
 *     because in the future we may also support commands for {@link org.apache.isis.applib.annotation.Property} and
 *     {@link org.apache.isis.applib.annotation.Collection}.
 * </p>
 */
public enum CommandExecuteIn {
    /**
     * Execute synchronously in the &quot;foreground&quot;, wait for the results.
     */
    FOREGROUND,
    /**
     * Execute &quot;asynchronously&quot; through the {@link org.apache.isis.applib.services.background.BackgroundCommandService}, returning (if possible) the
     * persisted {@link org.apache.isis.applib.services.command.Command command} object as a placeholder to the
     * result.
     */
    BACKGROUND;

    @Deprecated
    public static CommandExecuteIn from(final Command.ExecuteIn executeIn) {
        if(executeIn == null) return null;
        if(executeIn == Command.ExecuteIn.FOREGROUND) return FOREGROUND;
        if(executeIn == Command.ExecuteIn.BACKGROUND) return BACKGROUND;
        // shouldn't happen
        throw new IllegalArgumentException("Unrecognized : executeIn" + executeIn);
    }

    @Deprecated
    public static Command.ExecuteIn from(final CommandExecuteIn commandExecuteIn) {
        if(commandExecuteIn == null) return null;
        if(commandExecuteIn == FOREGROUND) return Command.ExecuteIn.FOREGROUND;
        if(commandExecuteIn == BACKGROUND) return Command.ExecuteIn.BACKGROUND;
        // shouldn't happen
        throw new IllegalArgumentException("Unrecognized : executeIn" + commandExecuteIn);
    }

    public static class Type {

        private Type() {}

        public static class Meta {

            public static final int MAX_LEN = 10;

            private Meta() {}

        }
    }


}
