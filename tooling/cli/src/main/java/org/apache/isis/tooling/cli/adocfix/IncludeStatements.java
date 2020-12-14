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
package org.apache.isis.tooling.cli.adocfix;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Refs;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.val;

public final class IncludeStatements {
    
    @Value @Builder
    public static class IncludeStatement {
        int zeroBasedLineIndex;
        String matchingLine;
        String referencePath;
        String referenceShortName;
        
        String version;
        String component;
        String module;
        String type; // usually 'page'
        String ext;
        String options;
        
        public boolean isLocal() {
            return _Strings.isNullOrEmpty(component);
        }
        
        public String toAdocAsString() {
            //TODO if local might look slightly different 
            if(isLocal()) {
                throw _Exceptions.notImplemented();
            }
            
            return String.format("include::%s%s:%s:%s$%s%s", 
                    _Strings.nullToEmpty(version).isEmpty() ? "" : version + "@",
                    _Strings.nullToEmpty(component),
                    _Strings.nullToEmpty(module),
                    type,
                    referencePath,
                    _Strings.nullToEmpty(options));
        }
        
    }
    
    // -- UTILITIES
    
    public static Can<IncludeStatement> find(
            final @NonNull Iterable<String> lines) {
        
        val matches = _Lists.<IncludeStatement>newArrayList();
        visit(lines, (line, incl)->incl.ifPresent(matches::add));
        return Can.ofCollection(matches); 
    }
    
    /**
     * @param lines input from eg. a file
     * @param rewriter - receives all include statements found, 
     *          when returning null, means the current line stays unmodified
     * @return updated lines ready to be eg. written to file
     */
    public static Can<String> rewrite(
            final @NonNull Iterable<String> lines, 
            final @NonNull UnaryOperator<IncludeStatement> rewriter) {
        
        val processedLines = _Lists.<String>newArrayList();
        visit(lines, (originalLine, inclOptional)->{
            inclOptional.ifPresentOrElse(
                    incl->processedLines.add(rewriter.apply(incl).toAdocAsString()),
                    ()->processedLines.add(originalLine)
            );
        });
        return Can.ofCollection(processedLines); 
    }
    
    // -- HELPER
    
    private static void visit(
            final Iterable<String> lines, 
            final BiConsumer<String, Optional<IncludeStatement>> onLine) {
        
        int zeroBasedLineIndex = 0;
        
        for(val line : lines) {
            
            // include::[version@]component:module:page$relative-path
            
            if(line.startsWith("include::")) {
                
                val acc = _Refs.stringRef(line);
                
                acc.cutAtIndex("include::".length());
                
                val incl = IncludeStatement.builder();
                incl.matchingLine(line);
                incl.zeroBasedLineIndex(zeroBasedLineIndex);
                
                if(acc.contains("@")) {
                    incl.version(acc.cutAtIndexOfAndDrop("@"));
                } 
                
                incl.component(acc.cutAtIndexOfAndDrop(":"));
                incl.module(acc.cutAtIndexOfAndDrop(":"));
                incl.type(acc.cutAtIndexOfAndDrop("$"));
                
                final String referencePath; 
                if(acc.contains("[")) {
                    referencePath = acc.cutAtIndexOf("[");
                    incl.options(acc.getValue()); 
                } else {
                    referencePath = acc.getValue();
                }
                
                incl.referencePath(referencePath);
                
                acc.setValue(referencePath);
                
                acc.cutAtLastIndexOfAndDrop("/");
                
                incl.referenceShortName(acc.cutAtLastIndexOfAndDrop("."));
                incl.ext(acc.getValue());
                
                onLine.accept(line, Optional.of(incl.build()));
            } else {
                onLine.accept(line, Optional.empty());
            }
            
            zeroBasedLineIndex++;
        }
         
    }
    

}
