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
package org.apache.isis.tooling.j2adoc.test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.isis.commons.internal.base._Text;
import org.apache.isis.commons.internal.collections._Lists;

import lombok.val;

class ExampleReferenceRewriter {

    static void processAdocExampleReferences(File source) {
        
        val lines = _Text.readLinesFromFile(source, StandardCharsets.UTF_8);
        
        val exampleRefs = ExampleReferenceFinder.find(
                lines, 
                line->line.contains("refguide:applib-svc:example$services/"));
                
        if(exampleRefs.isEmpty()) {
            return;
        }
        
        System.out.println(exampleRefs);
        
        val fixedLines = _Lists.<String>newArrayList();
        
        val it = lines.iterator();
        String line;
        int i = 0;
        
        for(val exRef : exampleRefs) {
            
            // seek chapter start
            while(i<exRef.chapterStart) {
                line = it.next();
                fixedLines.add(line);
                ++i;
            }
            
            appendHeader(exRef.name, fixedLines);
            
            // seek chapter end
            while(i<exRef.chapterEnd) {
                line = it.next();
                fixedLines.add(line);
                ++i;
            }
            
            appendFooter(fixedLines);
            
        }
        
        // seek document end
        while(it.hasNext()) {
            fixedLines.add(it.next());
        }
        
        _Text.writeLinesToFile(fixedLines, source, StandardCharsets.UTF_8);

    }
    
    // -- HELPER

    private static void appendHeader(String key, List<String> lines) {
        lines.add("== API");
        lines.add("");
        lines.add(String.format("include::system:generated:page$index/%s.adoc[leveloffset=+2]", key));
        lines.add("");
        lines.add("TODO example migration");
        lines.add("");
        lines.add(".Deprecated Docs");
        lines.add("[WARNING]");
        lines.add("================================");
        lines.add("");
    }
    
    private static void appendFooter(List<String> lines) {
        lines.add("");
        lines.add("================================");
        lines.add("");
    }

}
