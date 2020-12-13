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

import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.printer.PrettyPrinterConfiguration;

import lombok.NonNull;

public final class AnnotationMemberDeclarations {
    
    private static PrettyPrinterConfiguration printingConf = new PrettyPrinterConfiguration();
    static {
        printingConf.setPrintJavadoc(false);
    }
    
    /**
     * Returns given {@link AnnotationMemberDeclaration} as normal text, without formatting.
     */
    public static String asNormalized(final @NonNull AnnotationMemberDeclaration amd) {
        return amd.toString(printingConf).trim();
    }
    
    public static String asNormalizedName(final @NonNull AnnotationMemberDeclaration amd) {
        return amd.getNameAsString().trim();
    }
    
}
