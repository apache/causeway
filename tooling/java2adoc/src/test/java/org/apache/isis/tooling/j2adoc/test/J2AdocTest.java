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
import org.apache.isis.commons.internal.base._Text;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.tooling.j2adoc.J2AdocContext;
import org.apache.isis.tooling.j2adoc.util.AsciiDocIncludeTagFilter;
import org.apache.isis.tooling.javamodel.AnalyzerConfigFactory;
import org.apache.isis.tooling.model4adoc.AsciiDocWriter;

import lombok.NonNull;
import lombok.val;

import guru.nidi.codeassert.config.Language;

import static guru.nidi.codeassert.config.Language.JAVA;

class J2AdocTest {

    @Test //@Disabled
    void testJavaDoc2AsciiDoc() {
        
        val analyzerConfig = AnalyzerConfigFactory
                .maven(ProjectSampler.apacheIsisApplib(), Language.JAVA)
                .main();

        val j2aContext = J2AdocContext
                .javaSourceWithFootnotesFormat()
                //.compactFormat()
                .xrefPageIdFormat("system:generated:index/%s.adoc")
                .build();
        
        analyzerConfig.getSources(JAVA)
        .stream()
        //.filter(source->source.toString().contains("ExecutionMode"))
        //.filter(source->source.toString().contains("FactoryService"))
        .filter(source->source.toString().contains("Action"))
        .peek(source->System.out.println("parsing source: " + source))
        .forEach(j2aContext::add);
        
        j2aContext.streamUnits()
        .map(unit->unit.toAsciiDoc(j2aContext))
        .forEach(adoc->{
            
            //System.out.println(adoc);
            
            AsciiDocWriter.print(adoc);
            System.out.println();

        });
    }
    
    @Test @Disabled
    void adocDocMining() throws IOException {
        
        val adocFiles = ProjectSampler.adocFiles(ProjectSampler.apacheIsisRoot());
     
        val names = _Sets.<String>newTreeSet();
        
        Can.ofCollection(adocFiles)
        .stream()
        .filter(source->source.toString().contains("XmlSnapshotService"))
        .forEach(file->parseAdoc(file, names::add));
        
        names.forEach(System.out::println);
    }
    
    private void parseAdoc(final @NonNull File file, Consumer<String> onName) {
        val lines = _Text.readLinesFromFile(file, StandardCharsets.UTF_8);
        
        ExampleReferenceFinder.find(lines)
        .forEach(exRef->{
            onName.accept(exRef.name);
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
    
    @Test @Disabled("DANGER!")
    void adocExampleProcessing() throws IOException {
        
        val adocFiles = ProjectSampler.adocFiles(ProjectSampler.apacheIsisRoot());
     
        Can.ofCollection(adocFiles)
        .stream()
        //.filter(source->source.toString().contains("FactoryService"))
        .forEach(ExampleReferenceRewriter::processAdocExampleReferences);
    }
    
    
}
