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

import org.junit.jupiter.api.Test;

import org.apache.isis.tooling.cli.doclet.DocletContext;
import org.apache.isis.tooling.javamodel.AnalyzerConfigFactory;

import lombok.val;

import guru.nidi.codeassert.config.Language;

import static guru.nidi.codeassert.config.Language.JAVA;

class DocletTest {

    @Test
    void testJavaDocMining() {

        val projDir = new File("./").getAbsoluteFile();
        val analyzerConfig = AnalyzerConfigFactory.mavenTest(projDir, Language.JAVA).main();

        val docletContext = DocletContext.builder()
                .indexXrefRoot("system:index")
                .build();
        
        analyzerConfig.getSources(JAVA)
        .stream()
//        .filter(source->source.toString().contains("UserService"))
        .peek(source->System.out.println("parsing source: " + source))
        .forEach(docletContext::add);
        
        docletContext.streamDoclets()
        .forEach(doclet->{
            
            System.out.println("--------------------------------------------------");
            System.out.println(doclet.toAsciiDoc(docletContext));
            System.out.println("--------------------------------------------------");

        });
    }
    
}
