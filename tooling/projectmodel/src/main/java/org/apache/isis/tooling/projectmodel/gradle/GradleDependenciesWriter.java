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
package org.apache.isis.tooling.projectmodel.gradle;

import java.io.StringWriter;
import java.io.Writer;

import org.apache.isis.commons.internal.base._Strings;

import lombok.val;

public class GradleDependenciesWriter extends WriterAbstract {

    public static String toString(GradleDependencies gradleDependencies) {
        if(gradleDependencies==null) {
            return "";
        }
        val adocWriter = new GradleDependenciesWriter();
        val stringWriter = new StringWriter();
        adocWriter.write(gradleDependencies, stringWriter);
        return stringWriter.toString();
    }

// EXAMPLE    
//    ext {
//        cucumberVersion = "6.7.0"
//        jacksonVersion = "2.11.1"
//        Libs = [
//                cucumberJava                    : "io.cucumber:cucumber-java:$cucumberVersion",
//                cucumberSpring                  : "io.cucumber:cucumber-spring:$cucumberVersion",
//                cucumberJunit                   : "io.cucumber:cucumber-junit:$cucumberVersion",
//                jacksonDatabind                 : "com.fasterxml.jackson.core:jackson-databind:$jacksonVersion",
//                jacksonModuleJaxbAnnotations    : "com.fasterxml.jackson.module:jackson-module-jaxb-annotations:$jacksonVersion",
//        ]
//    }
    public void write(GradleDependencies gradleDependencies, Writer writer) {
        
        writeWithFormat(writer, "ext {\n");
        writeWithFormat(writer, "    Libs = [\n");
        
        gradleDependencies.getDependenciesByShortName().forEach((shortName, dependency)->{
            
            val shortNameWithPadding = _Strings.padEnd(shortName, 64, ' ');    
        
            writeWithFormat(writer, "        %s : \"%s\",\n", 
                    shortNameWithPadding, 
                    dependency.getArtifactCoordinates().toStringWithGroupAndIdAndVersion());
            
            
        });
        
        writeWithFormat(writer, "    ]\n");
        writeWithFormat(writer, "}\n");
    }
    

    
}
