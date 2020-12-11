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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Files;
import org.apache.isis.commons.internal.base._Text;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.functions._Predicates;
import org.apache.isis.tooling.j2adoc.J2AdocContext;
import org.apache.isis.tooling.j2adoc.util.AsciiDocIncludeTagFilter;
import org.apache.isis.tooling.javamodel.AnalyzerConfigFactory;

import lombok.NonNull;
import lombok.val;

import guru.nidi.codeassert.config.Language;

import static guru.nidi.codeassert.config.Language.JAVA;

class J2AdocTest {

    @Test //@Disabled
    void testJavaDocMining() {
        
        val analyzerConfig = AnalyzerConfigFactory
                .maven(ProjectSampler.apacheIsisApplib(), Language.JAVA)
                .main();

        val j2aContext = J2AdocContext
                //.javaSourceWithFootNotesFormat()
                .compactFormat()
                .xrefPageIdFormat("system:generated:index/%s.adoc")
                .build();
        
        analyzerConfig.getSources(JAVA)
        .stream()
        .filter(source->source.toString().contains("XmlSnapshotService"))
        //.peek(source->System.out.println("parsing source: " + source))
        .forEach(j2aContext::add);
        
        j2aContext.streamUnits()
        .forEach(unit->{
            
            System.out.println(unit.toAsciiDoc(j2aContext));
            System.out.println();

        });
    }
    
    @Test @Disabled("DANGER!")
    void removeAdocExampleTags() throws IOException {
        
        val analyzerConfig = AnalyzerConfigFactory
                .maven(ProjectSampler.apacheIsisApplib(), Language.JAVA)
                .main();
        
        analyzerConfig.getSources(JAVA)
        .stream()
        .peek(source->System.out.println("parsing source: " + source))
        .filter(source->source.toString().contains("\\applib\\services\\"))
        .forEach(AsciiDocIncludeTagFilter::removeAdocExampleTags);
        
    }
    
    @Test @Disabled
    void adocDocMining() throws IOException {
        
        val adocFiles = 
                _Files.searchFiles(
                        ProjectSampler.apacheIsisRoot(), 
                        _Predicates.alwaysTrue(), 
                        file->file.getName().endsWith(".adoc"));
     
        val names = _Sets.<String>newTreeSet();
        
        Can.ofCollection(adocFiles)
        .stream()
        .forEach(file->parseAdoc(file, names::add));
        
        names.forEach(System.out::println);
    }
    
    private void parseAdoc(final @NonNull File file, Consumer<String> onShortName) {
        val lines = _Text.readLinesFromFile(file, StandardCharsets.UTF_8);
        
        lines.stream()
        .filter(line->line.contains("refguide:applib-svc:example$services/"))
        .forEach(line->{
            //System.out.println("--- " + file);
            
            //val shortRef = line.substring(line.indexOf("/")+1);
            val shortRef = line.substring(line.lastIndexOf("/")+1);
            val shortName = shortRef.substring(0, shortRef.lastIndexOf(".java"));
            
            onShortName.accept(shortName);//+".java");
        });
     
        
        
    }

    
}
