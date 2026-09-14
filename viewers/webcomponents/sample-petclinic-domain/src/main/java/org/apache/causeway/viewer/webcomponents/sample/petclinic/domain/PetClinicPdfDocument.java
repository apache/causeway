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

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.NamedWithMimeType;

final class PetClinicPdfDocument {

    private static final int PAGE_COUNT = 3;

    private PetClinicPdfDocument() {
    }

    static Blob agreementFor(final PetOwner owner) {
        final var pets = owner.getPets().stream()
                .sorted(Comparator.comparing(Pet::getName).thenComparing(Pet::getId))
                .map(pet -> pet.getName() + " (" + pet.getSpecies().name().toLowerCase(Locale.ROOT) + ")")
                .collect(Collectors.joining(", "));
        return agreement(owner.getId(), owner.getName(), pets.isBlank() ? "No pets currently registered" : pets);
    }

    static Blob agreement(final String ownerId, final String ownerName, final String registeredPets) {
        return Blob.of(
                fileName(ownerId),
                NamedWithMimeType.CommonMimeType.PDF,
                createPdf(ownerId, ownerName, registeredPets));
    }

    private static String fileName(final String ownerId) {
        final var candidate = ownerId == null
                ? ""
                : ownerId.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        final var stableId = candidate.isBlank() ? "pet-owner" : candidate;
        return stableId + "-clinic-agreement.pdf";
    }

    private static byte[] createPdf(final String ownerId, final String ownerName, final String registeredPets) {
        final var agreementReference = ownerId == null || ownerId.isBlank()
                ? "PET-OWNER"
                : ownerId.toUpperCase(Locale.ROOT);
        final var output = new ByteArrayOutputStream();
        write(output, "%PDF-1.4\n");
        final List<Integer> offsets = new ArrayList<>();
        offsets.add(0);
        object(output, offsets, 1, "<< /Type /Catalog /Pages 2 0 R >>");
        object(output, offsets, 2, "<< /Type /Pages /Kids [3 0 R 5 0 R 7 0 R] /Count " + PAGE_COUNT + " >>");
        page(output, offsets, 3, 4);
        final List<String> registration = new ArrayList<>(List.of(
                "CAUSEWAY PET CLINIC",
                "Client Care Agreement",
                "Agreement reference: " + agreementReference,
                "Pet owner: " + ownerName));
        registration.addAll(wrappedLines("Registered pets: " + registeredPets));
        registration.add("This agreement records how the clinic and pet owner will work together.");
        registration.add("The owner confirms that the registration details supplied to the clinic are accurate.");
        content(output, offsets, 4, registration);
        page(output, offsets, 5, 6);
        content(output, offsets, 6, List.of(
                "CARE AND APPOINTMENTS",
                "The clinic will explain recommended examinations and treatment options.",
                "The owner will provide relevant medical history and changes in each pet's condition.",
                "Appointments should be cancelled as early as possible when they cannot be attended.",
                "Fees and material risks will be explained before non-emergency treatment where practicable.",
                "Emergency care may be provided when delay would place an animal at avoidable risk."));
        page(output, offsets, 7, 8);
        content(output, offsets, 8, List.of(
                "CONSENT AND CONTACT",
                "The owner authorises the clinic to use the supplied contact details for animal care.",
                "Clinical records will be retained according to the clinic's professional obligations.",
                "This demonstration agreement is provided for the Apache Causeway Petclinic sample.",
                "Owner signature: ______________________________",
                "Clinic representative: ________________________",
                "Date: __________________"));
        object(output, offsets, 9, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>");
        object(output, offsets, 10, "<< /Title (" + pdfText(ownerName + " clinic agreement") + ") "
                + "/Author (Causeway Pet Clinic) /Producer (Apache Causeway Petclinic) "
                + "/CreationDate (D:20260101000000Z) /ModDate (D:20260101000000Z) >>");
        final int xref = output.size();
        write(output, "xref\n0 11\n0000000000 65535 f \n");
        for (int index = 1; index <= 10; index++) {
            write(output, "%010d 00000 n \n".formatted(offsets.get(index)));
        }
        write(output, "trailer\n<< /Size 11 /Root 1 0 R /Info 10 0 R >>\nstartxref\n%d\n%%%%EOF\n".formatted(xref));
        return output.toByteArray();
    }

    private static void page(
            final ByteArrayOutputStream output,
            final List<Integer> offsets,
            final int number,
            final int content) {
        object(output, offsets, number,
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] "
                        + "/Resources << /Font << /F1 9 0 R >> >> /Contents " + content + " 0 R >>");
    }

    private static void content(
            final ByteArrayOutputStream output,
            final List<Integer> offsets,
            final int number,
            final List<String> lines) {
        final var stream = new StringBuilder("BT /F1 18 Tf 54 738 Td ");
        for (int index = 0; index < lines.size(); index++) {
            if (index == 1) {
                stream.append("0 -34 Td /F1 15 Tf ");
            } else if (index > 1) {
                stream.append("0 -30 Td /F1 11 Tf ");
            }
            stream.append('(').append(pdfText(lines.get(index))).append(") Tj ");
        }
        stream.append("ET\n");
        object(output, offsets, number,
                "<< /Length " + stream.toString().getBytes(StandardCharsets.US_ASCII).length + " >>\nstream\n"
                        + stream + "endstream");
    }

    private static List<String> wrappedLines(final String value) {
        final int maximumLength = 78;
        final List<String> lines = new ArrayList<>();
        final var words = value.split(" ");
        final var line = new StringBuilder();
        for (final String word : words) {
            if (!line.isEmpty() && line.length() + word.length() + 1 > maximumLength) {
                lines.add(line.toString());
                line.setLength(0);
            }
            if (!line.isEmpty()) {
                line.append(' ');
            }
            line.append(word);
        }
        if (!line.isEmpty()) {
            lines.add(line.toString());
        }
        return lines;
    }

    private static String pdfText(final String value) {
        final var ascii = new String(value.getBytes(StandardCharsets.US_ASCII), StandardCharsets.US_ASCII);
        return ascii.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    private static void object(
            final ByteArrayOutputStream output,
            final List<Integer> offsets,
            final int number,
            final String body) {
        while (offsets.size() <= number) {
            offsets.add(0);
        }
        offsets.set(number, output.size());
        write(output, number + " 0 obj\n" + body + "\nendobj\n");
    }

    private static void write(final ByteArrayOutputStream output, final String value) {
        output.writeBytes(value.getBytes(StandardCharsets.US_ASCII));
    }
}
