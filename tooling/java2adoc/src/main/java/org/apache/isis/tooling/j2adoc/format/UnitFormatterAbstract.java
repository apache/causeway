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

import org.apache.isis.tooling.j2adoc.J2AdocContext;
import org.apache.isis.tooling.j2adoc.J2AdocUnit;
import org.apache.isis.tooling.j2adoc.convert.J2AdocConverter;
import org.apache.isis.tooling.model4adoc.AsciiDocFactory;

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
                String.format("%s : _%s_\n\n", 
                        unit.getName(),
                        unit.getDeclarationKeyword()));
    }
    
    protected Optional<String> intro(final J2AdocUnit unit) {
        return unit.getJavadoc()
                .map(javadoc->getConverter().javadoc(javadoc, 0));    
        
    }
    
    protected Optional<String> javaSource(final J2AdocUnit unit) {
        return Optional.empty();
    }
    
    protected Optional<String> memberDescriptions(final J2AdocUnit unit) {
        
        val sb = new StringBuilder();
        
        unit.getPublicConstructorDeclarations().forEach(cd->{
            
            cd.getJavadoc()
            .ifPresent(javadoc->{
                sb.append(String.format(getContext().getFormatter().getMemberDescriptionFormat(),
                        getConverter().constructorDeclaration(cd),
                        getConverter().javadoc(javadoc, 1)));
            });
            
        });
        
        unit.getPublicMethodDeclarations().forEach(md->{
            
            md.getJavadoc()
            .ifPresent(javadoc->{
                sb.append(String.format(getContext().getFormatter().getMemberDescriptionFormat(),
                        getConverter().methodDeclaration(md),
                        getConverter().javadoc(javadoc, 1)));
            });
            
        });
        
        return Optional.of(sb.toString());
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

        // -- intro
        
        intro(unit)
        .ifPresent(source->AsciiDocFactory.block(doc).setSource(source));
        
        // -- java source
        
        javaSource(unit)
        .ifPresent(source->AsciiDocFactory.block(doc).setSource(source));
            
        // -- member descriptions
        
        memberDescriptions(unit)
        .ifPresent(source->AsciiDocFactory.block(doc).setSource(source));
        
        // -- outro
        
        outro(unit)
        .ifPresent(source->AsciiDocFactory.block(doc).setSource(source));
        
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
