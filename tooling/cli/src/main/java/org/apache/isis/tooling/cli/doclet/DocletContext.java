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
package org.apache.isis.tooling.cli.doclet;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.val;

@Value @Builder
public class DocletContext {

    private final @NonNull String xrefPageIdFormat;
    
    private final Map<String, Doclet> docletIndex = _Maps.newTreeMap();

    public DocletContext add(final @NonNull Doclet doclet) {
        val previousKey = docletIndex.put(doclet.getName(), doclet);
        if(previousKey!=null) {
            throw _Exceptions.unrecoverableFormatted(
                    "doclet index entries must be unique (index key collision on %s)", 
                    previousKey);
        }
        return this;
    }
    
    public Stream<Doclet> add(final @NonNull File sourceFile) {
        return Doclet.parse(sourceFile)
                .peek(this::add);
    }
    
    public Stream<Doclet> streamDoclets() {
        return docletIndex.values().stream();
    }

    public Optional<Doclet> getDoclet(String key) {
        return Optional.ofNullable(docletIndex.get(key));
    }
    
}
