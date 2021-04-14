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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.val;

class ProjectSamples {

    static File apacheIsisRoot() {
        final File projRootFolder = new File("./").getAbsoluteFile().getParentFile().getParentFile().getParentFile();
        return projRootFolder;
    }
    
    static File apacheIsisApplib() {
        return new File(apacheIsisRoot(), "api/applib");
    }
    
    static File apacheIsisRuntime() {
        return new File(apacheIsisRoot(), "core/runtime");
    }
    
    static File self() {
        return new File("./").getAbsoluteFile();
    }
    
    static void assertHasApacheIsisRuntimeClasses(Stream<String> classNames) {
        
        val components = classNames
        .map(s->s.replace("org.apache.isis.", "o.a.i."))
        //.peek(System.out::println) //debug
        .collect(Collectors.toSet());
        
        assertTrue(components.contains("o.a.i.core.runtime.persistence.transaction.AuditerDispatchService"));
        assertTrue(components.contains("o.a.i.core.runtime.persistence.transaction.ChangedObjectsService"));
        assertTrue(components.contains("o.a.i.core.runtime.events.persistence.TimestampService"));
        assertTrue(components.contains("o.a.i.core.runtime.events.RuntimeEventService"));
    }
    
    static void assertHasApacheIsisRuntimeSourceFiles(Stream<String> sourcePaths) {
        
        val sources = sourcePaths
        .map(s->s.replace("\\", "/"))
        .map(s->s.replace("/src/main/java/org/apache/isis/", "o.a.i/"))
        //.peek(System.out::println) //debug
        .collect(Collectors.toSet());
        
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
        assertTrue(sources.contains("o.a.i/core/runtime/iactn/InteractionFactory.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/iactn/InteractionTracker.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/iactn/scope/IsisInteractionScope.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/iactn/scope/IsisInteractionScopeBeanFactoryPostProcessor.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/iactn/scope/IsisInteractionScopeCloseListener.java"));
        assertTrue(sources.contains("o.a.i/core/runtime/IsisModuleCoreRuntime.java"));
        
    }
    
}
