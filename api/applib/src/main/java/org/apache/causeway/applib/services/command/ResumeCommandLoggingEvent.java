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
 * Resumes command-log persistence after a scoped pause.
 *
 * <p>
 * This event should be posted for each matching {@link PauseCommandLoggingEvent}.
 * Command logging implementations that track pause depth should tolerate unmatched resume events and never let pause
 * depth become negative.
 * </p>
 *
 * @since 3.x {@index}
 */
public class ResumeCommandLoggingEvent extends EventObjectBase<Object> {

    public ResumeCommandLoggingEvent(final Object source) {
        super(source);
    }

}
