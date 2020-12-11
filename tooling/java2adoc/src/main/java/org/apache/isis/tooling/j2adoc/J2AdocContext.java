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
package org.apache.isis.tooling.j2adoc;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.tooling.j2adoc.convert.J2AdocConverter;
import org.apache.isis.tooling.j2adoc.format.UnitFormatter;
import org.apache.isis.tooling.j2adoc.format.UnitFormatterCompact;
import org.apache.isis.tooling.j2adoc.format.UnitFormatterWithSourceAndFootNotes;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.val;

@Value @Builder
public class J2AdocContext {

    private final @NonNull String xrefPageIdFormat;
    
    @Builder.Default
    private final @NonNull String memberNameFormat = "[teal]#*%s*#";
    
    @Builder.Default
    private final @NonNull String staticMemberNameFormat = "[teal]#*_%s_*#";
    
    @Builder.Default
    private final @NonNull String deprecatedMemberNameFormat = "[line-through gray]#*%s*#";
    
    @Builder.Default
    private final @NonNull String deprecatedStaticMemberNameFormat = "[line-through gray]#*_%s_*#";
    
    // -- CONVERTER
    
    private final @NonNull Function<J2AdocContext, J2AdocConverter> converterFactory;
    
    @Getter(lazy=true)
    private final J2AdocConverter converter = getConverterFactory().apply(this);
    
    // -- FORMATTER
    
    private final @NonNull Function<J2AdocContext, UnitFormatter> formatterFactory;
    
    @Getter(lazy=true)
    private final UnitFormatter formatter = getFormatterFactory().apply(this);

    // -- UNIT INDEX
    
    private final Map<String, J2AdocUnit> unitIndex = _Maps.newTreeMap();
    
    public J2AdocContext add(final @NonNull J2AdocUnit unit) {
        val previousKey = unitIndex.put(unit.getName(), unit);
        if(previousKey!=null) {
            throw _Exceptions.unrecoverableFormatted(
                    "J2AUnit index entries must be unique (index key collision on %s)", 
                    previousKey);
        }
        return this;
    }
    
    public Stream<J2AdocUnit> add(final @NonNull File sourceFile) {
        return J2AdocUnit.parse(sourceFile)
        .peek(this::add)
        // ensure the stream is consumed here, 
        // current implementation does not expect more than 1 result per source file
        .collect(Collectors.toCollection(()->new ArrayList<>(1))) 
        .stream();
    }
    
    public Stream<J2AdocUnit> streamUnits() {
        return unitIndex.values().stream();
    }

    public Optional<J2AdocUnit> getUnit(String key) {
        return Optional.ofNullable(unitIndex.get(key));
    }
    
    // -- PREDEFINED FORMATS
    
    public static J2AdocContextBuilder javaSourceWithFootNotesFormat() {
        return J2AdocContext.builder()
                .converterFactory(J2AdocConverter::createDefault)
                .formatterFactory(UnitFormatterWithSourceAndFootNotes::new)
                ;
    }
    
    public static J2AdocContextBuilder compactFormat() {
        return J2AdocContext.builder()
                .converterFactory(J2AdocConverter::createDefault)
                .formatterFactory(UnitFormatterCompact::new)
                ;        
    }
    
}
