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

    static void assertHasApacheIsisRuntimeClasses(final Stream<String> classNames) {

        val components = classNames
        .map(s->s.replace("org.apache.isis.", "~."))
        //.peek(System.out::println) //debug
        .collect(Collectors.toSet());

        assertTrue(components.contains("~.core.runtime.events.MetamodelEventService"));
        assertTrue(components.contains("~.core.runtime.events.TransactionEventEmitter"));
    }

    static void assertHasApacheIsisRuntimeSourceFiles(final Stream<String> sourcePaths) {

        val sources = sourcePaths
        .map(s->s.replace("\\", "/"))
        .map(s->s.replace("/src/main/java/org/apache/isis/", "~/"))
        //.peek(System.out::println) //debug
        .collect(Collectors.toSet());

        assertTrue(sources.contains("~/core/runtime/IsisModuleCoreRuntime.java"));
        assertTrue(sources.contains("~/core/runtime/events/MetamodelEventService.java"));
        assertTrue(sources.contains("~/core/runtime/events/TransactionEventEmitter.java"));
    }

}
