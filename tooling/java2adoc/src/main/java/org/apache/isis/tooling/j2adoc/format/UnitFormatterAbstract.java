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
import java.util.function.BiFunction;
import java.util.function.Function;

import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.github.javaparser.javadoc.Javadoc;

import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.List;
import org.asciidoctor.ast.StructuralNode;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.tooling.j2adoc.J2AdocContext;
import org.apache.isis.tooling.j2adoc.J2AdocUnit;
import org.apache.isis.tooling.j2adoc.convert.J2AdocConverter;
import org.apache.isis.tooling.j2adoc.convert.J2AdocConverterDefault;
import org.apache.isis.tooling.javamodel.ast.Javadocs;
import org.apache.isis.tooling.model4adoc.AsciiDocFactory;

import static org.apache.isis.tooling.model4adoc.AsciiDocFactory.block;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class UnitFormatterAbstract
implements UnitFormatter {

    protected final @NonNull J2AdocContext j2aContext;

    @Override
    public String getEnumConstantFormat() {
        return "`%s`";
    }

    @Override
    public String getAnnotationMemberFormat() {
        return "`%2$s` : `%1$s`";
    }

    @Override
    public String getFieldFormat() {
        return "`%2$s` : `%1$s`";
    }

    @Override
    public String getConstructorFormat() {
        return "`%1$s(%2$s)`";
    }

    @Override
    public String getGenericConstructorFormat() {
        return "`%2$s%1$s(%3$s)`";
    }

    @Override
    public String getMethodFormat() {
        return "`%2$s(%3$s)` : `%1$s`";
    }

    @Override
    public String getGenericMethodFormat() {
        return "`%3$s%1$s(%4$s)` : `%2$s`";
    }


    /**
     * Main algorithm for laying out a unit.
     *
     * @param unit
     * @return
     */
    @Override
    public Document apply(final J2AdocUnit unit) {

        val doc = AsciiDocFactory.doc();

        // -- title
        if(!j2aContext.isSkipTitleHeader()) {
            title(unit)
            .ifPresent(doc::setTitle);
        }

        // -- license

        _Strings.nonEmpty(j2aContext.getLicenseHeader())
        .ifPresent(notice->AsciiDocFactory.attrNotice(doc, notice));


        // -- intro

        intro(unit, doc);

        // == API
        val titleBlock = block(doc);
        titleBlock.setSource("== API");

        // -- java source

        javaSource(unit)
        .ifPresent(block(doc)::setSource);

        // -- member descriptions

        memberDescriptions(unit, doc);

        // -- outro

        outro(unit)
        .ifPresent(block(doc)::setSource);

        return doc;
    }

    /**
     * Mandatory hook method to return a representation of the java source (if any)
     *
     * @param unit
     * @return
     */
    protected abstract Optional<String> javaSource(final J2AdocUnit unit);

    /**
     * Mandatory hook method to append representation of the members of the unit.
     *
     * @param unit
     * @param doc
     */
    protected abstract void memberDescriptions(final J2AdocUnit unit, final StructuralNode doc);

    protected Optional<String> title(final J2AdocUnit unit) {
        return Optional.of(Snippets.title(unit));
    }

    protected void intro(final J2AdocUnit unit, final StructuralNode parent) {
        J2AdocConverter converter = J2AdocConverterDefault.of(j2aContext);

        unit.getJavadoc()
                .filter(javadoc->!Javadocs.hasHidden(javadoc))
                .map(javadoc->converter.javadoc(javadoc, unit))
                .ifPresent(doc->parent.getBlocks().addAll(doc.getBlocks()));
    }

    /**
     * Helper method for use by subclasses; calls
     * {@link #appendMemberToList(List, String, Document)}
     * for all provided {@link NodeWithJavadoc declarations}, using the
     * provided memberRepresenter and the provided strategy for converting the
     * javadoc into Asciidoc.
     *
     * @param ul - the List within the Asciidoc document to append to.
     * @param unit - the containing java unit
     * @param declarations - the collection of {@link NodeWithJavadoc declarations} to process
     * @param memberRepresenter - encodes which parts of the member are to be pulled out into a representation
     * @param javadoc2Asciidocker - strategy for converting each node's javadoc into some Asciidoc
     *
     * @param <T> - the specific subtype of {@link NodeWithJavadoc}
     */
    protected <T extends NodeWithJavadoc<?>> void appendMembersToList(
            final List ul,
            final J2AdocUnit unit,
            final Can<T> declarations,
            final Function<T, String> memberRepresenter,
            final BiFunction<Javadoc, J2AdocUnit, Document> javadoc2Asciidocker) {

        declarations.stream()
                .filter(Javadocs::presentAndNotHidden)
                .forEach(nwj->{
                    nwj.getJavadoc()
                            .ifPresent(javadoc-> {
                                final String memberRepresentation = memberRepresenter.apply(nwj);
                                final Document asciidoc = javadoc2Asciidocker.apply(javadoc, unit);
                                appendMemberToList(ul,
                                        memberRepresentation,
                                        asciidoc);
                            });
                });
    }


    /**
     * Helper method called by {@link #appendMembersToList(List, J2AdocUnit, Can, Function, BiFunction)}.
     *
     * @param ul
     * @param listItemText
     * @param listItemParagraphs
     */
    private static void appendMemberToList(
            final List ul,
            final String listItemText,
            final Document listItemParagraphs) {

        val li = AsciiDocFactory.listItem(ul, listItemText);
        val openBlock = AsciiDocFactory.openBlock(li);
        val javaDocBlock = AsciiDocFactory.block(openBlock);
        javaDocBlock.getBlocks().addAll(listItemParagraphs.getBlocks());
    }

    /**
     * Hook method (with empty default implementation)
     * @param unit
     * @return
     */
    protected Optional<String> outro(final J2AdocUnit unit) {
        return Optional.empty();
    }

}
