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

import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.github.javaparser.javadoc.Javadoc;

import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.List;
import org.asciidoctor.ast.StructuralNode;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.tooling.j2adoc.J2AdocContext;
import org.apache.isis.tooling.j2adoc.J2AdocUnit;
import org.apache.isis.tooling.j2adoc.convert.J2AdocConverter;
import org.apache.isis.tooling.j2adoc.convert.J2AdocConverterDefault;
import org.apache.isis.tooling.javamodel.ast.CallableDeclarations;
import org.apache.isis.tooling.javamodel.ast.Javadocs;
import org.apache.isis.tooling.model4adoc.AsciiDocFactory;

import lombok.NonNull;
import lombok.val;

public class UnitFormatterWithSourceAndSections
extends UnitFormatterAbstract {

    public UnitFormatterWithSourceAndSections(final @NonNull J2AdocContext j2aContext) {
        super(j2aContext);
    }

    @Override
    protected Optional<String> javaSource(final J2AdocUnit unit) {

        final String javaSource = Snippets.javaSourceFor(unit, j2aContext);
        return Optional.of(
                AsciiDocFactory.toString(doc->
                    AsciiDocFactory.SourceFactory.java(doc, javaSource, unit.getCanonicalName() + ".java")));
    }

    @Override
    protected void memberDescriptions(final J2AdocUnit unit, final StructuralNode parent) {

        val ul = AsciiDocFactory.callouts(parent);

        var converter = J2AdocConverterDefault.of(j2aContext);
        val firstParaOnly = new BiFunction<Javadoc, J2AdocUnit, Document>() {
            @Override
            public Document apply(final Javadoc javadoc, final J2AdocUnit j2Unit) {
                return converter.javadoc(javadoc, unit, J2AdocConverter.Mode.FIRST_PARA_ONLY);
            }
        };

        appendMembersToList(ul, unit,
                unit.getTypeDeclaration().getEnumConstantDeclarations(),
                decl -> String.format("xref:#%s[%s]",
                        decl.getName(),
                        decl.getName()),
                firstParaOnly
        );

        appendMembersToList(ul, unit,
                unit.getTypeDeclaration().getPublicFieldDeclarations(),
                decl -> String.format("xref:#%s[%s]",
                        decl.getVariables().stream().findFirst().get().getName(),
                        decl.getVariables().stream().findFirst().get().getName()),
                firstParaOnly);

        appendMembersToList(ul, unit,
                unit.getTypeDeclaration().getAnnotationMemberDeclarations(),
                decl -> String.format("xref:#%s[%s]",
                        decl.getName(),
                        decl.getName()),
                firstParaOnly);

        appendMembersToList(ul, unit,
                unit.getTypeDeclaration().getPublicConstructorDeclarations(),
                decl -> String.format("xref:#%s[%s]",
                        CallableDeclarations.asAnchor(decl),
                        CallableDeclarations.asMethodSignature(decl)),
                firstParaOnly);

        appendMembersToList(ul, unit,
                unit.getTypeDeclaration().getPublicMethodDeclarations(),
                decl -> String.format("xref:#%s[%s]",
                        CallableDeclarations.asAnchor(decl),
                        CallableDeclarations.asMethodSignature(decl)),
                firstParaOnly);


        //
        // now the members section
        //
        val membersDoc = AsciiDocFactory.doc();

        val allJavadocStrategy = new BiFunction<Javadoc, J2AdocUnit, Document>() {
            @Override
            public Document apply(final Javadoc javadoc, final J2AdocUnit j2Unit) {
                return converter.javadoc(javadoc, unit, J2AdocConverter.Mode.ALL);
            }
        };

        appendMemberSections(membersDoc, unit,
                unit.getTypeDeclaration().getEnumConstantDeclarations(),
                decl -> decl.getName().toString(),
                decl -> decl.getName().toString(),
                allJavadocStrategy
        );

        appendMemberSections(membersDoc, unit,
                unit.getTypeDeclaration().getPublicFieldDeclarations(),
                decl -> decl.getVariables().stream().findFirst().get().getName().toString(),
                decl -> decl.getVariables().stream().findFirst().get().getName().toString(),
                allJavadocStrategy);

        appendMemberSections(membersDoc, unit,
                unit.getTypeDeclaration().getAnnotationMemberDeclarations(),
                decl -> decl.getName().toString(),
                decl -> decl.getName().toString(),
                allJavadocStrategy);

        appendMemberSections(membersDoc, unit,
                unit.getTypeDeclaration().getPublicConstructorDeclarations(),
                CallableDeclarations::asAnchor,
                CallableDeclarations::asMethodSignature,
                allJavadocStrategy);

        appendMemberSections(membersDoc, unit,
                unit.getTypeDeclaration().getPublicMethodDeclarations(),
                CallableDeclarations::asAnchor,
                CallableDeclarations::asMethodSignature,
                allJavadocStrategy);

        if (!membersDoc.getBlocks().isEmpty()) {

            val titleBlock = AsciiDocFactory.block(parent);
            titleBlock.setSource("== Members");

            parent.append(membersDoc);
        }

    }

    /**
     * Helper method for use by subclasses; calls
     * {@link #appendMemberSection(StructuralNode, String, Document)}
     * for all provided {@link NodeWithJavadoc declarations}, using the
     * provided memberRepresenter and the provided strategy for converting the
     * javadoc into Asciidoc.
     *
     * @param container - the List within the Asciidoc document to append to.
     * @param unit - the containing java unit (java source code model)
     * @param declarations - the collection of {@link NodeWithJavadoc declarations} to process
     * @param memberSignature - encodes which parts of the member are to be pulled out into a representation
     * @param javadoc2Asciidocker - strategy for converting each node's javadoc into some Asciidoc
     *
     * @param <T> - the specific subtype of {@link NodeWithJavadoc}
     */
    protected <T extends NodeWithJavadoc<?>> void appendMemberSections(
            final StructuralNode container,
            final J2AdocUnit unit,
            final Can<T> declarations,
            final Function<T, String> memberAnchor,
            final Function<T, String> memberSignature,
            final BiFunction<Javadoc, J2AdocUnit, Document> javadoc2Asciidocker) {

        declarations.stream()
                .filter(Javadocs::presentAndNotHidden)
                .forEach(nwj->{
                    nwj.getJavadoc()
                            .ifPresent(javadoc-> {
                                final Document asciidoc = javadoc2Asciidocker.apply(javadoc, unit);
                                appendMemberSection(container,
                                        memberAnchor.apply(nwj),
                                        memberSignature.apply(nwj),
                                        asciidoc);
                            });
                });
    }


    /**
     * Helper method called by {@link #appendMembersToList(List, J2AdocUnit, Can, Function, BiFunction)}.
     *
     * @param section
     * @param sectionHeader
     * @param sectionContent
     */
    private static void appendMemberSection(
            final StructuralNode section,
            final String sectionAnchor,
            final String sectionHeader,
            final Document sectionContent) {

        val titleBlock = AsciiDocFactory.block(section);
        titleBlock.setId(sectionAnchor);
        titleBlock.setLines(Arrays.asList(
                String.format("[#%s]", sectionAnchor),
                String.format("=== %s", sectionHeader)
        ));

        section.append(sectionContent);
    }

}
