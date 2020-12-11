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
import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.apache.isis.commons.internal.base._Files;
import org.apache.isis.tooling.javamodel.AnalyzerConfigFactory;
import org.apache.isis.tooling.javamodel.ast.ClassOrInterfaceDeclarations;
import org.apache.isis.tooling.javamodel.ast.CompilationUnits;

import lombok.val;

import guru.nidi.codeassert.config.Language;
import guru.nidi.codeassert.model.CodeClass;
import guru.nidi.codeassert.model.Model;

import static guru.nidi.codeassert.config.Language.JAVA;

class AnalyzerTest {

    @Test
    void testSourceFileListing() {

        val projDir = ProjectSamples.apacheIsisRuntime();
        val analyzerConfig = AnalyzerConfigFactory.maven(projDir, Language.JAVA).main();
        val commonPath = projDir.getAbsolutePath();

        final Stream<String> sources = analyzerConfig.getSources(JAVA)
                .stream()
                .map(File::getAbsolutePath)
                .map(sourceFile->_Files.toRelativePath(commonPath, sourceFile));

        ProjectSamples.assertHasApacheIsisRuntimeSourceFiles(sources);
    }

    @Test @Disabled("work in progress, as of yet a proof of concept")
    void testJavaDocMining() {

        val projDir = ProjectSamples.self();
        val analyzerConfig = AnalyzerConfigFactory.mavenTest(projDir, Language.JAVA).main();

        analyzerConfig.getSources(JAVA)
        .stream()
        .filter(source->source.toString().contains("UserService"))
        .peek(source->System.out.println("parsing source: " + source))
        .map(CompilationUnits::parse)
        .flatMap(CompilationUnits::streamPublicTypeDeclarations)
        .peek(td->{
            
            td.getJavadocComment().ifPresent(javadocComment->{
                val javadoc = javadocComment.parse();
            
                javadoc.getBlockTags().stream()
                .filter(tag->tag.getTagName().equals("since"))
                .forEach(tag->System.out.println("--- SINCE " + tag.getContent().toText()));
                
            });
            
        })
        .flatMap(ClassOrInterfaceDeclarations::streamMethodDeclarations)
        .forEach(md->{
            
            System.out.println("javadoc: " + md.getJavadocComment());
            System.out.println("non private method: " + md.getDeclarationAsString());

        });
    }

    @Test @Disabled("fails when run with the CI pipeline")
    void testAnnotationGathering() {

        val projDir = ProjectSamples.apacheIsisRuntime();
        val analyzerConfig = AnalyzerConfigFactory.maven(projDir, Language.JAVA).main();

        val model = Model.from(analyzerConfig.getClasses()).read();

        final Stream<String> components = model.getClasses()
                .stream()
                .filter(codeClass->codeClass
                        .getAnnotations()
                        .stream()
                        .map(CodeClass::getName)
                        .anyMatch(name->name.startsWith("org.springframework.stereotype.")))
                .map(CodeClass::getName);

        ProjectSamples.assertHasApacheIsisRuntimeClasses(components);
    }



}
