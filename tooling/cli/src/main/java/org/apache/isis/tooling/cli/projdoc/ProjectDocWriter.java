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
import java.util.function.BiConsumer;

import org.asciidoctor.ast.Document;

import org.apache.isis.commons.internal.base._Files;
import org.apache.isis.commons.internal.base._Refs;
import org.apache.isis.tooling.cli.CliConfig;
import org.apache.isis.tooling.j2adoc.J2AdocContext;
import org.apache.isis.tooling.j2adoc.J2AdocUnit;
import org.apache.isis.tooling.model4adoc.AsciiDocWriter;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

final class ProjectDocWriter {

    @SneakyThrows
    static void write(
            final @NonNull CliConfig cliConfig, 
            final @NonNull Document systemSummaryAdoc, 
            final @NonNull J2AdocContext j2aContext) {
        
        final BiConsumer<Document, File> docWriter = cliConfig.getProjectDoc().isDryRun()
                ? (doc, file)->AsciiDocWriter.print(doc) // print to system out only (dry run)
                : AsciiDocWriter::writeToFile;

        val currentUnit = _Refs.<J2AdocUnit>objectRef(null);
                
        try {

            // write system overview
            docWriter.accept(systemSummaryAdoc, cliConfig.getProjectDoc().getOutputFile());

            // delete document index
            
            val globalDocIndexRootFolder = cliConfig.getProjectDoc().getDocumentGlobalIndexOutputFolder();
            
            _Files.searchFiles(globalDocIndexRootFolder, dir->true, file->file.getName().endsWith(".adoc"))
            .stream()
            .peek(adocFile->System.out.println(String.format("deleting file: %s", adocFile.getName())))
            .forEach(_Files::deleteFile);
            
            // write document index
            for(val unit : j2aContext.getUnitIndex().values()) {
            
                currentUnit.setValue(unit);
                
                docWriter.accept(
                        unit.toAsciiDoc(j2aContext), 
                        new File(
                                cliConfig.getProjectDoc().getDocumentGlobalIndexOutputFolder(), 
                                unit.getName() + ".adoc"));
            }
                
            
        } catch (Exception e) {
            System.err.println(String.format(
                    "failed to write adoc for unit %s", 
                    currentUnit.getValue().map(J2AdocUnit::getName).orElse("none")));
            e.printStackTrace();
            return;
        } 
        
    }

    
}
