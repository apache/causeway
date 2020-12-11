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
package org.apache.isis.tooling.cli.projdoc;

import java.io.File;

import org.asciidoctor.ast.Document;

import org.apache.isis.tooling.cli.CliConfig;
import org.apache.isis.tooling.j2adoc.J2AContext;
import org.apache.isis.tooling.model4adoc.AsciiDocWriter;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

final class ProjectDocWriter {

    @SneakyThrows
    static void write(
            final @NonNull CliConfig cliConfig, 
            final @NonNull Document adoc, 
            final @NonNull J2AContext j2aContext) {
        
        try {
            
            if(cliConfig.isDryRun()) {
                AsciiDocWriter.print(adoc);
                for(val unit : j2aContext.getUnitIndex().values()) {
                    AsciiDocWriter.print(unit.toAsciiDoc(j2aContext));
                }
            } else {
                AsciiDocWriter.writeToFile(adoc, cliConfig.getOutputFile());
                for(val unit : j2aContext.getUnitIndex().values()) {

                    AsciiDocWriter.writeToFile(
                            unit.toAsciiDoc(j2aContext), 
                            new File(
                                    cliConfig.getDocumentGlobalIndexOutputFolder(), 
                                    unit.getName() + ".adoc"));
                }
            }    
            
        } catch (Exception e) {
            e.printStackTrace();
            return;
        } 
        
    }
    
}
