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
package org.apache.isis.testing.fakedata.applib.services;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.io.ByteSource;
import com.google.common.io.Resources;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.value.Blob;

public class IsisBlobs extends AbstractRandomValueGenerator {

    public IsisBlobs(final FakeDataService fakeDataService) {
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

    @Programmatic
    public Blob any() {
        final List<String> fileNames = IsisBlobs.fileNames;
        return asBlob(fileNames);
    }

    @Programmatic
    public Blob anyJpg() {
        return asBlob(fileNamesEndingWith(".jpg"));
    }

    @Programmatic
    public Blob anyPdf() {
        return asBlob(fileNamesEndingWith(".pdf"));
    }

    private static List<String> fileNamesEndingWith(final String suffix) {
        return IsisBlobs.fileNames.stream()
                .filter(input -> input.endsWith(suffix))
                .collect(Collectors.toList());
    }


    private Blob asBlob(final List<String> fileNames) {
        final int randomIdx = fake.ints().upTo(fileNames.size());
        final String randomFileName = fileNames.get(randomIdx);
        return asBlob(randomFileName);
    }

    private static Blob asBlob(final String fileName) {
        final URL resource = Resources.getResource(IsisBlobs.class, "blobs/" + fileName);
        final ByteSource byteSource = Resources.asByteSource(resource);
        final byte[] bytes;
        try {
            bytes = byteSource.read();
            return new Blob(fileName, mimeTypeFor(fileName), bytes);
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
