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

import java.util.stream.Stream;

import com.github.javaparser.ast.ImportDeclaration;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;

import lombok.val;

public final class ImportDeclarations {

    /**
     * Given a Can of Java import declarations returns a stream of possible fqn names
     * represented as Can of parts, where parts are the type's namespace parts and name
     * parts all together.
     * @param nameDiscriminator
     * @param importDeclarations
     */
    public static Stream<Can<String>> streamPotentialFqns(
            final Can<String> nameDiscriminator,
            final Can<ImportDeclaration> importDeclarations) {

        return importDeclarations.stream()
                .flatMap(importDeclaration->streamPotentialFqns(nameDiscriminator, importDeclaration));
    }

    /**
     * Given a Java import declaration returns a stream of possible fqn names
     * represented as Can of parts, where parts are the type's namespace parts and name
     * parts all together.
     * @param nameDiscriminator
     * @param importDeclaration
     */
    public static Stream<Can<String>> streamPotentialFqns(
            final Can<String> nameDiscriminator,
            final ImportDeclaration importDeclaration) {

        if(importDeclaration.isStatic()
                || nameDiscriminator.isEmpty()) {
            return Stream.empty();
        }

        val fqnParts = splitIntoParts(importDeclaration);

        if(!importDeclaration.isAsterisk()) {

            if(!fqnParts.endsWith(nameDiscriminator)) {
                return Stream.empty();
            }

            return Stream.of(fqnParts);
        }

        // handle asterisk case

        val nameDiscriminatorPartIterator = nameDiscriminator.reverseIterator();

        return Stream.iterate(
                Can.ofSingleton(nameDiscriminatorPartIterator.next()),
                parts->parts.add(0, nameDiscriminatorPartIterator.next()))
            .limit(nameDiscriminator.size())
            .map(fqnParts::addAll);
    }

    // -- HELPER

    private static Can<String> splitIntoParts(final ImportDeclaration importDeclaration) {
        return Can.ofStream(_Strings.splitThenStream(importDeclaration.getNameAsString(), "."));
    }


}
