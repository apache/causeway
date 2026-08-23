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
package demoapp.dom.types.causeway.blobs.samples;

import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.NamedWithMimeType;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Bytes;
import org.apache.causeway.commons.internal.resources._Resources;

import demoapp.dom.types.Samples;
import lombok.Getter;
import lombok.SneakyThrows;

@Service
public class CausewayBlobsSamples implements Samples<Blob> {

    @Getter(lazy = true)
    private final Can<Blob> blobs = Can.of(
            "file-sample_100kB.docx",
            //"file-sample_150kB.pdf",
            "causeway-logo-605x449.png",
            "compressed.tracemonkey-pldi-09.pdf" // advanced example from the Mozilla pdf.js project
            )
        .map(this::loadBlob);

    @Override
    public Stream<Blob> stream() {
        return getBlobs().stream();
    }

    @SneakyThrows
    private Blob loadBlob(final String name) {
        var bytes = _Bytes.of(_Resources.load(CausewayBlobsSamples.class, name));
        return Blob.of(name, mimeTypeFor(name), bytes);
    }

    private static NamedWithMimeType.CommonMimeType mimeTypeFor(final String name) {
        if (name.endsWith(".png")) return NamedWithMimeType.CommonMimeType.PNG;
        if (name.endsWith(".docx")) return NamedWithMimeType.CommonMimeType.DOCX;
        if (name.endsWith(".pdf")) return NamedWithMimeType.CommonMimeType.PDF;
        throw new IllegalArgumentException(name);
    }

}
