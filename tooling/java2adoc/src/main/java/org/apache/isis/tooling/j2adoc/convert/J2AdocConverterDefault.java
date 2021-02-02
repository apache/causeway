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

import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import com.github.javaparser.javadoc.description.JavadocInlineTag;
import com.github.javaparser.javadoc.description.JavadocSnippet;

import org.asciidoctor.ast.Document;
import org.jsoup.Jsoup;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal._Constants;
import org.apache.isis.tooling.j2adoc.J2AdocContext;
import org.apache.isis.tooling.j2adoc.J2AdocUnit;
import org.apache.isis.tooling.javamodel.ast.AnnotationMemberDeclarations;
import org.apache.isis.tooling.javamodel.ast.ConstructorDeclarations;
import org.apache.isis.tooling.javamodel.ast.EnumConstantDeclarations;
import org.apache.isis.tooling.javamodel.ast.FieldDeclarations;
import org.apache.isis.tooling.javamodel.ast.Javadocs;
import org.apache.isis.tooling.javamodel.ast.MethodDeclarations;
import org.apache.isis.tooling.model4adoc.AsciiDocFactory;

import lombok.NonNull;
import lombok.Value;
import lombok.val;

@Value(staticConstructor = "of")
final class J2AdocConverterDefault implements J2AdocConverter {

    private final J2AdocContext j2aContext;

    @Override
    public String annotationMemberDeclaration(
            final @NonNull AnnotationMemberDeclaration amd,
            final @NonNull Can<ImportDeclaration> importDeclarations) {
        val isDeprecated = amd.getAnnotations().stream()
                .anyMatch(a->a.getNameAsString().equals("Deprecated"))
                || amd.getJavadoc()
                    .map(Javadocs::hasDeprecated)
                    .orElse(false);
        
        val memberNameFormat = isDeprecated
                ? j2aContext.getDeprecatedStaticMemberNameFormat()
                : j2aContext.getStaticMemberNameFormat();
        
        val annotMemberFormat =  j2aContext.getFormatter().getAnnotationMemberFormat();
       
        return String.format(annotMemberFormat,
                type(amd.getType(), importDeclarations), 
                String.format(memberNameFormat, AnnotationMemberDeclarations.asNormalizedName(amd)));
    }
    
    @Override
    public String enumConstantDeclaration(final @NonNull EnumConstantDeclaration ecd) {
        val isDeprecated = ecd.getAnnotations().stream()
                .anyMatch(a->a.getNameAsString().equals("Deprecated"))
                || ecd.getJavadoc()
                    .map(Javadocs::hasDeprecated)
                    .orElse(false);
        
        val memberNameFormat = isDeprecated
                ? j2aContext.getDeprecatedStaticMemberNameFormat()
                : j2aContext.getStaticMemberNameFormat();
        
        val enumConstFormat =  j2aContext.getFormatter().getEnumConstantFormat();
        
        return String.format(enumConstFormat, 
                String.format(memberNameFormat, EnumConstantDeclarations.asNormalized(ecd)));
    }
    
    @Override
    public String fieldDeclaration(
            final @NonNull FieldDeclaration fd,
            final @NonNull Can<ImportDeclaration> importDeclarations) {
        
        val isDeprecated = fd.getAnnotations().stream()
                .anyMatch(a->a.getNameAsString().equals("Deprecated"))
                || fd.getJavadoc()
                    .map(Javadocs::hasDeprecated)
                    .orElse(false);
        
        val memberNameFormat = isDeprecated
                ? fd.isStatic()
                        ? j2aContext.getDeprecatedStaticMemberNameFormat()
                        : j2aContext.getDeprecatedMemberNameFormat()
                : fd.isStatic()
                        ? j2aContext.getStaticMemberNameFormat()
                        : j2aContext.getMemberNameFormat();
        
        val fieldFormat =  j2aContext.getFormatter().getFieldFormat();
       
        return String.format(fieldFormat,
                type(fd.getCommonType(), importDeclarations), 
                String.format(memberNameFormat, FieldDeclarations.asNormalizedName(fd)));
    }
    
    @Override
    public String constructorDeclaration(
            final @NonNull ConstructorDeclaration cd,
            final @NonNull Can<ImportDeclaration> importDeclarations) {
        
        val isDeprecated = cd.getAnnotations().stream()
                .anyMatch(a->a.getNameAsString().equals("Deprecated"))
                || cd.getJavadoc()
                    .map(Javadocs::hasDeprecated)
                    .orElse(false);
        
        val memberNameFormat = isDeprecated
                ? cd.isStatic()
                        ? j2aContext.getDeprecatedStaticMemberNameFormat()
                        : j2aContext.getDeprecatedMemberNameFormat()
                : cd.isStatic()
                        ? j2aContext.getStaticMemberNameFormat()
                        : j2aContext.getMemberNameFormat();

        val typeParams = ConstructorDeclarations.getTypeParameters(cd);
        
        val isGenericMember = !typeParams.isEmpty();
        
        val constructorFormat = isGenericMember
                ? j2aContext.getFormatter().getGenericConstructorFormat()
                : j2aContext.getFormatter().getConstructorFormat();
        
        val args = Can.<Object>of(
                isGenericMember ? typeParamters(typeParams) : null,  // Cans do ignored null 
                String.format(memberNameFormat, ConstructorDeclarations.asNormalizedName(cd)),
                parameters(cd.getParameters().stream(), importDeclarations)
                );
       
        return String.format(constructorFormat, args.toArray(_Constants.emptyObjects));
    }

    @Override
    public String methodDeclaration(
            final @NonNull MethodDeclaration md, 
            final @NonNull Can<ImportDeclaration> importDeclarations) {
        
        val isDeprecated = md.getAnnotations().stream()
                .anyMatch(a->a.getNameAsString().equals("Deprecated"))
                || md.getJavadoc()
                    .map(Javadocs::hasDeprecated)
                    .orElse(false);
        
        val memberNameFormat = isDeprecated
                ? md.isStatic()
                        ? j2aContext.getDeprecatedStaticMemberNameFormat()
                        : j2aContext.getDeprecatedMemberNameFormat()
                : md.isStatic()
                        ? j2aContext.getStaticMemberNameFormat()
                        : j2aContext.getMemberNameFormat();

        val typeParams = MethodDeclarations.getTypeParameters(md);
        
        val isGenericMember = !typeParams.isEmpty();
        
        val methodFormat = isGenericMember
                ? j2aContext.getFormatter().getGenericMethodFormat()
                : j2aContext.getFormatter().getMethodFormat();
        
        val args = Can.<Object>of(
                isGenericMember ? typeParamters(typeParams) : null,  // Cans do ignored null 
                type(md.getType(), importDeclarations),
                String.format(memberNameFormat, MethodDeclarations.asNormalizedName(md)), 
                parameters(md.getParameters().stream(), importDeclarations)
                );
       
        return String.format(methodFormat, args.toArray(_Constants.emptyObjects));
                
    }
    
    public String parameters(
            final @NonNull Stream<Parameter> parameterStream,
            final @NonNull Can<ImportDeclaration> importDeclarations) {
        return parameterStream
                .map(x->parameterDeclaration(x, importDeclarations))
                .collect(Collectors.joining(", "));
    }
    
    public String typeParamters(final @Nullable Can<TypeParameter> typeParamters) {
        if(typeParamters == null
                || typeParamters.isEmpty()) {
            return "";
        }
        return String.format("<%s>", typeParamters
                .stream()
                .map(TypeParameter::getName)
                .map(SimpleName::asString)
                .collect(Collectors.joining(", ")));
    }
    
    public String type(
            final @NonNull Type type,
            final @NonNull Can<ImportDeclaration> importDeclarations) {
        
        if(type instanceof ClassOrInterfaceType) {
            return classOrInterfaceType((ClassOrInterfaceType) type, importDeclarations);
        }
        return type.asString();
    }
    
    public String classOrInterfaceType(
            final @NonNull ClassOrInterfaceType type,
            final @NonNull Can<ImportDeclaration> importDeclarations) {
        
        val sb = new StringBuilder();
        sb.append(xrefIfRequired(type.getNameAsString(), importDeclarations)); // type simple name, no generics
        type.getTypeArguments()
        .ifPresent(typeArgs->{
            sb
            .append("<")
            .append(
                    typeArgs.stream()
                    .map(typeArg->type(typeArg, importDeclarations))
                    .collect(Collectors.joining(", "))
            )
            .append(">");
        });
        
        return sb.toString();
    }
        
    
    public String parameterDeclaration(
            final @NonNull Parameter p,
            final @NonNull Can<ImportDeclaration> importDeclarations) {
        
        return String.format("%s%s %s",
                type(p.getType(), importDeclarations),
                p.isVarArgs() ? "..." : "",
                p.getNameAsString());
    }
    
    @Override
    public Document javadoc(
            final @NonNull Javadoc javadoc,
            final @NonNull Can<ImportDeclaration> importDeclarations) {

        val doc = AsciiDocFactory.doc();
        
        Javadocs.streamTagContent(javadoc, "deprecated")
        .findFirst()
        .map(javadocDescription->javadocDescription(javadocDescription, importDeclarations))
        .ifPresent(deprecatedAdoc->{
            
            val deprecatedBlock = AsciiDocFactory.warning(doc);
            deprecatedBlock.setSource("[red]#_deprecated:_#");
            deprecatedBlock.getBlocks().addAll(deprecatedAdoc.getBlocks());
        });
        
        val descriptionAdoc = javadocDescription(javadoc.getDescription(), importDeclarations);
        
        doc.getBlocks().addAll(descriptionAdoc.getBlocks());
        
        return doc;
    }
    
    public String inlineTag(
            final @NonNull JavadocInlineTag inlineTag, 
            final @NonNull Can<ImportDeclaration> importDeclarations) {

        val inlineContent = inlineTag.getContent().trim();

        switch(inlineTag.getType()) {
        case LINK:
            val referencedUnit = j2aContext.findUnit(inlineContent, importDeclarations).orElse(null);
            if(referencedUnit!=null) {
                return String.format(" %s ", xref(referencedUnit));
            }
        default:
            return String.format(" _%s_ ", inlineContent);
        }
    }
    
    @Override
    public String xref(final @NonNull J2AdocUnit unit) {

        val xrefCoordinates = unit.getNamespace()
        .stream()
        .skip(j2aContext.getNamespacePartsSkipCount())
        .collect(Can.toCan())
        .add(unit.getCanonicalName()) 
        .stream()
        .collect(Collectors.joining("/"));
        
        val xref = String.format("xref:%s[%s]", 
                String.format(j2aContext.getXrefPageIdFormat(), xrefCoordinates), 
                unit.getFriendlyName());
        
        return xref;
    }

    private String xrefIfRequired(
            final @NonNull String typeSimpleName,
            final @NonNull Can<ImportDeclaration> importDeclarations) {
        return j2aContext.findUnit(typeSimpleName, importDeclarations)
                .map(this::xref)
                .orElse(typeSimpleName);
    }
    
    public String javadocSnippet(final @NonNull JavadocSnippet snippet) {
        return snippet.toText();
    }
    
    // -- HELPER
    
    private Document javadocDescription(
            final @NonNull JavadocDescription javadocDescription,
            final @NonNull Can<ImportDeclaration> importDeclarations) {
        
        val javadocResolved = new StringBuilder();

        javadocDescription.getElements()
        .forEach(e->{

            if(e instanceof JavadocSnippet) {
                javadocResolved.append(javadocSnippet((JavadocSnippet)e));
            } else if(e instanceof JavadocInlineTag) {
                javadocResolved.append(inlineTag((JavadocInlineTag) e, importDeclarations));
            } else {
                javadocResolved.append(e.toText());
            }

        });

        val descriptionAsHtml = Jsoup.parse(javadocResolved.toString());
        val adoc = HtmlToAsciiDoc.body(descriptionAsHtml.selectFirst("body"));
        return adoc;
    }


}
