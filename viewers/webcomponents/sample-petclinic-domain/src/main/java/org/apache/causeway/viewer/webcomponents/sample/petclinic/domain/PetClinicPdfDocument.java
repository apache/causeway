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
import java.util.List;

import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.NamedWithMimeType;

final class PetClinicPdfDocument {

    private PetClinicPdfDocument() {
    }

    static Blob sample() {
        return Blob.of(
                "petclinic-reader-sample.pdf",
                NamedWithMimeType.CommonMimeType.PDF,
                createPdf());
    }

    private static byte[] createPdf() {
        final var output = new ByteArrayOutputStream();
        write(output, "%PDF-1.4\n");
        final List<Integer> offsets = new ArrayList<>();
        offsets.add(0);
        object(output, offsets, 1, "<< /Type /Catalog /Pages 2 0 R >>");
        object(output, offsets, 2, "<< /Type /Pages /Kids [3 0 R 5 0 R 7 0 R] /Count 3 >>");
        page(output, offsets, 3, 4);
        content(output, offsets, 4, 1, "Welcome to the Causeway Petclinic PDF reader");
        page(output, offsets, 5, 6);
        content(output, offsets, 6, 2, "Every page remains reachable and renders progressively");
        page(output, offsets, 7, 8);
        content(output, offsets, 8, 3, "This deterministic final page verifies complete reading");
        object(output, offsets, 9, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>");
        final int xref = output.size();
        write(output, "xref\n0 10\n0000000000 65535 f \n");
        for (int index = 1; index <= 9; index++) {
            write(output, "%010d 00000 n \n".formatted(offsets.get(index)));
        }
        write(output, "trailer\n<< /Size 10 /Root 1 0 R >>\nstartxref\n%d\n%%%%EOF\n".formatted(xref));
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
            final int pageNumber,
            final String text) {
        final String stream = "BT /F1 18 Tf 72 720 Td (" + text + ") Tj "
                + "0 -36 Td /F1 14 Tf (Page " + pageNumber + " of 3) Tj ET\n";
        object(output, offsets, number,
                "<< /Length " + stream.getBytes(StandardCharsets.US_ASCII).length + " >>\nstream\n"
                        + stream + "endstream");
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
