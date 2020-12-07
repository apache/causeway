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
package org.apache.isis.tooling.javamodel.test;

import java.io.File;
import java.util.function.Consumer;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

public class MethodProcessor {

    @SneakyThrows
    public static void visitMethodsOf(
            final @NonNull File source,
            final @NonNull Consumer<MethodDeclaration> callback) {

        val cu = StaticJavaParser.parse(source);
        
        cu.getPrimaryType()
        .ifPresent(primaryType->{
         
            if(!primaryType.isPublic()) {
                return;
            }
            
            val methodVisitor = new MethodVisitor();
            methodVisitor.visit(cu, callback);    
            
        });
        
    }
    
    // -- HELPER
    
    private static class MethodVisitor extends VoidVisitorAdapter<Consumer<MethodDeclaration>> {

        @Override
        public void visit(MethodDeclaration md, Consumer<MethodDeclaration> callback) {
            super.visit(md, null);
            if(md.isPrivate()) {
                return;
            }
            callback.accept(md);
        }
    }

}
