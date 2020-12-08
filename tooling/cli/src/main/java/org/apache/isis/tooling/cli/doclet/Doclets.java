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

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocInlineTag;
import com.github.javaparser.javadoc.description.JavadocSnippet;

import org.apache.isis.tooling.javamodel.Javadocs;

import lombok.NonNull;
import lombok.val;

final class Doclets {

    static boolean isApacheIsisDoclet(final @NonNull TypeDeclaration<?> td) {
        return td.getJavadoc()
        .map(javadoc->{
        
            val toBeIncluded = Javadocs.streamTagContent(javadoc, "since") 
            .anyMatch(since->since.toText().contains("{@index}"));
            
            return toBeIncluded;
            
        }) 
        .orElse(false);
    }
    
    static String toNormalizedMethodDeclaration(final @NonNull MethodDeclaration md) {
        return md.getDeclarationAsString(false, false, true).trim();
    }
    
    static String toAsciiDoc(final @NonNull Javadoc javadoc) {
        
        val adoc = new StringBuilder();
        
        javadoc.getDescription().getElements()
        .forEach(e->{
            
            if(e instanceof JavadocSnippet) {
                adoc.append(normalizeHtmlTags(e.toText()));
            } else if(e instanceof JavadocInlineTag) {
                adoc.append(" _").append(((JavadocInlineTag) e).getContent().trim()).append("_ ");
            } else {
                adoc.append(e.toText());
            }
            
        });
        
        return adoc.toString();
    }
    
    // -- HELPER 
    
    private static String normalizeHtmlTags(final @NonNull String s) {
        return s.replace("<p>", "\n").replace("</p>", "");
    }
    
    
}
