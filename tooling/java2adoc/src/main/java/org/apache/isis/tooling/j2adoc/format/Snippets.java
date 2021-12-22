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

import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.tooling.j2adoc.J2AdocContext;
import org.apache.isis.tooling.j2adoc.J2AdocUnit;
import org.apache.isis.tooling.javamodel.ast.AnnotationMemberDeclarations;
import org.apache.isis.tooling.javamodel.ast.ConstructorDeclarations;
import org.apache.isis.tooling.javamodel.ast.EnumConstantDeclarations;
import org.apache.isis.tooling.javamodel.ast.FieldDeclarations;
import org.apache.isis.tooling.javamodel.ast.Javadocs;
import org.apache.isis.tooling.javamodel.ast.MethodDeclarations;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Snippets {

    public static String title(final J2AdocUnit unit) {
        final String format = formatFor(unit);
        return String.format(format,
                        unit.getFriendlyName());
    }

    private static String formatFor(final J2AdocUnit unit) {
        switch (unit.getTypeDeclaration().getKind()) {
            case ANNOTATION: return "@%s";
            case CLASS: return "%s";
            case ENUM: return "%s _(enum)_";
            case INTERFACE: return "%s _(interface)_";
            default:
                throw new IllegalArgumentException(String.format(
                        "unknown kind: %s", unit.getTypeDeclaration().getKind()));
        }
    }

    public static String javaSourceFor(final J2AdocUnit unit, final J2AdocContext j2aContext) {

        final UnaryOperator<String> methodPostProcessor = j2aContext.isSuppressFinalKeyword()
                ? s->s
                        .replace("(final ", "(")
                        .replace(" final ", " ")
                : UnaryOperator.identity();

        val buf = new StringBuilder();

        buf.append(String.format("%s %s%s {\n",
                unit.getDeclarationKeyword(),
                unit.getSimpleName(),
                unit.getTypeDeclaration().getTypeParametersAsString()));

        appendJavaSourceMemberFormat(buf,
                unit.getTypeDeclaration().getEnumConstantDeclarations(),
                EnumConstantDeclarations::asNormalized);

        appendJavaSourceMemberFormat(buf,
                unit.getTypeDeclaration().getPublicFieldDeclarations(),
                FieldDeclarations::asNormalized);

        appendJavaSourceMemberFormat(buf,
                unit.getTypeDeclaration().getAnnotationMemberDeclarations(),
                AnnotationMemberDeclarations::asNormalized);

        appendJavaSourceMemberFormat(buf,
                unit.getTypeDeclaration().getPublicConstructorDeclarations(),
                decl->methodPostProcessor.apply(ConstructorDeclarations.asNormalized(decl)));

        appendJavaSourceMemberFormat(buf,
                unit.getTypeDeclaration().getPublicMethodDeclarations(),
                decl->methodPostProcessor.apply(MethodDeclarations.asNormalized(decl)));

        buf.append("}\n");

        return buf.toString();
    }

    private static<T extends NodeWithJavadoc<?>> void appendJavaSourceMemberFormat(
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

    enum Callout { INCLUDE, EXCLUDE;
        public static Callout when(final boolean javadocPresent) {
            return javadocPresent ? INCLUDE : EXCLUDE;
        }
    }
    private static String javaSourceMemberFormat(final Callout callout) {
        return callout == Callout.INCLUDE
                ? "  %s     // <.>\n"
                : "  %s\n";
    }


}
