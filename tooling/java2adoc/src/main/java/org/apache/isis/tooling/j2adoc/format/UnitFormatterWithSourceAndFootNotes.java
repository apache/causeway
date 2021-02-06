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
package org.apache.isis.tooling.j2adoc.format;

import java.util.Optional;
import java.util.function.Function;

import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;

import org.asciidoctor.ast.StructuralNode;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.tooling.j2adoc.J2AdocContext;
import org.apache.isis.tooling.j2adoc.J2AdocUnit;
import org.apache.isis.tooling.javamodel.ast.AnnotationMemberDeclarations;
import org.apache.isis.tooling.javamodel.ast.ConstructorDeclarations;
import org.apache.isis.tooling.javamodel.ast.EnumConstantDeclarations;
import org.apache.isis.tooling.javamodel.ast.FieldDeclarations;
import org.apache.isis.tooling.javamodel.ast.Javadocs;
import org.apache.isis.tooling.javamodel.ast.MethodDeclarations;
import org.apache.isis.tooling.model4adoc.AsciiDocFactory;

import lombok.NonNull;
import lombok.val;

public class UnitFormatterWithSourceAndFootNotes
extends UnitFormatterAbstract {

    public UnitFormatterWithSourceAndFootNotes(final @NonNull J2AdocContext j2aContext) {
        super(j2aContext);
    }

    protected Optional<String> javaSource(final J2AdocUnit unit) {

        val java = new StringBuilder();

        java.append(String.format("%s %s {\n",
                unit.getDeclarationKeyword(),
                unit.getSimpleName()));

        appendJavaSourceMemberFormat(java,
                unit.getTypeDeclaration().getEnumConstantDeclarations(),
                EnumConstantDeclarations::asNormalized);

        appendJavaSourceMemberFormat(java,
                unit.getTypeDeclaration().getPublicFieldDeclarations(),
                FieldDeclarations::asNormalized);

        appendJavaSourceMemberFormat(java,
                unit.getTypeDeclaration().getAnnotationMemberDeclarations(),
                AnnotationMemberDeclarations::asNormalized);

        appendJavaSourceMemberFormat(java,
                unit.getTypeDeclaration().getPublicConstructorDeclarations(),
                ConstructorDeclarations::asNormalized);

        appendJavaSourceMemberFormat(java,
                unit.getTypeDeclaration().getPublicMethodDeclarations(),
                MethodDeclarations::asNormalized);

        java.append("}\n");

        return Optional.of(
                AsciiDocFactory.SourceFactory.java(java.toString(), unit.getCanonicalName() + ".java"));
    }

    private <T extends NodeWithJavadoc<?>> void appendJavaSourceMemberFormat(
            final StringBuilder java,
            final Can<T> declarations,
            final Function<T, String> normalizer) {
        declarations.stream()
        .filter(Javadocs::notExplicitlyHidden)
        .forEach(decl->{
            val memberFormat = javaSourceMemberFormat(Callout.when(decl.getJavadoc().isPresent()));
            java.append(String.format(memberFormat, normalizer.apply(decl)));
        });
    }

//XXX java language syntax (for footnote text), but not used any more
//
//    @Override
//    public String getEnumConstantFormat() {
//        return "`%s`";
//    }
//
//    @Override
//    public String getFieldFormat() {
//        return "`%s %s`";
//    }
//
//    @Override
//    public String getConstructorFormat() {
//        return "`%s(%s)`";
//    }
//
//    @Override
//    public String getGenericConstructorFormat() {
//        return "`%s %s(%s)`";
//    }
//
//    @Override
//    public String getMethodFormat() {
//        return "`%s %s(%s)`";
//    }
//
//    @Override
//    public String getGenericMethodFormat() {
//        return "`%s %s %s(%s)`";
//    }

    @Override
    protected StructuralNode getMemberDescriptionContainer(StructuralNode parent) {
        return AsciiDocFactory.footnotes(parent);
    }

    // -- HELPER

    enum Callout { INCLUDE, EXCLUDE;
        public static Callout when(boolean javadocPresent) {
            return javadocPresent ? INCLUDE : EXCLUDE;
        }
    }
    private String javaSourceMemberFormat(final Callout callout) {
        return callout == Callout.INCLUDE
                ? "  %s     // <.>\n"
                : "  %s\n";
    }


}
