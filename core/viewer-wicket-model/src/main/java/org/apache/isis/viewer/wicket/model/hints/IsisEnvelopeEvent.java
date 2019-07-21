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
package org.apache.isis.viewer.wicket.model.hints;


import org.apache.wicket.event.IEvent;

/**
 * Broadcast down by <tt>PageAbstract</tt> to all child components
 */
public class IsisEnvelopeEvent extends IsisEventAbstract {

    private final IsisEventLetterAbstract letter;

    public IsisEnvelopeEvent(IsisEventLetterAbstract ev) {
        super(ev.getTarget());
        this.letter = ev;
    }

    public IsisEventLetterAbstract getLetter() {
        return letter;
    }

    /**
     * Opens the event, checks the event is an {@link org.apache.isis.viewer.wicket.model.hints.IsisEnvelopeEvent envelope} (event),
     * fetches the {@link org.apache.isis.viewer.wicket.model.hints.IsisEventLetterAbstract letter} (event) inside, and
     * returns if the letter is of the required type.
     *
     * <p>
     * Otherwise returns <tt>null</tt>.
     * </p>
     */
    public static <T> T openLetter(final IEvent<?> event, Class<T> letterEventClass) {
        if(!(event.getPayload() instanceof IsisEnvelopeEvent)) {
            return null;
        }
        final IsisEnvelopeEvent ev = (IsisEnvelopeEvent) event.getPayload();
        final IsisEventLetterAbstract letter = ev.getLetter();
        if(letter == null) {
            return null;
        }
        if(!letterEventClass.isAssignableFrom(letter.getClass())) {
            return null;
        }
        return letterEventClass.cast(letter);
    }


}

