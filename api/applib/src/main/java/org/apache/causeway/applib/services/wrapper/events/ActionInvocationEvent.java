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
package org.apache.causeway.applib.services.wrapper.events;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;

/**
 * Supported only by {@link WrapperFactory}</i>, represents a check
 * as to whether a particular argument for an action is valid
 * or not.
 * <p>
 * If {@link #getReason()} is not <tt>null</tt> then provides the reason why the
 * set of arguments are invalid; otherwise the arguments are valid.
 * <p>
 * Called after each of the {@link ActionArgumentEvent}s.
 *
 * @since 1.x {@index}
 */
public class ActionInvocationEvent extends ValidityEvent {

    public ActionInvocationEvent(final Object source, final Identifier actionIdentifier, final Object[] args) {
        super(source, actionIdentifier);
        this.args = args;
    }

    private Object[] args;

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(final Object[] args) {
        this.args = args;
    }

    /**
     * Does not apply
     */
    @Override
    public Object getProposed() {
        return null;
    }

}
