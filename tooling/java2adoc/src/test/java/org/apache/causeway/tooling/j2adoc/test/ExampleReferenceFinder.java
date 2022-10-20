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
package org.apache.causeway.tooling.j2adoc.test;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.causeway.commons.internal.collections._Lists;

import lombok.ToString;
import lombok.val;

class ExampleReferenceFinder {
    
    @ToString(doNotUseGetters = true)
    static class ExampleReference {
        int exampleRef = -1;
        int chapterStart = -1;
        int chapterEnd = -1;
        String matchingLine;
        String name;
        String shortName;
    }
    
    static List<ExampleReference> find(Iterable<String> lines, Predicate<String> matcher) {
        val eRefs = _Lists.<ExampleReference>newArrayList();
        
        ExampleReference acc = new ExampleReference();
        
        int i = 0;
        
        for(val line : lines) {
            if(matcher.test(line)) {
                acc.exampleRef = i;
                
                val shortRef = line.substring(line.lastIndexOf("/")+1);
                
                val name = Stream.of(
                        ".java",
                        ".adoc")
                .filter(shortRef::contains)
                .map(ext->shortRef.substring(0, shortRef.lastIndexOf(ext)))
                .findFirst()
                .orElse("???");
                
                acc.name = name;
                acc.matchingLine = line;
                
                if(name.contains(".")) {
                    acc.shortName = name.substring(name.lastIndexOf(".")+1);
                } else {
                    acc.shortName = name;
                }
                
            } else if(line.startsWith("= ")
                    || line.startsWith("== ")
                    || line.startsWith("=== ")
                    || line.startsWith("==== ")
                    || line.startsWith("===== ")
                    ) {
                
                if(acc.exampleRef==-1) {
                    acc.chapterStart = i;    
                } else if(acc.chapterEnd==-1) {
                    acc.chapterEnd = i;
                    //commit
                    eRefs.add(acc);
                    acc = new ExampleReference();
                    acc.chapterStart = i;
                }
            }
            i++;
        }
        
        if(acc.exampleRef!=-1
                && acc.chapterEnd==-1) {
            acc.chapterEnd = i-1;
            //commit
            eRefs.add(acc);
        }
        
        return eRefs; 
    }

}
