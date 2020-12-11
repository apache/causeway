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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.val;

@Value @Builder
public class J2aContext {

    private final @NonNull String xrefPageIdFormat;
    
    /**
     * name | param-list
     */
    @Builder.Default
    private final @NonNull String constructorFormat = "`%s(%s)`";
    
    /**
     * constructor-generic-type | name | param-list
     */
    @Builder.Default
    private final @NonNull String genericConstructorFormat = "`%s %s(%s)`";
    
    /**
     * return-type | name | param-list
     */
    @Builder.Default
    private final @NonNull String methodFormat = "`%s %s(%s)`";
    
    /**
     * method-generic-type | return-type | name | param-list
     */
    @Builder.Default
    private final @NonNull String genericMethodFormat = "`%s %s %s(%s)`";
    
    @Builder.Default
    private final @NonNull String memberNameFormat = "[teal]#*%s*#";
    
    @Builder.Default
    private final @NonNull String staticMemberNameFormat = "[teal]#*_%s_*#";
    
    @Builder.Default
    private final @NonNull String deprecatedMemberNameFormat = "[line-through gray]#*%s*#";
    
    @Builder.Default
    private final @NonNull String deprecatedStaticMemberNameFormat = "[line-through gray]#*_%s_*#";
    
    /**
     * method | description
     */
    @Builder.Default
    private final @NonNull String memberDescriptionFormat = "\n<.> %s %s\n";
    
    @Builder.Default
    private final boolean includeJavaSource = true;
    
    private final Map<String, J2aUnit> unitIndex = _Maps.newTreeMap();

    public J2aContext add(final @NonNull J2aUnit unit) {
        val previousKey = unitIndex.put(unit.getName(), unit);
        if(previousKey!=null) {
            throw _Exceptions.unrecoverableFormatted(
                    "J2aUnit index entries must be unique (index key collision on %s)", 
                    previousKey);
        }
        return this;
    }
    
    public Stream<J2aUnit> add(final @NonNull File sourceFile) {
        return J2aUnit.parse(sourceFile)
        .peek(this::add)
        // ensure the stream is consumed here, 
        // current implementation does not expect more than 1 result per source file
        .collect(Collectors.toCollection(()->new ArrayList<>(1))) 
        .stream();
    }
    
    public Stream<J2aUnit> streamUnits() {
        return unitIndex.values().stream();
    }

    public Optional<J2aUnit> getUnit(String key) {
        return Optional.ofNullable(unitIndex.get(key));
    }
    
    // -- PREDEFINED FORMATS
    
    public static J2aContextBuilder javaSourceWithFootNotesFormat() {
        return J2aContext.builder();
    }
    
    public static J2aContextBuilder compactFormat() {
        return J2aContext.builder()
                .constructorFormat("`%1$s(%2$s)`") // name | param-list)
                .genericConstructorFormat("`%2$s%1$s(%3$s)`") //  method-generic-type | name | param-list)
                .methodFormat("`%2$s(%3$s)` : `%1$s`") //  return-type | name | param-list)
                .genericMethodFormat("`%3$s%1$s(%4$s)` : `%2$s`") //  method-generic-type | return-type | name | param-list)
                .memberDescriptionFormat("\n* %s\n%s\n") // method | description
                .includeJavaSource(false)
                ;        
    }
    
}
