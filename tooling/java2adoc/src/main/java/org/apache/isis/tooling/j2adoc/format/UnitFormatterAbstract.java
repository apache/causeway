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

import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.List;
import org.asciidoctor.ast.StructuralNode;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.tooling.j2adoc.J2AdocContext;
import org.apache.isis.tooling.j2adoc.J2AdocUnit;
import org.apache.isis.tooling.j2adoc.convert.J2AdocConverter;
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
                String.format("%s : _%s_", 
                        unit.getName(),
                        unit.getDeclarationKeywordFriendlyName().toLowerCase()));
    }
    
    protected void intro(final J2AdocUnit unit, final StructuralNode parent) {
        unit.getJavadoc()
        .filter(javadoc->!Javadocs.hasHidden(javadoc))
        .map(javadoc->getConverter().javadoc(javadoc))
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
    
    protected void memberDescriptions(final J2AdocUnit unit, final StructuralNode parent) {
        
        val ul = getMemberDescriptionContainer(parent);
        
        
        unit.getTypeDeclaration().getEnumConstantDeclarations().stream()
        .filter(Javadocs::presentAndNotHidden)
        .forEach(ecd->{
            ecd.getJavadoc()
            .ifPresent(javadoc->{
                
                appendMemberDescription(ul, 
                                getConverter().enumConstantDeclaration(ecd),
                                getConverter().javadoc(javadoc));
            });
        });
        
        unit.getTypeDeclaration().getPublicFieldDeclarations().stream()
        .filter(Javadocs::presentAndNotHidden)
        .forEach(fd->{
            
            fd.getJavadoc()
            .ifPresent(javadoc->{
                
                appendMemberDescription(ul,
                        getConverter().fieldDeclaration(fd),
                        getConverter().javadoc(javadoc));
            });
            
        });
        
        unit.getTypeDeclaration().getAnnotationMemberDeclarations().stream()
        .filter(Javadocs::presentAndNotHidden)
        .forEach(ecd->{
            ecd.getJavadoc()
            .ifPresent(javadoc->{
                
                appendMemberDescription(ul, 
                                getConverter().annotationMemberDeclaration(ecd),
                                getConverter().javadoc(javadoc));
            });
        });
        
        unit.getTypeDeclaration().getPublicConstructorDeclarations().stream()
        .filter(Javadocs::presentAndNotHidden)
        .forEach(cd->{
            
            cd.getJavadoc()
            .ifPresent(javadoc->{
                
                appendMemberDescription(ul,
                        getConverter().constructorDeclaration(cd),
                        getConverter().javadoc(javadoc));
            });
            
        });
        
        unit.getTypeDeclaration().getPublicMethodDeclarations().stream()
        .filter(Javadocs::presentAndNotHidden)
        .forEach(md->{
            
            md.getJavadoc()
            .ifPresent(javadoc->{
                
                appendMemberDescription(ul,
                        getConverter().methodDeclaration(md),
                        getConverter().javadoc(javadoc));
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
        
        title(unit)
        .ifPresent(doc::setTitle);
        
        // -- license
        
        _Strings.nonEmpty(getContext().getLicenseHeader())
        .ifPresent(notice->AsciiDocFactory.attrNotice(doc, notice));

        // -- intro
        
        intro(unit, doc);
        
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
    
    protected final J2AdocConverter getConverter() {
        return j2aContext.getConverter();
    }


}
