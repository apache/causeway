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
package org.apache.causeway.applib.services.wrapper;

import org.apache.causeway.applib.exceptions.recoverable.InteractionException;
import org.apache.causeway.applib.services.wrapper.events.InteractionEvent;
import org.apache.causeway.applib.services.wrapper.events.ValidityEvent;

/**
 * Superclass of exceptions which indicate an attempt to interact with an object
 * or member in a way that is invalid.
 *
 * @since 1.x {@index}
 */
public class InvalidException extends InteractionException {

    private static final long serialVersionUID = 1L;

    public InvalidException(final InteractionEvent interactionEvent) {
        super(interactionEvent);
    }

    @Override
    public ValidityEvent getInteractionEvent() {
        return (ValidityEvent) super.getInteractionEvent();
    }

}
