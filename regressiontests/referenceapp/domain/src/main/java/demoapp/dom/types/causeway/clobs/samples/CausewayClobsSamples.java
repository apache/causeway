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
package demoapp.dom.types.causeway.clobs.samples;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.applib.value.NamedWithMimeType;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.resources._Resources;

import demoapp.dom.types.Samples;
import lombok.SneakyThrows;

@Service
public class CausewayClobsSamples implements Samples<Clob> {

    @Override
    public Stream<Clob> stream() {
        return Stream.of(
                "document.txt", "file-sample_100kB.rtf", "all_well.xml")
                .map(this::loadClob);
    }

    @SneakyThrows
    private Clob loadClob(final String name) {
        var text = _Strings.read(_Resources.load(CausewayClobsSamples.class, name), StandardCharsets.UTF_8);
        return Clob.of(name, mimeTypeFor(name), text);
    }

    private static NamedWithMimeType.CommonMimeType mimeTypeFor(final String name) {
        if (name.endsWith(".txt")) return NamedWithMimeType.CommonMimeType.TXT;
        if (name.endsWith(".rtf")) return NamedWithMimeType.CommonMimeType.RTF;
        if (name.endsWith(".xml")) return NamedWithMimeType.CommonMimeType.XML;
        throw new IllegalArgumentException(name);
    }

}
