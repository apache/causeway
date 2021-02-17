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

import java.util.stream.Collectors;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.printer.configuration.DefaultConfigurationOption;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration.ConfigOption;
import com.github.javaparser.printer.configuration.PrinterConfiguration;

import org.apache.isis.commons.collections.Can;

import lombok.NonNull;
import lombok.val;

//TODO effective public might require more context
public final class FieldDeclarations {
    
    private static final PrinterConfiguration printingConf = new DefaultPrinterConfiguration()
            .removeOption(new DefaultConfigurationOption(ConfigOption.PRINT_JAVADOC));
    
    /**
     * Returns given {@link FieldDeclaration} as normal text, without formatting.
     */
    public static String asNormalized(final @NonNull FieldDeclaration fd) {
        
        //suppress initializer printing (that is assignments)
        val clone = fd.clone();
        clone.getVariables().stream()
                .forEach(vd->vd.setInitializer((Expression)null));
        
        return clone.toString(printingConf).trim();
    }
    
    public static String asNormalizedName(final @NonNull FieldDeclaration fd) {
        return fd.getVariables().stream()
                .map(VariableDeclarator::getNameAsString)
                .collect(Collectors.joining(", "))
                .trim();
    }
    
    public static Can<TypeParameter> getTypeParameters(final @NonNull FieldDeclaration fd) {
        return Can.ofCollection(fd.findAll(TypeParameter.class));
    }
    
    // -- CONTEXT
    
    public static boolean isEffectivePublic(
            final @NonNull FieldDeclaration fd, 
            final @NonNull ClassOrInterfaceDeclaration context) {
        
        if(!ClassOrInterfaceDeclarations.isEffectivePublic(context)) {
            return false;
        }
        if(context.isInterface()) {
            return true;
        }
        return !fd.isPrivate() 
                && !fd.isProtected()
                ;
    }
    
    public static boolean isEffectivePublic(
            final @NonNull FieldDeclaration fd, 
            final @NonNull EnumDeclaration context) {
        
        if(!EnumDeclarations.isEffectivePublic(context)) {
            return false;
        }
       
        return !fd.isPrivate() 
                && !fd.isProtected()
                ;
    }
    
}
