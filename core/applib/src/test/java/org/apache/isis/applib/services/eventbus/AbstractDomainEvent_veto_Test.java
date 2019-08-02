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
package org.apache.isis.applib.services.eventbus;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.apache.isis.applib.events.domain.AbstractDomainEvent;
import org.apache.isis.applib.services.i18n.TranslatableString;

public class AbstractDomainEvent_veto_Test {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    AbstractDomainEvent<?> ev = new AbstractDomainEvent<Object>() { private static final long serialVersionUID = 1L; };

    @Test
    public void hidden_phase_and_veto_using_null() throws Exception {

        // given
        ev.setEventPhase(AbstractDomainEvent.Phase.HIDE);

        // when
        ev.veto((String)null);

        // then ... vetoes
        Assert.assertTrue(ev.isHidden());
        Assert.assertFalse(ev.isDisabled());
        Assert.assertNull(ev.getDisabledReason());
        Assert.assertFalse(ev.isInvalid());
        Assert.assertNull(ev.getInvalidityReason());
    }

    @Test
    public void hidden_phase_and_veto_using_non_null_string() throws Exception {
        // given
        ev.setEventPhase(AbstractDomainEvent.Phase.HIDE);

        // when
        ev.veto("hidden");

        // then ... vetoes
        Assert.assertTrue(ev.isHidden());
        Assert.assertFalse(ev.isDisabled());
        Assert.assertNull(ev.getDisabledReason());
        Assert.assertFalse(ev.isInvalid());
        Assert.assertNull(ev.getInvalidityReason());
    }

    @Test
    public void hidden_phase_and_veto_using_translatable() throws Exception {
        // given
        ev.setEventPhase(AbstractDomainEvent.Phase.HIDE);

        // when
        final TranslatableString reason = TranslatableString.tr("hidden");
        ev.veto(reason);

        // then ... vetoes
        Assert.assertTrue(ev.isHidden());
        Assert.assertFalse(ev.isDisabled());
        Assert.assertNull(ev.getDisabledReason());
        Assert.assertFalse(ev.isInvalid());
        Assert.assertNull(ev.getInvalidityReason());
    }

    @Test
    public void disable_phase_and_attempt_to_veto_with_null() throws Exception {

        // given
        final AbstractDomainEvent<?> ev = new AbstractDomainEvent<Object>() { private static final long serialVersionUID = 1L; };
        ev.setEventPhase(AbstractDomainEvent.Phase.DISABLE);

        // expect
        expectedException.expect(IllegalArgumentException.class);

        // when
        ev.veto((String)null);
    }

    @Test
    public void disable_phase_and_veto_using_non_null_string() throws Exception {
        // given
        ev.setEventPhase(AbstractDomainEvent.Phase.DISABLE);

        // when
        final String reason = "no, you can't do that";
        ev.veto(reason);

        // then
        Assert.assertFalse(ev.isHidden());
        Assert.assertTrue(ev.isDisabled());
        Assert.assertEquals(reason, ev.getDisabledReason());
        Assert.assertFalse(ev.isInvalid());
        Assert.assertNull(ev.getInvalidityReason());
    }

    @Test
    public void disable_phase_and_veto_using_translatable() throws Exception {
        // given
        ev.setEventPhase(AbstractDomainEvent.Phase.DISABLE);

        // when
        final TranslatableString reason = TranslatableString.tr("no, you can't do that");
        ev.veto(reason);

        // then
        Assert.assertFalse(ev.isHidden());
        Assert.assertTrue(ev.isDisabled());
        Assert.assertEquals(reason, ev.getDisabledReasonTranslatable());
        Assert.assertFalse(ev.isInvalid());
        Assert.assertNull(ev.getInvalidityReason());
    }

    @Test
    public void validate_phase_and_attempt_to_veto_with_null() throws Exception {

        // given
        ev.setEventPhase(AbstractDomainEvent.Phase.VALIDATE);

        // expect
        expectedException.expect(IllegalArgumentException.class);

        // when
        ev.veto((String)null);
    }

    @Test
    public void validate_phase_and_veto_using_non_null_string() throws Exception {

        // given
        ev.setEventPhase(AbstractDomainEvent.Phase.VALIDATE);

        // when
        final String reason = "no, you can't do that";
        ev.veto(reason);

        // then
        Assert.assertFalse(ev.isHidden());
        Assert.assertFalse(ev.isDisabled());
        Assert.assertNull(ev.getDisabledReason());
        Assert.assertTrue(ev.isInvalid());
        Assert.assertEquals(reason, ev.getInvalidityReason());
    }

    @Test
    public void validate_phase_and_veto_using_translatable() throws Exception {

        // given
        ev.setEventPhase(AbstractDomainEvent.Phase.VALIDATE);

        // when
        final TranslatableString reason = TranslatableString.tr("no, you can't do that");
        ev.veto(reason);

        // then
        Assert.assertFalse(ev.isHidden());
        Assert.assertFalse(ev.isDisabled());
        Assert.assertNull(ev.getDisabledReason());
        Assert.assertTrue(ev.isInvalid());
        Assert.assertEquals(reason, ev.getInvalidityReasonTranslatable());
    }

}