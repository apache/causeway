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

import org.springframework.lang.Nullable;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.collections._Multimaps;
import org.apache.isis.commons.internal.collections._Multimaps.ListMultimap;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.tooling.j2adoc.J2AdocUnit.LookupKey;
import org.apache.isis.tooling.j2adoc.format.UnitFormatter;
import org.apache.isis.tooling.javamodel.ast.ImportDeclarations;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Value @Builder @Log4j2
public class J2AdocContext {

    private final @Nullable String licenseHeader;

    @Builder.Default
    private final int namespacePartsSkipCount = 0;

    @Builder.Default
    private final boolean skipTitleHeader = false;

    @Builder.Default
    private final boolean suppressFinalKeyword = true;

    @Builder.Default
    private final @NonNull String memberNameFormat = "[teal]#*%s*#";

    @Builder.Default
    private final @NonNull String staticMemberNameFormat = "[teal]#*_%s_*#";

    @Builder.Default
    private final @NonNull String deprecatedMemberNameFormat = "[line-through gray]#*%s*#";

    @Builder.Default
    private final @NonNull String deprecatedStaticMemberNameFormat = "[line-through gray]#*_%s_*#";

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
     * @param unit - the referring (originating) unit, that is currently processed
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

        //XXX debug entry point (keep)
//        if(unit.getFriendlyName().contains("")
//                && partialNameNoWhiteSpaces.equals("Blob")) {
//            log.debug("!!! debug entry point");
//        }

        // given the partialNameNoWhiteSpaces, we split it into parts delimited by '.'
        // any possible ordered subset reachable through removing only leading parts
        // is a candidate representation of the typeSimpleName
        // eg. given a.b.c.d the candidates are in order of likelihood ...
        // d
        // c.d
        // b.c.d
        // a.b.c.d

        final Can<String> nameDiscriminator = Can.ofStream(
                _Strings.splitThenStream(partialNameNoWhiteSpaces, "."));

        val nameDiscriminatorPartIterator = nameDiscriminator.reverseIterator();

        val typeSimpleNameCandidates = Stream.iterate(
                Can.ofSingleton(nameDiscriminatorPartIterator.next()),
                parts->parts.add(nameDiscriminatorPartIterator.next()))
        .limit(nameDiscriminator.size())
        .collect(Can.toCan());

        // each Can<String> represents a fully qualified name, where all its String parts are
        // collected; which are the Java package-name parts and the Java simple-name parts combined
        // note: Java simple-name parts, are multiple when the class is nested
        final Can<Can<String>> potentialFqns = Can.ofStream(
                ImportDeclarations
                .streamPotentialFqns(nameDiscriminator, unit.getImportDeclarations()));

        // for performance reasons we only search the units that are hash mapped
        // by the typeSimpleNameCandidates using the unitsByTypeSimpleName map
        val searchResult = typeSimpleNameCandidates.stream()
        .map((final Can<String> typeSimpleNameParts)->typeSimpleNameParts.stream()
                .collect(Collectors.joining(".")))
        .flatMap((final String typeSimpleNameCandidate)->unitsByTypeSimpleName
                .getOrElseEmpty(typeSimpleNameCandidate)
                .stream())
        // we have a match if either the candidate unit's namespace matches the one of the potentialFqns
        // or otherwise if candidate unit and originating unit share the same Java package;
        // that is, in Java sources, types may refer to other types within the same package without the
        // need for declaring an import statement, hence the second option is a fallback
        .filter((final J2AdocUnit referredUnit)->potentialFqns.stream()
                .anyMatch(potentialFqn->potentialFqn.isEqualTo(referredUnit.getFqnParts()))
                || unit.getNamespace().equals(referredUnit.getNamespace()) //same package
        )
        .collect(Can.toCan());

        // what's left to do at this point is to log empty or ambiguous search results
        // while also trying to suppress cases that are of no interest

        val skipLog = searchResult.isEmpty() && (
                unit.getFqnParts().endsWith(nameDiscriminator) // self referential
                // java.lang types don't need import statements
                || nameDiscriminator.isEqualTo(Can.of("String"))
                || nameDiscriminator.isEqualTo(Can.of("Boolean"))
                || nameDiscriminator.isEqualTo(Can.of("Exception"))
                || nameDiscriminator.isEqualTo(Can.of("Class"))
                || nameDiscriminator.isEqualTo(Can.of("Object"))
                || nameDiscriminator.isEqualTo(Can.of("Integer"))
                || nameDiscriminator.isEqualTo(Can.of("Long"))
                || nameDiscriminator.isEqualTo(Can.of("Byte"))
                || nameDiscriminator.isEqualTo(Can.of("Double"))
                || nameDiscriminator.isEqualTo(Can.of("Short"))
                || nameDiscriminator.isEqualTo(Can.of("Float"))
                || nameDiscriminator.isEqualTo(Can.of("Character"))
                || nameDiscriminator.isEqualTo(Can.of("Throwable"))
                || nameDiscriminator.isEqualTo(Can.of("Math"))
                || nameDiscriminator.isEqualTo(Can.of("Thread"))
                || potentialFqns.stream().anyMatch(fqn->
                    // known packages, we'll never find in the index
                    fqn.startsWith(Can.of("java"))
                    || fqn.startsWith(Can.of("javax")))
        );

        // don't log self-referential lookups, as these are not an issue
        if(!skipLog) {
            logIfEmptyOrAmbiguous(searchResult,
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

        logIfEmptyOrAmbiguous(searchResult,
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


    public String xref(final @NonNull J2AdocUnit unit) {

        val xrefModule = unit.getNamespace()
                .stream()
                .skip(getNamespacePartsSkipCount())
                .findFirst().get();
        val xrefCoordinates = unit.getNamespace()
                .stream()
                .skip(getNamespacePartsSkipCount() + 1)
                .collect(Can.toCan())
                .add(unit.getCanonicalName())
                .stream()
                .collect(Collectors.joining("/"));

        val xref = String.format("xref:refguide:%s:index/%s.adoc[%s]",
                xrefModule, xrefCoordinates, unit.getFriendlyName());

        return xref;
    }

    // -- LOG

    private static void logIfEmptyOrAmbiguous(final Can<J2AdocUnit> units, final String doingWhat) {
        if(units.isEmpty()) {
            log.warn("{} yielded no match %n", doingWhat);
        } else if(units.isCardinalityMultiple()) {
            log.warn("{} was ambiguous with results: ", doingWhat);
            units.forEach(unit->log.warn("\t{}", unit.toString()));
        }
    }


}
