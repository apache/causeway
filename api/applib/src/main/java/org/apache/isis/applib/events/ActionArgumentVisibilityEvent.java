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

package org.apache.isis.applib.events;

import org.apache.isis.applib.id.FeatureIdentifier;
import org.apache.isis.applib.services.wrapper.events.VisibilityEvent;

/**
 * <i>Supported only by {@link org.apache.isis.applib.services.wrapper.WrapperFactory} service, </i> represents a check as to whether a particular parameter for an action is visible
 * or not.
 * 
 * <p>
 * If {@link #getReason()} is not <tt>null</tt> then provides the reason why the
 * argument's parameter should be hidden; otherwise the parameter is shown.
 * 
 * @deprecated
 */
@Deprecated
public class ActionArgumentVisibilityEvent extends VisibilityEvent {

    private final Object[] args;
    private final int position;

    public ActionArgumentVisibilityEvent(
            final Object source, final FeatureIdentifier actionIdentifier,
            final Object[] args, final int position) {
        super(source, actionIdentifier);
        this.args = args;
        this.position = position;
    }

    public Object[] getArgs() {
        return args;
    }

    /**
     * The position (0-based) of the hidden parameter.
     */
    public int getPosition() {
        return position;
    }

    @Override
    public String getReasonMessage() {
        return String.format("Invalid action argument. Position: %s. Reason: %s", this.getPosition(), super.getReasonMessage());
    }

}
