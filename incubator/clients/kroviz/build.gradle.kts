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
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    val kotlinVersion: String by System.getProperties()
    kotlin("multiplatform") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    val kvisionVersion: String by System.getProperties()
    id("io.kvision") version kvisionVersion
}

var version = "2.0.0-SNAPSHOT"

repositories {
    maven ("https://repository.int.kn/nexus/content/repositories/thirdparty/")
    mavenCentral()
    mavenLocal()
}

// Versions
val kotlinVersion: String by System.getProperties()
val kvisionVersion: String by System.getProperties()

// Custom Properties
val webDir = file("src/main/web")

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                outputFileName = "main.bundle.js"
                sourceMaps = false
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        binaries.executable()
    }
    // The KVision Gradle plugin requires the standard Kotlin Multiplatform source set
    // names ('jsMain'/'jsTest').  This project keeps its Maven-style directory layout,
    // so the source and resource directories are wired up explicitly here.
    sourceSets["jsMain"].kotlin.srcDir("src/main/kotlin")
    sourceSets["jsMain"].resources.srcDir("src/main/resources")
    sourceSets["jsMain"].resources.srcDir(webDir)
    sourceSets["jsMain"].dependencies {
        // platform independent RESTful Objects transfer objects (JS and JVM)
        implementation(project(":model"))
        implementation(npm("react-awesome-button", "*"))
        implementation(npm("prop-types", "*"))
        implementation("io.kvision:kvision:$kvisionVersion")
        implementation("io.kvision:kvision-bootstrap:$kvisionVersion")
//        implementation("io.kvision:kvision-bootstrap-css:$kvisionVersion")
//        implementation("io.kvision:kvision-bootstrap-dialog:$kvisionVersion")
        implementation("io.kvision:kvision-datetime:$kvisionVersion")
        implementation("io.kvision:kvision-tom-select:$kvisionVersion")
//        implementation("io.kvision:kvision-bootstrap-spinner:$kvisionVersion")
//        implementation("io.kvision:kvision-bootstrap-typeahead:$kvisionVersion")
        implementation("io.kvision:kvision-bootstrap-upload:$kvisionVersion")
        implementation("io.kvision:kvision-fontawesome:$kvisionVersion")
        implementation("io.kvision:kvision-i18n:$kvisionVersion")
        implementation("io.kvision:kvision-richtext:$kvisionVersion")
        implementation("io.kvision:kvision-handlebars:$kvisionVersion")
//        implementation("io.kvision:kvision-datacontainer:$kvisionVersion")
        implementation("io.kvision:kvision-chart:$kvisionVersion")
        implementation("io.kvision:kvision-tabulator:$kvisionVersion")
        implementation("io.kvision:kvision-pace:$kvisionVersion")
        implementation("io.kvision:kvision-toastify:$kvisionVersion")
        implementation("io.kvision:kvision-react:$kvisionVersion")
        implementation("io.kvision:kvision-routing-navigo:$kvisionVersion")
        implementation("io.kvision:kvision-state:$kvisionVersion")
        implementation("io.kvision:kvision-rest:$kvisionVersion")
//        implementation("io.kvision:kvision-moment:$kvisionVersion")
        implementation("io.kvision:kvision-maps:$kvisionVersion")
        implementation(npm("xmltojson", "1.3.5"))
        implementation(npm("flatted", "3.2.2"))
        implementation(npm("diff", "5.0.0"))
        implementation(npm("diff2html", "3.4.13"))
        implementation(npm("xml-beautify", "1.1.2"))
        implementation(npm("xterm", "4.15.0"))
        implementation(npm("pdfjs", "2.4.7"))
        implementation(npm("vega", "5.22.1"))
//        implementation(npm("vega-lite", "5.6.0"))
        implementation(npm("element-resize-event", "3.0.6"))
        implementation(npm("asciidoctor", "2.2.6"))
    }
    sourceSets["jsTest"].kotlin.srcDir("src/test/kotlin")
    sourceSets["jsTest"].resources.srcDir("src/test/resources")
    sourceSets["jsTest"].dependencies {
        implementation(kotlin("test-js"))
        implementation(project(":snapshots"))
        implementation("io.kvision:kvision-testutils:$kvisionVersion")
    }
}

afterEvaluate {
    // 'jsBrowserProductionWebpack' is contributed by the Kotlin Multiplatform plugin and is only
    // available once the project has been evaluated.  The task (and the providers derived from it)
    // are resolved here, so that the lazy 'jar' task configuration below does not have to touch
    // the (deprecated at execution time) Task.project API.
    val webpackTask = tasks.getByName("jsBrowserProductionWebpack", KotlinWebpack::class)
    val webpackOutput = webpackTask.outputDirectory
    val libsDir = layout.buildDirectory.dir("libs")
    tasks.register("jar", Jar::class) {
        dependsOn(webpackTask)
        group = "package"
        destinationDirectory.set(libsDir)
        from(webpackOutput) {
            include("**/*.*")
            into("/public/kroviz/")
        }
        from(webDir) { into("/public/kroviz/") }
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        inputs.files(webpackOutput, webDir)
        outputs.file(archiveFile)

    }
}
