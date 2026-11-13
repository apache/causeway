//
//  Licensed to the Apache Software Foundation (ASF) under one
//  or more contributor license agreements.  See the NOTICE file
//  distributed with this work for additional information
//  regarding copyright ownership.  The ASF licenses this file
//  to you under the Apache License, Version 2.0 (the
//  "License"); you may not use this file except in compliance
//  with the License.  You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing,
//  software distributed under the License is distributed on an
//  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
//  KIND, either express or implied.  See the License for the
//  specific language governing permissions and limitations
//  under the License.
//
plugins {
    val kotlinVersion: String by System.getProperties()
    kotlin("multiplatform") version kotlinVersion
}

repositories {
    maven("https://repository.int.kn/nexus/content/repositories/thirdparty/")
    mavenCentral()
    mavenLocal()
}

// Recorded RESTful Objects responses (as Kotlin constants) used as test fixtures by
// ':model' and by the KVision user interface.  Being pure Kotlin strings they compile
// for both targets, which allows the JVM based tests of ':model' to reuse them.
kotlin {
    js(IR) {
        nodejs()
    }
    jvm()
}
