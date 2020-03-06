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
package org.apache.isis.subdomains.base.applib.utils;

import org.assertj.core.api.Assertions;
import org.junit.Test;

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