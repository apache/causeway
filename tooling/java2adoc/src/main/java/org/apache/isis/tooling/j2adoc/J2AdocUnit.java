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
import java.util.stream.Stream;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.javadoc.Javadoc;

import org.asciidoctor.ast.Document;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.tooling.j2adoc.util.AsciiDocIncludeTagFilter;
import org.apache.isis.tooling.javamodel.ast.AnyTypeDeclaration;
import org.apache.isis.tooling.javamodel.ast.CompilationUnits;
import org.apache.isis.tooling.javamodel.ast.PackageDeclarations;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Log4j2
public final class J2AdocUnit {

    private final AnyTypeDeclaration atd;
    
    public static Stream<J2AdocUnit> parse(final @NonNull File sourceFile) {

        if("package-info.java".equals(sourceFile.getName())) {
            // ignore package files
            return Stream.empty();
        }
        
        try {
            
            // remove 'tag::' and 'end::' lines
            // remove '// <.>' foot note references
            val source = AsciiDocIncludeTagFilter.read(sourceFile);

            val cu = StaticJavaParser.parse(source);
            
            cu.getPackageDeclaration();
            
            return Stream.of(cu)
            .flatMap(CompilationUnits::streamTypeDeclarations)
            .filter(AnyTypeDeclaration::hasIndexDirective)
            .map(J2AdocUnit::new);

        } catch (Exception e) {
            log.error("failed to parse java source file {}", sourceFile, e);
            return Stream.empty();
        }

    }

    public AnyTypeDeclaration getTypeDeclaration() {
        return atd;
    }
    
    /**
     * Returns the recursively resolved (nested) type name. 
     * Same as {@link #getSimpleName()} if type is not nested. 
     */
    public String getName() {
        return atd.getName();
    }
    
    public String getSimpleName() {
        return atd.getSimpleName();
    }
    
    public Can<String> getNamespace() {
        return PackageDeclarations.namespace(atd.getPackageDeclaration());
    }
    
    public String getDeclarationKeywordFriendlyName() {
        return _Strings.capitalize(atd.getKind().name().toLowerCase());
    }
    
    public String getDeclarationKeyword() {
        return atd.getKind().getJavaKeyword();
    }
    
    @Getter(lazy = true)
    private final Optional<Javadoc> javadoc = atd.getJavadoc();
    
    public String getAsciiDocXref(
            final @NonNull J2AdocContext j2aContext) {
        return j2aContext.getConverter().xref(this);
    }
    
    public Document toAsciiDoc(
            final @NonNull J2AdocContext j2aContext) {
        return j2aContext.getFormatter().apply(this);
    }


}
