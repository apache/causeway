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
package org.apache.isis.viewer.wicket.ui.pages.accmngt.password_reset;


import org.apache.wicket.validation.Validatable;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class EmailAddressValidator_Test {

    @Test
    void valid() {
        final Validatable<String> validatable = new Validatable<>("foo@bar.com");
        EmailAddressValidator.getInstance().validate(validatable);

        Assertions.assertThat(validatable.getErrors()).isEmpty();
    }

    @Test
    void invalid() {
        final Validatable<String> validatable = new Validatable<>("foo@bar.co.");
        EmailAddressValidator.getInstance().validate(validatable);

        Assertions.assertThat(validatable.getErrors()).isNotEmpty();
    }

    @Test
    void invalid_with_plus_at_start() {
        final Validatable<String> validatable = new Validatable<>("+foo@bar.com");
        EmailAddressValidator.getInstance().validate(validatable);

        Assertions.assertThat(validatable.getErrors()).isNotEmpty();
    }

    @Test
    void valid_with_plus_at_end() {
        final Validatable<String> validatable = new Validatable<>("foo+@bar.com");
        EmailAddressValidator.getInstance().validate(validatable);

        Assertions.assertThat(validatable.getErrors()).isEmpty();
    }

    @Test
    void valid_with_plus() {
        final Validatable<String> validatable = new Validatable<>("foo+bop@bar.com");
        EmailAddressValidator.getInstance().validate(validatable);

        Assertions.assertThat(validatable.getErrors()).isEmpty();
    }

    @Test
    void valid_co_uk() {
        final Validatable<String> validatable = new Validatable<>("foo@bar.co.uk");
        EmailAddressValidator.getInstance().validate(validatable);

        Assertions.assertThat(validatable.getErrors()).isEmpty();
    }


}
