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

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.javadoc.Javadoc;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AnyTypeDeclaration {

    @RequiredArgsConstructor
    public static enum Kind {
        ANNOTATION("@interface"),
        CLASS("class"),
        ENUM("enum"),
        INTERFACE("interface")
        ;
        @Getter private final String javaKeyword;
        public boolean isAnnotation() { return this == ANNOTATION; }
        public boolean isClass() { return this == CLASS; }
        public boolean isEnum() { return this == ENUM; }
        public boolean isInterface() { return this == INTERFACE; }
    }
    
    private final @NonNull CompilationUnit cu;
    private final @NonNull Kind kind;
    private final TypeDeclaration<?> td;
    private final AnnotationDeclaration annotationDeclaration;
    private final ClassOrInterfaceDeclaration classOrInterfaceDeclaration; 
    private final EnumDeclaration enumDeclaration;
    
    private final Can<AnnotationMemberDeclaration> annotationMemberDeclarations;
    private final Can<EnumConstantDeclaration> enumConstantDeclarations;
    private final Can<FieldDeclaration> publicFieldDeclarations;
    private final Can<ConstructorDeclaration> publicConstructorDeclarations;
    private final Can<MethodDeclaration> publicMethodDeclarations;
    
    
    // -- FACTORIES
    
    
    public static AnyTypeDeclaration of(
            final @NonNull AnnotationDeclaration annotationDeclaration,
            final @NonNull CompilationUnit cu) {
        
        return new AnyTypeDeclaration(
                cu,
                Kind.ANNOTATION, 
                annotationDeclaration,
                annotationDeclaration,
                null,
                null,
                //members
                AnnotationDeclarations.streamAnnotationMemberDeclarations(annotationDeclaration)
                    .collect(Can.toCan()),
                Can.empty(),
                AnnotationDeclarations.streamFieldDeclarations(annotationDeclaration)
                    .collect(Can.toCan()),
                Can.empty(),
                AnnotationDeclarations.streamMethodDeclarations(annotationDeclaration)
                    .collect(Can.toCan())
                );
    }
    
    public static AnyTypeDeclaration of(
            final @NonNull ClassOrInterfaceDeclaration classOrInterfaceDeclaration,
            final @NonNull CompilationUnit cu) {
        return new AnyTypeDeclaration(
                cu,
                classOrInterfaceDeclaration.isInterface() ? Kind.INTERFACE : Kind.CLASS, 
                classOrInterfaceDeclaration,
                null,
                classOrInterfaceDeclaration,
                null,
                //members
                Can.empty(),
                Can.empty(),
                ClassOrInterfaceDeclarations.streamPublicFieldDeclarations(classOrInterfaceDeclaration)
                    .collect(Can.toCan()),
                ClassOrInterfaceDeclarations.streamPublicConstructorDeclarations(classOrInterfaceDeclaration)
                    .collect(Can.toCan()),
                ClassOrInterfaceDeclarations.streamPublicMethodDeclarations(classOrInterfaceDeclaration)
                    .collect(Can.toCan())
                );
    }
    
    public static AnyTypeDeclaration of(
            final @NonNull EnumDeclaration enumDeclaration,
            final @NonNull CompilationUnit cu) {
        return new AnyTypeDeclaration(
                cu,
                Kind.ENUM, 
                enumDeclaration, 
                null, 
                null,
                enumDeclaration,
                //members
                Can.empty(),
                EnumDeclarations.streamEnumConstantDeclarations(enumDeclaration)
                    .collect(Can.toCan()),
                EnumDeclarations.streamPublicFieldDeclarations(enumDeclaration)
                    .collect(Can.toCan()),
                EnumDeclarations.streamPublicConstructorDeclarations(enumDeclaration)
                    .collect(Can.toCan()),
                EnumDeclarations.streamPublicMethodDeclarations(enumDeclaration)
                    .collect(Can.toCan())
                );
    }
    
    public static AnyTypeDeclaration auto(
            final @NonNull TypeDeclaration<?> td,
            final @NonNull CompilationUnit cu) {
        
        if(td instanceof ClassOrInterfaceDeclaration) {
            return of((ClassOrInterfaceDeclaration)td, cu);
        }
        if(td instanceof EnumDeclaration) {
            return of((EnumDeclaration)td, cu);
        }
        if(td instanceof AnnotationDeclaration) {
            return of((AnnotationDeclaration)td, cu);
        }
        throw _Exceptions.unsupportedOperation("unsupported TypeDeclaration %s", td.getClass());
    }
    
    // -- UTILITY
    
    public Optional<Javadoc> getJavadoc() {
        return td.getJavadoc();
    }
    
    public Optional<PackageDeclaration> getPackageDeclaration() {
        return cu.getPackageDeclaration();
    }
    
    public boolean hasIndexDirective() {
        return TypeDeclarations.hasIndexDirective(td);
    }
    
    /**
     * Returns the recursively resolved (nested) type name. 
     * Same as {@link #getSimpleName()} if type is not nested. 
     */
    @Getter(lazy = true)
    private final String name = createName();
    
    public String getSimpleName() {
        return td.getNameAsString();
    }
    
    // -- HELPER 
    
    private String createName() {
        String name = td.getNameAsString(); 
        Node walker = td; 
        while(walker.getParentNode().isPresent()) {
            walker = walker.getParentNode().get();
            if(walker instanceof NodeWithSimpleName) {
                name = ((NodeWithSimpleName<?>)walker).getNameAsString() + "." + name;
            } else break;
        }
        return name;
    }

    
}
