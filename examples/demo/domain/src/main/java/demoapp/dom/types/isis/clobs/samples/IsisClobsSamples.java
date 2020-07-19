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
package demoapp.dom.types.isis.clobs.samples;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.value.Clob;
import org.apache.isis.applib.value.NamedWithMimeType;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.commons.internal.resources._Resources;

import lombok.SneakyThrows;
import lombok.val;

import demoapp.dom.types.Samples;

@Service
public class IsisClobsSamples implements Samples<Clob> {

    @Override
    public Stream<Clob> stream() {
        return Stream.of(
                "document.txt", "file-sample_100kB.rtf")
                .map(this::loadClob);
    }

    @SneakyThrows
    private Clob loadClob(String name) {
        val text = _Strings.read(_Resources.load(IsisClobsSamples.class, name), StandardCharsets.UTF_8);
        return Clob.of(name, mimeTypeFor(name), text);
    }

    private static NamedWithMimeType.CommonMimeType mimeTypeFor(String name) {
        if (name.endsWith(".txt")) return NamedWithMimeType.CommonMimeType.TXT;
        if (name.endsWith(".rtf")) return NamedWithMimeType.CommonMimeType.RTF;
        throw new IllegalArgumentException(name);
    }


}
