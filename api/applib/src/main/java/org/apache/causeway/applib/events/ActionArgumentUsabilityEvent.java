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
package org.apache.causeway.applib.events;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.services.wrapper.events.UsabilityEvent;

/**
 * Represents a check as to whether a particular argument for an action is disabled
 * or not.
 * <p>
 * If {@link #getReason()} is not <tt>null</tt> then provides the reason why the
 * argument's parameter should be disabled; otherwise the parameter can be entered.
 */
public class ActionArgumentUsabilityEvent extends UsabilityEvent {

    private final Object[] args;
    private final int position;

    public ActionArgumentUsabilityEvent(
            final Object source, final Identifier actionIdentifier,
            final Object[] args, final int position) {
        super(source, actionIdentifier);
        this.args = args;
        this.position = position;
    }

    public Object[] getArgs() {
        return args;
    }

    /**
     * The position (0-based) of the disabled parameter.
     */
    public int getPosition() {
        return position;
    }

    @Override
    public String getReasonMessage() {
        return String.format("Invalid action argument. Position: %s. Reason: %s",
                this.getPosition(), super.getReasonMessage());
    }

}
