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

import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

import org.apache.isis.tooling.javamodel.Javadocs;

import lombok.NonNull;
import lombok.val;

final class Adoclets {

    /**
     * Whether to include given {@link TypeDeclaration} with the index.
     * <p>
     * This is decided base on whether the type's java-doc has a
     * {@literal @since} tag that contains the literal {@literal {@index}}. 
     */
    static boolean hasIndexDirective(final @NonNull TypeDeclaration<?> td) {
        return td.getJavadoc()
        .map(javadoc->{
        
            val toBeIncluded = Javadocs.streamTagContent(javadoc, "since") 
            .anyMatch(since->since.toText().contains("{@index}"));
            
            return toBeIncluded;
            
        }) 
        .orElse(false);
    }
    
    /**
     * Returns given {@link ConstructorDeclaration} as normal text, without formatting.
     */
    static String toNormalizedConstructorDeclaration(final @NonNull ConstructorDeclaration cd) {
        return cd.getDeclarationAsString(false, false, true).trim();
    }
    
    /**
     * Returns given {@link MethodDeclaration} as normal text, without formatting.
     */
    static String toNormalizedMethodDeclaration(final @NonNull MethodDeclaration md) {
        return md.getDeclarationAsString(false, false, true).trim();
    }
    
    
    
    
}
