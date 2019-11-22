package org.incode.module.base.dom.utils;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import org.incode.module.base.dom.utils.MessageUtils;

public class MessageUtils_Test {

    @Test
    public void when_matches_pattern() throws Exception {

        final String normalized = MessageUtils.normalize(new Exception(
                "Temp *00000000: Reason: Must be in status of 'Approved'. Identifier: org.estatio.dom.invoice.Invoice$_invoice#$$(org.joda.time.LocalDate)"));
        
        Assertions.assertThat(normalized).isEqualTo("Must be in status of 'Approved'.");

    }

    @Test
    public void when_matches_pattern_nothing_before_Reason() throws Exception {

        final String normalized = MessageUtils.normalize(new Exception(
                "Reason: Must be in status of 'Approved'. Identifier: org.estatio.dom.invoice.Invoice$_invoice#$$(org.joda.time.LocalDate)"));

        Assertions.assertThat(normalized).isEqualTo("Must be in status of 'Approved'.");

    }

    @Test
    public void when_matches_pattern_nothing_extra_spaces_before_Identifier() throws Exception {

        final String normalized = MessageUtils.normalize(new Exception(
                "Temp *00000000: Reason: Must be in status of 'Approved'.    Identifier: org.estatio.dom.invoice.Invoice$_invoice#$$(org.joda.time.LocalDate)"));

        Assertions.assertThat(normalized).isEqualTo("Must be in status of 'Approved'.");

    }

    @Test
    public void when_does_not_match_pattern_no_Reason() throws Exception {

        final String message = "Temp *00000000: ReAson: Must be in status of 'Approved'. Identifier: org.estatio.dom.invoice.Invoice$_invoice#$$(org.joda.time.LocalDate)";
        final String normalized = MessageUtils.normalize(new Exception(message));

        Assertions.assertThat(normalized).isEqualTo(message);

    }

    @Test
    public void when_does_not_match_pattern_no_Identifier() throws Exception {

        final String message = "Temp *00000000: Reason: Must be in status of 'Approved'. IDentifier: org.estatio.dom.invoice.Invoice$_invoice#$$(org.joda.time.LocalDate)";
        final String normalized = MessageUtils.normalize(new Exception(message));

        Assertions.assertThat(normalized).isEqualTo(message);

    }
}