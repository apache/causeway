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
package org.apache.isis.tooling.j2adoc;

import java.io.File;
import java.util.stream.Stream;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import org.asciidoctor.ast.Document;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.tooling.j2adoc.util.AsciiDocIncludeTagFilter;
import org.apache.isis.tooling.javamodel.ast.ClassOrInterfaceDeclarations;
import org.apache.isis.tooling.javamodel.ast.CompilationUnits;
import org.apache.isis.tooling.javamodel.ast.Javadocs;
import org.apache.isis.tooling.model4adoc.AsciiDocFactory;

import lombok.NonNull;
import lombok.Value;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Value
@Log4j2
public class J2AUnit {

    private final ClassOrInterfaceDeclaration td;

    public static Stream<J2AUnit> parse(final @NonNull File sourceFile) {

        if("package-info.java".equals(sourceFile.getName())) {
            // ignore package files
            return Stream.empty();
        }
        
        try {
            
            // remove 'tag::' and 'end::' lines
            // remove '// <.>' foot note references
            val source = AsciiDocIncludeTagFilter.read(sourceFile);

            val cu = StaticJavaParser.parse(source);
            
            return Stream.of(cu)
            .flatMap(CompilationUnits::streamPublicTypeDeclarations)
            .filter(J2AUnits::hasIndexDirective)
            .map(J2AUnit::new);

        } catch (Exception e) {
            log.error("failed to parse java source file {}", sourceFile, e);
            return Stream.empty();
        }

    }
    
    public String getName() {
        return ClassOrInterfaceDeclarations.name(td);
    }

    public String getAsciiDocXref(
            final @NonNull J2AContext j2aContext) {
        val toAdocConverter = JavaToAsciiDoc.of(j2aContext);
        return toAdocConverter.xref(this);
    }
    
    public Document toAsciiDoc(
            final @NonNull J2AContext j2aContext) {
        
        val doc = AsciiDocFactory.doc();
        
        val introBlock = AsciiDocFactory.block(doc);
        val javaSourceBlock = AsciiDocFactory.block(doc);
        val methodDescriptionBlock = AsciiDocFactory.block(doc);
        
        val mds = ClassOrInterfaceDeclarations.streamPublicMethodDeclarations(td)
                .filter(Javadocs::presentAndNotHidden)
                .collect(Can.toCan());
        
        val cds = ClassOrInterfaceDeclarations.streamPublicConstructorDeclarations(td)
                .filter(Javadocs::presentAndNotHidden)
                .collect(Can.toCan());
        
        
        val toAdocConverter = JavaToAsciiDoc.of(j2aContext);
        
        // -- title
        
        val title = String.format("%s : _%s_\n\n", 
                getName(),
                getDeclarationKeyword());
        
        doc.setTitle(title);

        // -- intro
        
        td.getJavadoc().ifPresent(javadoc->{
            introBlock.setSource(toAdocConverter.javadoc(javadoc, 0));    
        });
        
        // -- java content
        
        if(j2aContext.isIncludeJavaSource()) {
        
            val java = new StringBuilder();
            
            java.append(String.format("%s %s {\n", 
                    getDeclarationKeyword(), 
                    td.getName().asString()));
            
            
            cds.forEach(cd->{
                
                java.append(String.format("\n  %s // <.>\n", 
                        J2AUnits.toNormalizedConstructorDeclaration(cd)));
                
            });
            
            mds.forEach(md->{
    
                java.append(String.format("\n  %s // <.>\n", 
                        J2AUnits.toNormalizedMethodDeclaration(md)));
                
            });
    
            java.append("}\n");
            
            javaSourceBlock.setSource(
                    AsciiDocFactory.SourceFactory.java(java.toString(), td.getName().asString()));
        }
            
        // -- constructor and method descriptions
        
        val methodDescriptions = new StringBuilder();
        
        cds.forEach(cd->{
            
            cd.getJavadoc()
            .ifPresent(javadoc->{
                methodDescriptions.append(String.format(j2aContext.getMemberDescriptionFormat(),
                        toAdocConverter.constructorDeclaration(cd),
                        toAdocConverter.javadoc(javadoc, 1)));
            });
            
        });
        
        mds.forEach(md->{
            
            md.getJavadoc()
            .ifPresent(javadoc->{
                methodDescriptions.append(String.format(j2aContext.getMemberDescriptionFormat(),
                        toAdocConverter.methodDeclaration(md),
                        toAdocConverter.javadoc(javadoc, 1)));
            });
            
        });
        
        methodDescriptionBlock.setSource(methodDescriptions.toString());
        
        return doc;
        
//        try {
//
//            return AsciiDocWriter.toString(doc);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "ERROR: " + e.getMessage();
//        }
        
    }

    // -- HELPER

    private String getDeclarationKeyword() {
        return td.isInterface()
                ? "interface"
                : "class";
    }



}
