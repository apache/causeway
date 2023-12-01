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
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    val kotlinVersion: String by System.getProperties()
    kotlin("plugin.serialization") version kotlinVersion
    kotlin("multiplatform") version kotlinVersion

    val kvisionVersion: String by System.getProperties()
    id("io.kvision") version kvisionVersion

    id("com.github.roroche.plantuml") version "1.0.2"
}

var version = "2.0.0-SNAPSHOT"

/*configurations.kotlinCompilerPluginClasspath {
    resolutionStrategy.force("com.github.roroche:plantuml-gradle-plugin:1.0.2")
}*/
repositories {
    maven("https://repository.int.kn/nexus/content/repositories/thirdparty/")
    maven("https://plugins.gradle.org/m2/")
    mavenCentral()
    google()
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
            runTask(Action {
                outputFileName = "main.bundle.js"
                sourceMaps = false
                devServer = KotlinWebpackConfig.DevServer(
                    open = false,
                    port = 3000,
                    proxy = mutableMapOf(
                        "/kv/*" to "http://localhost:8080",
                        "/kvws/*" to mapOf("target" to "ws://localhost:8080", "ws" to true)
                    ),
                    static = mutableListOf("$buildDir/processedResources/js/main")
                )
            })
            webpackTask(Action {
                outputFileName = "main.bundle.js"
            })
            testTask(Action {
                useKarma {
                    useChromeHeadless()
                }
            })
        }
        binaries.executable()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
//                api("io.kvision:kvision-server-ktor-koin:$kvisionVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0")
                implementation(npm("react-awesome-button", "*"))
                implementation(npm("prop-types", "*"))
                implementation("io.kvision:kvision:$kvisionVersion")
                implementation("io.kvision:kvision-bootstrap:$kvisionVersion")
                implementation("io.kvision:kvision-bootstrap-upload:$kvisionVersion")
                implementation("io.kvision:kvision-datetime:$kvisionVersion")
                implementation("io.kvision:kvision-tom-select:$kvisionVersion")
                implementation("io.kvision:kvision-fontawesome:$kvisionVersion")
                implementation("io.kvision:kvision-i18n:$kvisionVersion")
                implementation("io.kvision:kvision-richtext:$kvisionVersion")
                implementation("io.kvision:kvision-handlebars:$kvisionVersion")
                implementation("io.kvision:kvision-chart:$kvisionVersion")
                implementation("io.kvision:kvision-tabulator:$kvisionVersion")
                implementation("io.kvision:kvision-pace:$kvisionVersion")
                implementation("io.kvision:kvision-react:$kvisionVersion")
                implementation("io.kvision:kvision-routing-navigo:$kvisionVersion")
                implementation("io.kvision:kvision-state:$kvisionVersion")
                implementation("io.kvision:kvision-rest:$kvisionVersion")
                implementation("io.kvision:kvision-maps:$kvisionVersion")
                implementation("io.kvision:kvision-jquery:$kvisionVersion")
                implementation(npm("xmltojson", "1.3.5"))
                implementation(npm("flatted", "3.2.7"))
                implementation(npm("diff", "5.0.0"))
                implementation(npm("diff2html", "3.4.13"))
                implementation(npm("xml-beautify", "1.1.2"))
                implementation(npm("xterm", "4.15.0"))
                implementation(npm("pdfjs", "2.4.7"))
                implementation(npm("vega", "5.22.1"))
                implementation(npm("vega-lite", "5.6.0"))
                implementation(npm("element-resize-event", "3.0.6"))
                implementation(npm("asciidoctor", "2.2.6"))
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
                implementation("io.kvision:kvision-testutils:$kvisionVersion")
            }
        }
    }
    jvmToolchain(17)
}

afterEvaluate {
    tasks {
        create("jar", Jar::class) {
            dependsOn("jsBrowserProductionWebpack")
            group = "package"
            destinationDirectory.set(file("$buildDir/libs"))
            val distribution =
                project.tasks.getByName(
                    "jsBrowserProductionWebpack",
                    KotlinWebpack::class
                ).destinationDirectory
            from(distribution) {
                include("**/*.*")
                into("/public/kroviz/")
            }
            from(webDir) { into("/public/kroviz/") }
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            inputs.files(distribution, webDir)
            outputs.file(archiveFile)

        }
    }

}
dependencies {
    //implementation(kotlin("stdlib-js"))
    //classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${kotlinVersion}")
//    implementation(kotlin("org.jetbrains.kotlin:kotlin-dce-js:$kotlinVersion"))
    //implementation(kotlin("stdlib"))
}

classDiagram { // (1)
    packageName = "org.apache.causeway.clients.kroviz" // (2)
    outputFile = project.file("diagrams/class_diagram.plantuml") // (3)
    //outputFile = new File("${projectDir}/diagrams/class_diagram.plantuml") // (4)
//    ignoredClasses = [ // (5)
//        "com.github.roroche.plantuml.diagrams.Diagram\$Simple",
//    ]

}
