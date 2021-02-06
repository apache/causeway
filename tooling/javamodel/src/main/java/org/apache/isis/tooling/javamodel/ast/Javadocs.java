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
package org.apache.isis.tooling.javamodel.ast;

import java.util.Optional;
import java.util.stream.Stream;

import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.description.JavadocDescription;

import lombok.NonNull;

public final class Javadocs {

    public static Stream<JavadocBlockTag> streamTagsByName(
            final @NonNull Javadoc javadoc,
            final @NonNull String tagName) {

        return javadoc.getBlockTags().stream()
        .filter(tag->tag.getTagName().equals(tagName));
    }

    public static Stream<JavadocDescription> streamTagContent(
            final @NonNull Javadoc javadoc,
            final @NonNull String tagName) {

        return streamTagsByName(javadoc, tagName)
        .map(tag->tag.getContent());
    }

    // -- PREDICATES FOR STREAMS (PRESENT AND NOT HIDDEN)

    public static boolean presentAndNotHidden(final @NonNull NodeWithJavadoc<?> nwj) {
        return presentAndNotHidden(nwj.getJavadoc());
    }

    public static boolean presentAndNotHidden(final @NonNull AnnotationMemberDeclaration amd) {
        return presentAndNotHidden(amd.getJavadoc());
    }

    public static boolean presentAndNotHidden(final @NonNull FieldDeclaration fd) {
        return presentAndNotHidden(fd.getJavadoc());
    }

    public static boolean presentAndNotHidden(final @NonNull EnumConstantDeclaration ecd) {
        return presentAndNotHidden(ecd.getJavadoc());
    }

    public static boolean presentAndNotHidden(final @NonNull ConstructorDeclaration cd) {
        return presentAndNotHidden(cd.getJavadoc());
    }

    public static boolean presentAndNotHidden(final @NonNull MethodDeclaration md) {
        return presentAndNotHidden(md.getJavadoc());
    }

    // -- PREDICATES FOR STREAMS (NOT EXPLICITLY HIDDEN)

    public static boolean notExplicitlyHidden(final @NonNull AnnotationMemberDeclaration amd) {
        return !hasHidden(amd.getJavadoc());
    }

    public static boolean notExplicitlyHidden(final @NonNull FieldDeclaration fd) {
        return !hasHidden(fd.getJavadoc());
    }

    public static boolean notExplicitlyHidden(final @NonNull EnumConstantDeclaration ecd) {
        return !hasHidden(ecd.getJavadoc());
    }

    public static boolean notExplicitlyHidden(final @NonNull ConstructorDeclaration cd) {
        return !hasHidden(cd.getJavadoc());
    }

    public static boolean notExplicitlyHidden(final @NonNull MethodDeclaration md) {
        return !hasHidden(md.getJavadoc());
    }

    // --

    public static boolean hasDeprecated(final @NonNull Javadoc javadoc) {
        return streamTagsByName(javadoc, "deprecated")
        .findAny()
        .isPresent();
    }

    public static boolean hasHidden(final @NonNull Javadoc javadoc) {
        return streamTagsByName(javadoc, "hidden")
        .findAny()
        .isPresent();
    }

    public static boolean hasHidden(final @NonNull Optional<Javadoc> javadocOptional) {
        return javadocOptional
        .map(Javadocs::hasHidden)
        .orElse(false);
    }

    public static boolean presentAndNotHidden(final @NonNull Optional<Javadoc> javadocOptional) {
        return javadocOptional
        .map(jd->!hasHidden(jd))
        .orElse(false);
    }


}
