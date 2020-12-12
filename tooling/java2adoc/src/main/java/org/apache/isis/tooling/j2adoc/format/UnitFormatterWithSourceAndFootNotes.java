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
import org.apache.isis.tooling.javamodel.ast.ConstructorDeclarations;
import org.apache.isis.tooling.javamodel.ast.EnumConstantDeclarations;
import org.apache.isis.tooling.javamodel.ast.FieldDeclarations;
import org.apache.isis.tooling.javamodel.ast.Javadocs;
import org.apache.isis.tooling.javamodel.ast.MethodDeclarations;
import org.apache.isis.tooling.model4adoc.AsciiDocFactory;

import lombok.val;

public class UnitFormatterWithSourceAndFootNotes 
extends UnitFormatterAbstract {

    public UnitFormatterWithSourceAndFootNotes(final J2AdocContext j2aContext) {
        super(j2aContext);
    }
    
    protected Optional<String> javaSource(final J2AdocUnit unit) {
        
        val java = new StringBuilder();
        
        java.append(String.format("%s %s {\n", 
                unit.getDeclarationKeyword(), 
                unit.getSimpleName()));
        
        unit.streamEnumConstantDeclarations()
        .filter(Javadocs::notExplicitlyHidden)
        .forEach(ecd->{
            
            val memberFormat = memberSourceFormat(ecd.getJavadoc().isPresent());
            
            java.append(String.format(memberFormat, 
                    EnumConstantDeclarations.toNormalizedEnumConstantDeclaration(ecd)));
            
        });
        
        unit.streamPublicFieldDeclarations()
        .filter(Javadocs::notExplicitlyHidden)
        .forEach(fd->{
            
            val memberFormat = memberSourceFormat(fd.getJavadoc().isPresent());
            
            java.append(String.format(memberFormat, 
                    FieldDeclarations.toNormalizedFieldDeclaration(fd)));
            
        });
        
        unit.streamPublicConstructorDeclarations()
        .filter(Javadocs::notExplicitlyHidden)
        .forEach(cd->{
            
            val memberFormat = memberSourceFormat(cd.getJavadoc().isPresent());
            
            java.append(String.format(memberFormat, 
                    ConstructorDeclarations.toNormalizedConstructorDeclaration(cd)));
            
        });
        
        unit.streamPublicMethodDeclarations()
        .filter(Javadocs::notExplicitlyHidden)
        .forEach(md->{
            
            val memberFormat = memberSourceFormat(md.getJavadoc().isPresent());

            java.append(String.format(memberFormat, 
                    MethodDeclarations.toNormalizedMethodDeclaration(md)));
            
        });

        java.append("}\n");
        
        
        return Optional.of(
                AsciiDocFactory.SourceFactory.java(java.toString(), "Java Sources"));
            
    }
    
//    @Override
//    public String getEnumConstantFormat() {
//        return "`%s`";
//    }
//
//    @Override
//    public String getFieldFormat() {
//        return "`%s %s`";
//    }
//    
//    @Override
//    public String getConstructorFormat() {
//        return "`%s(%s)`";
//    }
//
//    @Override
//    public String getGenericConstructorFormat() {
//        return "`%s %s(%s)`";
//    }
//
//    @Override
//    public String getMethodFormat() {
//        return "`%s %s(%s)`";
//    }
//
//    @Override
//    public String getGenericMethodFormat() {
//        return "`%s %s %s(%s)`";
//    }

    @Override
    protected StructuralNode getMemberDescriptionContainer(StructuralNode parent) {
        val ul = AsciiDocFactory.footnotes(parent);
        return ul;
    }
    
    // -- HELPER
    
    private String memberSourceFormat(boolean addFootnote) {
        return addFootnote
                ? "\n  %s // <.>\n"
                : "\n  %s\n";
    }
    

}
