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
package org.apache.causeway.tooling.javamodel.test;

import java.io.File;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.val;

class ProjectSamples {

    static File apacheCausewayRoot() {
        final File projRootFolder = new File("./").getAbsoluteFile().getParentFile().getParentFile().getParentFile();
        return projRootFolder;
    }
    
    static File apacheCausewayApplib() {
        return new File(apacheCausewayRoot(), "api/applib");
    }
    
    static File apacheCausewayRuntime() {
        return new File(apacheCausewayRoot(), "core/runtime");
    }
    
    static File self() {
        return new File("./").getAbsoluteFile();
    }
    
    static void assertHasApacheCausewayRuntimeClasses(Stream<String> classNames) {
        
        val components = classNames
        .map(s->s.replace("org.apache.causeway.", "o.a.i."))
        //.peek(System.out::println) //debug
        .collect(Collectors.toSet());
        
        assertTrue(components.contains("o.a.i.core.runtime.persistence.transaction.AuditerDispatchService"));
        assertTrue(components.contains("o.a.i.core.runtime.persistence.transaction.ChangedObjectsService"));
        assertTrue(components.contains("o.a.i.core.runtime.events.persistence.TimestampService"));
        assertTrue(components.contains("o.a.i.core.runtime.events.RuntimeEventService"));
    }
    
    static void assertHasApacheCausewayRuntimeSourceFiles(Stream<String> sourcePaths) {
        
        val sources = sourcePaths
        .map(s->s.replace("\\", "/"))
        .map(s->s.replace("/src/main/java/org/apache/causeway/", "o.a.i/"))
        //.peek(System.out::println) //debug
        .collect(Collectors.toSet());
        
        assertTrue(sources.contains("o.a.i/core/runtime/context/MetaModelContext.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/context/CausewayContext.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/context/memento/ObjectMemento.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/context/memento/ObjectMementoCollection.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/context/memento/ObjectMementoForEmpty.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/context/memento/ObjectMementoService.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/context/RuntimeContext.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/context/RuntimeContextBase.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/events/app/AppLifecycleEvent.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/events/iactn/CausewayInteractionLifecycleEvent.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/events/persistence/PostStoreEvent.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/events/persistence/PreStoreEvent.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/events/persistence/TimestampService.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/events/RuntimeEventService.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/iactn/CausewayInteraction.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/iactn/InteractionService.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/iactn/InteractionTracker.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/iactn/scope/CausewayInteractionScope.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/iactn/scope/CausewayInteractionScopeBeanFactoryPostProcessor.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/iactn/scope/CausewayInteractionScopeCloseListener.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/CausewayModuleCoreRuntime.java"));
        
    }
    
}
