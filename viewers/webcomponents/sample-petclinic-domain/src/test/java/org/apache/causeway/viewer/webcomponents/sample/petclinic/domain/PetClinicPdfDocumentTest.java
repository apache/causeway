/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.causeway.viewer.webcomponents.sample.petclinic.domain;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.annotation.LabelPosition;
import org.apache.causeway.applib.annotation.PropertyLayout;

import static org.assertj.core.api.Assertions.assertThat;

class PetClinicPdfDocumentTest {

    @Test
    void agreementPropertySuppressesItsRedundantLabel() throws NoSuchMethodException {
        final var layout = PetOwner.class.getMethod("getAgreement").getAnnotation(PropertyLayout.class);

        assertThat(layout.named()).isEqualTo("Agreement");
        assertThat(layout.labelPosition()).isEqualTo(LabelPosition.NONE);
        assertThat(layout.fieldSetId()).isEqualTo("documents");
    }

    @Test
    void agreementIsOwnerSpecificMultipageAndDeterministic() {
        final var mary = new PetOwner("owner-mary", "Mary Smith");
        mary.addSeedPet(new Pet("pet-samantha", mary, "Samantha", PetSpecies.CAT));
        mary.addSeedPet(new Pet("pet-basil", mary, "Basil", PetSpecies.DOG));

        final var first = PetClinicPdfDocument.agreementFor(mary);
        final var second = PetClinicPdfDocument.agreementFor(mary);
        final var pdf = new String(first.bytes(), StandardCharsets.US_ASCII);

        assertThat(first.name()).isEqualTo("owner-mary-clinic-agreement.pdf");
        assertThat(first.mimeType().getBaseType()).isEqualTo("application/pdf");
        assertThat(first.bytes()).isEqualTo(second.bytes());
        assertThat(pdf)
                .contains("/Count 3")
                .contains("Client Care Agreement")
                .contains("Pet owner: Mary Smith")
                .contains("Registered pets: Basil \\(dog\\), Samantha \\(cat\\)")
                .contains("CARE AND APPOINTMENTS")
                .contains("CONSENT AND CONTACT")
                .contains("Owner signature")
                .doesNotContain("PDF reader", "renders progressively", "verifies complete reading");
    }
}
