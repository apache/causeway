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

    private final @NonNull J2AdocContext j2aContext;

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

    protected Optional<String> title(final J2AdocUnit unit) {
        return Optional.of(
                String.format(formatFor(unit),
                        unit.getFriendlyName()));
    }

    private static String formatFor(J2AdocUnit unit) {
        switch (unit.getTypeDeclaration().getKind()) {
            case ANNOTATION: return "@%s";
            case CLASS: return "%s";
            case ENUM: return "%s _(enum)_";
            case INTERFACE: return "%s _(interface)_";
            default:
                throw new IllegalArgumentException(String.format(
                    "unknown kind: %s", unit.getTypeDeclaration().getKind()));
        }
    }

    protected void intro(final J2AdocUnit unit, final StructuralNode parent) {
        J2AdocConverter converter = J2AdocConverterDefault.of(j2aContext);

        unit.getJavadoc()
        .filter(javadoc->!Javadocs.hasHidden(javadoc))
        .map(javadoc->converter.javadoc(javadoc, unit))
        .ifPresent(doc->parent.getBlocks().addAll(doc.getBlocks()));
    }

    protected Optional<String> javaSource(final J2AdocUnit unit) {
        return Optional.empty();
    }

    protected abstract StructuralNode getMemberDescriptionContainer(StructuralNode parent);

    protected void appendMemberDescription(StructuralNode ul, String member, Document javadoc) {
        val li = AsciiDocFactory.listItem((List) ul, member);
        val openBlock = AsciiDocFactory.openBlock(li);
        val javaDocBlock = AsciiDocFactory.block(openBlock);
        javaDocBlock.getBlocks().addAll(javadoc.getBlocks());
    }

    protected void memberDescriptions(final J2AdocUnit unit, final StructuralNode doc) {

        val ul = getMemberDescriptionContainer(doc);

        if(! j2aContext.isMemberSections() || true) {

            val converter = J2AdocConverterDefault.of(j2aContext);
            appendMemberDescriptions(ul, unit,
                    unit.getTypeDeclaration().getEnumConstantDeclarations(),
                    decl -> converter.enumConstantDeclaration(decl),
                    (javadoc, j2Unit) -> converter.javadoc(javadoc, unit)
            );

            appendMemberDescriptions(ul, unit,
                    unit.getTypeDeclaration().getPublicFieldDeclarations(),
                    decl -> converter.fieldDeclaration(decl, unit),
                    (javadoc, j2Unit) -> converter.javadoc(javadoc, unit));

            appendMemberDescriptions(ul, unit,
                    unit.getTypeDeclaration().getAnnotationMemberDeclarations(),
                    decl -> converter.annotationMemberDeclaration(decl, unit),
                    (javadoc, j2Unit) -> converter.javadoc(javadoc, unit));

            appendMemberDescriptions(ul, unit,
                    unit.getTypeDeclaration().getPublicConstructorDeclarations(),
                    decl -> converter.constructorDeclaration(decl, unit),
                    (javadoc, j2Unit) -> converter.javadoc(javadoc, unit));

            appendMemberDescriptions(ul, unit,
                    unit.getTypeDeclaration().getPublicMethodDeclarations(),
                    decl -> converter.methodDeclaration(decl, unit),
                    (javadoc, j2Unit) -> converter.javadoc(javadoc, unit));

        } else {

//            var converter = J2AdocConverterDefault.of(j2aContext);
//            appendMemberDescriptions(ul, unit,
//                    unit.getTypeDeclaration().getEnumConstantDeclarations(),
//                    decl -> converter.enumConstantDeclaration(decl),
//                    (javadoc, j2Unit) -> converter.javadoc(javadoc, unit)
//            );
//
//            appendMemberDescriptions(ul, unit,
//                    unit.getTypeDeclaration().getPublicFieldDeclarations(),
//                    decl -> converter.fieldDeclaration(decl, unit),
//                    (javadoc, j2Unit) -> converter.javadoc(javadoc, unit));
//
//            appendMemberDescriptions(ul, unit,
//                    unit.getTypeDeclaration().getAnnotationMemberDeclarations(),
//                    decl -> converter.annotationMemberDeclaration(decl, unit),
//                    (javadoc, j2Unit) -> converter.javadoc(javadoc, unit));
//
//            appendMemberDescriptions(ul, unit,
//                    unit.getTypeDeclaration().getPublicConstructorDeclarations(),
//                    decl -> converter.constructorDeclaration(decl, unit),
//                    (javadoc, j2Unit) -> converter.javadoc(javadoc, unit));
//
//            appendMemberDescriptions(ul, unit,
//                    unit.getTypeDeclaration().getPublicMethodDeclarations(),
//                    decl -> converter.methodDeclaration(decl, unit),
//                    (javadoc, j2Unit) -> converter.javadoc(javadoc, unit));

            val titleBlock = block(doc);
             titleBlock.setSource("== Members");

//            val converter = J2AdocConverterDefault.of(j2aContext);
//            appendMemberDescriptions(ul, unit,
//                    unit.getTypeDeclaration().getEnumConstantDeclarations(),
//                    decl -> converter.enumConstantDeclaration(decl),
//                    (javadoc, j2Unit) -> converter.javadoc(javadoc, unit)
//            );
//
//            appendMemberDescriptions(ul, unit,
//                    unit.getTypeDeclaration().getPublicFieldDeclarations(),
//                    decl -> converter.fieldDeclaration(decl, unit),
//                    (javadoc, j2Unit) -> converter.javadoc(javadoc, unit));
//
//            appendMemberDescriptions(ul, unit,
//                    unit.getTypeDeclaration().getAnnotationMemberDeclarations(),
//                    decl -> converter.annotationMemberDeclaration(decl, unit),
//                    (javadoc, j2Unit) -> converter.javadoc(javadoc, unit));
//
//            appendMemberDescriptions(ul, unit,
//                    unit.getTypeDeclaration().getPublicConstructorDeclarations(),
//                    decl -> converter.constructorDeclaration(decl, unit),
//                    (javadoc, j2Unit) -> converter.javadoc(javadoc, unit));
//
//            appendMemberDescriptions(ul, unit,
//                    unit.getTypeDeclaration().getPublicMethodDeclarations(),
//                    decl -> converter.methodDeclaration(decl, unit),
//                    (javadoc, j2Unit) -> converter.javadoc(javadoc, unit));


        }

    }

    private <T extends NodeWithJavadoc<?>> void appendMemberDescriptions(
            final StructuralNode container,
            final J2AdocUnit unit,
            final Can<T> declarations,
            final Function<T, String> memberDescriber,
            final BiFunction<Javadoc, J2AdocUnit, Document> javadoc2Asciidocker) {

        declarations.stream()
        .filter(Javadocs::presentAndNotHidden)
        .forEach(nwj->{
            nwj.getJavadoc()
            .ifPresent(javadoc->{
                appendMemberDescription(container,
                                memberDescriber.apply(nwj),
                                javadoc2Asciidocker.apply(javadoc, unit));
            });
        });
    }


    protected Optional<String> outro(final J2AdocUnit unit) {
        return Optional.empty();
    }


    @Override
    public Document apply(final J2AdocUnit unit) {

        val doc = AsciiDocFactory.doc();

        // -- title
        if(!j2aContext.isSkipTitleHeader()) {
            title(unit)
            .ifPresent(doc::setTitle);
        }

        // -- license

        _Strings.nonEmpty(getContext().getLicenseHeader())
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

    // -- DEPENDENCIES

    protected final J2AdocContext getContext() {
        return j2aContext;
    }

//    protected final J2AdocConverter getConverter() {
//        return j2aContext.getConverter();
//    }


}
