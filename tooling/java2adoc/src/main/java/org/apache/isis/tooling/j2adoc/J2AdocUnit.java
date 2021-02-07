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
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.javadoc.Javadoc;

import org.asciidoctor.ast.Document;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.resource.ResourceCoordinates;
import org.apache.isis.tooling.j2adoc.util.AsciiDocIncludeTagFilter;
import org.apache.isis.tooling.javamodel.ast.AnyTypeDeclaration;
import org.apache.isis.tooling.javamodel.ast.CompilationUnits;
import org.apache.isis.tooling.javamodel.ast.PackageDeclarations;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Log4j2
public final class J2AdocUnit {

    @Value
    public static class LookupKey implements Comparable<LookupKey> {

        /** full namespace, no parts discarded; delimited by {@literal .} */
        private final @NonNull String namespace;

        /** full name, no parts discarded; delimited by {@literal $} */
        private final @NonNull String name;

        public static LookupKey of(final @NonNull ResourceCoordinates resco) {
            return new LookupKey(
                    resco.getNamespace().stream().collect(Collectors.joining(".")),
                    resco.getName().stream().collect(Collectors.joining("$")));
        }

        @Override
        public int compareTo(LookupKey other) {
            if(other==null) {
                return -1;
            }
            int c = _Strings.compareNullsFirst(this.namespace, other.namespace);
            return (c!=0)
                ?  c
                : _Strings.compareNullsFirst(this.name, other.name);
        }
    }

    @Getter private final ResourceCoordinates resourceCoordinates;
    @Getter private final AnyTypeDeclaration typeDeclaration;

    public static Stream<J2AdocUnit> parse(final @NonNull File sourceFile) {

        if("package-info.java".equals(sourceFile.getName())) {
            // ignore package files
            return Stream.empty();
        }

        try {

            // remove 'tag::' and 'end::' lines
            // remove '// <.>' foot note references
            val source = AsciiDocIncludeTagFilter.read(sourceFile);

            val origin = ResourceCoordinates.fromFile(sourceFile.getAbsoluteFile());

            val cu = StaticJavaParser.parse(source);

            cu.getPackageDeclaration();


            return Stream.of(cu)
            .flatMap(CompilationUnits::streamTypeDeclarations)
            .filter(AnyTypeDeclaration::hasIndexDirective)
            .map(atd->{

                val resourceCoordinates = ResourceCoordinates.builder()
                .friendlyName(atd.getName().stream()
                        .collect(Collectors.joining(".")))
                .nameAsString(atd.getName().stream()
                        .collect(Collectors.joining("~")))
                .simpleName(atd.getSimpleName())
                .location(origin.getNamespace()) //originating file location
                .namespace(PackageDeclarations.namespace(atd.getPackageDeclaration()))
                .name(atd.getName())
                .build();

                return new J2AdocUnit(resourceCoordinates, atd);

            });

        } catch (Exception e) {
            log.error("failed to parse java source file {}", sourceFile, e);
            return Stream.empty();
        }

    }

    public String getCanonicalName() {
        return resourceCoordinates.getNameAsString();
    }

    public Can<String> getLocation() {
        return resourceCoordinates.getLocation();
    }

    /**
     * Returns the recursively resolved (nested) type name.
     * Same as {@link #getSimpleName()} if type is not nested.
     */
    public Can<String> getName() {
        return resourceCoordinates.getName();
    }

    public String getFriendlyName() {
        return resourceCoordinates.getFriendlyName();
    }

    public String getSimpleName() {
        return resourceCoordinates.getSimpleName();
    }

    public Can<String> getNamespace() {
        return resourceCoordinates.getNamespace();
    }

    public String getDeclarationKeywordFriendlyName() {
        return _Strings.capitalize(typeDeclaration.getKind().name().toLowerCase());
    }

    public String getDeclarationKeyword() {
        return typeDeclaration.getKind().getJavaKeyword();
    }

    public Can<ImportDeclaration> getImportDeclarations() {
        return typeDeclaration.getImportDeclarations();
    }

    @Getter(lazy = true)
    private final Can<String> fqnParts = getNamespace().addAll(getName());

    @Getter(lazy = true)
    private final Optional<Javadoc> javadoc = typeDeclaration.getJavadoc();

    public String getAsciiDocXref(
            final @NonNull J2AdocContext j2aContext) {
        return j2aContext.xref(this);
    }

    public Document toAsciiDoc(
            final @NonNull J2AdocContext j2aContext) {
        return j2aContext.getFormatter().apply(this);
    }

    @Override
    public String toString() {
        return String.format("J2AdocUnit[location=%s, namespace=%s, simpleName=%s, friendlyName=%s]",
                getLocation(),
                getNamespace(),
                getSimpleName(),
                getFriendlyName());
    }

}
