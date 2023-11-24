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

import java.io.File;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

public final class CompilationUnits {

    @SneakyThrows
    public static CompilationUnit parse(final @NonNull File sourceFile) {
        return StaticJavaParser.parse(sourceFile);
    }

    public static Predicate<CompilationUnit> isPublic() {
        return cu->cu.getPrimaryType()
        .map(primaryType->primaryType.isPublic())
        .orElse(false);
    }

    public static <T> Stream<ImportDeclaration> streamImportDeclarations(
            final @NonNull CompilationUnit compilationUnit) {
        return compilationUnit.getImports().stream();
    }

    public static <T> Stream<AnyTypeDeclaration> streamTypeDeclarations(
            final @NonNull CompilationUnit compilationUnit) {

        return getPrimaryType(compilationUnit)
        .map(type->type
                .findAll(TypeDeclaration.class)
                .stream()
                .map(td->AnyTypeDeclaration.auto(td, compilationUnit)))
        .orElseGet(Stream::empty);
    }

    public static <T> Stream<ClassOrInterfaceDeclaration> streamClassOrInterfaceDeclarations(
            final @NonNull CompilationUnit compilationUnit) {

        return getPrimaryType(compilationUnit)
        .map(type->type
                .findAll(ClassOrInterfaceDeclaration.class)
                .stream())
        .orElseGet(Stream::empty);
    }

    public static <T> Stream<EnumDeclaration> streamEnumDeclarations(
            final @NonNull CompilationUnit compilationUnit) {

        return getPrimaryType(compilationUnit)
        .map(type->type
                .findAll(EnumDeclaration.class)
                .stream())
        .orElseGet(Stream::empty);
    }

    // -- HELPER

    private static Optional<TypeDeclaration<?>> getPrimaryType(
            final @NonNull CompilationUnit compilationUnit) {

        val type = compilationUnit.getPrimaryType()
                .orElseGet(()->
                    compilationUnit.getTypes()
                    .getFirst()
                    .orElse(null));

        if(type==null) {
            System.err.println("could not find any type in CompilationUnit ...\n" +
                    compilationUnit);
            return Optional.empty();
        }

        return Optional.of(type);
    }


}
