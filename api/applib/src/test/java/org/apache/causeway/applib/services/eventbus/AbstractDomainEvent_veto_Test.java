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
package org.apache.causeway.applib.services.eventbus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.events.domain.AbstractDomainEvent;
import org.apache.causeway.applib.services.i18n.TranslatableString;

class AbstractDomainEvent_veto_Test {

    AbstractDomainEvent<?> ev = new AbstractDomainEvent<Object>() {  };

    @Test
    public void hidden_phase_and_veto_using_null() throws Exception {

        // given
        ev.setEventPhase(AbstractDomainEvent.Phase.HIDE);

        // when
        ev.veto((String)null);

        // then ... vetoes
        assertTrue(ev.isHidden());
        assertFalse(ev.isDisabled());
        assertNull(ev.getDisabledReason());
        assertFalse(ev.isInvalid());
        assertNull(ev.getInvalidityReason());
    }

    @Test
    public void hidden_phase_and_veto_using_non_null_string() throws Exception {
        // given
        ev.setEventPhase(AbstractDomainEvent.Phase.HIDE);

        // when
        ev.veto("hidden");

        // then ... vetoes
        assertTrue(ev.isHidden());
        assertFalse(ev.isDisabled());
        assertNull(ev.getDisabledReason());
        assertFalse(ev.isInvalid());
        assertNull(ev.getInvalidityReason());
    }

    @Test
    public void hidden_phase_and_veto_using_translatable() throws Exception {
        // given
        ev.setEventPhase(AbstractDomainEvent.Phase.HIDE);

        // when
        final TranslatableString reason = TranslatableString.tr("hidden");
        ev.veto(reason);

        // then ... vetoes
        assertTrue(ev.isHidden());
        assertFalse(ev.isDisabled());
        assertNull(ev.getDisabledReason());
        assertFalse(ev.isInvalid());
        assertNull(ev.getInvalidityReason());
    }

    @Test
    public void disable_phase_and_attempt_to_veto_with_null() throws Exception {

        // given
        final AbstractDomainEvent<?> ev = new AbstractDomainEvent<Object>() {  };
        ev.setEventPhase(AbstractDomainEvent.Phase.DISABLE);

        // when
        assertThrows(IllegalArgumentException.class, ()->
            ev.veto((String)null));
    }

    @Test
    public void disable_phase_and_veto_using_non_null_string() throws Exception {
        // given
        ev.setEventPhase(AbstractDomainEvent.Phase.DISABLE);

        // when
        final String reason = "no, you can't do that";
        ev.veto(reason);

        // then
        assertFalse(ev.isHidden());
        assertTrue(ev.isDisabled());
        assertEquals(reason, ev.getDisabledReason());
        assertFalse(ev.isInvalid());
        assertNull(ev.getInvalidityReason());
    }

    @Test
    public void disable_phase_and_veto_using_translatable() throws Exception {
        // given
        ev.setEventPhase(AbstractDomainEvent.Phase.DISABLE);

        // when
        final TranslatableString reason = TranslatableString.tr("no, you can't do that");
        ev.veto(reason);

        // then
        assertFalse(ev.isHidden());
        assertTrue(ev.isDisabled());
        assertEquals(reason, ev.getDisabledReasonTranslatable());
        assertFalse(ev.isInvalid());
        assertNull(ev.getInvalidityReason());
    }

    @Test
    public void validate_phase_and_attempt_to_veto_with_null() throws Exception {

        // given
        ev.setEventPhase(AbstractDomainEvent.Phase.VALIDATE);

        // when
        assertThrows(IllegalArgumentException.class, ()->
            ev.veto((String)null));
    }

    @Test
    public void validate_phase_and_veto_using_non_null_string() throws Exception {

        // given
        ev.setEventPhase(AbstractDomainEvent.Phase.VALIDATE);

        // when
        final String reason = "no, you can't do that";
        ev.veto(reason);

        // then
        assertFalse(ev.isHidden());
        assertFalse(ev.isDisabled());
        assertNull(ev.getDisabledReason());
        assertTrue(ev.isInvalid());
        assertEquals(reason, ev.getInvalidityReason());
    }

    @Test
    public void validate_phase_and_veto_using_translatable() throws Exception {

        // given
        ev.setEventPhase(AbstractDomainEvent.Phase.VALIDATE);

        // when
        final TranslatableString reason = TranslatableString.tr("no, you can't do that");
        ev.veto(reason);

        // then
        assertFalse(ev.isHidden());
        assertFalse(ev.isDisabled());
        assertNull(ev.getDisabledReason());
        assertTrue(ev.isInvalid());
        assertEquals(reason, ev.getInvalidityReasonTranslatable());
    }

}