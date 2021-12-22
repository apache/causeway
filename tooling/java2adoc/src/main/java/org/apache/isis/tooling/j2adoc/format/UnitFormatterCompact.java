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

import org.asciidoctor.ast.StructuralNode;

import org.apache.isis.tooling.j2adoc.J2AdocContext;
import org.apache.isis.tooling.j2adoc.J2AdocUnit;
import org.apache.isis.tooling.j2adoc.convert.J2AdocConverter;
import org.apache.isis.tooling.j2adoc.convert.J2AdocConverterDefault;
import org.apache.isis.tooling.model4adoc.AsciiDocFactory;

import lombok.NonNull;
import lombok.val;

public class UnitFormatterCompact
extends UnitFormatterAbstract {

    public UnitFormatterCompact(final @NonNull J2AdocContext j2aContext) {
        super(j2aContext);
    }

    @Override
    protected Optional<String> javaSource(final J2AdocUnit unit) {
        return Optional.empty();
    }

    @Override
    protected void memberDescriptions(final J2AdocUnit unit, final StructuralNode doc) {

        val ul = AsciiDocFactory.list(doc);

        val converter = J2AdocConverterDefault.of(j2aContext);
        appendMembersToList(ul, unit,
                unit.getTypeDeclaration().getEnumConstantDeclarations(),
                decl -> converter.enumConstantDeclaration(decl),
                (javadoc, j2Unit) -> converter.javadoc(javadoc, unit, J2AdocConverter.Mode.ALL)
        );

        appendMembersToList(ul, unit,
                unit.getTypeDeclaration().getPublicFieldDeclarations(),
                decl -> converter.fieldDeclaration(decl, unit),
                (javadoc, j2Unit) -> converter.javadoc(javadoc, unit, J2AdocConverter.Mode.ALL));

        appendMembersToList(ul, unit,
                unit.getTypeDeclaration().getAnnotationMemberDeclarations(),
                decl -> converter.annotationMemberDeclaration(decl, unit),
                (javadoc, j2Unit) -> converter.javadoc(javadoc, unit, J2AdocConverter.Mode.ALL));

        appendMembersToList(ul, unit,
                unit.getTypeDeclaration().getPublicConstructorDeclarations(),
                decl -> converter.constructorDeclaration(decl, unit),
                (javadoc, j2Unit) -> converter.javadoc(javadoc, unit, J2AdocConverter.Mode.ALL));

        appendMembersToList(ul, unit,
                unit.getTypeDeclaration().getPublicMethodDeclarations(),
                decl -> converter.methodDeclaration(decl, unit),
                (javadoc, j2Unit) -> converter.javadoc(javadoc, unit, J2AdocConverter.Mode.ALL));

    }

}
