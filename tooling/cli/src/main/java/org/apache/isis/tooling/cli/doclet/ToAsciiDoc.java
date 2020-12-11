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
package org.apache.isis.tooling.cli.doclet;

import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ConstructorDeclaration;
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
import org.apache.isis.tooling.javamodel.ast.Javadocs;
import org.apache.isis.tooling.model4adoc.AsciiDocFactory;
import org.apache.isis.tooling.model4adoc.AsciiDocWriter;

import lombok.NonNull;
import lombok.Value;
import lombok.val;

@Value(staticConstructor = "of")
class ToAsciiDoc {

    private final AdocletContext docletContext;
    
    public String constructorDeclaration(final @NonNull ConstructorDeclaration cd) {
        val isDeprecated = cd.getAnnotations().stream()
                .anyMatch(a->a.getNameAsString().equals("Deprecated"))
                || cd.getJavadoc()
                    .map(Javadocs::hasDeprecated)
                    .orElse(false);
        
        val memberNameFormat = isDeprecated
                ? cd.isStatic()
                        ? docletContext.getDeprecatedStaticMemberNameFormat()
                        : docletContext.getDeprecatedMemberNameFormat()
                : cd.isStatic()
                    ? docletContext.getStaticMemberNameFormat()
                    : docletContext.getMemberNameFormat();

        val isGenericMember = cd.getTypeParameters().isNonEmpty();
        
        val constructorFormat = isGenericMember
                ? docletContext.getGenericConstructorFormat()
                : docletContext.getConstructorFormat();
        
        val args = Can.<Object>of(
                isGenericMember ? typeParamters(cd.getTypeParameters()) : null,  // Cans do ignored null 
                String.format(memberNameFormat, cd.getNameAsString()), 
                cd.getParameters()
                    .stream()
                    .map(this::parameterDeclaration)
                    .collect(Collectors.joining(", "))
                );
       
        return String.format(constructorFormat, args.toArray(_Constants.emptyObjects));
    }

    public String methodDeclaration(final @NonNull MethodDeclaration md) {
        
        val isDeprecated = md.getAnnotations().stream()
                .anyMatch(a->a.getNameAsString().equals("Deprecated"))
                || md.getJavadoc()
                    .map(Javadocs::hasDeprecated)
                    .orElse(false);
        
        val memberNameFormat = isDeprecated
                ? md.isStatic()
                        ? docletContext.getDeprecatedStaticMemberNameFormat()
                        : docletContext.getDeprecatedMemberNameFormat()
                : md.isStatic()
                    ? docletContext.getStaticMemberNameFormat()
                    : docletContext.getMemberNameFormat();

        val isGenericMember = md.getTypeParameters().isNonEmpty();
        
        val methodFormat = isGenericMember
                ? docletContext.getGenericMethodFormat()
                : docletContext.getMethodFormat();
        
        val args = Can.<Object>of(
                isGenericMember ? typeParamters(md.getTypeParameters()) : null,  // Cans do ignored null 
                type(md.getType()),
                String.format(memberNameFormat, md.getNameAsString()), 
                md.getParameters()
                    .stream()
                    .map(this::parameterDeclaration)
                    .collect(Collectors.joining(", "))
                );
       
        return String.format(methodFormat, args.toArray(_Constants.emptyObjects));
                
    }

    
    public String typeParamters(final @Nullable NodeList<TypeParameter> typeParamters) {
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
    
    public String type(final @NonNull Type type) {
        if(type instanceof ClassOrInterfaceType) {
            return classOrInterfaceType((ClassOrInterfaceType) type);
        }
        return type.asString();
    }
    
    public String classOrInterfaceType(final @NonNull ClassOrInterfaceType type) {
        val sb = new StringBuilder();
        sb.append(xrefIfRequired(type.getNameAsString())); // type simple name, no generics
        type.getTypeArguments()
        .ifPresent(typeArgs->{
            sb
            .append("<")
            .append(
                    typeArgs.stream()
                    .map(typeArg->type(typeArg))
                    .collect(Collectors.joining(", "))
            )
            .append(">");
        });
        
        return sb.toString();
    }
        
    
    public String parameterDeclaration(Parameter p) {
        return String.format("%s%s %s",
                type(p.getType()),
                p.isVarArgs() ? "..." : "",
                p.getNameAsString());
    }
    
    public String javadoc(final @NonNull Javadoc javadoc, final int level) {

        val adoc = AsciiDocFactory.doc();
        
        Javadocs.streamTagContent(javadoc, "deprecated")
        .findFirst()
        .map(javadocDescription->javadocDescription(javadocDescription, level))
        .ifPresent(deprecatedAdoc->{
            
            val deprecatedBlock = AsciiDocFactory.block(adoc);
            deprecatedBlock.setSource("+\n[red]#_deprecated:_#");
            deprecatedBlock.getBlocks().addAll(deprecatedAdoc.getBlocks());
        });
        
        val descriptionAdoc = javadocDescription(javadoc.getDescription(), level);
        
        adoc.getBlocks().addAll(descriptionAdoc.getBlocks());

        return AsciiDocWriter.toString(adoc);
    }
    
    public String inlineTag(final @NonNull JavadocInlineTag inlineTag) {

        val inlineContent = inlineTag.getContent().trim();

        switch(inlineTag.getType()) {
        case LINK:
            val refDoclet = docletContext.getAdoclet(inlineContent).orElse(null);
            if(refDoclet!=null) {
                return String.format(" %s ", xref(refDoclet));
            }
        default:
            return String.format(" _%s_ ", inlineContent);
        }
    }
    
    public String xref(final @NonNull Adoclet doclet) {
        return String.format("xref:%s[%s]", 
                String.format(docletContext.getXrefPageIdFormat(), doclet.getName()), 
                doclet.getName()); 
    }

    public String xrefIfRequired(final @NonNull String docIndexKey) {
        return docletContext.getAdoclet(docIndexKey)
                .map(this::xref)
                .orElse(docIndexKey);
    }
    
    public String javadocSnippet(final @NonNull JavadocSnippet snippet) {
        return snippet.toText();
    }
    
    // -- HELPER
    
    private Document javadocDescription(final @NonNull JavadocDescription javadocDescription, final int level) {
        val javadocResolved = new StringBuilder();

        javadocDescription.getElements()
        .forEach(e->{

            if(e instanceof JavadocSnippet) {
                javadocResolved.append(javadocSnippet((JavadocSnippet)e));
            } else if(e instanceof JavadocInlineTag) {
                javadocResolved.append(inlineTag((JavadocInlineTag) e));
            } else {
                javadocResolved.append(e.toText());
            }

        });

        val descriptionAsHtml = Jsoup.parse(javadocResolved.toString());
        val adoc = HtmlToAsciiDoc.body(descriptionAsHtml.selectFirst("body"), level);
        return adoc;
    }

}
