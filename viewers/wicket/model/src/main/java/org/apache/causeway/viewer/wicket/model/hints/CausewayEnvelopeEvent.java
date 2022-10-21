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
package org.apache.causeway.viewer.wicket.model.hints;


import org.apache.wicket.event.IEvent;

/**
 * Broadcast down by <tt>PageAbstract</tt> to all child components
 */
public class CausewayEnvelopeEvent extends CausewayEventAbstract {

    private final CausewayEventLetterAbstract letter;

    public CausewayEnvelopeEvent(CausewayEventLetterAbstract ev) {
        super(ev.getTarget());
        this.letter = ev;
    }

    public CausewayEventLetterAbstract getLetter() {
        return letter;
    }

    /**
     * Opens the event, checks the event is an {@link org.apache.causeway.viewer.wicket.model.hints.CausewayEnvelopeEvent envelope} (event),
     * fetches the {@link org.apache.causeway.viewer.wicket.model.hints.CausewayEventLetterAbstract letter} (event) inside, and
     * returns if the letter is of the required type.
     *
     * <p>
     * Otherwise returns <tt>null</tt>.
     * </p>
     */
    public static <T> T openLetter(final IEvent<?> event, Class<T> letterEventClass) {
        if(!(event.getPayload() instanceof CausewayEnvelopeEvent)) {
            return null;
        }
        final CausewayEnvelopeEvent ev = (CausewayEnvelopeEvent) event.getPayload();
        final CausewayEventLetterAbstract letter = ev.getLetter();
        if(letter == null) {
            return null;
        }
        if(!letterEventClass.isAssignableFrom(letter.getClass())) {
            return null;
        }
        return letterEventClass.cast(letter);
    }


}

