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

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.javadoc.Javadoc;

import org.asciidoctor.ast.Document;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.tooling.j2adoc.J2AdocContext;
import org.apache.isis.tooling.j2adoc.J2AdocUnit;

import lombok.NonNull;

public interface J2AdocConverter {

    Document javadoc(Javadoc javadoc, Can<ImportDeclaration> importDeclarations);

    String annotationMemberDeclaration(AnnotationMemberDeclaration amd, Can<ImportDeclaration> importDeclarations);
    
    String enumConstantDeclaration(EnumConstantDeclaration ecd);
    
    String fieldDeclaration(FieldDeclaration fd, Can<ImportDeclaration> importDeclarations);
    
    String constructorDeclaration(ConstructorDeclaration cd, Can<ImportDeclaration> importDeclarations);

    String methodDeclaration(MethodDeclaration md, Can<ImportDeclaration> importDeclarations);
    
    String xref(@NonNull J2AdocUnit unit);
    
    // -- FACTORIES
    
    public static J2AdocConverter createDefault(final @NonNull J2AdocContext context) {
        return J2AdocConverterDefault.of(context);
    }
    

}
