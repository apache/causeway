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
import org.asciidoctor.ast.StructuralNode;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.tooling.j2adoc.J2AdocContext;
import org.apache.isis.tooling.j2adoc.J2AdocUnit;
import org.apache.isis.tooling.j2adoc.convert.J2AdocConverter;
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
    
    protected Optional<String> title(final J2AdocUnit unit) {
        return Optional.of(
                String.format("%s : _%s_", 
                        unit.getName(),
                        unit.getDeclarationKeyword()));
    }
    
    protected Optional<String> intro(final J2AdocUnit unit) {
        return unit.getJavadoc()
                .map(javadoc->getConverter().javadoc(javadoc));    
        
    }
    
    protected Optional<String> javaSource(final J2AdocUnit unit) {
        return Optional.empty();
    }
    
    protected abstract StructuralNode getMemberDescriptionContainer(StructuralNode parent);
    protected abstract void appendMemberDescription(StructuralNode parent, String member, String javadoc);
    
    protected void memberDescriptions(final J2AdocUnit unit, final StructuralNode parent) {
        
        val ul = getMemberDescriptionContainer(parent);
        
        unit.getEnumConstantDeclarations().forEach(ecd->{
            ecd.getJavadoc()
            .ifPresent(javadoc->{
                
                appendMemberDescription(ul, 
                                getConverter().enumConstantDeclaration(ecd),
                                getConverter().javadoc(javadoc));
            });
        });
        
        unit.getPublicFieldDeclarations().forEach(fd->{
            
            fd.getJavadoc()
            .ifPresent(javadoc->{
                
                appendMemberDescription(ul,
                        getConverter().fieldDeclaration(fd),
                        getConverter().javadoc(javadoc));
            });
            
        });
        
        unit.getPublicConstructorDeclarations().forEach(cd->{
            
            cd.getJavadoc()
            .ifPresent(javadoc->{
                
                appendMemberDescription(ul,
                        getConverter().constructorDeclaration(cd),
                        getConverter().javadoc(javadoc));
            });
            
        });
        
        unit.getPublicMethodDeclarations().forEach(md->{
            
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
        
        intro(unit)
        .ifPresent(block(doc)::setSource);
        
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
