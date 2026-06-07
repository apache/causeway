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
package org.apache.causeway.applib.services.command;

import org.apache.causeway.applib.events.EventObjectBase;

/**
 * Pauses command-log persistence for a scoped operation.
 *
 * <p>
 * Command logging implementations can subscribe to this event to temporarily ignore command lifecycle notifications
 * without disabling command publishing itself.
 * Every pause event should be paired with a {@link ResumeCommandLoggingEvent}, preferably from a {@code finally} block.
 * Nested pauses are supported by command logging implementations that track pause depth, so command logging resumes
 * only after all active pauses have been resumed.
 * </p>
 *
 * @since 3.x {@index}
 */
public class PauseCommandLoggingEvent extends EventObjectBase<Object> {

    public PauseCommandLoggingEvent(final Object source) {
        super(source);
    }

}
