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
package org.apache.isis.tooling.cli.test.doclet;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Files;
import org.apache.isis.commons.internal.base._Text;
import org.apache.isis.commons.internal.functions._Predicates;
import org.apache.isis.tooling.cli.doclet.AdocletContext;
import org.apache.isis.tooling.javamodel.AnalyzerConfigFactory;

import lombok.NonNull;
import lombok.val;

import guru.nidi.codeassert.config.Language;

import static guru.nidi.codeassert.config.Language.JAVA;

class AdocletTest {

    @Test //@Disabled
    void testJavaDocMining() {
        
        val analyzerConfig = AnalyzerConfigFactory
                .maven(ProjectSampler.apacheIsisApplib(), Language.JAVA)
                .main();

        val docletContext = AdocletContext
                //.javaSourceWithFootNotesFormat()
                .compactFormat()
                .xrefPageIdFormat("system:generated:index/%s.adoc")
                .build();
        
        analyzerConfig.getSources(JAVA)
        .stream()
        .filter(source->source.toString().contains("XmlSnapshotService"))
        //.peek(source->System.out.println("parsing source: " + source))
        .forEach(docletContext::add);
        
        docletContext.streamAdoclets()
        .forEach(doclet->{
            
            System.out.println(doclet.toAsciiDoc(docletContext));
            System.out.println();

        });
    }
    
    @Test @Disabled
    void testAdocDocMining() throws IOException {
        
        val adocFiles = 
                _Files.searchFiles(
                        ProjectSampler.apacheIsisApplib(), 
                        _Predicates.alwaysTrue(), 
                        file->file.getName().endsWith(".adoc"));
     
        Can.ofCollection(adocFiles)
        .stream()
        .forEach(this::parseAdoc);
        
    }
    
    private void parseAdoc(final @NonNull File file) {
        val lines = _Text.readLinesFromFile(file, StandardCharsets.UTF_8);
        
        lines.stream()
        .filter(line->line.contains("refguide:applib-svc:example$services/"))
        .forEach(line->{
            //System.out.println("--- " + file);
            
            System.out.println(line.substring(line.lastIndexOf("/")+1));
        });
        
    }
    
}
