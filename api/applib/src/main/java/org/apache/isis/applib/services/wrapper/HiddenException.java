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

package org.apache.isis.applib.services.wrapper;

import org.apache.isis.applib.exceptions.InteractionException;
import org.apache.isis.applib.services.wrapper.events.InteractionEvent;
import org.apache.isis.applib.services.wrapper.events.VisibilityEvent;

/**
 * Superclass of exceptions which indicate an attempt to interact with a class
 * member that is in some way hidden or invisible.
 * @since 2.0 {@index}
 */
public class HiddenException extends InteractionException {

    private static final long serialVersionUID = 1L;

    public HiddenException(final InteractionEvent interactionEvent) {
        super(interactionEvent);
    }

    @Override
    public VisibilityEvent getInteractionEvent() {
        return (VisibilityEvent) super.getInteractionEvent();
    }

}
