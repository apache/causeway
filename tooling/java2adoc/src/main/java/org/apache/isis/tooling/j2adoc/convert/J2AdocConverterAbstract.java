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
package org.apache.isis.tooling.j2adoc.convert;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.javadoc.description.JavadocDescription;
import com.github.javaparser.javadoc.description.JavadocInlineTag;
import com.github.javaparser.javadoc.description.JavadocSnippet;

import org.asciidoctor.ast.Document;
import org.jsoup.Jsoup;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.tooling.j2adoc.J2AdocContext;
import org.apache.isis.tooling.j2adoc.J2AdocUnit;

import lombok.NonNull;
import lombok.val;

public abstract class J2AdocConverterAbstract implements J2AdocConverter {

    protected final J2AdocContext j2aContext;

    protected J2AdocConverterAbstract(J2AdocContext j2aContext) {
        this.j2aContext = j2aContext;
    }

    protected String parameters(
            final @NonNull Stream<Parameter> parameterStream,
            final @NonNull J2AdocUnit unit) {
        return parameterStream
                .map(x->parameterDeclaration(x, unit))
                .collect(Collectors.joining(", "));
    }

    protected String typeParameters(final @Nullable Can<TypeParameter> typeParamters) {
        if(typeParamters == null
                || typeParamters.isEmpty()) {
            return "";
        }
        return String.format("<%s>", typeParamters
                .stream()
                .map(TypeParameter::getName)
                .map(SimpleName::asString)
                .collect(Collectors.joining(", ")));
    }

    protected String type(
            final @NonNull Type type,
            final @NonNull J2AdocUnit unit) {

        if(type instanceof ClassOrInterfaceType) {
            return classOrInterfaceType((ClassOrInterfaceType) type, unit);
        }
        return type.asString();
    }

    protected String classOrInterfaceType(
            final @NonNull ClassOrInterfaceType type,
            final @NonNull J2AdocUnit unit) {

        val sb = new StringBuilder();
        sb.append(xrefIfRequired(type.getNameAsString(), unit)); // type simple name, no generics
        type.getTypeArguments()
        .ifPresent(typeArgs->{
            sb
            .append("<")
            .append(
                    typeArgs.stream()
                    .map(typeArg->type(typeArg, unit))
                    .collect(Collectors.joining(", "))
            )
            .append(">");
        });

        return sb.toString();
    }


    protected String parameterDeclaration(
            final @NonNull Parameter p,
            final @NonNull J2AdocUnit unit) {

        return String.format("%s%s %s",
                type(p.getType(), unit),
                p.isVarArgs() ? "..." : "",
                p.getNameAsString());
    }

    protected String inlineTag(
            final @NonNull JavadocInlineTag inlineTag,
            final @NonNull J2AdocUnit unit) {

        val inlineContent = inlineTag.getContent().trim();

        switch(inlineTag.getType()) {
        case LINK:
            val referencedUnit = j2aContext.findUnit(inlineContent, unit).orElse(null);
            if(referencedUnit!=null) {
                return String.format(" %s ", j2aContext.xref(referencedUnit));
            }
        default:
            return String.format(" _%s_ ", inlineContent);
        }
    }

    private String xrefIfRequired(
            final @NonNull String typeSimpleName,
            final @NonNull J2AdocUnit unit) {
        return j2aContext.findUnit(typeSimpleName, unit)
                .map(j2aContext::xref)
                .orElse(typeSimpleName);
    }

    private static String javadocSnippet(final @NonNull JavadocSnippet snippet) {
        return snippet.toText();
    }

    // -- HELPER

    protected Document javadocDescription(
            final @NonNull JavadocDescription javadocDescription,
            final @NonNull J2AdocUnit unit) {

        val javadocResolved = new StringBuilder();

        javadocDescription.getElements()
        .forEach(e->{

            if(e instanceof JavadocSnippet) {
                javadocResolved.append(javadocSnippet((JavadocSnippet)e));
            } else if(e instanceof JavadocInlineTag) {
                javadocResolved.append(inlineTag((JavadocInlineTag) e, unit));
            } else {
                javadocResolved.append(e.toText());
            }

        });

        val descriptionAsHtml = Jsoup.parse(javadocResolved.toString());
        val adoc = HtmlToAsciiDoc.body(descriptionAsHtml.selectFirst("body"));
        return adoc;
    }

}
