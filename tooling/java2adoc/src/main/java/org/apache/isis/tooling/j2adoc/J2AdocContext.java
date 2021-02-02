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

import javax.annotation.Nullable;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.collections._Multimaps;
import org.apache.isis.commons.internal.collections._Multimaps.ListMultimap;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.tooling.j2adoc.J2AdocUnit.LookupKey;
import org.apache.isis.tooling.j2adoc.convert.J2AdocConverter;
import org.apache.isis.tooling.j2adoc.format.UnitFormatter;
import org.apache.isis.tooling.j2adoc.format.UnitFormatterCompact;
import org.apache.isis.tooling.j2adoc.format.UnitFormatterWithSourceAndFootNotes;
import org.apache.isis.tooling.javamodel.ast.ImportDeclarations;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.val;

@Value @Builder
public class J2AdocContext {

    private final @NonNull String xrefPageIdFormat;
    private final @Nullable String licenseHeader;
    
    @Builder.Default
    private final int namespacePartsSkipCount = 0;
    
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
    
    private final Map<LookupKey, J2AdocUnit> unitIndex = _Maps.newTreeMap();
    private final ListMultimap<String, J2AdocUnit> unitsByTypeSimpleName = _Multimaps.newListMultimap();
    
    public J2AdocContext add(final @NonNull J2AdocUnit unit) {
        val unitKey = LookupKey.of(unit.getResourceCoordinates());
        val previousKey = unitIndex.put(unitKey, unit);
        if(previousKey!=null) {
            throw _Exceptions.unrecoverableFormatted(
                    "J2AUnit index entries must be unique, "
                    + "index key collision on \nexists: %s\nnew:    %s", 
                    previousKey,
                    unit);
        }
        unitsByTypeSimpleName.putElement(unit.getName().stream().collect(Collectors.joining(".")), unit);
        return this;
    }
    
    public Stream<J2AdocUnit> add(final @NonNull File sourceFile) {
        return J2AdocUnit.parse(sourceFile)
        .peek(this::add)
        // ensure the stream is consumed here, 
        // optimized for 1 result per source file, but can be more
        .collect(Collectors.toCollection(()->new ArrayList<>(1))) 
        .stream();
    }
    
    /**
     * Find the J2AdocUnit by given search parameters.
     * @param partialName - can be anything, originating eg. from java-doc {@literal link} tags.
     * @param unit
     */
    public Optional<J2AdocUnit> findUnit(
            final @Nullable String partialName, 
            final @NonNull  J2AdocUnit unit) {
        
        if(_Strings.isNullOrEmpty(partialName)) {
            return Optional.empty();
        }
        
        val partialNameNoWhiteSpaces = partialName.split("\\s")[0];
        
        
        if(partialNameNoWhiteSpaces.contains("#")) {
            // skip member reference lookup
            //XXX reserved for future extensions ... 
            //val partialNameWithoutMember = _Refs.stringRef(partialName).cutAtIndexOf("#");
            return Optional.empty();  
        }
        
        if(unit.getFriendlyName().equals("Action")
                && partialNameNoWhiteSpaces.equals("Where")) {
            System.out.println("Action");
        }
        
        final Can<String> nameDiscriminator = Can.ofStream(
                _Strings.splitThenStream(partialNameNoWhiteSpaces, "."));
        
        val nameDiscriminatorPartIterator = nameDiscriminator.reverseIterator();
        
        val typeSimpleNameCandidates = Stream.iterate(
                Can.ofSingleton(nameDiscriminatorPartIterator.next()), 
                parts->parts.add(nameDiscriminatorPartIterator.next()))
        .limit(nameDiscriminator.size())
        .collect(Can.toCan());
        
        final Can<Can<String>> potentialFqns = Can.ofStream(
                ImportDeclarations
                .streamPotentialFqns(nameDiscriminator, unit.getImportDeclarations()));
        
        val searchResult = typeSimpleNameCandidates.stream()
        .map((Can<String> typeSimpleNameParts)->typeSimpleNameParts.stream()
                .collect(Collectors.joining(".")))
        .flatMap((String typeSimpleNameCandidate)->unitsByTypeSimpleName
                .getOrElseEmpty(typeSimpleNameCandidate)
                .stream())
        .filter((J2AdocUnit referredUnit)->potentialFqns.stream()
                .anyMatch(potentialFqn->potentialFqn.isEqualTo(referredUnit.getFqnParts()))
                || unit.getNamespace().equals(referredUnit.getNamespace()) //same package
        )
        .collect(Can.toCan());
        

        val selfReferential = searchResult.isEmpty()
                && unit.getFqnParts().endsWith(nameDiscriminator);

        // don't log self-referential lookups, as these are not an issue
        if(!selfReferential) {
            logIfCardinaltiyNotOne(searchResult, 
                    String.format("while processing %s searching referenced unit by partial name '%s'",
                            unit.getFriendlyName(),
                            partialNameNoWhiteSpaces));
        }
        
        return searchResult.getSingleton();
    }
    
    public Optional<J2AdocUnit> findUnitByTypeSimpleName(final @Nullable String typeSimpleName) {
        
        if(_Strings.isNullOrEmpty(typeSimpleName)) {
            return Optional.empty();
        }
        
        val searchResult = Can.ofCollection(unitsByTypeSimpleName.getOrElseEmpty(typeSimpleName));
        
        logIfCardinaltiyNotOne(searchResult, 
                String.format("searching unit by type-simple-name '%s'", typeSimpleName));
        
        return searchResult.getSingleton();
    }
    
    public Stream<J2AdocUnit> streamUnits() {
        return unitIndex.values().stream();
    }

    /**
     * @param key - unique key for types 
     * @return optionally the unit available for given key
     */
    public Optional<J2AdocUnit> getUnit(final @NonNull LookupKey key) {
        return Optional.ofNullable(unitIndex.get(key));
    }
    
    // -- PREDEFINED FORMATS
    
    public static J2AdocContextBuilder javaSourceWithFootnotesFormat() {
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

    // -- LOG

    private static void logIfCardinaltiyNotOne(Can<J2AdocUnit> units, String doingWhat) {
        if(units.isEmpty()) {
            System.out.printf("%s yielded no match %n", doingWhat);
        } else if(units.isCardinalityMultiple()) {
            System.err.printf("%s was ambiguous, "
                    + "%s was ambiguous with results: %n", doingWhat);
            units.forEach(unit->System.err.printf("\t%s%n", unit.toString()));
        }
    }

    
}
