package org.apache.isis.applib.services.eventbus;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.apache.isis.applib.services.i18n.TranslatableString;

public class AbstractDomainEvent_veto_Test {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    AbstractDomainEvent ev = new AbstractDomainEvent() { };

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
        final AbstractDomainEvent ev = new AbstractDomainEvent() {
        };
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