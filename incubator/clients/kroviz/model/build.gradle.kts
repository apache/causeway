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
    kotlin("plugin.serialization") version kotlinVersion
}

repositories {
    maven("https://repository.int.kn/nexus/content/repositories/thirdparty/")
    mavenCentral()
    mavenLocal()
}

// Versions
val serializationVersion = "1.7.3"
val junitVersion = "5.11.4"
val junitPlatformVersion = "1.11.4"
val pitestVersion = "1.15.0"
val pitestJunit5PluginVersion = "1.2.1"

// Platform independent RESTful Objects transfer objects.  Deliberately kept free of
// any KVision (or otherwise browser specific) dependency, so that the very same
// parsing code can be reused by the KVision client and by other front ends.
kotlin {
    js(IR) {
        nodejs()
    }
    jvm {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(project(":snapshots"))
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter:$junitVersion")
                runtimeOnly("org.junit.platform:junit-platform-launcher:$junitPlatformVersion")
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Mutation testing with PIT.
//
// PIT's Gradle plugin needs Gradle's 'java' plugin, which the Kotlin
// Multiplatform plugin declares to be incompatible.  PIT is therefore driven
// through its command line interface, against the classes of the 'jvm' target.
// ---------------------------------------------------------------------------
val pitestClasspath: Configuration by configurations.creating

dependencies {
    pitestClasspath("org.pitest:pitest-command-line:$pitestVersion")
    pitestClasspath("org.pitest:pitest-junit5-plugin:$pitestJunit5PluginVersion")
    // The PIT junit5 plugin brings an older JUnit platform along; align it with the
    // one used by the tests, or the PIT minion fails to discover them.
    pitestClasspath("org.junit.platform:junit-platform-launcher:$junitPlatformVersion")
}

val jvmMainCompilation = kotlin.targets.getByName("jvm").compilations.getByName("main")
val jvmTestCompilation = kotlin.targets.getByName("jvm").compilations.getByName("test")

val pitestProjectBase = layout.projectDirectory.asFile.absolutePath
val pitestReportDir = layout.buildDirectory.dir("reports/pitest").get().asFile.absolutePath
val pitestSourceDirs = listOf("src/commonMain/kotlin", "src/jvmMain/kotlin")
    .map { layout.projectDirectory.dir(it).asFile }
    .filter { it.exists() }
    .joinToString(",") { it.absolutePath }

tasks.register<JavaExec>("pitest") {
    group = "verification"
    description = "Runs PIT mutation testing against the JVM target (report in build/reports/pitest)."
    dependsOn("jvmTestClasses")
    mainClass.set("org.pitest.mutationtest.commandline.MutationCoverageReport")
    classpath = pitestClasspath
    doFirst {
        // PIT expects comma separated lists, *not* the platform path separator.
        val classPath = (jvmTestCompilation.output.allOutputs.files
                + jvmMainCompilation.output.allOutputs.files
                + jvmTestCompilation.runtimeDependencyFiles?.files.orEmpty())
            .filter { it.exists() }
            .joinToString(",") { it.absolutePath }
        val mutableCodePaths = jvmMainCompilation.output.allOutputs.files
            .filter { it.exists() }
            .joinToString(",") { it.absolutePath }

        args = listOf(
            "--projectBase", pitestProjectBase,
            "--reportDir", pitestReportDir,
            "--targetClasses", "org.apache.causeway.client.kroviz.to.*",
            "--targetTests", "*Test",
            "--sourceDirs", pitestSourceDirs,
            "--mutableCodePaths", mutableCodePaths,
            "--classPath", classPath,
            "--outputFormats", "XML,HTML",
            "--timestampedReports", "false",
            "--mutationThreshold", "0",
            "--failWhenNoMutations", "false",
            "--threads", "4",
        )
    }
}
