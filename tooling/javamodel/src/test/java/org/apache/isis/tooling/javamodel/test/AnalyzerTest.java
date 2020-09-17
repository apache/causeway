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
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.commons.internal.base._Files;
import org.apache.isis.tooling.javamodel.AnalyzerConfigFactory;

import lombok.val;

import guru.nidi.codeassert.config.Language;
import guru.nidi.codeassert.model.CodeClass;
import guru.nidi.codeassert.model.Model;

import static guru.nidi.codeassert.config.Language.JAVA;

class AnalyzerTest {

    File projDir;
    
    @BeforeEach
    void setUp() throws Exception {
        File projRootFolder = new File("./").getAbsoluteFile().getParentFile().getParentFile().getParentFile();
        projDir = new File(projRootFolder, "core/runtime");
        System.out.println("running AnalyzerTest at " + projDir.getAbsolutePath());
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test
    void testSourceFileListing() {
        
        val analyzerConfig = AnalyzerConfigFactory.maven(projDir, Language.JAVA).main();
        
        val commonPath = projDir.getAbsolutePath();
        
        Set<String> sources = analyzerConfig.getSources(JAVA)
                .stream()
                .map(File::getAbsolutePath)
                .map(sourceFile->_Files.toRelativePath(commonPath, sourceFile))
                .map(s->s.replace("\\", "/"))
                .map(s->s.replace("/src/main/java/org/apache/isis/", "o.a.i/"))
                //.peek(System.out::println) //debug
                .collect(Collectors.toSet());
        
        assertHasSomeSourceFiles(sources);
    }
    
    @Test @Disabled("fails when run with the CI pipeline")
    void testAnnotationGathering() {

        val analyzerConfig = AnalyzerConfigFactory.maven(projDir, Language.JAVA).main();
        
        val model = Model.from(analyzerConfig.getClasses()).read();
        
        Set<String> components = model.getClasses()
            .stream()
            .filter(codeClass->codeClass
                    .getAnnotations()
                    .stream()
                    .map(CodeClass::getName)
                    .anyMatch(name->name.startsWith("org.springframework.stereotype.")))
            .map(CodeClass::getName)
            .map(s->s.replace("org.apache.isis.", "o.a.i."))
            //.peek(System.out::println) //debug
            .collect(Collectors.toSet());
        
        assertHasSomeComponents(components);
    }
    
    // -- HELPER
    
    private void assertHasSomeComponents(Set<String> components) {
        assertTrue(components.contains("o.a.i.core.runtime.persistence.transaction.AuditerDispatchService"));
        assertTrue(components.contains("o.a.i.core.runtime.persistence.transaction.ChangedObjectsService"));
        assertTrue(components.contains("o.a.i.core.runtime.events.persistence.TimestampService"));
        assertTrue(components.contains("o.a.i.core.runtime.events.RuntimeEventService"));
    }
    
    private void assertHasSomeSourceFiles(Set<String> sources) {
        assertTrue(sources.contains("o.a.i/core/runtime/context/IsisAppCommonContext.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/context/IsisContext.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/context/memento/ObjectMemento.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/context/memento/ObjectMementoCollection.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/context/memento/ObjectMementoForEmpty.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/context/memento/ObjectMementoService.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/context/RuntimeContext.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/context/RuntimeContextBase.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/events/app/AppLifecycleEvent.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/events/iactn/IsisInteractionLifecycleEvent.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/events/persistence/PostStoreEvent.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/events/persistence/PreStoreEvent.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/events/persistence/TimestampService.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/events/RuntimeEventService.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/iactn/IsisInteraction.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/iactn/IsisInteractionFactory.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/iactn/IsisInteractionTracker.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/iactn/scope/IsisInteractionScope.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/iactn/scope/IsisInteractionScopeBeanFactoryPostProcessor.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/iactn/scope/IsisInteractionScopeCloseListener.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/iactn/template/AbstractIsisInteractionTemplate.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/IsisModuleCoreRuntime.java"));
        
    }
    
}
