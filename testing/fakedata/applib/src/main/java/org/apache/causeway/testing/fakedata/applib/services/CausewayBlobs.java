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
package org.apache.causeway.testing.fakedata.applib.services;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.commons.internal.base._Bytes;
import org.apache.causeway.commons.internal.resources._Resources;

/**
 * Returns random {@link Blob} values, optionally constrained to either PDFs or JPGs.
 *
 * @since 2.0 {@index}
 */
public class CausewayBlobs extends AbstractRandomValueGenerator {

    public CausewayBlobs(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    private static final List<String> fileNames = Arrays.asList(
            "image01-150x150.jpg",
            "image01-240x180.jpg",
            "image01-640x480.jpg",
            "image01-2048x1536.jpg",
            "image01-4000x3000.jpg",
            "image02-150x150.jpg",
            "image02-240x180.jpg",
            "image02-640x480.jpg",
            "image02-2048x1536.jpg",
            "image02-4000x3000.jpg",
            "Pawson-Naked-Objects-thesis.pdf",
            "rick-mugridge-paper.pdf");

    public Blob any() {
        final List<String> fileNames = CausewayBlobs.fileNames;
        return asBlob(fileNames);
    }

    public Blob anyJpg() {
        return asBlob(fileNamesEndingWith(".jpg"));
    }

    public Blob anyPdf() {
        return asBlob(fileNamesEndingWith(".pdf"));
    }

    private Blob asBlob(final List<String> fileNames) {
        final int randomIdx = fake.ints().upTo(fileNames.size());
        final String randomFileName = fileNames.get(randomIdx);
        return asBlob(randomFileName);
    }

    private static List<String> fileNamesEndingWith(final String suffix) {
        return CausewayBlobs.fileNames.stream()
                .filter(input -> input.endsWith(suffix))
                .collect(Collectors.toList());
    }

    private static Blob asBlob(final String fileName) {
        try(var is = _Resources.load(CausewayBlobs.class, "blobs/" + fileName)){
            return new Blob(fileName, mimeTypeFor(fileName), _Bytes.of(is));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String mimeTypeFor(final String fileName) {
        if(fileName.endsWith("jpg")) {
            return "image/jpeg";
        }
        return "application/pdf";
    }

}
