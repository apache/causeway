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
package org.apache.causeway.extensions.commandlog.applib.dom;

/**
 * Whether the command is executed explicitly by the end-user, or is scheduled (for example, using the
 * {@link BackgroundService}) to be executed asynchronously at some later time.
 *
 * @since 2.0 {@index}
 */
public enum ExecuteIn {
    /**
     * Command executed in immediately, in the current thread of execution.
     */
    FOREGROUND,

    /**
     * Command scheduled to be executed at some later time, in a &quot;background&quot; thread of execution.
     */
    BACKGROUND
}
