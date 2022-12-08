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

    static void assertHasApacheCausewayRuntimeClasses(final Stream<String> classNames) {

        val components = classNames
        .map(s->s.replace("org.apache.causeway.", "~."))
        //.peek(System.out::println) //debug
        .collect(Collectors.toSet());

        assertTrue(components.contains("~.core.runtime.events.MetamodelEventService"));
        assertTrue(components.contains("~.core.runtime.events.TransactionEventEmitter"));
    }

    static void assertHasApacheCausewayRuntimeSourceFiles(final Stream<String> sourcePaths) {

        val sources = sourcePaths
        .map(s->s.replace("\\", "/"))
        .map(s->s.replace("/src/main/java/org/apache/causeway/", "~/"))
        //.peek(System.out::println) //debug
        .collect(Collectors.toSet());

        assertTrue(sources.contains("~/core/runtime/CausewayModuleCoreRuntime.java"));
        assertTrue(sources.contains("~/core/runtime/events/MetamodelEventService.java"));
        assertTrue(sources.contains("~/core/runtime/events/TransactionEventEmitter.java"));
    }

}
