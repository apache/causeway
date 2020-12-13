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
package org.apache.isis.tooling.javamodel.ast;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.TypeParameter;

import org.apache.isis.commons.collections.Can;

import lombok.NonNull;
import lombok.val;

//TODO effective public might require more context
public final class MethodDeclarations {


    /**
     * Returns given {@link MethodDeclaration} as normal text, without formatting.
     */
    public static String asNormalized(final @NonNull MethodDeclaration md) {
        val clone = md.clone();
        clone.getParameters()
        .forEach(p->p.getAnnotations().clear());
        return clone.getDeclarationAsString(false, false, true);
    }
    
    public static String asNormalizedName(final @NonNull MethodDeclaration md) {
        return md.getNameAsString().trim();
    }
    
    public static Can<TypeParameter> getTypeParameters(final @NonNull MethodDeclaration md) {
        return Can.ofStream(md.getTypeParameters().stream());
    }

    public static boolean isEffectivePublic(
            final @NonNull MethodDeclaration md, 
            final @NonNull ClassOrInterfaceDeclaration context) {

        if(!ClassOrInterfaceDeclarations.isEffectivePublic(context)) {
            return false;
        }
        if(context.isInterface()) {
            return true;
        }

        return !md.isPrivate() 
                && !md.isAbstract() 
                && !md.isProtected()
                ;
    }

    public static boolean isEffectivePublic(
            final @NonNull MethodDeclaration md, 
            final @NonNull EnumDeclaration context) {

        if(!EnumDeclarations.isEffectivePublic(context)) {
            return false;
        }

        return !md.isPrivate() 
                && !md.isAbstract() 
                && !md.isProtected()
                ;
    }

}
