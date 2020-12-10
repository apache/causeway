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
package org.apache.isis.tooling.javamodel;

import java.util.stream.Stream;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;

import lombok.NonNull;

public final class ClassOrInterfaceDeclarations {

    public static <T> Stream<ConstructorDeclaration> streamConstructorDeclarations(
            final @NonNull ClassOrInterfaceDeclaration typeDeclaration) {
        return typeDeclaration.getConstructors().stream();
    }
    
    public static <T> Stream<ConstructorDeclaration> streamPublicConstructorDeclarations(
            final @NonNull ClassOrInterfaceDeclaration typeDeclaration) {
        return streamConstructorDeclarations(typeDeclaration)
                .filter(cd->ConstructorDeclarations.isEffectivePublic(cd, typeDeclaration));
    }
    
    public static <T> Stream<MethodDeclaration> streamMethodDeclarations(
            final @NonNull ClassOrInterfaceDeclaration typeDeclaration) {
        return typeDeclaration.getMethods().stream();
    }
    
    public static <T> Stream<MethodDeclaration> streamPublicMethodDeclarations(
            final @NonNull ClassOrInterfaceDeclaration typeDeclaration) {
        return streamMethodDeclarations(typeDeclaration)
                .filter(md->MethodDeclarations.isEffectivePublic(md, typeDeclaration));
    }

    public static String name(
            final @NonNull ClassOrInterfaceDeclaration td) {
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
